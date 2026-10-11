# Week 3: Detection Logic and Thresholds

## Objective
This week aimed to improve the EAR and MAR logic and get it ready for later integration with the application.

## Work Completed
- Refined the conditions used to detect eye closure and yawning.
- Selected 0.3 as the EAR threshold and 0.6 as the MAR threshold.
- Added consecutive-frame counters so that a single frame would not trigger detection.
- Set the eye-closure counter to 50 frames and the mouth-opening counter to 15 frames.
- Considered how the detection output could be linked to an alert system later.

## Challenges / Observations
The detection logic needed to use consecutive frames instead of depending on one measurement.

## Learning
Understood how thresholds and consecutive-frame counters help detect sustained eye closure and mouth opening.

## Week 3 Outcome
The basic detection logic was ready for implementation in the Android application.
