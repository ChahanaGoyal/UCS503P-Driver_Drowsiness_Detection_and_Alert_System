package com.example.driverdrowsinessdetection

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt

data class DrowsinessResult(
    val ear: Float,
    val mar: Float,
    val status: String,
    val alarmRequired: Boolean
)

class DrowsinessAnalyzer {

    companion object {
        private const val EAR_THRESHOLD = 0.22f
        private const val MAR_THRESHOLD = 0.35f

        private const val EYE_CLOSED_DURATION = 1500L
        private const val YAWN_DURATION = 1000L
    }

    private var eyesClosedSince: Long? = null
    private var mouthOpenSince: Long? = null

    fun analyze(
        landmarks: List<NormalizedLandmark>,
        currentTime: Long
    ): DrowsinessResult {

        val leftEyeEar = calculateEyeRatio(
            landmarks = landmarks,
            outerCorner = 263,
            innerCorner = 362,
            upperPoint1 = 386,
            lowerPoint1 = 374,
            upperPoint2 = 385,
            lowerPoint2 = 380
        )

        val rightEyeEar = calculateEyeRatio(
            landmarks = landmarks,
            outerCorner = 33,
            innerCorner = 133,
            upperPoint1 = 159,
            lowerPoint1 = 145,
            upperPoint2 = 158,
            lowerPoint2 = 153
        )

        val ear = (leftEyeEar + rightEyeEar) / 2f
        val mar = calculateMouthRatio(landmarks)

        if (ear < EAR_THRESHOLD) {
            if (eyesClosedSince == null) {
                eyesClosedSince = currentTime
            }
        } else {
            eyesClosedSince = null
        }

        if (mar > MAR_THRESHOLD) {
            if (mouthOpenSince == null) {
                mouthOpenSince = currentTime
            }
        } else {
            mouthOpenSince = null
        }

        val eyesClosedTooLong =
            eyesClosedSince?.let {
                currentTime - it >= EYE_CLOSED_DURATION
            } ?: false

        val mouthOpenTooLong =
            mouthOpenSince?.let {
                currentTime - it >= YAWN_DURATION
            } ?: false

        val status: String
        val alarmRequired: Boolean

        when {
            eyesClosedTooLong -> {
                status = "DROWSY!"
                alarmRequired = true
            }

            mouthOpenTooLong -> {
                status = "YAWNING!"
                alarmRequired = true
            }

            ear < EAR_THRESHOLD -> {
                status = "Eyes closed"
                alarmRequired = false
            }

            mar > MAR_THRESHOLD -> {
                status = "Mouth open"
                alarmRequired = false
            }

            else -> {
                status = "Awake"
                alarmRequired = false
            }
        }

        return DrowsinessResult(
            ear = ear,
            mar = mar,
            status = status,
            alarmRequired = alarmRequired
        )
    }

    private fun calculateEyeRatio(
        landmarks: List<NormalizedLandmark>,
        outerCorner: Int,
        innerCorner: Int,
        upperPoint1: Int,
        lowerPoint1: Int,
        upperPoint2: Int,
        lowerPoint2: Int
    ): Float {

        val vertical1 = distance(
            landmarks[upperPoint1],
            landmarks[lowerPoint1]
        )

        val vertical2 = distance(
            landmarks[upperPoint2],
            landmarks[lowerPoint2]
        )

        val horizontal = distance(
            landmarks[outerCorner],
            landmarks[innerCorner]
        )

        if (horizontal == 0f) {
            return 0f
        }

        return (vertical1 + vertical2) / (2f * horizontal)
    }

    private fun calculateMouthRatio(
        landmarks: List<NormalizedLandmark>
    ): Float {

        val mouthOpening = distance(
            landmarks[13],
            landmarks[14]
        )

        val mouthWidth = distance(
            landmarks[61],
            landmarks[291]
        )

        if (mouthWidth == 0f) {
            return 0f
        }

        return mouthOpening / mouthWidth
    }

    private fun distance(
        first: NormalizedLandmark,
        second: NormalizedLandmark
    ): Float {

        val xDifference = first.x() - second.x()
        val yDifference = first.y() - second.y()

        return sqrt(
            xDifference * xDifference +
                    yDifference * yDifference
        )
    }

    fun reset() {
        eyesClosedSince = null
        mouthOpenSince = null
    }
}