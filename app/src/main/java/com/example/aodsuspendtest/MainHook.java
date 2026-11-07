package com.example.aodsuspendtest;

import android.os.Handler;
import android.os.Looper;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/** Minimal proof of concept: request DOZE_SUSPEND five seconds after DOZE_AOD. */
public final class MainHook implements IXposedHookLoadPackage {
    private static final Map<Object, Boolean> scheduled = new WeakHashMap<>();

    @Override public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!"com.android.systemui".equals(lpparam.packageName)) return;
        final Class<?> machine = XposedHelpers.findClass("com.android.systemui.doze.DozeMachine", lpparam.classLoader);
        XposedBridge.hookAllMethods(machine, "requestState", new XC_MethodHook() {
            @Override protected void afterHookedMethod(final MethodHookParam p) {
                if (p.args.length == 0 || p.args[0] == null) return;
                final String state = String.valueOf(p.args[0]);
                if (!"DOZE_AOD".equals(state)) return;
                synchronized (scheduled) {
                    if (scheduled.containsKey(p.thisObject)) return;
                    scheduled.put(p.thisObject, Boolean.TRUE);
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        Object suspend = null;
                        for (Object value : p.args[0].getClass().getEnumConstants()) {
                            if ("DOZE_SUSPEND".equals(String.valueOf(value))) suspend = value;
                        }
                        if (suspend != null) ((Method) p.method).invoke(p.thisObject, suspend);
                        XposedBridge.log("AOD Suspend Test: requested DOZE_SUSPEND");
                    } catch (Throwable t) { XposedBridge.log(t); }
                }, 5000L);
            }
        });
        XposedBridge.log("AOD Suspend Test loaded for SystemUI");
    }
}
