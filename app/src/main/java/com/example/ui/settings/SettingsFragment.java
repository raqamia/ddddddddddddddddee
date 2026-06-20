package com.example.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.data.prefs.AppPreferences;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppPreferences prefs = new AppPreferences(requireContext());
        com.example.data.prefs.SessionManager sessionManager = new com.example.data.prefs.SessionManager(requireContext());

        View btnChangeTrack = view.findViewById(R.id.btn_change_track);
        View btnLogout = view.findViewById(R.id.btn_logout);

        btnChangeTrack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_trackSelectFragment);
        });

        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setMessage("هل أنت متأكد من تسجيل الخروج؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        sessionManager.clearSession();
                        prefs.setUserTrack(null);
                        Navigation.findNavController(view).navigate(R.id.action_global_loginFragment);
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
    }
}
