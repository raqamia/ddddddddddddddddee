package com.example.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;

public class SplashFragment extends Fragment {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View logoContainer = view.findViewById(R.id.logo_container);
        View textContainer = view.findViewById(R.id.text_container);

        logoContainer.setAlpha(0f);
        logoContainer.setTranslationY(60f);

        textContainer.setAlpha(0f);

        logoContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .start();

        handler.postDelayed(() -> {
            if (isAdded()) {
                textContainer.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .start();
            }
        }, 300);

        navigateRunnable = this::checkNavigation;
        handler.postDelayed(navigateRunnable, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }

    private void checkNavigation() {
        if (!isAdded() || getView() == null) return;

        NavController navController = Navigation.findNavController(getView());

        AppPreferences prefs = new AppPreferences(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());

        if (!prefs.isOnboardingDone()) {
            navController.navigate(R.id.action_splashFragment_to_onboardingFragment);
        } else if (!sessionManager.hasValidSession()) {
            navController.navigate(R.id.action_splashFragment_to_loginFragment);
        } else {
            String track = prefs.getUserTrack();
            if (track == null || track.isEmpty()) {
                navController.navigate(R.id.action_splashFragment_to_trackSelectFragment);
            } else {
                navController.navigate(R.id.action_splashFragment_to_homeFragment);
            }
        }
    }
}
