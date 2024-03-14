package com.eunho.crossfitposedetection

import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import android.widget.VideoView
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

// pose 분석
@ExperimentalGetImage
class PoseAnalyzer(
    private val detector: PoseDetector,
    private val videoView: VideoView
) : ImageAnalysis.Analyzer {

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        val accuracy = 0.9f

        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // 포즈 좌표 인식
            detector.process(inputImage)
                .addOnSuccessListener { pose ->

                    val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                    val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                    val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)


                    if (leftHip != null && leftKnee != null && leftAnkle != null) {
                        Log.e("accuracy", """accuracy: ${leftHip.inFrameLikelihood} """)
                        var accuracyFrameLike = arrayOf(leftHip.inFrameLikelihood,leftKnee.inFrameLikelihood, leftAnkle.inFrameLikelihood).average()
                        if(accuracyFrameLike > accuracy){
                            Log.e("test",getSquatState(leftHip.position, leftKnee.position ,leftAnkle.position))
                        }


                    }
                }
                .addOnFailureListener { e ->
                    Log.e("test", "Pose detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
    private fun getAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        var result = Math.toDegrees(
            atan2(lastPoint.y - midPoint.y, lastPoint.x - midPoint.x) - atan2(firstPoint.y - midPoint.y,firstPoint.x - midPoint.x).toDouble()
        )
        result = abs(result) // Angle should never be negative
        if (result > 180) {
            result = 360.0 - result // Always get the acute representation of the angle
        }
        return result
    }

    private fun drawPosesOnVideo(poses: MutableList<PoseLandmark>) {
        val canvas = videoView.holder.lockCanvas()


        val scaleFactorX = canvas.width.toFloat() / videoView.width
        val scaleFactorY = canvas.height.toFloat() / videoView.height

        for (landmark in poses) {
            val landmarkX = scaleFactorX * landmark.position.x
            val landmarkY = scaleFactorY * landmark.position.y

            canvas.drawCircle(landmarkX, landmarkY, 8f, paint)
        }

        videoView.holder.unlockCanvasAndPost(canvas)
    }

    private fun getSquatState(hip: PointF, knee: PointF, ankle: PointF): String {
        val angleThreshold = 100

        var angle = getAngle(hip, knee, ankle)

        Log.e("angle", "angle: $angle")

        if ( angle < angleThreshold){
            return "down"
        }else{
            return "up"
        }
    }



}