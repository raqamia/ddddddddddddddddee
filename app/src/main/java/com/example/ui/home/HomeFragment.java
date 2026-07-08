package com.example.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.R;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.prefs.AppPreferences;
import com.example.util.ErrorMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSubjects;
    private View emptyState;
    private View recentSection;
    private RecyclerView rvRecent;
    private RecentAdapter recentAdapter;
    private View badgeDot;
    private final List<SubjectEntity> allSubjects = new ArrayList<>();
    private String query = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(HomeViewModel.class);

        EditText etSearch = view.findViewById(R.id.et_search);
        rvSubjects = view.findViewById(R.id.rv_subjects);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        badgeDot = view.findViewById(R.id.badge_dot);
        recentSection = view.findViewById(R.id.recent_section);
        rvRecent = view.findViewById(R.id.rv_recent);

        // Featured card random message
        TextView tvTitle = view.findViewById(R.id.tv_featured_title);
        TextView tvSub = view.findViewById(R.id.tv_featured_subtitle);
        if (tvTitle != null && tvSub != null) {
            int idx = new Random().nextInt(5);
            String[] titles = {
                getString(R.string.featured_title_1),
                getString(R.string.featured_title_2),
                getString(R.string.featured_title_3),
                getString(R.string.featured_title_4),
                getString(R.string.featured_title_5)
            };
            String[] subs = {
                getString(R.string.featured_sub_1),
                getString(R.string.featured_sub_2),
                getString(R.string.featured_sub_3),
                getString(R.string.featured_sub_4),
                getString(R.string.featured_sub_5)
            };
            tvTitle.setText(titles[idx]);
            tvSub.setText(subs[idx]);
        }

        View btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_notificationsFragment));
        }

        View btnDownloads = view.findViewById(R.id.btn_downloads);
        if (btnDownloads != null) {
            btnDownloads.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_downloadsFragment));
        }

        viewModel.getHasUnseenNotification().observe(getViewLifecycleOwner(), unseen -> {
            if (badgeDot != null) {
                badgeDot.setVisibility(Boolean.TRUE.equals(unseen) ? View.VISIBLE : View.GONE);
            }
        });

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recentAdapter = new RecentAdapter(item -> {
            Bundle args = new Bundle();
            args.putString("fileId", item.fileId);
            args.putString("fileName", item.name);
            if (item.localPath != null) args.putString("localPath", item.localPath);
            Navigation.findNavController(view).navigate(R.id.action_global_pdfViewerFragment, args);
        });
        rvRecent.setAdapter(recentAdapter);

        viewModel.getRecentItems().observe(getViewLifecycleOwner(), items -> {
            boolean has = items != null && !items.isEmpty();
            recentSection.setVisibility(has ? View.VISIBLE : View.GONE);
            recentAdapter.setItems(items);
        });

        rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SubjectAdapter(subject -> {
            Bundle args = new Bundle();
            args.putString("subjectId", subject.id);
            args.putString("subjectName", subject.name);
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_subjectDetailFragment, args);
        });
        rvSubjects.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = s == null ? "" : s.toString().trim();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            swipeRefresh.setRefreshing(false);
            allSubjects.clear();
            if (subjects != null) allSubjects.addAll(subjects);
            applyFilter();
        });

        viewModel.getDownloadedPerSubject().observe(getViewLifecycleOwner(), map -> {
            if (adapter != null) adapter.setDownloadedPerSubject(map);
        });

        viewModel.getNetworkError().observe(getViewLifecycleOwner(), hasError -> {
            swipeRefresh.setRefreshing(false);
            if (Boolean.TRUE.equals(hasError)) {
                Toast.makeText(requireContext(), ErrorMessages.get("network_error"), Toast.LENGTH_LONG).show();
            }
        });

        swipeRefresh.setOnRefreshListener(() -> {
            AppPreferences prefs = new AppPreferences(requireContext());
            viewModel.loadSubjects(prefs.getUserTrack());
            viewModel.fetchNotifications();
        });

        AppPreferences prefs = new AppPreferences(requireContext());
        viewModel.loadSubjects(prefs.getUserTrack());
    }

    /** Filters the cached subjects by the current search query and updates the list/empty state. */
    private void applyFilter() {
        if (adapter == null) return;
        List<SubjectEntity> shown;
        if (query.isEmpty()) {
            shown = new ArrayList<>(allSubjects);
        } else {
            shown = new ArrayList<>();
            String q = query.toLowerCase(Locale.ROOT);
            for (SubjectEntity s : allSubjects) {
                if (s.name != null && s.name.toLowerCase(Locale.ROOT).contains(q)) shown.add(s);
            }
        }
        adapter.setSubjects(shown);
        boolean has = !shown.isEmpty();
        if (rvSubjects != null) rvSubjects.setVisibility(has ? View.VISIBLE : View.GONE);
        if (emptyState != null) emptyState.setVisibility(has ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeRefresh = null;
        adapter = null;
        rvSubjects = null;
        emptyState = null;
        recentSection = null;
        rvRecent = null;
        recentAdapter = null;
        badgeDot = null;
    }
}
