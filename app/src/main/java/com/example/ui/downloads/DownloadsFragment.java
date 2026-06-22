package com.example.ui.downloads;

import android.content.Context;
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
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.SavedFileEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.DownloadRepository;
import com.example.data.repository.RecentRepository;
import com.example.data.repository.SavedRepository;
import com.example.ui.subject.FileAdapter;

import java.util.HashSet;
import java.util.Set;

public class DownloadsFragment extends Fragment {

    private DownloadsViewModel viewModel;
    private FileAdapter adapter;
    private View emptyState;
    private ProgressBar progressBar;
    private DownloadRepository downloadRepo;
    private SavedRepository savedRepo;
    private RecentRepository recentRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_downloads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(DownloadsViewModel.class);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        RecyclerView rv = view.findViewById(R.id.rv_downloads);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);
        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipe = view.findViewById(R.id.swipe_refresh_downloads);
        swipe.setOnRefreshListener(() -> {
            savedRepo.syncFromServer();
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) swipe.setRefreshing(false);
            }, 600);
        });

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        Context appContext = requireContext().getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);
        downloadRepo = new DownloadRepository(
                SupabaseApiClient.getApi(sessionManager),
                AppDatabase.getDatabase(appContext).downloadDao());
        savedRepo = new SavedRepository(
                SupabaseApiClient.getApi(sessionManager),
                AppDatabase.getDatabase(appContext).savedDao(),
                AppDatabase.getDatabase(appContext).fileDao(),
                sessionManager);
        recentRepo = new RecentRepository(AppDatabase.getDatabase(appContext).recentDao());

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FileAdapter(new FileAdapter.OnItemClickListener() {
            @Override
            public void onDownloadClick(FileEntity file) {
                downloadRepo.getDownloadStatusAsync(file.id, download -> {
                    if (!isAdded()) return;
                    String localPath = download != null ? download.localPath : null;
                    recentRepo.record(file.id, file.name, localPath);
                    Bundle args = new Bundle();
                    args.putString("fileId", file.id);
                    args.putString("fileName", file.name);
                    if (localPath != null) args.putString("localPath", localPath);
                    Navigation.findNavController(view).navigate(R.id.action_global_pdfViewerFragment, args);
                });
            }

            @Override
            public void onSaveToggle(FileEntity file, boolean save) {
                savedRepo.setSaved(file.id, save);
            }
        });
        rv.setAdapter(adapter);

        savedRepo.getSavedLive().observe(getViewLifecycleOwner(), saved -> {
            Set<String> ids = new HashSet<>();
            if (saved != null) for (SavedFileEntity s : saved) ids.add(s.fileId);
            if (adapter != null) adapter.setSavedIds(ids);
        });
        savedRepo.syncFromServer();

        viewModel.getDownloads().observe(getViewLifecycleOwner(), files -> {
            progressBar.setVisibility(View.GONE);
            boolean has = files != null && !files.isEmpty();
            if (has) {
                adapter.setFiles(files);
                Set<String> ids = new HashSet<>();
                for (FileEntity f : files) ids.add(f.id);
                adapter.setDownloadedIds(ids); // everything here is downloaded
            }
            rv.setVisibility(has ? View.VISIBLE : View.GONE);
            emptyState.setVisibility(has ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyState = null;
        progressBar = null;
        adapter = null;
    }
}
