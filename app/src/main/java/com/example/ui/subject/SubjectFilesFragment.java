package com.example.ui.subject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.DownloadRepository;
import com.example.data.repository.FileDownloadRepository;
import com.example.util.ErrorMessages;

public class SubjectFilesFragment extends Fragment {

    private static final String TAG = "SubjectFilesFragment";
    private static final String ARG_SUBJECT_ID = "subject_id";
    private static final String ARG_CATEGORY = "category";

    private String subjectId;
    private String category;
    private SubjectFilesViewModel viewModel;
    private FileAdapter adapter;
    private View emptyState;
    private TextView tvError;
    private Button btnRetry;
    private ProgressBar progressBar;
    private DownloadRepository downloadRepo;
    private FileDownloadRepository fileDownloadRepo;

    public static SubjectFilesFragment newInstance(String subjectId, String category) {
        SubjectFilesFragment fragment = new SubjectFilesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT_ID, subjectId);
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectId = getArguments().getString(ARG_SUBJECT_ID);
            category = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SubjectFilesViewModel.class);

        RecyclerView rvFiles = view.findViewById(R.id.rv_files);
        emptyState = view.findViewById(R.id.empty_state);
        tvError = view.findViewById(R.id.tv_error);
        btnRetry = view.findViewById(R.id.btn_retry);
        progressBar = view.findViewById(R.id.progress_bar);

        rvFiles.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Use the application context to avoid leaking the fragment/activity, and build the
        // repositories once instead of on every click.
        Context appContext = requireContext().getApplicationContext();
        SessionManager sessionManager = new SessionManager(appContext);
        downloadRepo = new DownloadRepository(
                SupabaseApiClient.getApi(sessionManager),
                AppDatabase.getDatabase(appContext).downloadDao()
        );
        fileDownloadRepo = new FileDownloadRepository(
                SupabaseApiClient.getApi(sessionManager),
                AppDatabase.getDatabase(appContext).downloadDao(),
                sessionManager,
                appContext
        );

        adapter = new FileAdapter(file -> {
            // Reading the download status touches the DB, so it must run off the main thread.
            downloadRepo.getDownloadStatusAsync(file.id, existing -> {
                if (!isAdded()) return;
                if (existing != null && "COMPLETED".equals(existing.state) && existing.localPath != null) {
                    openPdf(existing.localPath, file.name);
                } else {
                    startDownload(file);
                }
            });
        });
        rvFiles.setAdapter(adapter);

        viewModel.getFiles().observe(getViewLifecycleOwner(), files -> {
            progressBar.setVisibility(View.GONE);
            if (files != null && !files.isEmpty()) {
                adapter.setFiles(files);
                rvFiles.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
            } else {
                rvFiles.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
            }
        });

        viewModel.getNetworkError().observe(getViewLifecycleOwner(), hasError -> {
            progressBar.setVisibility(View.GONE);
            if (Boolean.TRUE.equals(hasError) && (adapter == null || adapter.getItemCount() == 0)) {
                tvError.setText(ErrorMessages.get("network_error"));
                tvError.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });

        btnRetry.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
            viewModel.loadFiles(subjectId, category);
        });

        viewModel.loadFiles(subjectId, category);
    }

    private void openPdf(String localPath, String fileName) {
        if (!isAdded()) return;
        Bundle args = new Bundle();
        args.putString("localPath", localPath);
        args.putString("fileName", fileName);
        try {
            Navigation.findNavController(requireParentFragment().requireView())
                    .navigate(R.id.action_subjectDetailFragment_to_pdfViewerFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "navigation to pdf viewer failed", e);
        }
    }

    private void startDownload(com.example.data.local.entity.FileEntity file) {
        Toast.makeText(requireContext(), "جاري تحميل: " + file.name, Toast.LENGTH_SHORT).show();
        fileDownloadRepo.downloadFile(file.id, file.name, file.storagePath, new FileDownloadRepository.DownloadCallback() {
            @Override
            public void onSuccess(String localPath) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "تم التحميل", Toast.LENGTH_SHORT).show();
                        openPdf(localPath, file.name);
                    });
                }
            }

            @Override
            public void onError(String errorCode) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), ErrorMessages.get(errorCode), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyState = null;
        tvError = null;
        btnRetry = null;
        progressBar = null;
        adapter = null;
    }
}
