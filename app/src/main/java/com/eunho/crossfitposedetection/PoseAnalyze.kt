package com.eunho.crossfitposedetection

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetector

// pose 분석
@ExperimentalGetImage
class PoseAnalyzer(
    private val detector: PoseDetector
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // 포즈 좌표 인식
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    val allKeyPoints = pose.allPoseLandmarks
                    for (landmark in allKeyPoints) {
                        val landmarkX = landmark.position.x
                        val landmarkY = landmark.position.y
                        Log.d("test", "Landmark X: $landmarkX, Landmark Y: $landmarkY")
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
}