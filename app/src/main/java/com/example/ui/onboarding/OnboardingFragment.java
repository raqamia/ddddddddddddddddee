package com.example.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.R;
import com.example.data.prefs.AppPreferences;

public class OnboardingFragment extends Fragment {

    private ViewPager2 viewPager;
    private LinearLayout dotsContainer;
    private Button btnNext;
    private TextView tvSkip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.onboardingPager);
        dotsContainer = view.findViewById(R.id.dots_container);
        btnNext = view.findViewById(R.id.btn_next);
        tvSkip = view.findViewById(R.id.tv_skip);

        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(adapter.getItemCount());
        updateIndicators(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("ابدأ الآن");
                    tvSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText("التالي");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24); // 8dp approx
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            if (i == position) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(60, 24); // 20dp x 8dp approx
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
                params.setMargins(8, 0, 8, 0);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
        }
    }

    private void finishOnboarding() {
        AppPreferences prefs = new AppPreferences(requireContext());
        prefs.setOnboardingDone(true);
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_onboardingFragment_to_loginFragment);
    }
}
