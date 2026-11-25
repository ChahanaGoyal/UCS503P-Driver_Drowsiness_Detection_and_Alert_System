package com.example.driverdrowsinessdetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.driverdrowsinessdetection.ui.theme.DriverDrowsinessDetectionTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val storageExecutor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var store: SessionStore
    private var helper: FaceLandmarkerHelper? = null
    private var analyzer = DrowsinessAnalyzer()
    private var generation = 0
    private var startedElapsed = 0L
    private var lastSample = -1000L
    private var wasDrowsy = false
    private var wasYawning = false
    private var running by mutableStateOf(false)
    private var current by mutableStateOf(SessionData())
    private var selected by mutableStateOf<SessionData?>(null)
    private var history by mutableStateOf<List<SessionData>>(emptyList())
    private var result by mutableStateOf<DrowsinessResult?>(null)
    private var status by mutableStateOf("Ready to monitor")
    private var saveStatus by mutableStateOf("")
    private var page by mutableStateOf("Dashboard")
    private val alertManager = AlertManager()
    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) startSession() else { status = "Camera permission denied. Tap Start to try again." }
    }
    private val ticker = object : Runnable {
        override fun run() {
            if (running) {
                current = current.copy(durationMs = SystemClock.elapsedRealtime() - startedElapsed)
                handler.postDelayed(this, 250)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = SessionStore(applicationContext)
        storageExecutor.execute { val loaded = store.load(); handler.post { history = loaded } }
        setContent {
            DriverDrowsinessDetectionTheme {
                BackHandler(enabled = running || page != "Dashboard") {
                    if (running) stopSession() else page = "Dashboard"
                }
                DashboardScreen(page, running, current, selected, history, result, status, saveStatus,
                    onStart = {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                            startSession() else requestCamera.launch(Manifest.permission.CAMERA)
                    }, onStop = { stopSession() },
                    onPage = { page = it }, onSelect = { selected = it; page = "Summary" },
                    camera = { modifier -> helper?.let { LiveCamera(this, cameraExecutor, it, modifier) { message ->
                        status = "Camera error: $message"
                        stopSession()
                    } } })
            }
        }
    }
    private fun startSession() {
        if (running) return
        generation++
        val token = generation
        analyzer = DrowsinessAnalyzer()
        current = SessionData()
        startedElapsed = SystemClock.elapsedRealtime()
        lastSample = -1000
        wasDrowsy = false; wasYawning = false
        result = null; status = "Looking for face…"; saveStatus = ""
        page = "Dashboard"; running = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        helper = FaceLandmarkerHelper(applicationContext, onResult = { face ->
            handler.post {
                if (running && generation == token) {
                    val elapsed = SystemClock.elapsedRealtime() - startedElapsed
                    val landmarks = face.faceLandmarks().firstOrNull()
                    if (landmarks == null) {
                        analyzer.reset(); result = null; status = "No face detected"
                        wasDrowsy = false; wasYawning = false
                    } else {
                        val value = analyzer.analyze(landmarks, SystemClock.elapsedRealtime())
                        result = value; status = value.status
                        val newDrowsy = value.drowsy && !wasDrowsy
                        val newYawn = value.yawning && !wasYawning
                        if (newDrowsy) {
                            alertManager.playDrowsinessAlert()
                        }

                        if (newYawn) {
                            alertManager.playYawningAlert()
                        }
                        val events = mutableListOf<AlertEvent>()
                        if (newDrowsy) events.add(AlertEvent(System.currentTimeMillis(), "Drowsiness detected"))
                        if (newYawn) events.add(AlertEvent(System.currentTimeMillis(), "Yawning detected"))
                        current = current.copy(
                            drowsyCount = current.drowsyCount + if (newDrowsy) 1 else 0,
                            yawnCount = current.yawnCount + if (newYawn) 1 else 0,
                            longestClosureMs = maxOf(current.longestClosureMs, value.eyeClosedMs),
                            alerts = (current.alerts + events).takeLast(1000))
                        wasDrowsy = value.drowsy; wasYawning = value.yawning
                    }
                    if (elapsed - lastSample >= 500) {
                        current = current.copy(readings = (current.readings +
                            Reading(elapsed / 1000f, result?.ear, result?.mar)).takeLast(7200))
                        lastSample = elapsed
                    }
                }
            }
        }, onError = { message -> handler.post {
            if (running && generation == token) { status = "Detection error: $message"; stopSession() }
        } })
        handler.post(ticker)
    }
    private fun stopSession() {
        if (!running) return
        running = false; generation++
        handler.removeCallbacks(ticker)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val finished = current.copy(ended = System.currentTimeMillis(),
            durationMs = SystemClock.elapsedRealtime() - startedElapsed)
        current = finished; selected = finished; page = "Summary"
        val oldHelper = helper; helper = null
        // Queue close after any analyzer work already submitted.
        cameraExecutor.execute { oldHelper?.close() }
        saveStatus = "Saving session…"
        storageExecutor.execute {
            try {
                store.save(finished)
                val loaded = store.load()
                handler.post { history = loaded; saveStatus = "Session saved on this phone" }
            } catch (e: Exception) {
                handler.post { saveStatus = "Could not save: ${e.message}. Summary remains visible until closing." }
            }
        }
    }
    override fun onPause() { stopSession(); super.onPause() }
    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        alertManager.release()
        cameraExecutor.shutdown()
        storageExecutor.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun LiveCamera(owner: ComponentActivity, executor: java.util.concurrent.ExecutorService,
                       helper: FaceLandmarkerHelper, modifier: Modifier, onError: (String) -> Unit) {
    val view = remember {
        PreviewView(owner).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val error by rememberUpdatedState(onError)
    DisposableEffect(helper) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        val preview = Preview.Builder().build().also { it.surfaceProvider = view.surfaceProvider }
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        analysis.setAnalyzer(executor, CameraAnalyzer(helper))
        val future = ProcessCameraProvider.getInstance(owner)
        future.addListener({
            if (!disposed) {
                try {
                    provider = future.get()
                    provider?.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
                } catch (e: Exception) { error(e.message ?: "Cannot start camera") }
            }
        }, ContextCompat.getMainExecutor(owner))
        onDispose { disposed = true; analysis.clearAnalyzer(); provider?.unbind(preview, analysis) }
    }
    AndroidView(factory = { view }, modifier = modifier)
}
