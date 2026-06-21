package com.example.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.data.remote.dto.NotificationDto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<NotificationDto> items = new ArrayList<>();

    public void setItems(List<NotificationDto> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvBody, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvBody = itemView.findViewById(R.id.tv_body);
            tvDate = itemView.findViewById(R.id.tv_date);
        }

        void bind(NotificationDto n) {
            tvTitle.setText(n.getTitle());
            tvBody.setText(n.getBody());
            tvDate.setText(formatDate(n.getCreatedAt()));
        }

        private String formatDate(String iso) {
            if (iso == null) return "";
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date d = in.parse(iso.length() >= 19 ? iso.substring(0, 19) : iso);
                return new SimpleDateFormat("d MMM yyyy", new Locale("ar")).format(d);
            } catch (ParseException e) {
                return "";
            }
        }
    }
}
