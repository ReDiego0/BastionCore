package org.ReDiego0.bastionCore.utils

import java.io.File

object FileUtils {

    fun copyDirectory(source: File, target: File) {
        if (!source.exists()) return

        if (source.isDirectory) {
            if (!target.exists()) {
                target.mkdirs()
            }
            val files = source.list() ?: return
            for (file in files) {
                val srcFile = File(source, file)
                val destFile = File(target, file)
                if (file.equals("uid.dat", ignoreCase = true)) continue

                copyDirectory(srcFile, destFile)
            }
        } else {
            source.copyTo(target, overwrite = true)
        }
    }

    fun deleteDirectory(directory: File): Boolean {
        if (!directory.exists()) return true
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
        return directory.delete()
    }
}