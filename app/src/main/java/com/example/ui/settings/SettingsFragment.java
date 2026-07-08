package com.example.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.data.local.AppDatabase;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.ProfileRepository;
import com.example.util.AppExecutors;
import com.example.util.FileDownloadManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private static final float[] SCALES = {0.85f, 1.0f, 1.15f, 1.3f};
    private static final String[] SCALE_LABELS = {"صغير", "عادي", "كبير", "كبير جدًا"};

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
        ProfileRepository profileRepository = new ProfileRepository(SupabaseApiClient.getApi(sessionManager), sessionManager);

        TextView tvProfileName = view.findViewById(R.id.tv_profile_name);
        TextView tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        SwitchMaterial switchNightMode = view.findViewById(R.id.switch_night_mode);
        TextView tvFontValue = view.findViewById(R.id.tv_font_value);
        TextView tvStorage = view.findViewById(R.id.tv_storage);

        // ---- Profile (tap to edit name) ----
        final String[] currentName = {""};
        final String[] currentEmail = {""};
        profileRepository.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;
            currentName[0] = TextUtils.isEmpty(profile.getName()) ? "" : profile.getName();
            currentEmail[0] = profile.getEmail() != null ? profile.getEmail() : "";
            tvProfileName.setText(TextUtils.isEmpty(currentName[0]) ? "مستخدم منارة" : currentName[0]);
            tvProfileEmail.setText(currentEmail[0]);
        });
        profileRepository.fetchProfile();
        view.findViewById(R.id.profile_card).setOnClickListener(v -> showEditNameDialog(profileRepository, currentName[0]));

        // ---- Dark mode ----
        switchNightMode.setChecked(prefs.isNightMode());
        switchNightMode.setOnCheckedChangeListener((b, checked) -> {
            if (!b.isPressed()) return;
            prefs.setNightMode(checked);
            AppCompatDelegate.setDefaultNightMode(checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // ---- Font size ----
        tvFontValue.setText(labelForScale(prefs.getFontScale()));
        view.findViewById(R.id.row_font_size).setOnClickListener(v -> showFontDialog(prefs));

        // ---- Storage ----
        refreshStorage(tvStorage);
        view.findViewById(R.id.btn_clear_downloads).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("حذف كل الملفات المحمّلة؟")
                        .setPositiveButton("حذف", (d, w) -> clearDownloads(tvStorage))
                        .setNegativeButton("إلغاء", null)
                        .show());

        // ---- Other ----
        view.findViewById(R.id.row_countdown).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_examCountdownFragment));
        view.findViewById(R.id.row_share).setOnClickListener(v -> shareApp());
        view.findViewById(R.id.row_rate).setOnClickListener(v -> rateApp());
        view.findViewById(R.id.row_help).setOnClickListener(v -> simpleDialog("مساعدة",
                "• اختر مادة ثم القسم (كتب/امتحانات/ملخصات).\n• اضغط على الملف لتحميله ثم فتحه.\n• احفظ المهم في المفضلة، وتابع قراءتك من الشاشة الرئيسية.\n• حمّل الملفات لقراءتها بدون إنترنت.\n\nلأي استفسار راسلنا عبر متجر التطبيقات."));
        view.findViewById(R.id.row_about).setOnClickListener(v -> simpleDialog("عن التطبيق",
                "منارة — رفيقك في رحلة التوجيهي.\nكل الكتب والملخصات والامتحانات في مكان واحد.\n\nالإصدار: " + com.example.BuildConfig.VERSION_NAME));

        // ---- Change track / Logout ----
        view.findViewById(R.id.btn_change_track).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_trackSelectFragment));
        view.findViewById(R.id.btn_logout).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setMessage("هل أنت متأكد من تسجيل الخروج؟")
                        .setPositiveButton("نعم", (d, w) -> {
                            sessionManager.clearSession();
                            prefs.setUserTrack(null);
                            Navigation.findNavController(view).navigate(R.id.action_global_loginFragment);
                        })
                        .setNegativeButton("إلغاء", null)
                        .show());
    }

    private String labelForScale(float scale) {
        for (int i = 0; i < SCALES.length; i++) if (Math.abs(SCALES[i] - scale) < 0.01f) return SCALE_LABELS[i];
        return "عادي";
    }

    private void showFontDialog(AppPreferences prefs) {
        int current = 1;
        float s = prefs.getFontScale();
        for (int i = 0; i < SCALES.length; i++) if (Math.abs(SCALES[i] - s) < 0.01f) current = i;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("حجم الخط")
                .setSingleChoiceItems(SCALE_LABELS, current, (d, which) -> {
                    prefs.setFontScale(SCALES[which]);
                    d.dismiss();
                    if (isAdded()) requireActivity().recreate();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void showEditNameDialog(ProfileRepository repo, String prefill) {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_CLASS_TEXT);
        input.setHint("الاسم");
        input.setText(prefill);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout box = new FrameLayout(requireContext());
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("تعديل الاسم")
                .setView(box)
                .setPositiveButton("حفظ", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(requireContext(), "الاسم مطلوب", Toast.LENGTH_SHORT).show(); return; }
                    repo.updateName(name, ok -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), ok ? "تم حفظ الاسم" : "تعذّر الحفظ", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void refreshStorage(TextView tvStorage) {
        AppExecutors.io().execute(() -> {
            long size = FileDownloadManager.getDownloadsSize(requireContext().getApplicationContext());
            String text = FileDownloadManager.formatSize(size);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) tvStorage.setText(text);
            });
        });
    }

    private void clearDownloads(TextView tvStorage) {
        AppExecutors.io().execute(() -> {
            FileDownloadManager.clearDownloads(requireContext().getApplicationContext());
            AppDatabase.getDatabase(requireContext().getApplicationContext()).downloadDao().deleteAll();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                tvStorage.setText(FileDownloadManager.formatSize(0));
                Toast.makeText(requireContext(), "تم حذف كل التحميلات", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void shareApp() {
        String pkg = requireContext().getPackageName();
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,
                "حمّل تطبيق منارة — كل مصادر التوجيهي في مكان واحد 📚\nhttps://play.google.com/store/apps/details?id=" + pkg);
        startActivity(Intent.createChooser(i, "شارك التطبيق"));
    }

    private void rateApp() {
        String pkg = requireContext().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg)));
        }
    }

    private void simpleDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("حسناً", null)
                .show();
    }
}
