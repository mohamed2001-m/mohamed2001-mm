package com.deutschb1trainer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * Deutsch B1 Trainer: Arabic navigation, German B1 readings, vocabulary and
 * instant-feedback quizzes. No login, network permission or third-party SDK.
 */
public final class MainActivity extends Activity {
    private static final int TEAL = Color.rgb(4, 118, 108);
    private static final int TEAL_DARK = Color.rgb(4, 87, 84);
    private static final int INK = Color.rgb(20, 38, 51);
    private static final int MUTED = Color.rgb(89, 105, 119);
    private static final int BG = Color.rgb(245, 248, 249);
    private static final int GREEN = Color.rgb(27, 120, 77);
    private static final int RED = Color.rgb(184, 48, 48);

    private JSONArray lessons;
    private LinearLayout page;
    private SharedPreferences prefs;
    private int lessonIndex, questionIndex, correctAnswers;
    private boolean answered;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(TEAL_DARK);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences("b1_progress", Context.MODE_PRIVATE);
        try {
            lessons = loadLessons();
            if (lessons.length() == 0) {
                showError("لا توجد دروس متاحة.");
            } else {
                showHome();
            }
        } catch (Exception e) {
            showError("تعذّر تحميل الدروس. الرجاء إعادة تثبيت التطبيق.");
        }
    }

    private JSONArray loadLessons() throws Exception {
        try (InputStream input = getAssets().open("lessons.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return new JSONArray(new String(output.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void begin() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(32));
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(page);
        setContentView(scroll);
    }

    private GradientDrawable shape(int color) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(color);
        result.setCornerRadius(dp(15));
        return result;
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT | Gravity.TOP);
        view.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        view.setLineSpacing(dp(3), 1.07f);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(13);
        page.addView(view, p);
        return view;
    }

    private TextView card(String value, boolean german, boolean emphasized) {
        TextView view = text(value, german ? 17 : 16, emphasized, INK);
        view.setPadding(dp(17), dp(17), dp(17), dp(17));
        view.setBackground(shape(Color.WHITE));
        if (german) {
            view.setTextDirection(View.TEXT_DIRECTION_LTR);
            view.setGravity(Gravity.LEFT | Gravity.TOP);
            view.setLineSpacing(dp(6), 1.14f);
        }
        return view;
    }

    private Button button(String title, View.OnClickListener click) {
        return button(title, click, TEAL);
    }

    private Button button(String title, View.OnClickListener click, int color) {
        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(ColorStateList.valueOf(color));
        b.setOnClickListener(click);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(10);
        page.addView(b, p);
        return b;
    }

    private JSONObject lesson(int index) {
        return lessons.optJSONObject(index);
    }

    private int dailyLesson() {
        Calendar date = Calendar.getInstance();
        return Math.floorMod(date.get(Calendar.YEAR) * 366 + date.get(Calendar.DAY_OF_YEAR),
                lessons.length());
    }

    private int completedLessons() {
        int count = 0;
        for (int i = 0; i < lessons.length(); i++) if (prefs.getInt("best_" + i, -1) >= 3) count++;
        return count;
    }

    private void showHome() {
        begin();
        text("DEUTSCH · B1", 29, true, TEAL_DARK);
        text("قصص ألمانية، مفردات جديدة واختبارات تفاعلية", 16, false, MUTED);
        card("تقدّمك: " + completedLessons() + " من " + lessons.length() + " دروس مكتملة"
                + "\nسلسلة التعلّم: " + prefs.getInt("streak", 0) + " يوم", false, true);
        int today = dailyLesson();
        text("✦ درس اليوم", 21, true, INK);
        card(lesson(today).optString("title") + "\n" + lesson(today).optString("topic"), false, true);
        button("ابدأ درس اليوم", v -> showReading(today));
        button("إنجازاتي", v -> showProgress(), TEAL_DARK);
        text("جميع القصص", 21, true, INK);
        for (int i = 0; i < lessons.length(); i++) {
            final int selected = i;
            int best = prefs.getInt("best_" + i, -1);
            String suffix = best < 0 ? "" : "  ·  أفضل نتيجة " + best + "/5";
            button((i + 1) + ". " + lesson(i).optString("title") + suffix,
                    v -> showReading(selected));
        }
        text("تعمل الدروس والاختبارات دون إنترنت. التقدم محفوظ على هذا الهاتف.",
                13, false, MUTED);
    }

    private void showReading(int index) {
        lessonIndex = index;
        JSONObject current = lesson(index);
        begin();
        button("‹ العودة إلى الرئيسية", v -> showHome(), TEAL_DARK);
        text(current.optString("title"), 25, true, TEAL_DARK);
        text(current.optString("topic"), 16, false, MUTED);
        card("قاعدة اليوم: " + current.optString("grammar"), false, true);
        text("Lesen · نص القراءة", 20, true, INK);
        card(current.optString("reading"), true, false);
        text("Wortschatz · المفردات", 20, true, INK);
        JSONArray words = current.optJSONArray("words");
        if (words != null) {
            for (int i = 0; i < words.length(); i++) {
                JSONArray item = words.optJSONArray(i);
                if (item == null) continue;
                card((i + 1) + ". " + item.optString(0) + "\n" + item.optString(1), false, false);
            }
        }
        button("أنهيت القراءة · احفظ تقدّمي", v -> {
            prefs.edit().putBoolean("read_" + lessonIndex, true).apply();
            recordStudy();
            showReading(lessonIndex);
        }, TEAL_DARK);
        button("ابدأ اختبار الفهم · 5 أسئلة", v -> {
            prefs.edit().putBoolean("read_" + lessonIndex, true).apply();
            recordStudy();
            startQuiz();
        });
    }

    private void startQuiz() {
        questionIndex = 0;
        correctAnswers = 0;
        showQuestion();
    }

    private void showQuestion() {
        JSONArray questions = lesson(lessonIndex).optJSONArray("questions");
        if (questions == null || questionIndex >= questions.length()) {
            showResult();
            return;
        }
        JSONObject item = questions.optJSONObject(questionIndex);
        if (item == null) { showError("هذا السؤال غير متاح."); return; }
        JSONArray choices = item.optJSONArray("options");
        if (choices == null || choices.length() != 3) { showError("خيارات غير مكتملة."); return; }
        answered = false;
        begin();
        button("‹ العودة إلى النص", v -> showReading(lessonIndex), TEAL_DARK);
        text("Lesen B1 · " + (questionIndex + 1) + " / " + questions.length(),
                18, true, TEAL);
        card(item.optString("q"), true, true);
        final Button[] buttons = new Button[choices.length()];
        for (int i = 0; i < choices.length(); i++) {
            final int chosen = i;
            buttons[i] = button(choices.optString(i), v -> {
                if (answered) return;
                answered = true;
                int answer = item.optInt("answer", -1);
                boolean correct = chosen == answer;
                if (correct) correctAnswers++;
                for (Button b : buttons) b.setEnabled(false);
                text(correct ? "✓ إجابة صحيحة" : "✗ حاول مرة أخرى في المراجعة",
                        20, true, correct ? GREEN : RED);
                if (!correct && answer >= 0 && answer < choices.length()) {
                    card("الإجابة الصحيحة: " + choices.optString(answer), true, false);
                }
                button(questionIndex + 1 < questions.length() ? "السؤال التالي" : "عرض النتيجة",
                        next -> { questionIndex++; showQuestion(); });
            });
        }
    }

    private void showResult() {
        int best = Math.max(correctAnswers, prefs.getInt("best_" + lessonIndex, -1));
        prefs.edit().putInt("best_" + lessonIndex, best)
                .putInt("attempts", prefs.getInt("attempts", 0) + 1).apply();
        recordStudy();
        begin();
        text("نتيجة الاختبار", 28, true, TEAL_DARK);
        card(correctAnswers + " / 5  ·  أفضل نتيجة: " + best + " / 5", false, true);
        text(correctAnswers == 5 ? "ممتاز! أجبت عن كل الأسئلة بشكل صحيح." :
                correctAnswers >= 3 ? "جيد جدًا! أكملت الدرس. استمرّ بالمراجعة." :
                "اقرأ القصة مرة أخرى وحاول تحسين نتيجتك.", 17, false, INK);
        button("إعادة الاختبار", v -> startQuiz());
        button("مراجعة المفردات", v -> showReading(lessonIndex), TEAL_DARK);
        button("الرئيسية", v -> showHome(), TEAL_DARK);
    }

    private void showProgress() {
        begin();
        button("‹ العودة إلى الرئيسية", v -> showHome(), TEAL_DARK);
        text("إنجازاتي", 28, true, TEAL_DARK);
        card("دروس مكتملة: " + completedLessons() + " / " + lessons.length()
                + "\nاختبارات أُجريت: " + prefs.getInt("attempts", 0)
                + "\nأيام متتالية: " + prefs.getInt("streak", 0), false, true);
        for (int i = 0; i < lessons.length(); i++) {
            boolean read = prefs.getBoolean("read_" + i, false);
            int best = prefs.getInt("best_" + i, -1);
            String status = best >= 3 ? "✓ مكتمل" : read ? "قيد التعلّم" : "لم تبدأ";
            card(lesson(i).optString("title") + "\n" + status +
                    (best >= 0 ? " · أفضل نتيجة " + best + "/5" : ""), false, false);
        }
        text("التقدم يُحفظ محليًا فقط وقد يُحذف عند إزالة التطبيق أو حذف بياناته.",
                13, false, MUTED);
    }

    private void recordStudy() {
        Calendar now = Calendar.getInstance();
        String today = now.get(Calendar.YEAR) + "-" + now.get(Calendar.DAY_OF_YEAR);
        if (today.equals(prefs.getString("last_study", ""))) return;
        Calendar yesterdayDate = Calendar.getInstance();
        yesterdayDate.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = yesterdayDate.get(Calendar.YEAR) + "-"
                + yesterdayDate.get(Calendar.DAY_OF_YEAR);
        int streak = yesterday.equals(prefs.getString("last_study", ""))
                ? prefs.getInt("streak", 0) + 1 : 1;
        prefs.edit().putString("last_study", today).putInt("streak", streak).apply();
    }

    private void showError(String value) {
        begin();
        TextView view = text(value, 19, true, RED);
        view.setGravity(Gravity.CENTER);
    }

    @Override public void onBackPressed() {
        if (lessons == null || lessons.length() == 0) { super.onBackPressed(); return; }
        showHome();
    }
}
