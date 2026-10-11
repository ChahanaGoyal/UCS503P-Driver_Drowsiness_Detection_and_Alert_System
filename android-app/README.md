# Driver Drowsiness Detection — Android App

Current Android source snapshot using Kotlin, Jetpack Compose, CameraX and MediaPipe Face Landmarker. Detection runs on the device; this project does not include a separate server backend.

## Included functionality
- Camera permission request and front-camera preview.
- Rotation and mirroring of camera frames.
- MediaPipe face landmark detection with the bundled `face_landmarker.task` model.
- EAR and MAR calculations with on-screen readings and detection status.
- Eye-closure detection: EAR below 0.22 for at least 1500 ms.
- Mouth-opening/yawning detection: MAR above 0.35 for at least 1000 ms.
- Detection state reset when no face is returned.

## Current limitations
The analyzer returns an `alarmRequired` flag, but the current interface does not use it to play a sound or vibrate. Home/dashboard, history, statistics and settings screens are not implemented in this snapshot. These thresholds are implementation settings, not validated accuracy claims.

## Main source files
All four files are in `app/src/main/java/com/example/driverdrowsinessdetection/`:
- `MainActivity.kt`: permission handling, camera preview and detection display.
- `CameraAnalyzer.kt`: camera frame preparation.
- `FaceLandmarkerHelper.kt`: MediaPipe setup and asynchronous detection.
- `DrowsinessAnalyzer.kt`: measurements, duration checks and detection state.

## Opening the project
Open this `android-app` folder in Android Studio and allow Gradle to sync. Install the SDK and toolchain requested by the project, then run on a compatible Android device with a front camera and grant camera permission.

The supplied build configuration specifies minimum SDK 26, compile/target SDK 37, Android Gradle Plugin 9.4.0 and CameraX 1.6.2. MediaPipe uses `latest.release`; pinning a tested version would improve repeatability. Build settings are preserved from the supplied project.

## Packaging notes
Generated build/cache folders, IDE configuration and `local.properties` were excluded. Source code, resources, the model and Gradle wrapper were retained. This archive was checked for file completeness; it was not built or tested on a device during packaging.
