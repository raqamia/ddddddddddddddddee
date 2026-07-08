package com.example.ui.examcountdown;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.R;
import com.example.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExamCountdownFragment extends Fragment {

    private TextView tvDays, tvHours, tvMinutes, tvSeconds, tvMotivation;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long examTime;
    private boolean running = true;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exam_countdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        tvDays = view.findViewById(R.id.tv_days);
        tvHours = view.findViewById(R.id.tv_hours);
        tvMinutes = view.findViewById(R.id.tv_minutes);
        tvSeconds = view.findViewById(R.id.tv_seconds);
        tvMotivation = view.findViewById(R.id.tv_motivation);

        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date exam = fmt.parse(Constants.TAWJIHI_EXAM_DATE);
            examTime = exam.getTime();
        } catch (Exception e) {
            examTime = 0;
        }

        startTicker();
    }

    private void startTicker() {
        running = true;
        handler.post(ticker);
    }

    private void updateDisplay() {
        long diff = examTime - System.currentTimeMillis();

        if (diff <= 0) {
            tvDays.setText("00");
            tvHours.setText("00");
            tvMinutes.setText("00");
            tvSeconds.setText("00");
            tvMotivation.setText("الامتحانات انطلقت! — بالتوفيق والنجاح 🎓");
            return;
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

        tvDays.setText(pad(days));
        tvHours.setText(pad(hours));
        tvMinutes.setText(pad(minutes));
        tvSeconds.setText(pad(seconds));

        if (days > 30) {
            tvMotivation.setText("كل ثانية بتفرق — ابدأ من اليوم");
        } else if (days > 7) {
            tvMotivation.setText("الوقت يضيق — ركّز واجتهد");
        } else if (days > 1) {
            tvMotivation.setText("الأيام الأخيرة — ثابر ولا تستسلم");
        } else {
            tvMotivation.setText("غداً يوم الحسم — أنت مستعد!");
        }
    }

    private String pad(long n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        handler.removeCallbacks(ticker);
    }
}
