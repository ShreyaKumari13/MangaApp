package com.example.androiddevelopmenttask.presentation.face

sealed class FaceDetectionState {
    object Initial : FaceDetectionState()
    object Processing : FaceDetectionState()
    object FaceWithinRectangle : FaceDetectionState()
    object FaceOutsideRectangle : FaceDetectionState()
    data class Error(val message: String) : FaceDetectionState()
}
