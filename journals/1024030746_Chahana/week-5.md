# Week 5: Camera and Face Detection Integration

## Objective
The objective was to connect the device camera to the application and begin integrating facial landmark detection.

## Work Completed
- Added CameraX dependencies and camera permissions.
- Integrated the front-camera preview into the monitoring screen.
- Set up image analysis to process camera frames.
- Added the MediaPipe Face Landmarker dependency and model.
- Created a helper class to initialize MediaPipe and process camera frames.

## Challenges / Observations
During integration, a native-library loading error occurred when starting monitoring. This required additional debugging and compatibility checks.

## Learning
Learned how camera access, image processing and facial landmark detection can be connected in an Android application.

## Week 5 Outcome
The camera-processing pipeline and initial MediaPipe integration were set up, providing the foundation for real-time detection.
