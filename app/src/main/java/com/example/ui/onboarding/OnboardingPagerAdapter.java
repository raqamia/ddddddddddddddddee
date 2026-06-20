package com.example.ui.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.R;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OnboardingSlideFragment.newInstance(
                        R.drawable.ic_library_outline,
                        R.color.subject_physics,
                        R.color.subject_physics_bg,
                        "كل مواد التوجيهي في مكان واحد",
                        "كتب، ملخصات، وامتحانات سابقة — كل شي منظم ومرتب"
                );
            case 1:
                return OnboardingSlideFragment.newInstance(
                        R.drawable.ic_school_outline,
                        R.color.subject_math,
                        R.color.subject_math_bg,
                        "بدون تشتيت، بدون دردشة",
                        "منارة مختلفة — فقط المواد والملفات اللي تحتاجها"
                );
            case 2:
                return OnboardingSlideFragment.newInstance(
                        R.drawable.ic_cloud_download_outline,
                        R.color.subject_biology,
                        R.color.subject_biology_bg,
                        "حمّل واقرأ بدون إنترنت",
                        "نزّل الملفات مرة وحدة واقرأها في أي وقت بدون نت"
                );
            default:
                throw new IllegalStateException("Unexpected position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
