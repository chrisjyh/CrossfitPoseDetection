//package com.eunho.crossfitposedetection
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import com.google.mlkit.common.model.LocalModel
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.pose.PoseDetection
//import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
//
//class PoseDetect (){
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        val localModel = LocalModel.Builder()
//            .setAssetFilePath("pose_detector_model.tflite")
//            .build()
//
//        val poseDetectorOptions = PoseDetectorOptions.Builder()
//            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
//            .build()
//
//        val poseDetector = PoseDetection.getClient(poseDetectorOptions)
//
//        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.yoga)
//        val image = InputImage.fromBitmap(bitmap, 0)
//
//        poseDetector.process(image)
//            .addOnSuccessListener { pose ->
//                val allKeypoints = pose.allPoseLandmarks
//
//                for (landmark in allKeypoints) {
//                    val landmarkX = landmark.position.x
//                    val landmarkY = landmark.position.y
//                    Log.e("test","x 좌표: $landmarkX y좌표: $landmarkY")
//                }
//                Toast.makeText(this, "Pose Detected!", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                // Error occurred while detecting pose.
//                Toast.makeText(this, "Failed to detect pose: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//}