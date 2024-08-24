package com.catglo.openphysicaltherapy.Widgets

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.catglo.openphysicaltherapy.R
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickCaptureButton(
    onImageFilePicked : (Uri) -> Unit,
    onVideoFilePicked : (Uri) -> Unit,
    imageVector: ImageVector = ImageVector.vectorResource(id = R.drawable.icon_add_image)
) {
    val directory = File(LocalContext.current.cacheDir, "media")

    val context = LocalContext.current
    val tempUri = remember { mutableStateOf<Uri?>(null) }
    val authority = stringResource(id = R.string.fileprovider)

    // for takePhotoLauncher used
    fun getTempUri(): Uri? {
        directory.mkdirs()
        val file = File.createTempFile(
            "media_" + System.currentTimeMillis().toString(),
            "",
            directory
        )

        return FileProvider.getUriForFile(
            context,
            authority,
            file
        )
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                onImageFilePicked(it)
            }
        }
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { isSaved ->
            tempUri.value?.let {
                onImageFilePicked(it)
            }
        }
    )

    val takeVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { isSaved ->
            tempUri.value?.let {
                onVideoFilePicked(it)
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, launch takePhotoLauncher
            val tmpUri = getTempUri()
            tempUri.value = tmpUri
            tempUri.value?.let { takePhotoLauncher.launch(it) }
        } else {
            // Permission is denied, handle it accordingly
        }
    }

    var showImagePickerBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    if (showImagePickerBottomSheet){
        ModalBottomSheet(
            onDismissRequest = { showImagePickerBottomSheet = false },
            sheetState = sheetState,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick =
                    {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showImagePickerBottomSheet = false
                            }
                        }
                    }, modifier = Modifier
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Button")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.Center)
                ) {
                    Text(text = "Image or Video", fontWeight = FontWeight.Bold)
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier
                    .height(55.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .clickable {
                        showImagePickerBottomSheet = false
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permission is already granted, proceed to step 2
                            val tmpUri = getTempUri()
                            tempUri.value = tmpUri
                            tempUri.value?.let { takePhotoLauncher.launch(it) }
                        } else {
                            // Permission is not granted, request it
                            cameraPermissionLauncher.launch(permission)
                        }
                    }) {
                    Text(
                        text = "Take a new photo", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .fillMaxWidth()
                    )
                }
                Row(modifier = Modifier
                    .height(55.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .clickable {
                        showImagePickerBottomSheet = false
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permission is already granted, proceed to step 2
                            val tmpUri = getTempUri()
                            tempUri.value = tmpUri
                            tempUri.value?.let { takeVideoLauncher.launch(it) }
                        } else {
                            // Permission is not granted, request it
                            cameraPermissionLauncher.launch(permission)
                        }
                    }) {
                    Text(
                        text = "Take a new video", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .fillMaxWidth()
                    )
                }
                Row(modifier = Modifier
                    .height(55.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .clickable {
                        showImagePickerBottomSheet = false
                        imagePicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            )
                        )
                    }) {
                    Text(
                        text = "Pick from gallery", textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    IconButton(onClick = {
        showImagePickerBottomSheet = true
    }) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Add Image"
        )
    }
}