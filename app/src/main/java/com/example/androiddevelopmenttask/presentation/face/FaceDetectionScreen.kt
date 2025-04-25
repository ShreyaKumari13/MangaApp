package com.example.androiddevelopmenttask.presentation.face

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.androiddevelopmenttask.presentation.common.components.BottomNavigationBar
import com.example.androiddevelopmenttask.presentation.common.components.ZenithraButton
import com.example.androiddevelopmenttask.presentation.common.navigation.BottomNavItem
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun FaceDetectionScreen(
    navController: NavController,
    viewModel: FaceDetectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val faceDetectionState by viewModel.faceDetectionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(faceDetectionState) {
        when (faceDetectionState) {
            is FaceDetectionState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (faceDetectionState as FaceDetectionState.Error).message
                    )
                }
                viewModel.resetState()
            }
            is FaceDetectionState.FaceWithinRectangle -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Face is properly positioned within the rectangle!")
                }
            }
            is FaceDetectionState.FaceOutsideRectangle -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Please position your face within the rectangle.")
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = listOf(
                    BottomNavItem.Manga,
                    BottomNavItem.Face
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Face Detection",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        // Remember the reference rectangle dimensions
                        val referenceRectSize = 200.dp
                        val referenceRect = remember {
                            Rect(
                                0, 0,
                                referenceRectSize.value.toInt(), referenceRectSize.value.toInt()
                            )
                        }

                        // Determine the border color based on face detection state
                        val borderColor = when (faceDetectionState) {
                            is FaceDetectionState.FaceWithinRectangle -> Color.Green
                            is FaceDetectionState.FaceOutsideRectangle -> Color.Red
                            else -> Color.White
                        }

                        CameraPreview(
                            onImageCaptured = { bitmap ->
                                // Calculate the center position of the reference rectangle
                                val centerX = bitmap.width / 2
                                val centerY = bitmap.height / 2
                                val halfSize = (referenceRectSize.value * bitmap.width / 400).toInt()

                                // Update the reference rectangle position to be centered
                                referenceRect.set(
                                    centerX - halfSize,
                                    centerY - halfSize,
                                    centerX + halfSize,
                                    centerY + halfSize
                                )

                                viewModel.detectFace(bitmap, referenceRect)
                            }
                        )

                        // Face detection guide overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(referenceRectSize)
                                    .border(
                                        width = 2.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Put your face inside the frame and press the button below",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // This button is just for UI display, actual capture is handled in CameraPreview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Capture",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Camera permission is required for face detection",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            ZenithraButton(
                                text = "Grant Permission",
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            )
                        }
                    }
                }
            }

            if (faceDetectionState is FaceDetectionState.Processing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            if (faceDetectionState is FaceDetectionState.FaceWithinRectangle) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color.Green,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Face Properly Positioned!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.resetState() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }

            if (faceDetectionState is FaceDetectionState.FaceOutsideRectangle) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color.Red,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Failed",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Face Not Properly Positioned",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please make sure your face is clearly visible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.resetState() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Remember camera state
    var cameraInitialized by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    // Create camera components
    val preview = remember { Preview.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { 
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build() 
    }
    val cameraSelector = remember { CameraSelector.DEFAULT_FRONT_CAMERA }
    val executor = remember { Executors.newSingleThreadExecutor() }

    // Initialize camera when view is created
    LaunchedEffect(previewView) {
        try {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            
            // Bind camera to lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            // Set surface provider for preview
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraInitialized = true
            Log.d("FaceDetection", "Camera initialized successfully")
        } catch (e: Exception) {
            cameraError = "Failed to initialize camera: ${e.message}"
            Log.e("FaceDetection", "Camera initialization error", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Show error if camera initialization failed
        cameraError?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Capture button
        if (cameraInitialized) {
            IconButton(
                onClick = {
                    try {
                        Log.d("FaceDetection", "Capture button clicked")
                        captureImage(
                            context = context,
                            imageCapture = imageCapture,
                            executor = executor,
                            onImageCaptured = onImageCaptured
                        )
                    } catch (e: Exception) {
                        Log.e("FaceDetection", "Error in capture button click", e)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Take Photo",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit
) {
    try {
        Log.d("FaceDetection", "Starting image capture process")
        
        // Use a simpler approach with ImageCapture.OutputFileOptions
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            java.io.File.createTempFile("temp_", ".jpg", context.cacheDir)
        ).build()
        
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        Log.d("FaceDetection", "Image saved to file: ${outputFileResults.savedUri}")
                        
                        // Load the saved image as a bitmap
                        val savedUri = outputFileResults.savedUri
                        if (savedUri != null) {
                            try {
                                // Open input stream from the URI
                                context.contentResolver.openInputStream(savedUri)?.use { inputStream ->
                                    // Decode with options to prevent OOM
                                    val options = BitmapFactory.Options().apply {
                                        inSampleSize = 4  // Downsample to 1/4 size
                                    }
                                    
                                    val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                                    
                                    if (bitmap != null) {
                                        Log.d("FaceDetection", "Bitmap loaded successfully: ${bitmap.width}x${bitmap.height}")
                                        
                                        // Pass the bitmap to the callback
                                        try {
                                            onImageCaptured(bitmap)
                                        } catch (e: Exception) {
                                            Log.e("FaceDetection", "Error in callback: ${e.message}")
                                            e.printStackTrace()
                                        }
                                    } else {
                                        Log.e("FaceDetection", "Failed to decode bitmap from saved image")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FaceDetection", "Error loading saved image: ${e.message}")
                                e.printStackTrace()
                            }
                        } else {
                            Log.e("FaceDetection", "Saved URI is null")
                        }
                    } catch (e: Exception) {
                        Log.e("FaceDetection", "Error processing saved image: ${e.message}")
                        e.printStackTrace()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("FaceDetection", "Error saving image: ${exception.message}")
                    exception.printStackTrace()
                }
            }
        )
    } catch (e: Exception) {
        Log.e("FaceDetection", "Error taking picture: ${e.message}")
        e.printStackTrace()
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    try {
        if (bitmap.isRecycled) {
            Log.e("FaceDetection", "Cannot rotate a recycled bitmap")
            return bitmap
        }
        
        if (degrees == 0f) {
            return bitmap
        }
        
        val matrix = Matrix()
        matrix.postRotate(degrees)
        
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 
            0, 
            0, 
            bitmap.width, 
            bitmap.height, 
            matrix, 
            true
        )
        
        // If the rotated bitmap is different from the original, recycle the original
        if (rotatedBitmap != bitmap) {
            try {
                bitmap.recycle()
            } catch (e: Exception) {
                Log.e("FaceDetection", "Error recycling original bitmap: ${e.message}")
            }
        }
        
        return rotatedBitmap
    } catch (e: Exception) {
        Log.e("FaceDetection", "Error rotating bitmap: ${e.message}")
        e.printStackTrace()
        return bitmap
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }
}
