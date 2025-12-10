package com.horhe.hostseditor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader


object RootAction {
    suspend fun readRootFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat \"$path\""))

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val content = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }

            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Result.success(content.toString())
            } else {
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val errorMsg = errorReader.readText()
                Result.failure(Exception("Error ($exitCode): $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}