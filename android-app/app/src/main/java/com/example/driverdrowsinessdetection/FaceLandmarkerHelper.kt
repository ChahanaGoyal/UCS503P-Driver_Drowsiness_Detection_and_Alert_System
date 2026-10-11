package com.example.driverdrowsinessdetection

import android.content.Context
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkerHelper(
    private val context: Context,
    private val onResult: (FaceLandmarkerResult) -> Unit,
    private val onError: (String) -> Unit
) {

    private var faceLandmarker: FaceLandmarker? = null

    init {
        setupFaceLandmarker()
    }

    private fun setupFaceLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("face_landmarker.task")
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(0.5f)
                .setMinFacePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setResultListener { result, _ ->
                    onResult(result)
                }
                .setErrorListener { error ->
                    onError(error.message ?: "Unknown MediaPipe error")
                }
                .build()

            faceLandmarker =
                FaceLandmarker.createFromOptions(context, options)

        } catch (exception: Exception) {
            onError(
                exception.message ?: "Could not load the face landmark model"
            )
        }
    }

    fun detectAsync(image: MPImage, timestamp: Long) {
        faceLandmarker?.detectAsync(image, timestamp)
    }

    fun close() {
        faceLandmarker?.close()
        faceLandmarker = null
    }
}