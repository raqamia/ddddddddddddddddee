package com.example.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.ProfileRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;

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
        SessionManager sessionManager = new SessionManager(requireContext());

        TextView tvProfileName = view.findViewById(R.id.tv_profile_name);
        TextView tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        SwitchMaterial switchNightMode = view.findViewById(R.id.switch_night_mode);
        View btnChangeTrack = view.findViewById(R.id.btn_change_track);
        View btnLogout = view.findViewById(R.id.btn_logout);

        // Profile
        ProfileRepository profileRepository = new ProfileRepository(
                SupabaseApiClient.getApi(sessionManager), sessionManager);
        profileRepository.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;
            tvProfileName.setText(TextUtils.isEmpty(profile.getName()) ? "مستخدم منارة" : profile.getName());
            tvProfileEmail.setText(profile.getEmail() != null ? profile.getEmail() : "");
        });
        profileRepository.fetchProfile();

        // Dark mode
        switchNightMode.setChecked(prefs.isNightMode());
        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // ignore the programmatic initial state
            prefs.setNightMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        btnChangeTrack.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_trackSelectFragment));

        btnLogout.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("هل أنت متأكد من تسجيل الخروج؟")
                        .setPositiveButton("نعم", (dialog, which) -> {
                            sessionManager.clearSession();
                            prefs.setUserTrack(null);
                            Navigation.findNavController(view).navigate(R.id.action_global_loginFragment);
                        })
                        .setNegativeButton("إلغاء", null)
                        .show());
    }
}
