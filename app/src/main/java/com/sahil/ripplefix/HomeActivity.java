package com.sahil.ripplefix;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

    public static final String PREFS_NAME = "evotempfix_prefs";
    public static final String KEY_FP_RIPPLE = "fix_fingerprint_ripple";
    public static final String KEY_SCROLL_CAPTURE_FIX = "fix_scroll_capture_overlap";

    private SharedPreferences prefs;
    private boolean isModuleActive = true;

    @SuppressLint("WorldReadableFiles")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        } catch (SecurityException e) {
            isModuleActive = false;
            prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        boolean isNight = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int bgColor = getColor(isNight
                ? android.R.color.system_neutral1_900
                : android.R.color.system_neutral1_50);
        int headerColor = getColor(isNight
                ? android.R.color.system_neutral1_800
                : android.R.color.system_neutral1_10);
        int cardColor = getColor(isNight
                ? android.R.color.system_neutral1_800
                : android.R.color.system_neutral1_0);
        int textPrimary = getColor(isNight
                ? android.R.color.system_neutral1_50
                : android.R.color.system_neutral1_900);
        int textSecondary = getColor(isNight
                ? android.R.color.system_neutral2_200
                : android.R.color.system_neutral2_700);
        int accentColor = getColor(isNight
                ? android.R.color.system_accent1_200
                : android.R.color.system_accent1_600);
        int onAccentColor = getColor(isNight
                ? android.R.color.system_accent1_800
                : android.R.color.system_neutral1_0);
        int outlineColor = getColor(isNight
                ? android.R.color.system_neutral2_600
                : android.R.color.system_neutral2_300);
        int rippleColor = adjustAlpha(textPrimary, 0.10f);

        Window window = getWindow();
        window.setStatusBarColor(headerColor);
        window.setNavigationBarColor(bgColor);
        if (!isNight) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        root.setFitsSystemWindows(true);

        // Elevated Material You Top App Bar with Shadow
        LinearLayout appBar = new LinearLayout(this);
        appBar.setOrientation(LinearLayout.HORIZONTAL);
        appBar.setGravity(Gravity.CENTER_VERTICAL);
        appBar.setBackgroundColor(headerColor);
        appBar.setPadding(dp(20), dp(14), dp(10), dp(14));
        appBar.setElevation(dp(6));
        appBar.setOutlineProvider(ViewOutlineProvider.BOUNDS);

        TextView titleView = new TextView(this);
        titleView.setText("EvoTempFix");
        titleView.setTextColor(textPrimary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        appBar.addView(titleView, titleLp);

        FrameLayout menuBtn = new FrameLayout(this);
        GradientDrawable circleMask = new GradientDrawable();
        circleMask.setShape(GradientDrawable.OVAL);
        circleMask.setColor(Color.WHITE);
        menuBtn.setBackground(new RippleDrawable(
                ColorStateList.valueOf(rippleColor), null, circleMask));

        TextView menuIcon = new TextView(this);
        menuIcon.setText("⋮");
        menuIcon.setTextColor(textPrimary);
        menuIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        menuIcon.setGravity(Gravity.CENTER);
        menuBtn.addView(menuIcon, new FrameLayout.LayoutParams(dp(42), dp(42)));
        menuBtn.setOnClickListener(this::showOverflowMenu);
        appBar.addView(menuBtn);

        root.addView(appBar);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setClipToPadding(false);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(28));

        if (!isModuleActive) {
            TextView warn = new TextView(this);
            warn.setText("Module inactive in LSPosed. Enable EvoTempFix and reboot to sync toggles.");
            warn.setTextColor(Color.parseColor(isNight ? "#FFB4AB" : "#93000A"));
            warn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            warn.setPadding(dp(16), dp(12), dp(16), dp(12));

            GradientDrawable warnBg = new GradientDrawable();
            warnBg.setCornerRadius(dp(16));
            warnBg.setColor(Color.parseColor(isNight ? "#410002" : "#FFDAD6"));
            warn.setBackground(warnBg);

            LinearLayout.LayoutParams warnLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            warnLp.bottomMargin = dp(16);
            content.addView(warn, warnLp);
        }

        TextView sectionLabel = new TextView(this);
        sectionLabel.setText("SystemUI");
        sectionLabel.setTextColor(accentColor);
        sectionLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        sectionLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        sectionLabel.setPadding(dp(8), dp(2), dp(8), dp(10));
        content.addView(sectionLabel);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setCornerRadius(dp(22));
        cardBg.setColor(cardColor);
        cardBg.setStroke(dp(1), adjustAlpha(outlineColor, 0.45f));
        card.setBackground(cardBg);
        card.setElevation(dp(1));
        card.setClipToOutline(true);

        card.addView(createMaterialToggleRow(
                "Fingerprint unlock ripple",
                "Restore side-mounted fingerprint sensor ripple animation on Evolution X lockscreen.",
                KEY_FP_RIPPLE,
                true,
                textPrimary,
                textSecondary,
                accentColor,
                onAccentColor,
                outlineColor,
                rippleColor,
                isNight
        ));

        View divider = new View(this);
        divider.setBackgroundColor(adjustAlpha(outlineColor, 0.35f));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        divLp.setMargins(dp(20), 0, dp(20), 0);
        card.addView(divider, divLp);

        card.addView(createMaterialToggleRow(
                "Fix scrolling screenshot overlap",
                "Trim floating bottom navigation bar overlap from stitched tiles in Compose apps.",
                KEY_SCROLL_CAPTURE_FIX,
                true,
                textPrimary,
                textSecondary,
                accentColor,
                onAccentColor,
                outlineColor,
                rippleColor,
                isNight
        ));

        content.addView(card);
        scrollView.addView(content);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
    }

    private View createMaterialToggleRow(
            String title,
            String summary,
            String prefKey,
            boolean defaultValue,
            int textPrimary,
            int textSecondary,
            int accentColor,
            int onAccentColor,
            int outlineColor,
            int rippleColor,
            boolean isNight) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(18), dp(18), dp(18));

        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        row.setBackground(new RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask));

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textColLp.setMarginEnd(dp(16));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(textPrimary);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.5f);
        tvTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        textCol.addView(tvTitle);

        TextView tvSummary = new TextView(this);
        tvSummary.setText(summary);
        tvSummary.setTextColor(textSecondary);
        tvSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        tvSummary.setLineSpacing(dp(2), 1.0f);
        tvSummary.setPadding(0, dp(4), 0, 0);
        textCol.addView(tvSummary);

        row.addView(textCol, textColLp);

        int trackOffFill = getColor(isNight
                ? android.R.color.system_neutral2_800
                : android.R.color.system_neutral2_100);
        int thumbOffFill = getColor(isNight
                ? android.R.color.system_neutral2_300
                : android.R.color.system_neutral2_600);

        Material3Switch m3Switch = new Material3Switch(
                this,
                prefs.getBoolean(prefKey, defaultValue),
                accentColor,
                onAccentColor,
                trackOffFill,
                thumbOffFill,
                outlineColor
        );
        row.addView(m3Switch, new LinearLayout.LayoutParams(dp(52), dp(32)));

        row.setOnClickListener(v -> {
            boolean newState = !m3Switch.isChecked();
            m3Switch.setChecked(newState, true);
            prefs.edit().putBoolean(prefKey, newState).apply();
            Toast.makeText(this, "Restart SystemUI from menu to apply", Toast.LENGTH_SHORT).show();
        });

        return row;
    }

    private static class Material3Switch extends View {
        private boolean checked;
        private float progress;
        private final int trackOnColor;
        private final int thumbOnColor;
        private final int trackOffColor;
        private final int thumbOffColor;
        private final int borderOffColor;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF trackRect = new RectF();
        private ValueAnimator animator;

        Material3Switch(Context ctx, boolean initial, int trackOn, int thumbOn,
                        int trackOff, int thumbOff, int borderOff) {
            super(ctx);
            this.checked = initial;
            this.progress = initial ? 1f : 0f;
            this.trackOnColor = trackOn;
            this.thumbOnColor = thumbOn;
            this.trackOffColor = trackOff;
            this.thumbOffColor = thumbOff;
            this.borderOffColor = borderOff;
        }

        boolean isChecked() {
            return checked;
        }

        void setChecked(boolean value, boolean animate) {
            if (this.checked == value) return;
            this.checked = value;
            if (animator != null) animator.cancel();
            if (!animate) {
                progress = value ? 1f : 0f;
                invalidate();
                return;
            }
            animator = ValueAnimator.ofFloat(progress, value ? 1f : 0f);
            animator.setDuration(180);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float density = getResources().getDisplayMetrics().density;
            float stroke = 2f * density;
            float radius = h / 2f;

            trackRect.set(stroke / 2f, stroke / 2f, w - stroke / 2f, h - stroke / 2f);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blendColor(trackOffColor, trackOnColor, progress));
            canvas.drawRoundRect(trackRect, radius, radius, paint);

            if (progress < 1f) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(stroke);
                int alpha = Math.round(Color.alpha(borderOffColor) * (1f - progress));
                paint.setColor(Color.argb(alpha, Color.red(borderOffColor),
                        Color.green(borderOffColor), Color.blue(borderOffColor)));
                canvas.drawRoundRect(trackRect, radius, radius, paint);
            }

            float thumbRadiusOff = 8f * density;
            float thumbRadiusOn = 12f * density;
            float thumbRadius = thumbRadiusOff + (thumbRadiusOn - thumbRadiusOff) * progress;

            float startX = h / 2f;
            float endX = w - (h / 2f);
            float cx = startX + (endX - startX) * progress;
            float cy = h / 2f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(blendColor(thumbOffColor, thumbOnColor, progress));
            canvas.drawCircle(cx, cy, thumbRadius, paint);
        }

        private static int blendColor(int c1, int c2, float ratio) {
            float inv = 1f - ratio;
            int a = Math.round(Color.alpha(c1) * inv + Color.alpha(c2) * ratio);
            int r = Math.round(Color.red(c1) * inv + Color.red(c2) * ratio);
            int g = Math.round(Color.green(c1) * inv + Color.green(c2) * ratio);
            int b = Math.round(Color.blue(c1) * inv + Color.blue(c2) * ratio);
            return Color.argb(a, r, g, b);
        }
    }

    private void showOverflowMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Restart SystemUI");
        popup.getMenu().add(0, 2, 1, "Reset defaults");
        popup.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == 1) {
                try {
                    Runtime.getRuntime().exec(new String[]{"su", "-c", "killall com.android.systemui"});
                } catch (Exception e) {
                    Toast.makeText(this, "Root permission required to restart SystemUI", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (item.getItemId() == 2) {
                prefs.edit().clear().apply();
                recreate();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
