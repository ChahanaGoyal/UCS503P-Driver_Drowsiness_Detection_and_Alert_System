
package com.example.driverdrowsinessdetection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Teal = Color(0xFF007F78)
private val Orange = Color(0xFFD16A21)
private val LightBackground = Color(0xFFF3F7F8)
private val MutedText = Color(0xFF657078)
private val DrowsyColor = Color(0xFFC62828)
private val AwakeColor = Color(0xFF16845B)

private fun stamp(time: Long): String {
    return SimpleDateFormat(
        "dd MMM, HH:mm:ss",
        Locale.getDefault()
    ).format(Date(time))
}

private fun duration(ms: Long): String {
    val seconds = ms / 1000
    return "%02d:%02d:%02d".format(
        seconds / 3600,
        seconds / 60 % 60,
        seconds % 60
    )
}

@Composable
fun DashboardScreen(
    page: String,
    running: Boolean,
    current: SessionData,
    selected: SessionData?,
    history: List<SessionData>,
    result: DrowsinessResult?,
    status: String,
    saveStatus: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPage: (String) -> Unit,
    onSelect: (SessionData) -> Unit,
    camera: @Composable (Modifier) -> Unit
) {
    var showTechnical by remember(page, selected?.started) {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 22.dp)
        ) {

            // HEADER
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "DRIVER MONITOR",
                        color = Teal,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        when (page) {
                            "Summary" -> "Session summary"
                            "History" -> "Past sessions"
                            else -> "Live dashboard"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        when (page) {
                            "Summary" -> "Here is how your monitoring session went."
                            "History" -> "Review your previous monitoring sessions."
                            else -> "Stay aware. Monitor your driving."
                        },
                        color = MutedText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // NAVIGATION
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onPage("Dashboard") },
                        enabled = page != "Dashboard" && !running
                    ) {
                        Text("Dashboard")
                    }

                    OutlinedButton(
                        onClick = { onPage("History") },
                        enabled = page != "History" && !running
                    ) {
                        Text("Past sessions")
                    }
                }
            }

            // LIVE DASHBOARD
            if (page == "Dashboard") {

                if (running) {
                    item {
                        camera(
                            Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Ready to monitor?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "Start monitoring to check for signs of drowsiness and yawning.",
                                    color = MutedText
                                )
                            }
                        }
                    }
                }

                item {
                    StatusCard(
                        running = running,
                        result = result,
                        status = status
                    )
                }

                item {
                    Button(
                        onClick = if (running) onStop else onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (running) {
                                DrowsyColor
                            } else {
                                Teal
                            }
                        )
                    ) {
                        Text(
                            if (running) {
                                "Stop monitoring & save session"
                            } else {
                                "Start monitoring"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Metric(
                        label = if (running) {
                            "Monitoring time"
                        } else {
                            "Last session time"
                        },
                        value = duration(current.durationMs),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Counts(current)
                }

                if (running) {

                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Current readings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Metric(
                                    "Eye movement reading",
                                    result?.ear?.let {
                                        "%.3f".format(it)
                                    } ?: "—",
                                    Modifier.weight(1f)
                                )

                                Metric(
                                    "Mouth movement reading",
                                    result?.mar?.let {
                                        "%.3f".format(it)
                                    } ?: "—",
                                    Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Recent alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (current.alerts.isEmpty()) {
                        item {
                            Text(
                                "No alerts recorded yet.",
                                color = MutedText
                            )
                        }
                    }

                    items(current.alerts.takeLast(5).reversed()) { event ->
                        EventRow(event)
                    }

                } else if (current.ended != 0L) {
                    item {
                        OutlinedButton(
                            onClick = { onSelect(current) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View last session summary")
                        }
                    }
                }
            }

            // SESSION SUMMARY
            else if (page == "Summary" && selected != null) {

                val session = selected

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "SESSION DETAILS",
                                color = Teal,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "${stamp(session.started)} → ${stamp(session.ended)}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                "Monitoring time: ${duration(session.durationMs)}",
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                "Camera off · These are recorded results",
                                color = Teal,
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (saveStatus.isNotEmpty()) {
                                Text(
                                    saveStatus,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Your session at a glance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    SummaryStatus(session)
                }

                item {
                    Counts(session)
                }

                item {
                    Metric(
                        "Longest observed eye closure",
                        "%.1f seconds".format(
                            session.longestClosureMs / 1000f
                        ),
                        Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        "Drowsiness activity timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ActivityTimeline(session)
                }

                item {
                    Text(
                        "Alert history (${session.alerts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (session.alerts.isEmpty()) {
                    item {
                        Text(
                            "No alerts were recorded in this session.",
                            color = MutedText
                        )
                    }
                }

                items(session.alerts.reversed()) { event ->
                    EventRow(event)
                }

                // TECHNICAL DETAILS
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showTechnical = !showTechnical
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                if (showTechnical) {
                                    "Hide technical details ▲"
                                } else {
                                    "View technical details ▼"
                                },
                                color = Teal,
                                fontWeight = FontWeight.Bold
                            )

                            if (showTechnical) {
                                Text(
                                    "EAR (Eye Aspect Ratio) measures eye openness. " +
                                            "MAR (Mouth Aspect Ratio) helps identify yawning.",
                                    color = MutedText
                                )

                                ReadingGraph(
                                    session.readings,
                                    "Recorded EAR / MAR readings"
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start new session")
                    }
                }
            }

            // PAST SESSIONS
            else if (page == "History") {

                if (history.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No saved sessions yet. Start monitoring and stop a session to see it here.",
                                modifier = Modifier.padding(20.dp),
                                color = MutedText
                            )
                        }
                    }
                }

                items(
                    history,
                    key = { it.started }
                ) { session ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                stamp(session.started),
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Duration: ${duration(session.durationMs)}",
                                color = MutedText
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "${session.drowsyCount} drowsiness events",
                                    color = DrowsyColor
                                )

                                Text(
                                    "${session.yawnCount} yawns",
                                    color = Orange
                                )
                            }

                            TextButton(
                                onClick = { onSelect(session) }
                            ) {
                                Text("View session summary")
                            }
                        }
                    }
                }
            }

            // FOOTER
            item {
                Text(
                    "Experimental threshold-based detection. " +
                            "EAR < 0.22 for 1.5 seconds; MAR > 0.35 for 1 second. " +
                            "This app provides alerts based on detected facial patterns " +
                            "and should not replace safe driving practices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    running: Boolean,
    result: DrowsinessResult?,
    status: String
) {
    val isDrowsy = result?.drowsy == true ||
            status.contains("drowsy", ignoreCase = true)

    val isYawning = result?.yawning == true ||
            status.contains("yawning", ignoreCase = true)

    val noFace = status.contains("no face", ignoreCase = true) ||
            status.contains("looking for face", ignoreCase = true)

    val heading = when {
        !running -> "READY TO MONITOR"
        noFace -> "FACE NOT DETECTED"
        isDrowsy -> "DROWSINESS DETECTED"
        isYawning -> "YAWNING DETECTED"
        else -> "AWAKE"
    }

    val explanation = when {
        !running -> "Start a session when you are ready."
        noFace -> "Please position your face clearly in front of the camera."
        isDrowsy -> "Signs of prolonged eye closure have been detected. Please stay alert."
        isYawning -> "A possible yawn has been detected."
        else -> "No drowsiness or yawning is currently detected."
    }

    val color = when {
        !running -> Teal
        noFace -> Orange
        isDrowsy -> DrowsyColor
        isYawning -> Orange
        else -> AwakeColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "CURRENT DRIVER STATUS",
                color = MutedText,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                heading,
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                explanation,
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SummaryStatus(session: SessionData) {
    val label = if (session.drowsyCount > 0) {
        "Drowsiness events were detected"
    } else {
        "No drowsiness events were recorded"
    }

    val color = if (session.drowsyCount > 0) {
        DrowsyColor
    } else {
        AwakeColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "This summary is based on the events recorded during this session.",
                color = MutedText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Metric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MutedText
            )

            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = Teal,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun Counts(session: SessionData) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Metric(
            "Drowsiness events",
            session.drowsyCount.toString(),
            Modifier.weight(1f)
        )

        Metric(
            "Yawning events",
            session.yawnCount.toString(),
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun EventRow(event: AlertEvent) {
    val isDrowsy = event.label.contains(
        "drowsiness",
        ignoreCase = true
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "●",
                color = if (isDrowsy) DrowsyColor else Orange
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    event.label,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    stamp(event.time),
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ActivityTimeline(session: SessionData) {
    val drowsyEvents = session.alerts.filter {
        it.label.contains("drowsiness", ignoreCase = true)
    }

    val yawnEvents = session.alerts.filter {
        it.label.contains("yawning", ignoreCase = true)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("● Drowsiness", color = DrowsyColor)
                Text("● Yawning", color = Orange)
            }

            if (session.durationMs <= 0L) {
                Text(
                    "Timeline unavailable for this session.",
                    color = MutedText
                )
            } else if (session.alerts.isEmpty()) {
                Text(
                    "No drowsiness or yawning events were recorded.",
                    color = MutedText
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                ) {
                    val left = 12.dp.toPx()
                    val right = size.width - 12.dp.toPx()
                    val width = right - left

                    val firstTrack = 25.dp.toPx()
                    val secondTrack = 60.dp.toPx()

                    drawLine(
                        Color.LightGray,
                        Offset(left, firstTrack),
                        Offset(right, firstTrack),
                        strokeWidth = 4.dp.toPx()
                    )

                    drawLine(
                        Color.LightGray,
                        Offset(left, secondTrack),
                        Offset(right, secondTrack),
                        strokeWidth = 4.dp.toPx()
                    )

                    fun eventX(time: Long): Float {
                        val elapsed = (time - session.started)
                            .coerceIn(0L, session.durationMs)

                        return left +
                                (elapsed.toFloat() / session.durationMs) * width
                    }

                    drowsyEvents.forEach { event ->
                        drawCircle(
                            color = DrowsyColor,
                            radius = 7.dp.toPx(),
                            center = Offset(
                                eventX(event.time),
                                firstTrack
                            )
                        )
                    }

                    yawnEvents.forEach { event ->
                        drawCircle(
                            color = Orange,
                            radius = 7.dp.toPx(),
                            center = Offset(
                                eventX(event.time),
                                secondTrack
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Session start",
                        color = MutedText,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        "Session end",
                        color = MutedText,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    "Each dot represents a recorded event. Its position shows when it occurred.",
                    color = MutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ReadingGraph(
    readings: List<Reading>,
    title: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("EAR", color = Teal)
                Text("MAR", color = Orange)
            }

            if (readings.none { it.ear != null || it.mar != null }) {
                Text("No valid face readings were recorded.")
            } else {
                val maxY = maxOf(
                    0.5f,
                    readings.maxOf {
                        maxOf(it.ear ?: 0f, it.mar ?: 0f)
                    }
                )

                Text(
                    "Ratio: 0–${"%.2f".format(maxY)}",
                    style = MaterialTheme.typography.labelSmall
                )

                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFFAFAFA))
                ) {
                    for (i in 0..4) {
                        val y = size.height * i / 4

                        drawLine(
                            Color.LightGray,
                            Offset(0f, y),
                            Offset(size.width, y),
                            1f
                        )
                    }

                    val start = readings.first().seconds
                    val range = maxOf(
                        1f,
                        readings.last().seconds - start
                    )

                    fun point(
                        reading: Reading,
                        value: Float
                    ): Offset {
                        return Offset(
                            (reading.seconds - start) / range * size.width,
                            size.height * (1 - value / maxY)
                        )
                    }

                    for (i in 1 until readings.size) {
                        val a = readings[i - 1]
                        val b = readings[i]

                        if (b.seconds - a.seconds <= 2f) {
                            if (a.ear != null && b.ear != null) {
                                drawLine(
                                    Teal,
                                    point(a, a.ear),
                                    point(b, b.ear),
                                    3f
                                )
                            }

                            if (a.mar != null && b.mar != null) {
                                drawLine(
                                    Orange,
                                    point(a, a.mar),
                                    point(b, b.mar),
                                    3f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
