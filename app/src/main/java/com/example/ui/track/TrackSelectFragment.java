package com.example.ui.track;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.data.prefs.AppPreferences;

public class TrackSelectFragment extends Fragment {

    private String selectedTrack = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Scientific Card
        View cardScientific = view.findViewById(R.id.card_scientific);
        RadioButton rbScientific = cardScientific.findViewById(R.id.radio_btn);
        ImageView ivScientific = cardScientific.findViewById(R.id.iv_track_icon);
        TextView tvTitleScientific = cardScientific.findViewById(R.id.tv_track_title);
        TextView tvSubtitleScientific = cardScientific.findViewById(R.id.tv_track_subtitle);

        ivScientific.setImageResource(R.drawable.ic_science);
        ivScientific.setColorFilter(requireContext().getColor(R.color.subject_physics));
        tvTitleScientific.setText("العلمي");
        tvTitleScientific.setTextColor(requireContext().getColor(R.color.navy));
        tvSubtitleScientific.setText("رياضيات، فيزياء، كيمياء، أحياء");
        cardScientific.setBackgroundResource(R.drawable.bg_card);

        // Setup Literary Card
        View cardLiterary = view.findViewById(R.id.card_literary);
        RadioButton rbLiterary = cardLiterary.findViewById(R.id.radio_btn);
        ImageView ivLiterary = cardLiterary.findViewById(R.id.iv_track_icon);
        TextView tvTitleLiterary = cardLiterary.findViewById(R.id.tv_track_title);
        TextView tvSubtitleLiterary = cardLiterary.findViewById(R.id.tv_track_subtitle);

        ivLiterary.setImageResource(R.drawable.ic_menu_book);
        ivLiterary.setColorFilter(requireContext().getColor(R.color.subject_history));
        tvTitleLiterary.setText("الأدبي");
        tvTitleLiterary.setTextColor(requireContext().getColor(R.color.navy));
        tvSubtitleLiterary.setText("تاريخ، جغرافيا، ثقافة علمية");
        cardLiterary.setBackgroundResource(R.drawable.bg_card);

        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        cardScientific.setOnClickListener(v -> {
            selectedTrack = "scientific";
            rbScientific.setChecked(true);
            rbLiterary.setChecked(false);
            btnConfirm.setEnabled(true);
            cardScientific.setBackgroundResource(R.drawable.bg_card_selected);
            cardLiterary.setBackgroundResource(R.drawable.bg_card);
        });

        cardLiterary.setOnClickListener(v -> {
            selectedTrack = "literary";
            rbScientific.setChecked(false);
            rbLiterary.setChecked(true);
            btnConfirm.setEnabled(true);
            cardLiterary.setBackgroundResource(R.drawable.bg_card_selected);
            cardScientific.setBackgroundResource(R.drawable.bg_card);
        });

        btnConfirm.setOnClickListener(v -> {
            if (selectedTrack != null) {
                AppPreferences prefs = new AppPreferences(requireContext());
                prefs.setUserTrack(selectedTrack);
                Navigation.findNavController(view).navigate(R.id.action_trackSelectFragment_to_homeFragment);
            }
        });
    }
}
