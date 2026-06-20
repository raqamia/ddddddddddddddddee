package com.example.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.example.data.local.AppDatabase;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.DownloadRepository;
import com.example.ui.subject.FileAdapter;

public class SavedFilesFragment extends Fragment {

    private SavedFilesViewModel viewModel;
    private FileAdapter adapter;
    private View emptyState;
    private ProgressBar progressBar;
    private DownloadRepository downloadRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SavedFilesViewModel.class);

        RecyclerView rvSavedFiles = view.findViewById(R.id.rv_saved_files);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        rvSavedFiles.setLayoutManager(new LinearLayoutManager(requireContext()));

        android.content.Context appContext = requireContext().getApplicationContext();
        downloadRepo = new DownloadRepository(
                SupabaseApiClient.getApi(new SessionManager(appContext)),
                AppDatabase.getDatabase(appContext).downloadDao()
        );

        adapter = new FileAdapter(file -> {
            // The local path lives in the DB, so look it up off the main thread before navigating.
            downloadRepo.getDownloadStatusAsync(file.id, download -> {
                if (!isAdded()) return;
                Bundle args = new Bundle();
                args.putString("fileName", file.name);
                if (download != null) args.putString("localPath", download.localPath);
                Navigation.findNavController(view).navigate(R.id.action_global_pdfViewerFragment, args);
            });
        });
        rvSavedFiles.setAdapter(adapter);

        viewModel.getSavedFiles().observe(getViewLifecycleOwner(), files -> {
            progressBar.setVisibility(View.GONE);
            if (files != null && !files.isEmpty()) {
                adapter.setFiles(files);
                rvSavedFiles.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            } else {
                rvSavedFiles.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.loadSavedFiles();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyState = null;
        progressBar = null;
        adapter = null;
    }
}
