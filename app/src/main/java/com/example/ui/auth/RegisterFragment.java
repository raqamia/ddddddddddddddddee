package com.example.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.R;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.AuthRepository;
import com.example.util.ErrorMessages;

public class RegisterFragment extends Fragment {

    private AuthViewModel authViewModel;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnRegister = view.findViewById(R.id.btn_register);
        TextView tvLogin = view.findViewById(R.id.tv_login);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        cbTerms = view.findViewById(R.id.cb_terms);
        progressBar = view.findViewById(R.id.progress_bar);

        SessionManager sessionManager = new SessionManager(requireContext());
        AuthRepository repository = new AuthRepository(
                SupabaseApiClient.getApi(sessionManager),
                sessionManager
        );
        authViewModel = new ViewModelProvider(this, new AuthViewModelFactory(repository))
                .get(AuthViewModel.class);

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            btnRegister.setEnabled(!loading);
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorCode -> {
            if (errorCode != null) {
                String message = ErrorMessages.get(errorCode);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getRegisterResult().observe(getViewLifecycleOwner(), auth -> {
            if (auth != null) {
                Toast.makeText(requireContext(), "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(
                        R.id.action_registerFragment_to_trackSelectFragment
                );
            }
        });

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        tvLogin.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("الاسم مطلوب");
                return;
            }
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
            if (password.length() < 8) {
                etPassword.setError("كلمة المرور يجب أن تكون ٨ أحرف على الأقل");
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("كلمة المرور غير متطابقة");
                return;
            }
            if (!cbTerms.isChecked()) {
                Toast.makeText(requireContext(), "يرجى الموافقة على الشروط والأحكام", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(name, email, password);
        });
    }
}
