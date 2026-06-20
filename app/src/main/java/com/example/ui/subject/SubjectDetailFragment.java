package com.example.ui.subject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SubjectDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);

        String subjectId = getArguments() != null ? getArguments().getString("subjectId") : null;
        String subjectName = getArguments() != null ? getArguments().getString("subjectName") : null;

        // A missing subject id means we were navigated to without the required args; there is
        // nothing meaningful to show, so go back instead of loading an arbitrary subject.
        if (subjectId == null) {
            Navigation.findNavController(view).popBackStack();
            return;
        }

        tvTitle.setText(subjectName != null ? subjectName : "المادة");

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        SubjectPagerAdapter adapter = new SubjectPagerAdapter(this, subjectId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("الكتب"); break;
                case 1: tab.setText("امتحانات سابقة"); break;
                case 2: tab.setText("ملخصات"); break;
            }
        }).attach();
    }
}
