# Week 3: Detection Logic and Thresholds

## Objective
The objective was to refine the EAR and MAR logic and prepare it for future integration into the application.

## Work Completed
- Worked on the eye-closure and yawning detection conditions.
- Set the EAR threshold to 0.3 and the MAR threshold to 0.6.
- Used consecutive-frame counters to avoid triggering detection based on a single frame.
- Set the eye-closure counter threshold to 50 frames and the mouth-opening counter threshold to 15 frames.
- Reviewed how the detection results could later be connected to an alert system.

## Challenges / Observations
The logic needed to consider consecutive frames rather than relying on a single measurement.

## Learning
Learned how threshold-based detection and consecutive-frame counting can help identify sustained eye closure and mouth opening.

## Week 3 Outcome
The basic detection logic was prepared for implementation in the Android application.
