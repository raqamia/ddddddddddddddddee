package com.example.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.AuthRepository;
import com.example.util.ErrorMessages;

public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        TextView tvRegister = view.findViewById(R.id.tv_register);
        progressBar = view.findViewById(R.id.progress_bar);

        SessionManager sessionManager = new SessionManager(requireContext());
        AuthRepository repository = new AuthRepository(
                SupabaseApiClient.getApi(sessionManager),
                sessionManager
        );
        authViewModel = new ViewModelProvider(this, new AuthViewModelFactory(repository))
                .get(AuthViewModel.class);

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnLogin.setEnabled(!loading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorCode -> {
            if (errorCode != null) {
                String message = ErrorMessages.get(errorCode);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLoginResult().observe(getViewLifecycleOwner(), auth -> {
            if (auth != null) {
                AppPreferences prefs = new AppPreferences(requireContext());
                String track = prefs.getUserTrack();
                NavController navController = Navigation.findNavController(view);
                if (track == null || track.isEmpty()) {
                    navController.navigate(R.id.action_loginFragment_to_trackSelectFragment);
                } else {
                    navController.navigate(R.id.action_loginFragment_to_homeFragment);
                }
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("البريد الإلكتروني مطلوب");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("صيغة البريد الإلكتروني غير صحيحة");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("كلمة المرور مطلوبة");
                return;
            }
            authViewModel.login(email, password);
        });

        tvRegister.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etEmail = null;
        etPassword = null;
        btnLogin = null;
        progressBar = null;
    }
}
