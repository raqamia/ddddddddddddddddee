package com.example.ui.pdfviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.util.ErrorMessages;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import java.io.File;

public class PdfViewerFragment extends Fragment {
    
    private PDFView pdfView;
    private TextView tvPageCount, tvTitle;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        pdfView = view.findViewById(R.id.pdfView);
        tvPageCount = view.findViewById(R.id.tv_page_count);
        tvTitle = view.findViewById(R.id.tv_title);
        progressBar = view.findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        String localPath = null;
        String fileName = "";
        if (getArguments() != null) {
            localPath = getArguments().getString("localPath");
            fileName = getArguments().getString("fileName", "ملف PDF");
        }

        tvTitle.setText(fileName);

        // Fallback for mock demo: if path is null or we are testing, use an empty handler
        if (localPath == null || localPath.trim().isEmpty() || localPath.equals("/mock/path")) {
            Toast.makeText(requireContext(), ErrorMessages.get("file_not_found"), Toast.LENGTH_LONG).show();
            return;
        }

        File pdfFile = new File(localPath);
        if (!pdfFile.exists()) {
            Toast.makeText(requireContext(), ErrorMessages.get("file_not_found"), Toast.LENGTH_LONG).show();
            return;
        }

        loadPdf(pdfFile);
    }

    private void loadPdf(File file) {
        progressBar.setVisibility(View.VISIBLE);
        pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .onLoad(nbPages -> {
                    // The PDF library decodes asynchronously; the view may already be destroyed.
                    if (!isAdded() || pdfView == null) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updatePageCount(pdfView.getCurrentPage(), nbPages);
                })
                .onPageChange((page, pageCount) -> updatePageCount(page, pageCount))
                .onError(t -> {
                    Log.e("PdfViewer", "Error loading PDF", t);
                    if (!isAdded()) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), ErrorMessages.get("pdf_render_error"), Toast.LENGTH_LONG).show();
                })
                .enableAnnotationRendering(true)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .spacing(0)
                .load();
    }

    private void updatePageCount(int currentPage, int totalPages) {
        if (tvPageCount == null) return;
        tvPageCount.setText((currentPage + 1) + " / " + totalPages);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // PDFView holds rendered bitmaps; release them and drop view references to avoid leaks.
        if (pdfView != null) {
            pdfView.recycle();
            pdfView = null;
        }
        tvPageCount = null;
        tvTitle = null;
        progressBar = null;
    }
}
