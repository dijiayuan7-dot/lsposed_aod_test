# LSPosed AOD Suspend Test

This is source for a minimal LSPosed module. It hooks `DozeMachine.requestState()` in `com.android.systemui`; after `DOZE_AOD` it waits five seconds and requests `DOZE_SUSPEND`. It is intentionally experimental and may not match every SystemUI build.

Build with Android Studio/Gradle, install the APK, enable it in LSPosed for **System SystemUI**, then reboot. Disable the module if SystemUI crashes or the display does not wake.
