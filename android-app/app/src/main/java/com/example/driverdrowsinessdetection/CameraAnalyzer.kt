package com.example.driverdrowsinessdetection

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder

class CameraAnalyzer(
    private val faceLandmarkerHelper: FaceLandmarkerHelper
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        try {
            val originalBitmap = imageProxy.toBitmap()

            val rotationDegrees =
                imageProxy.imageInfo.rotationDegrees.toFloat()

            val transformationMatrix = Matrix().apply {
                postRotate(rotationDegrees)

                // Mirror the front-camera image
                postScale(-1f, 1f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                transformationMatrix,
                true
            )

            val mediaPipeImage =
                BitmapImageBuilder(rotatedBitmap).build()

            faceLandmarkerHelper.detectAsync(
                mediaPipeImage,
                SystemClock.uptimeMillis()
            )

        } catch (exception: Exception) {
            exception.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }
}