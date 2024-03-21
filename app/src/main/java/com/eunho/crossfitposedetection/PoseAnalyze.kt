package com.eunho.crossfitposedetection

import android.graphics.PointF
import android.util.Log
import android.widget.VideoView
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

val TAG = "test"
@ExperimentalGetImage
class PoseAnalyzer(
    private val detector: PoseDetector,
    private val videoView: VideoView
) : ImageAnalysis.Analyzer {

    private var previousState = PoseState.STANDING
    private var squatCount = 0
    private var lastSquatTime = 0L

    // 포즈 상태
    enum class PoseState {
        STANDING,
        SQUATTING
    }

    override fun analyze(imageProxy: ImageProxy) {
        // 포즈 정확도
        val accuracyThreshold = 0.9f
        // 이미지 처리 딜레이
        val coolDownMillis = 500L
        // 스쿼트 각도
        val isSquatAngle = 90
        val mediaImage = imageProxy.image

        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                    // 왼쪽 다리 기준
                    if (leftHip != null && leftKnee != null && leftAnkle != null &&
                        leftHip.inFrameLikelihood > accuracyThreshold &&
                        leftKnee.inFrameLikelihood > accuracyThreshold &&
                        leftAnkle.inFrameLikelihood > accuracyThreshold) {

                        // 왼쪽 다리 각도 계산
                        val angle = calculateAngle(leftHip.position, leftKnee.position, leftAnkle.position)

                        // 현재 상태
                        val currentState = if (angle < isSquatAngle) PoseState.SQUATTING else PoseState.STANDING

                        // 스쿼트 체크 후 카운트
                        if (previousState == PoseState.STANDING && currentState == PoseState.SQUATTING &&
                            System.currentTimeMillis() - lastSquatTime > coolDownMillis) {
                            squatCount++
                            lastSquatTime = System.currentTimeMillis()
                            Log.d(TAG, "Count: $squatCount")
                        }

                        // 스쿼트 상태 변경
                        previousState = currentState
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Pose detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    /**
     * 각도 구하기
     * */
    private fun calculateAngle(point1: PointF, point2: PointF, point3: PointF): Double {
        val a = distance(point2.x, point2.y, point3.x, point3.y)
        val b = distance(point1.x, point1.y, point3.x, point3.y)
        val c = distance(point1.x, point1.y, point2.x, point2.y)
        return Math.toDegrees(acos((b * b + c * c - a * a) / (2 * b * c)))
    }

    /**
     * 지점 사이의 거리
     * */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1).toDouble())
    }
}
