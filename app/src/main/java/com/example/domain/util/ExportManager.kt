package com.example.domain.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ExportManager {
    fun exportToTxt(context: Context, fileName: String, content: String): Uri? {
        return try {
            val file = File(context.cacheDir, "$fileName.txt")
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToMarkdown(context: Context, fileName: String, content: String): Uri? {
        return try {
            val file = File(context.cacheDir, "$fileName.md")
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
