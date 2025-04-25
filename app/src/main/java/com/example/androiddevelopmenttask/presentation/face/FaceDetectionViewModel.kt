package com.example.androiddevelopmenttask.presentation.face

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevelopmenttask.domain.model.Result
import com.example.androiddevelopmenttask.domain.usecase.face.DetectFaceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceDetectionViewModel @Inject constructor(
    private val detectFaceUseCase: DetectFaceUseCase
) : ViewModel() {

    private val _faceDetectionState = MutableStateFlow<FaceDetectionState>(FaceDetectionState.Initial)
    val faceDetectionState: StateFlow<FaceDetectionState> = _faceDetectionState.asStateFlow()

    private val TAG = "FaceDetectionViewModel"

    fun detectFace(bitmap: Bitmap, referenceRect: Rect) {
        viewModelScope.launch {
            try {
                // Validate input
                if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
                    Log.e(TAG, "Invalid bitmap provided: width=${bitmap.width}, height=${bitmap.height}, recycled=${bitmap.isRecycled}")
                    _faceDetectionState.value = FaceDetectionState.Error("Invalid image captured. Please try again.")
                    return@launch
                }
                
                // Set state to processing
                _faceDetectionState.value = FaceDetectionState.Processing
                Log.d(TAG, "Processing face detection for bitmap: ${bitmap.width}x${bitmap.height}")

                try {
                    // Call use case with error handling
                    val result = detectFaceUseCase(bitmap, referenceRect)
                    
                    when (result) {
                        is Result.Success -> {
                            if (result.data) {
                                // Face is detected AND within the reference rectangle
                                _faceDetectionState.value = FaceDetectionState.FaceWithinRectangle
                                Log.d(TAG, "Face detected and within rectangle")
                            } else {
                                // Either no face detected or face is outside the reference rectangle
                                _faceDetectionState.value = FaceDetectionState.FaceOutsideRectangle
                                Log.d(TAG, "Face detected but outside rectangle or no face detected")
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Face detection error: ${result.message}")
                            _faceDetectionState.value = FaceDetectionState.Error(result.message)
                        }
                        is Result.Loading -> {
                            // Already handled
                        }
                    }
                } catch (e: Exception) {
                    // Handle any exceptions from the use case
                    Log.e(TAG, "Exception during face detection: ${e.message}")
                    e.printStackTrace()
                    _faceDetectionState.value = FaceDetectionState.Error("Face detection failed. Please try again.")
                }
            } catch (e: Exception) {
                // Handle any exceptions in the coroutine
                Log.e(TAG, "Exception in detectFace coroutine: ${e.message}")
                e.printStackTrace()
                _faceDetectionState.value = FaceDetectionState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }

    fun resetState() {
        _faceDetectionState.value = FaceDetectionState.Initial
    }
}
