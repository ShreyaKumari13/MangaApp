package com.example.androiddevelopmenttask.domain.usecase.face

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.repository.FaceDetectionRepository
import javax.inject.Inject

class DetectFaceUseCase @Inject constructor(
    private val faceDetectionRepository: FaceDetectionRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, referenceRect: Rect): Result<Boolean> {
        return faceDetectionRepository.detectFace(bitmap, referenceRect)
    }
}
