package com.jiwei.app.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

class AttachmentPicker(
    private val context: Context
) {
    fun copyToAttachmentsDir(uri: Uri, fileName: String): String {
        val dir = java.io.File(context.filesDir, "attachments").also { it.mkdirs() }
        val destFile = java.io.File(dir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            java.io.FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }
}

@Composable
fun rememberImagePickerLauncher(onImagePicked: (Uri) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }

@Composable
fun rememberFilePickerLauncher(onFilePicked: (Uri, String, String) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // File name and mime type would be determined by the caller
            onFilePicked(it, "file", "application/octet-stream")
        }
    }

@Composable
fun rememberCameraLauncher(onImageCaptured: (Uri) -> Unit) =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Camera URI is passed through the contract
    }
