# AuraFit3D ProGuard rules

# Keep biometric engine entry points (used by Compose reflection in tests)
-keep class com.aurafit3d.data.biometric.** { *; }
-keep class com.aurafit3d.data.model.** { *; }

# Compose (handled by the Compose Gradle plugin, included for safety)
-dontobfuscate
