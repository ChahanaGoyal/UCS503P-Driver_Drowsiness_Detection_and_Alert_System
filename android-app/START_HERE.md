# Driver Monitor dashboard update

## Open on your Windows computer
1. Extract this ZIP into a NEW folder; keep your original project as a backup.
2. Android Studio > File > Open > select DriverDrowsinessDetection (the folder with settings.gradle.kts).
3. Let Gradle Sync finish. The original SDK / Gradle dependency settings were preserved.
4. Connect your Android phone, enable USB debugging, choose the phone and press Run.
5. Allow camera permission. Tap Start monitoring. Tap Stop monitoring & save summary.
6. Open Past sessions to view saved summaries after restarting the app.

You can instead copy MainActivity.kt, DrowsinessAnalyzer.kt, DashboardScreen.kt and SessionData.kt into the existing app/src/main/java/com/example/driverdrowsinessdetection/ folder. Replace the first two; add the latter two. No new dependencies are required.

## Included
- Smaller live preview, EAR/MAR cards, status, eye-closure duration, session timer.
- Independent yawn/drowsiness counters, counted once per continuous event.
- Live last-60-second graphs; recorded graphs and timestamped alert list.
- Stop saves a session summary; Past sessions survives app restarts.
- Completed sessions stored in atomic JSON files in app-private storage. No internet or server needed; no camera video is recorded.
- Leaving the app, turning the screen off or rotating ends/saves the session. Start a new one after returning.
- Missing face resets continuity and produces graph gaps, not zero measurements.
- Graph storage: latest 7,200 samples at up to 2 Hz (about 60 min); latest 1,000 alert entries. Total event counts and duration cover the whole session.

## Limitations and validation
This environment does not have an Android SDK, Gradle or emulator. This project has been source-reviewed but not compiled or tested on a phone here. Run the checks below on your device. Original model, thresholds, CameraAnalyzer and dependency versions retained. This update provides visual alerts, not an audible alarm. Results are heuristic detections, not a measured medical/safety score.

## Phone checks
- Start: permission, preview, timer and EAR/MAR update.
- Close eyes for >1.5 seconds: count increases ONCE; keep closed and it should not keep counting. Reopen and repeat for a second event.
- Open mouth >1 second: one yawn event. Counts track eyes and mouth independently.
- Move face away: no-face status, blank EAR/MAR, gap in graph.
- Stop: camera indicator turns off, duration stops, summary remains visible.
- Reopen app: Past sessions contains saved summary and graph.
- Start another session: fresh counters and timer; previous history retained.
- Background/rotate: session ends and saves; camera stops.
- Deny permission: readable message; no active monitoring.
- Saving failures are shown in the summary. Uninstalling/clearing app data removes saved history. Abrupt process kill before Stop may lose the active session.
