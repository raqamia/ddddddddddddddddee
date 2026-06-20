package com.example.ui.subject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.local.entity.FileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileEntity> files = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDownloadClick(FileEntity file);
    }

    public FileAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFiles(List<FileEntity> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFileName;
        private final TextView tvFileInfo;
        private final ImageButton btnAction;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileInfo = itemView.findViewById(R.id.tv_file_info);
            btnAction = itemView.findViewById(R.id.btn_action);

            btnAction.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDownloadClick(files.get(position));
                }
            });
            // Click on the whole item to open (if downloaded)
            itemView.setOnClickListener(v -> {
                 int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDownloadClick(files.get(position));
                }
            });
        }

        public void bind(FileEntity file) {
            tvFileName.setText(file.name);
            String sizeMb = String.format(Locale.US, "%.1f", file.sizeBytes / (1024.0 * 1024.0));
            tvFileInfo.setText("PDF • " + sizeMb + " MB • " + file.pageCount + " صفحات");
        }
    }
}
