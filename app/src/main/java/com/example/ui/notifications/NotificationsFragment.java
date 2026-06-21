package com.example.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel viewModel;
    private NotificationAdapter adapter;
    private View emptyState;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(NotificationsViewModel.class);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        RecyclerView rv = view.findViewById(R.id.rv_notifications);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter();
        rv.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        viewModel.getNotifications().observe(getViewLifecycleOwner(), list -> {
            progressBar.setVisibility(View.GONE);
            boolean has = list != null && !list.isEmpty();
            adapter.setItems(list);
            rv.setVisibility(has ? View.VISIBLE : View.GONE);
            emptyState.setVisibility(has ? View.GONE : View.VISIBLE);
        });
        viewModel.getNetworkError().observe(getViewLifecycleOwner(), err -> {
            progressBar.setVisibility(View.GONE);
            if (Boolean.TRUE.equals(err) && (adapter == null || adapter.getItemCount() == 0)) {
                emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyState = null;
        progressBar = null;
        adapter = null;
    }
}
