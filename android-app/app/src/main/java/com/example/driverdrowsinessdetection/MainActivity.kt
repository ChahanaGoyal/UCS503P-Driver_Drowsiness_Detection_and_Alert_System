package com.example.driverdrowsinessdetection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.driverdrowsinessdetection.ui.theme.DriverDrowsinessDetectionTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            DriverDrowsinessDetectionTheme {
                CameraPermissionScreen(
                    lifecycleOwner = this@MainActivity,
                    cameraExecutor = cameraExecutor
                )
            }
        }
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }
}

@Composable
fun CameraPermissionScreen(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->
            hasCameraPermission = permissionGranted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        FaceDetectionScreen(
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required")
        }
    }
}
@Composable
fun FaceDetectionScreen(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current

    val mainThreadHandler = remember {
        Handler(Looper.getMainLooper())
    }

    var detectionStatus by remember {
        mutableStateOf("Looking for face...")
    }

    val drowsinessAnalyzer = remember {
        DrowsinessAnalyzer()
    }

    val faceLandmarkerHelper = remember {
        FaceLandmarkerHelper(
            context = context.applicationContext,

            onResult = { result ->
                if (result.faceLandmarks().isNotEmpty()) {

                    val landmarks = result.faceLandmarks()[0]

                    val drowsinessResult =
                        drowsinessAnalyzer.analyze(
                            landmarks = landmarks,
                            currentTime = System.currentTimeMillis()
                        )

                    mainThreadHandler.post {
                        detectionStatus =
                            "EAR: %.2f\nMAR: %.2f\nStatus: %s".format(
                                drowsinessResult.ear,
                                drowsinessResult.mar,
                                drowsinessResult.status
                            )
                    }

                } else {
                    drowsinessAnalyzer.reset()

                    mainThreadHandler.post {
                        detectionStatus = "No face detected"
                    }
                }
            },

            onError = { message ->
                mainThreadHandler.post {
                    detectionStatus = "Error: $message"
                }
            }
        )
    }

    DisposableEffect(faceLandmarkerHelper) {
        onDispose {
            faceLandmarkerHelper.close()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(
            lifecycleOwner = lifecycleOwner,
            cameraExecutor = cameraExecutor,
            faceLandmarkerHelper = faceLandmarkerHelper
        )

        Text(
            text = detectionStatus,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(
                    horizontal = 20.dp,
                    vertical = 10.dp
                )
        )
    }
}

@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    faceLandmarkerHelper: FaceLandmarkerHelper
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),

        factory = { context ->

            val previewView = PreviewView(context)

            val cameraProviderFuture =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor,
                    CameraAnalyzer(faceLandmarkerHelper)
                )

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )

            }, ContextCompat.getMainExecutor(context))

            previewView
        }
    )
}