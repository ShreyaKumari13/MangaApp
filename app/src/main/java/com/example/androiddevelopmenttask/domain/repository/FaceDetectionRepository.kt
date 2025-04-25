package com.example.androiddevelopmenttask.domain.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.androiddevelopmenttask.domain.model.Result

interface FaceDetectionRepository {
    suspend fun detectFace(bitmap: Bitmap, referenceRect: Rect): Result<Boolean>
    suspend fun saveFaceData(userId: Int, faceData: ByteArray): Result<Unit>
    suspend fun verifyFace(userId: Int, faceData: ByteArray): Result<Boolean>
}
