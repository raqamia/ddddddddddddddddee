package com.example.ui.subject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SubjectPagerAdapter extends FragmentStateAdapter {

    private final String subjectId;

    public SubjectPagerAdapter(@NonNull Fragment fragment, String subjectId) {
        super(fragment);
        this.subjectId = subjectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String category;
        switch (position) {
            case 0: category = "books"; break;
            case 1: category = "exams"; break;
            case 2: category = "summaries"; break;
            default: category = "books"; break;
        }
        
        return SubjectFilesFragment.newInstance(subjectId, category);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
