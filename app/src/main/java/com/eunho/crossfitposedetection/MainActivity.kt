package com.eunho.crossfitposedetection

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.eunho.crossfitposedetection.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityMainBinding
    private lateinit var poseDetector: PoseDetector
    private var isBackCamera = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // Real-time detection
            .build()

        poseDetector = PoseDetection.getClient(options)

        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }

        startCamera()
    }

    @OptIn(ExperimentalGetImage::class) private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer(poseDetector))
                }

            val cameraSelector = if(isBackCamera){
                CameraSelector.DEFAULT_BACK_CAMERA
            }else{
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchCamera() {
        isBackCamera = !isBackCamera
        startCamera()
    }

    @ExperimentalGetImage private inner class PoseAnalyzer(private val detector: PoseDetector) : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                detector.process(inputImage)
                    .addOnSuccessListener { pose ->
                        val allKeypoints = pose.allPoseLandmarks
                        for (landmark in allKeypoints) {
                            val landmarkX = landmark.position.x
                            val landmarkY = landmark.position.y
                            Log.d(TAG, "Landmark X: $landmarkX, Landmark Y: $landmarkY")
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
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraX"
    }
}