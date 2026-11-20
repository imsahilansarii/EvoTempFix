package com.sahil.ripplefix;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static Object sRippleController = null;
    private static Method sInternalMethod = null;
    private static Method sPublicMethod = null;
    private static Object sFpSourceType = null;
    private static long sLastRippleTime = 0;
    private static final Point SENSOR_POINT = new Point(1080, 1025);

    private static boolean isFixEnabled(String key, boolean defaultValue) {
        try {
            XSharedPreferences prefs = new XSharedPreferences("com.sahil.ripplefix", HomeActivity.PREFS_NAME);
            prefs.makeWorldReadable();
            prefs.reload();
            return prefs.getBoolean(key, defaultValue);
        } catch (Throwable ignored) {
            return defaultValue;
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) return;

        if (isFixEnabled(HomeActivity.KEY_FP_RIPPLE, true)) {
            hookRippleFix(lpparam.classLoader);
        }

        if (isFixEnabled(HomeActivity.KEY_SCROLL_CAPTURE_FIX, true)) {
            hookScrollCaptureOverlapFix(lpparam.classLoader);
        }
    }

    private void hookScrollCaptureOverlapFix(ClassLoader cl) {
        Class<?> tileSetClass = XposedHelpers.findClassIfExists(
                "com.android.systemui.screenshot.scroll.ImageTileSet", cl);
        if (tileSetClass == null) return;

        XposedBridge.hookAllMethods(tileSetClass, "addTile", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args == null || param.args.length == 0 || param.args[0] == null) return;
                Object imageTile = param.args[0];
                Rect location = (Rect) XposedHelpers.getObjectField(imageTile, "mLocation");
                if (location == null) return;

                float density = Resources.getSystem().getDisplayMetrics().density;
                int trimPx = Math.round(108f * density);

                // Only trim when the captured tile is tall enough so short end-tiles stay intact
                if (location.height() > trimPx * 2) {
                    location.bottom -= trimPx;
                }
            }
        });
    }

    private void hookRippleFix(ClassLoader cl) {
        Class<?> rippleCtrlClass = XposedHelpers.findClassIfExists(
                "com.android.systemui.biometrics.AuthRippleController", cl);
        if (rippleCtrlClass == null) return;

        try {
            Class<?> bioTypeClass = XposedHelpers.findClass(
                    "android.hardware.biometrics.BiometricSourceType", cl);
            sFpSourceType = Enum.valueOf((Class<Enum>) bioTypeClass, "FINGERPRINT");
        } catch (Throwable ignored) {}

        for (Method m : rippleCtrlClass.getDeclaredMethods()) {
            if (m.getName().equals("access$showUnlockRippleInternal")) {
                m.setAccessible(true);
                sInternalMethod = m;
            } else if (m.getName().equals("showUnlockedRipple")) {
                m.setAccessible(true);
                sPublicMethod = m;
            }
        }

        XposedBridge.hookAllConstructors(rippleCtrlClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                sRippleController = param.thisObject;
            }
        });

        XposedBridge.hookAllMethods(rippleCtrlClass, "showUnlockedRipple", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object ctrl = param.thisObject;
                try {
                    XposedHelpers.setObjectField(ctrl, "fingerprintSensorLocation", SENSOR_POINT);
                } catch (Throwable ignored) {}
                for (Field f : ctrl.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(ctrl);
                    if (val instanceof View) prepareRippleView((View) val);
                }
                try {
                    Field mViewField = XposedHelpers.findFieldIfExists(ctrl.getClass().getSuperclass(), "mView");
                    if (mViewField != null) {
                        mViewField.setAccessible(true);
                        View v = (View) mViewField.get(ctrl);
                        if (v != null) prepareRippleView(v);
                    }
                } catch (Throwable ignored) {}
            }
        });

        Class<?> kumClass = XposedHelpers.findClassIfExists(
                "com.android.keyguard.KeyguardUpdateMonitor", cl);
        if (kumClass != null) {
            XposedBridge.hookAllMethods(kumClass, "onFingerprintAuthenticated", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    long now = System.currentTimeMillis();
                    if (now - sLastRippleTime < 450 || sRippleController == null) return;
                    sLastRippleTime = now;

                    try {
                        if (sInternalMethod != null && sFpSourceType != null) {
                            if (sInternalMethod.getParameterCount() == 2) {
                                sInternalMethod.invoke(null, sRippleController, sFpSourceType);
                            } else {
                                sInternalMethod.invoke(sRippleController, sFpSourceType);
                            }
                        } else if (sPublicMethod != null) {
                            if (sPublicMethod.getParameterCount() == 0) {
                                sPublicMethod.invoke(sRippleController);
                            } else {
                                sPublicMethod.invoke(sRippleController, sFpSourceType);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            });
        }
    }

    private static void prepareRippleView(View v) {
        v.setVisibility(View.VISIBLE);
        if (v.getWidth() == 0 || v.getHeight() == 0) {
            DisplayMetrics dm = v.getResources().getDisplayMetrics();
            int w = dm.widthPixels > 0 ? dm.widthPixels : 1080;
            int h = dm.heightPixels > 0 ? dm.heightPixels : 2400;
            v.layout(0, 0, w, h);
        }
        try {
            XposedHelpers.callMethod(v, "setFingerprintSensorLocation", SENSOR_POINT, 0f);
        } catch (Throwable e1) {
            try {
                XposedHelpers.callMethod(v, "setFingerprintSensorLocation", SENSOR_POINT);
            } catch (Throwable ignored) {}
        }
    }
}
