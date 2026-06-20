package com.example.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.data.prefs.AppPreferences;
import com.example.util.ErrorMessages;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private SubjectAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

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
        RecyclerView rvSubjects = view.findViewById(R.id.rv_subjects);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        View emptyState = view.findViewById(R.id.empty_state);

        rvSubjects.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new SubjectAdapter(subject -> {
            Bundle args = new Bundle();
            args.putString("subjectId", subject.id);
            args.putString("subjectName", subject.name);
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_subjectDetailFragment, args);
        });
        rvSubjects.setAdapter(adapter);

        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            swipeRefresh.setRefreshing(false);
            if (subjects != null && !subjects.isEmpty()) {
                adapter.setSubjects(subjects);
                rvSubjects.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                rvSubjects.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getGreeting().observe(getViewLifecycleOwner(), greeting -> {
            tvTrackName.setText(greeting);
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
        });

        AppPreferences prefs = new AppPreferences(requireContext());
        viewModel.loadSubjects(prefs.getUserTrack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        swipeRefresh = null;
        adapter = null;
    }
}
