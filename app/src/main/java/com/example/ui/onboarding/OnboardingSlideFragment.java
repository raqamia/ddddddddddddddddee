package com.example.ui.onboarding;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.R;

public class OnboardingSlideFragment extends Fragment {

    private static final String ARG_ICON = "icon";
    private static final String ARG_ICON_COLOR = "icon_color";
    private static final String ARG_BG_COLOR = "bg_color";
    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";

    public static OnboardingSlideFragment newInstance(int iconResId, int iconColorResId, int bgColorResId, String title, String subtitle) {
        OnboardingSlideFragment fragment = new OnboardingSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON, iconResId);
        args.putInt(ARG_ICON_COLOR, iconColorResId);
        args.putInt(ARG_BG_COLOR, bgColorResId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_onboarding_slide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivIcon = view.findViewById(R.id.iv_icon);
        View circleContainer = view.findViewById(R.id.circle_container);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_subtitle);

        Bundle args = getArguments();
        if (args != null) {
            ivIcon.setImageResource(args.getInt(ARG_ICON));
            ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), args.getInt(ARG_ICON_COLOR))));

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(requireContext(), args.getInt(ARG_BG_COLOR)));
            circleContainer.setBackground(shape);

            tvTitle.setText(args.getString(ARG_TITLE));
            tvSubtitle.setText(args.getString(ARG_SUBTITLE));
        }
    }
}
