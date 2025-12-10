package com.horhe.hostseditor

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import com.topjohnwu.superuser.Shell


class FileViewModel: ViewModel() {
    val path = "/data/adb/modules/hosts/system/etc/hosts"

    var fileContent by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun readHostsFile() {
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            val result = RootAction.readRootFile(path)

            result.onSuccess { text ->
                fileContent = text
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Unknown error"
                isLoading = false
            }
        }
    }

    fun saveHostsFile(cacheDir: File) {
        val contentToSave = fileContent ?: return

        if (isLoading) return
        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                if (!Shell.getShell().isRoot) {
                    throw IOException("Error: no Root access")
                }

                tempFile = File.createTempFile("hosts_buffer", null, cacheDir)
                tempFile.writeText(contentToSave)

                val tempPath = tempFile.absolutePath
                val backupPath = "$path.bak"

                val commands = listOf(
                    //"mount -o rw,remount /",
                    "cp \"$path\" \"$backupPath\"",
                    "cp \"$tempPath\" \"$path\"",
                    "chmod 644 \"$path\"",
                    "chown 0:0 \"$path\""
                )

                val result = Shell.cmd(*commands.toTypedArray()).exec()

                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                } else {
                    val errorLog = result.out.joinToString("\n")
                    throw IOException("Error code: ${result.code}. Log: $errorLog")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error during saving: ${e.message}"
                    isLoading = false
                }
            } finally {
                tempFile?.delete()
            }
        }
    }

    fun exportToUri(context: Context, uri: Uri) {
        val contentToSave = fileContent ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(contentToSave.toByteArray())
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Successfully saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error during export: ${e.message}"
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onContentChanged(newText: String) {
        fileContent = newText
    }
}