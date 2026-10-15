# Week 7: Alerts, History and Statistics

## Objective
The objective was to add alert functionality to the application and begin storing and displaying monitoring data through the History and Statistics screens.

## Work Completed
- Connected the drowsiness and yawning detection results to an alert system.
- Added a sound alert and a visual warning message on the monitoring screen when sustained eye closure is detected.
- Added a separate, milder warning for repeated yawning detected using the MAR threshold.
- Created a local database to store monitoring events such as the time, type of alert and session duration.
- Developed the History screen to list previous monitoring sessions and the alerts recorded in them.
- Started the Statistics screen to show a summary of the total number of alerts and monitoring time.

## Challenges / Observations
Alerts were sometimes triggered repeatedly while the eyes stayed closed, which made the sound irritating. A short delay between consecutive alerts was needed. Saving data without slowing down the camera processing also required care.

## Learning
Learned how to trigger alerts from detection results, store data locally in an Android application and display saved data on separate screens. Also learned that alert behaviour should be designed carefully so that it helps the driver without becoming distracting.

## Week 7 Outcome
The application can now warn the user when drowsiness or yawning is detected, and monitoring events are saved and shown in the History screen. The Statistics screen is partly working and needs to be completed.
