package com.example.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.data.remote.dto.AuthResponse;
import com.example.data.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;

    private final MutableLiveData<AuthResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<AuthResponse> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<AuthResponse> getLoginResult() { return loginResult; }
    public LiveData<AuthResponse> getRegisterResult() { return registerResult; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            error.setValue("البريد الإلكتروني مطلوب");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            error.setValue("كلمة المرور مطلوبة");
            return;
        }
        isLoading.setValue(true);
        error.setValue(null);
        repository.login(email.trim(), password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse auth) {
                isLoading.postValue(false);
                loginResult.postValue(auth);
            }

            @Override
            public void onError(String errorCode) {
                isLoading.postValue(false);
                error.postValue(errorCode);
            }
        });
    }

    public void register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            error.setValue("الاسم مطلوب");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            error.setValue("البريد الإلكتروني مطلوب");
            return;
        }
        if (password == null || password.length() < 8) {
            error.setValue("كلمة المرور يجب أن تكون ٨ أحرف على الأقل");
            return;
        }
        isLoading.setValue(true);
        error.setValue(null);
        repository.register(name.trim(), email.trim(), password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse auth) {
                isLoading.postValue(false);
                registerResult.postValue(auth);
            }

            @Override
            public void onError(String errorCode) {
                isLoading.postValue(false);
                error.postValue(errorCode);
            }
        });
    }

    public void clearError() {
        error.setValue(null);
    }
}
