package com.example.driverdrowsinessdetection

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

 data class Reading(val seconds: Float, val ear: Float?, val mar: Float?)
 data class AlertEvent(val time: Long, val label: String)
 data class SessionData(
    val started: Long = System.currentTimeMillis(),
    val ended: Long = 0,
    val durationMs: Long = 0,
    val drowsyCount: Int = 0,
    val yawnCount: Int = 0,
    val longestClosureMs: Long = 0,
    val readings: List<Reading> = emptyList(),
    val alerts: List<AlertEvent> = emptyList()
 )

/** One atomic JSON file per completed session, private to this app. */
class SessionStore(context: Context) {
    private val directory = File(context.filesDir, "sessions").apply { mkdirs() }
    fun save(s: SessionData) {
        val data = JSONObject().put("started", s.started).put("ended", s.ended)
            .put("duration", s.durationMs).put("drowsy", s.drowsyCount)
            .put("yawns", s.yawnCount).put("longest", s.longestClosureMs)
        data.put("readings", JSONArray().apply { s.readings.forEach {
            put(JSONObject().put("t", it.seconds).put("ear", it.ear ?: JSONObject.NULL)
                .put("mar", it.mar ?: JSONObject.NULL))
        } })
        data.put("alerts", JSONArray().apply { s.alerts.forEach {
            put(JSONObject().put("time", it.time).put("label", it.label))
        } })
        val file = AtomicFile(File(directory, "${s.started}.json"))
        val stream = file.startWrite()
        try {
            stream.write(data.toString().toByteArray(Charsets.UTF_8))
            file.finishWrite(stream)
        } catch (e: Exception) { file.failWrite(stream); throw e }
    }
    fun load(): List<SessionData> = directory.listFiles().orEmpty()
        .filter { it.extension == "json" }.mapNotNull { file ->
            runCatching {
                val j = JSONObject(AtomicFile(file).openRead().bufferedReader().use { it.readText() })
                val r = j.getJSONArray("readings")
                val a = j.getJSONArray("alerts")
                SessionData(j.getLong("started"), j.getLong("ended"), j.getLong("duration"),
                    j.getInt("drowsy"), j.getInt("yawns"), j.getLong("longest"),
                    List(r.length()) { i -> r.getJSONObject(i).let {
                        Reading(it.getDouble("t").toFloat(),
                            if (it.isNull("ear")) null else it.getDouble("ear").toFloat(),
                            if (it.isNull("mar")) null else it.getDouble("mar").toFloat())
                    } }, List(a.length()) { i -> a.getJSONObject(i).let {
                        AlertEvent(it.getLong("time"), it.getString("label"))
                    } })
            }.getOrNull()
        }.sortedByDescending { it.started }
}
