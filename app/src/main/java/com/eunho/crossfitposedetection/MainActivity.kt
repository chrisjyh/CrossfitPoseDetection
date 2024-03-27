package com.eunho.crossfitposedetection

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.eunho.crossfitposedetection.databinding.ActivityMainBinding
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

        // 카메라 executor 세팅 안하면 메인 스레드 사용
        cameraExecutor = Executors.newSingleThreadExecutor()

        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
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

            // 프리뷰 세팅
            val preview = Preview
                .Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // 이미지 분석
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PoseAnalyzer(poseDetector, binding.tvSquatCount))
                }

            // 카메라 앞뒤 전환
            val cameraSelector = if(isBackCamera){
                Log.e(CAMERA_TAG,"camera back")

                CameraSelector.DEFAULT_BACK_CAMERA
            }else{
                Log.e(CAMERA_TAG,"camera front")
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                Log.e(CAMERA_TAG,"try camerax xxxxx")
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(CAMERA_TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))


    }

    private fun switchCamera() {
        isBackCamera = !isBackCamera
        startCamera()
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}