package com.example.androiddevelopmenttask.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.example.androiddevelopmenttask.data.db.dao.UserDao
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.FaceDetectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.abs

class FaceDetectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao
) : FaceDetectionRepository {

    private val TAG = "FaceDetectionRepo"

    override suspend fun detectFace(bitmap: Bitmap, referenceRect: Rect): Result<Boolean> {
        return try {
            // Validate input bitmap
            if (bitmap.width <= 0 || bitmap.height <= 0 || bitmap.isRecycled) {
                Log.e(TAG, "Invalid bitmap provided: width=${bitmap.width}, height=${bitmap.height}, recycled=${bitmap.isRecycled}")
                return Result.Error("Invalid image provided. Please try again.")
            }

            Log.d(TAG, "Starting simple face detection on bitmap: ${bitmap.width}x${bitmap.height}")

            // Use a simple algorithm to detect if there's a face-like object in the image
            // This is a very basic approach and won't be as accurate as ML-based detection
            val isFaceDetected = detectFaceSimple(bitmap, referenceRect)

            Log.d(TAG, "Simple face detection result: $isFaceDetected")

            Result.Success(isFaceDetected)
        } catch (e: Exception) {
            Log.e(TAG, "Face detection error: ${e.message}")
            e.printStackTrace()
            // Return success with false instead of error to prevent app crashes
            Result.Success(false)
        }
    }

    private fun detectFaceSimple(bitmap: Bitmap, referenceRect: Rect): Boolean {
        try {
            // Convert the reference rectangle to image coordinates
            val scaledReferenceRect = convertReferenceRectToImageCoordinates(referenceRect, bitmap.width, bitmap.height)

            // Get the center of the reference rectangle
            val centerX = scaledReferenceRect.centerX().toInt()
            val centerY = scaledReferenceRect.centerY().toInt()

            // Define a smaller region to analyze (center of the reference rectangle)
            val regionSize = minOf(scaledReferenceRect.width(), scaledReferenceRect.height()) / 2
            val left = maxOf(0, centerX - regionSize.toInt() / 2)
            val top = maxOf(0, centerY - regionSize.toInt() / 2)
            val right = minOf(bitmap.width, left + regionSize.toInt())
            val bottom = minOf(bitmap.height, top + regionSize.toInt())

            // Count skin-colored pixels in the region
            var skinPixelCount = 0
            var totalPixels = 0

            for (x in left until right) {
                for (y in top until bottom) {
                    totalPixels++
                    val pixel = bitmap.getPixel(x, y)
                    if (isSkinColor(pixel)) {
                        skinPixelCount++
                    }
                }
            }

            // Calculate the ratio of skin-colored pixels
            val skinRatio = skinPixelCount.toFloat() / totalPixels
            Log.d(TAG, "Skin pixel ratio: $skinRatio (${skinPixelCount}/${totalPixels})")

            // If more than 30% of pixels are skin-colored, consider it a face
            return skinRatio > 0.3f
        } catch (e: Exception) {
            Log.e(TAG, "Error in simple face detection: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    private fun isSkinColor(pixel: Int): Boolean {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)

        // Simple skin color detection based on RGB values
        // This is a very basic approach and won't work for all skin tones
        return red > 95 && green > 40 && blue > 20 &&
                red > green && red > blue &&
                abs(red - green) > 15 &&
                red > 150 && green > 100 && blue > 80
    }

    private fun convertReferenceRectToImageCoordinates(referenceRect: Rect, imageWidth: Int, imageHeight: Int): RectF {
        // The reference rectangle is defined in the UI coordinate space (e.g., 200dp x 200dp centered)
        // We need to convert it to the image coordinate space

        // Calculate the scale factors
        val scaleX = imageWidth.toFloat() / referenceRect.width()
        val scaleY = imageHeight.toFloat() / referenceRect.height()

        // Create a scaled rectangle
        return RectF(
            referenceRect.left * scaleX,
            referenceRect.top * scaleY,
            referenceRect.right * scaleX,
            referenceRect.bottom * scaleY
        )
    }

    override suspend fun saveFaceData(userId: Int, faceData: ByteArray): Result<Unit> {
        return try {
            userDao.updateFaceData(userId, faceData)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save face data")
        }
    }

    override suspend fun verifyFace(userId: Int, faceData: ByteArray): Result<Boolean> {
        return try {
            val user = userDao.getUserById(userId)
            if (user?.faceData != null) {
                // In a real app, you would use a proper face recognition algorithm here
                // For this demo, we'll just check if face data exists
                Result.Success(true)
            } else {
                Result.Error("No face data found for this user")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Face verification failed")
        }
    }
}
