# Driver Drowsiness Detection And Alert System

## About the Project

Driver Drowsiness Detection is a mobile application being developed to identify signs of driver fatigue by monitoring eye closure and mouth movements using computer vision.
The application uses the device's front camera to capture facial images and aims to detect prolonged eye closure and possible yawning. The goal is to provide timely alerts that may help drivers recognize signs of fatigue.
This project is being developed as part of the UCS503 Software Engineering course at Thapar Institute of Engineering and Technology.

## Objectives

- Detect prolonged eye closure using the Eye Aspect Ratio (EAR).
- Identify possible yawning using the Mouth Aspect Ratio (MAR).
- Provide alerts when the defined detection conditions are met.
- Display facial measurements and monitoring status in a mobile interface.
- Develop a simple and user-friendly Android application.

## Technologies Used

- **Kotlin** — Android application logic.
- **Android Studio** — Development environment.
- **Jetpack Compose** — User interface development.
- **CameraX** — Camera preview and image analysis.
- **MediaPipe Face Landmarker** — Facial landmark detection.
- **Gradle** — Project build and dependency management.
- **Git and GitHub** — Version control and project collaboration.

## Detection Approach

The application uses facial landmarks to calculate two measurements:

### 1. Eye Aspect Ratio (EAR)

EAR measures eye openness using distances between selected eye landmarks. A lower value can indicate that the eyes are closing.

- EAR threshold: `0.3`
- Consecutive-frame threshold: `50`

### 2. Mouth Aspect Ratio (MAR)

MAR measures mouth opening using distances between selected mouth landmarks. A higher value may indicate yawning.

- MAR threshold: `0.6`
- Consecutive-frame threshold: `15`

The system is designed to use consecutive frames rather than relying on a single measurement. These thresholds are initial project settings and may require further testing and adjustment.

## Getting Started

### Prerequisites

- Android Studio.
- A compatible Android SDK.
- Kotlin and Gradle support.
- An Android emulator or a compatible physical Android device.

### Setup Instructions

1. Clone or download this repository.
2. Open the Android project.
3. Allow Android Studio to sync the Gradle dependencies.
4. Ensure that the 'face_landmarker.task' model is present in the application's assets directory.
5. Run the application on a compatible emulator or Android device.
6. Grant camera permission when prompted.

## Limitations

- Detection accuracy depends on camera quality, lighting conditions and facial landmark detection.
- The current thresholds require further validation under different conditions.
- The application is a student project and is not a substitute for adequate rest or responsible driving practices.

## Future Work

- Improve eye-closure and yawning detection reliability.
- Test the application under different lighting conditions and camera positions.
## Disclaimer

This application is developed for educational and experimental purposes. Its detection results may be inaccurate, and it should not be relied upon as the sole method of determining driver alertness.
