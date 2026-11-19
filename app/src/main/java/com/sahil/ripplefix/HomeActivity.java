package com.sahil.ripplefix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity {

    public static final String PREFS_NAME = "evotempfix_prefs";
    public static final String KEY_FP_RIPPLE = "fix_fingerprint_ripple";

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
                : android.R.color.system_neutral1_10);
        int textPrimary = getColor(isNight
                ? android.R.color.system_neutral1_50
                : android.R.color.system_neutral1_900);
        int textSecondary = getColor(isNight
                ? android.R.color.system_neutral2_200
                : android.R.color.system_neutral2_700);
        int accentColor = getColor(isNight
                ? android.R.color.system_accent1_200
                : android.R.color.system_accent1_600);

        Window window = getWindow();
        window.setStatusBarColor(bgColor);
        window.setNavigationBarColor(bgColor);
        if (!isNight) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);
        root.setFitsSystemWindows(true);

        // Top App Bar ("EvoTempFix" + 3-dot menu)
        LinearLayout appBar = new LinearLayout(this);
        appBar.setOrientation(LinearLayout.HORIZONTAL);
        appBar.setGravity(Gravity.CENTER_VERTICAL);
        appBar.setPadding(dp(20), dp(14), dp(12), dp(14));

        TextView titleView = new TextView(this);
        titleView.setText("EvoTempFix");
        titleView.setTextColor(textPrimary);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 21);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        appBar.addView(titleView, titleLp);

        TextView menuBtn = new TextView(this);
        menuBtn.setText("⋮");
        menuBtn.setTextColor(textPrimary);
        menuBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        menuBtn.setGravity(Gravity.CENTER);
        menuBtn.setPadding(dp(14), dp(6), dp(14), dp(6));
        menuBtn.setOnClickListener(v -> showOverflowMenu(v));
        appBar.addView(menuBtn);

        root.addView(appBar);

        // Warning banner if LSPosed New XSharedPreferences isn't active yet
        if (!isModuleActive) {
            TextView warn = new TextView(this);
            warn.setText("Module not active in LSPosed. Enable EvoTempFix and reboot to sync toggles.");
            warn.setTextColor(Color.parseColor(isNight ? "#FFB4AB" : "#BA1A1A"));
            warn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            warn.setPadding(dp(20), dp(8), dp(20), dp(12));
            root.addView(warn);
        }

        ScrollView scrollView = new ScrollView(this);
        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, dp(4), 0, dp(24));

        // Add Fix Toggles here (Core Patch N style)
        listContainer.addView(createToggleRow(
                "Fingerprint unlock ripple",
                "Restore side-mounted fingerprint sensor ripple animation on Evolution X lockscreen.",
                KEY_FP_RIPPLE,
                true,
                textPrimary,
                textSecondary,
                accentColor,
                isNight
        ));

        scrollView.addView(listContainer);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
    }

    private View createToggleRow(
            String title,
            String summary,
            String prefKey,
            boolean defaultValue,
            int textPrimary,
            int textSecondary,
            int accentColor,
            boolean isNight) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(14), dp(20), dp(14));

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        row.setBackgroundResource(outValue.resourceId);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textColLp.setMarginEnd(dp(16));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(textPrimary);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tvTitle.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        textCol.addView(tvTitle);

        TextView tvSummary = new TextView(this);
        tvSummary.setText(summary);
        tvSummary.setTextColor(textSecondary);
        tvSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        tvSummary.setPadding(0, dp(3), 0, 0);
        textCol.addView(tvSummary);

        row.addView(textCol, textColLp);

        Switch toggle = new Switch(this);
        toggle.setChecked(prefs.getBoolean(prefKey, defaultValue));
        toggle.setClickable(false);
        toggle.setFocusable(false);

        int trackOff = getColor(isNight
                ? android.R.color.system_neutral2_700
                : android.R.color.system_neutral2_200);
        int trackOn = adjustAlpha(accentColor, 0.35f);
        int thumbOff = getColor(isNight
                ? android.R.color.system_neutral2_300
                : android.R.color.system_neutral1_0);

        toggle.setThumbTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{accentColor, thumbOff}
        ));
        toggle.setTrackTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{trackOn, trackOff}
        ));

        row.addView(toggle);

        row.setOnClickListener(v -> {
            boolean newState = !toggle.isChecked();
            toggle.setChecked(newState);
            prefs.edit().putBoolean(prefKey, newState).apply();
        });

        return row;
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
