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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.R;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.NotificationsRepository;
import com.example.util.Constants;
import com.example.util.ErrorMessages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvSubjects;
    private View emptyState;
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

        TextView tvTrackName = view.findViewById(R.id.tv_track_name);
        TextView tvCountdown = view.findViewById(R.id.tv_countdown);
        EditText etSearch = view.findViewById(R.id.et_search);
        rvSubjects = view.findViewById(R.id.rv_subjects);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);

        setCountdown(tvCountdown);

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

        // New-notification badge: show a dot when the newest announcement is unseen.
        View badgeDot = view.findViewById(R.id.badge_dot);
        AppPreferences badgePrefs = new AppPreferences(requireContext());
        NotificationsRepository notifRepo = new NotificationsRepository(
                SupabaseApiClient.getApi(new SessionManager(requireContext().getApplicationContext())));
        notifRepo.getNotifications().observe(getViewLifecycleOwner(), list -> {
            if (badgeDot == null || list == null || list.isEmpty()) return;
            long newest = parseIsoMillis(list.get(0).getCreatedAt());
            badgeDot.setVisibility(newest > badgePrefs.getLastSeenNotif() ? View.VISIBLE : View.GONE);
        });
        notifRepo.fetch();

        rvSubjects.setLayoutManager(new GridLayoutManager(requireContext(), 2));

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

        viewModel.getGreeting().observe(getViewLifecycleOwner(), greeting -> tvTrackName.setText(greeting));

        viewModel.getNetworkError().observe(getViewLifecycleOwner(), hasError -> {
            swipeRefresh.setRefreshing(false);
            if (Boolean.TRUE.equals(hasError)) {
                Toast.makeText(requireContext(), ErrorMessages.get("network_error"), Toast.LENGTH_LONG).show();
            }
        });

        swipeRefresh.setOnRefreshListener(() -> {
            AppPreferences prefs = new AppPreferences(requireContext());
            viewModel.loadSubjects(prefs.getUserTrack());
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

    /** Parses a Supabase ISO timestamp to epoch millis (0 on failure). */
    private long parseIsoMillis(String iso) {
        if (iso == null) return 0;
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            return in.parse(iso.length() >= 19 ? iso.substring(0, 19) : iso).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /** Computes the number of days remaining until the Tawjihi exam date. */
    private void setCountdown(TextView tv) {
        if (tv == null) return;
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date exam = fmt.parse(Constants.TAWJIHI_EXAM_DATE);
            long diff = exam.getTime() - System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days > 1) {
                tv.setText(days + " يوم");
            } else if (days >= 0) {
                tv.setText("الامتحانات بدأت — بالتوفيق! 🎓");
            } else {
                tv.setText("بالتوفيق في نتائجك! 🎓");
            }
        } catch (Exception e) {
            tv.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeRefresh = null;
        adapter = null;
        rvSubjects = null;
        emptyState = null;
    }
}
