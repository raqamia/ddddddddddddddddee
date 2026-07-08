package com.example.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.local.entity.SubjectEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<SubjectEntity> subjects = new ArrayList<>();
    private final Map<String, Integer> downloadedPerSubject = new HashMap<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SubjectEntity subject);
    }

    public SubjectAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSubjects(List<SubjectEntity> subjects) {
        this.subjects = subjects;
        notifyDataSetChanged();
    }

    public void setDownloadedPerSubject(Map<String, Integer> map) {
        downloadedPerSubject.clear();
        if (map != null) downloadedPerSubject.putAll(map);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectEntity subject = subjects.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvTrackBadge;
        private final TextView tvProgress;
        private final FrameLayout iconContainer;
        private final ImageView ivIcon;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTrackBadge = itemView.findViewById(R.id.tv_track_badge);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            iconContainer = itemView.findViewById(R.id.icon_container);
            ivIcon = itemView.findViewById(R.id.iv_icon);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(subjects.get(position));
                }
            });
        }

        public void bind(SubjectEntity subject) {
            tvName.setText(subject.name);
            tvTrackBadge.setText("scientific".equals(subject.track) ? "علمي" : ("literary".equals(subject.track) ? "أدبي" : "مشترك"));

            Integer dl = downloadedPerSubject.get(subject.id);
            if (dl != null && dl > 0) {
                tvProgress.setText("⬇ " + dl + " ملف محمّل");
                tvProgress.setVisibility(View.VISIBLE);
            } else {
                tvProgress.setVisibility(View.GONE);
            }

            int iconBgRes = R.drawable.bg_circle_subject_math;
            int iconRes = R.drawable.ic_science;
            String name = subject.name != null ? subject.name : "";
            if (name.contains("فيزياء")) { iconBgRes = R.drawable.bg_circle_subject_physics; iconRes = R.drawable.ic_science; }
            else if (name.contains("كيمياء")) { iconBgRes = R.drawable.bg_circle_subject_chemistry; iconRes = R.drawable.ic_science; }
            else if (name.contains("أحياء")) { iconBgRes = R.drawable.bg_circle_subject_biology; iconRes = R.drawable.ic_science; }
            else if (name.contains("عربي")) { iconBgRes = R.drawable.bg_circle_subject_math; iconRes = R.drawable.ic_menu_book; }
            else if (name.contains("دين")) { iconBgRes = R.drawable.bg_circle_subject_math; iconRes = R.drawable.ic_menu_book; }
            else if (name.contains("رياضيات")) { iconBgRes = R.drawable.bg_circle_subject_math; iconRes = R.drawable.ic_science; }
            else if (name.contains("تاريخ") || name.contains("جغرافيا")) { iconBgRes = R.drawable.bg_circle_subject_biology; iconRes = R.drawable.ic_menu_book; }
            else if (name.contains("انجليزي")) { iconBgRes = R.drawable.bg_circle_subject_physics; iconRes = R.drawable.ic_menu_book; }

            iconContainer.setBackgroundResource(iconBgRes);
            ivIcon.setImageResource(iconRes);
        }
    }
}
