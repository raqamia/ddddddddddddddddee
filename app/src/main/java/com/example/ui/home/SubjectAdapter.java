package com.example.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.example.data.local.entity.SubjectEntity;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<SubjectEntity> subjects = new ArrayList<>();
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
        private final View cardBg;
        private final ImageView ivIcon;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTrackBadge = itemView.findViewById(R.id.tv_track_badge);
            cardBg = itemView.findViewById(R.id.card_bg);
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
            
            // Assign some placeholder colours based on subject length to make it look dynamic without images right now
            int colorResInfoBg = R.color.subject_math_bg;
            int colorResInfo = R.color.subject_math;
            int icon = R.drawable.ic_science;
            if (subject.name.contains("فيزياء")) { colorResInfoBg = R.color.subject_physics_bg; colorResInfo = R.color.subject_physics; icon = R.drawable.ic_science; }
            else if (subject.name.contains("كيمياء")) { colorResInfoBg = R.color.subject_chemistry_bg; colorResInfo = R.color.subject_chemistry; icon = R.drawable.ic_science; }
            else if (subject.name.contains("أحياء")) { colorResInfoBg = R.color.subject_biology_bg; colorResInfo = R.color.subject_biology; icon = R.drawable.ic_science; }
            else if (subject.name.contains("عربي")) { colorResInfoBg = R.color.subject_arabic_bg; colorResInfo = R.color.subject_arabic; icon = R.drawable.ic_menu_book; }
            else if (subject.name.contains("دين")) { colorResInfoBg = R.color.subject_islamic_bg; colorResInfo = R.color.subject_islamic; icon = R.drawable.ic_menu_book; }
            else if (subject.name.contains("رياضيات")) { colorResInfoBg = R.color.subject_math_bg; colorResInfo = R.color.subject_math; icon = R.drawable.ic_science; }

            cardBg.setBackgroundColor(itemView.getContext().getColor(colorResInfoBg));
            tvName.setTextColor(itemView.getContext().getColor(colorResInfo));
            tvTrackBadge.setTextColor(itemView.getContext().getColor(colorResInfo));
            ivIcon.setColorFilter(itemView.getContext().getColor(colorResInfo));
            ivIcon.setImageResource(icon);
        }
    }
}
