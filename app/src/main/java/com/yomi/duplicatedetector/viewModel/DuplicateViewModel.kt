package com.yomi.duplicatedetector.viewModel

import android.app.Application
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.yomi.duplicatedetector.model.DuplicatesData
import com.yomi.duplicatedetector.model.FileMeta
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


/**
 * Created by Yomi Joseph on 2020-04-26.
 */
class DuplicateViewModel(val app: Application): AndroidViewModel(app) {

    val duplicateData by lazy { MutableLiveData<MutableList<DuplicatesData>>() }

    fun findDuplicateImages(startingDirectory: String) {
        val filesSeenAlready = HashMap<String, FileMeta>()
        val stack = ArrayDeque<String>()
        app.assets.list(startingDirectory)?.forEach {
            stack.push(it)
        }

        val duplicates = ArrayList<DuplicatesData>()

        while (!stack.isEmpty()) {

            val currentPath = stack.pop()
            val currentFile = File(currentPath.toString())

            if (isDirectory(startingDirectory, currentPath, app.assets)) {
                app.assets.list("$startingDirectory/$currentPath")?.forEach { stack.push("$currentPath/$it") }
            } else {
                val fileHash: String
                try {
                    fileHash = sampleHashFile(startingDirectory, currentPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                    continue
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                    continue
                }

                val currentLastEditedTime = currentFile.lastModified()

                if (filesSeenAlready.containsKey(fileHash)) {

                    val fileInfo = filesSeenAlready[fileHash]
                    val existingLastEditedTime = fileInfo!!.timeLastEdited
                    val existingPath = fileInfo.path

                    if (currentLastEditedTime > existingLastEditedTime) {
                        duplicates.add(DuplicatesData(currentPath, existingPath))
                    } else {
                        duplicates.add(DuplicatesData(existingPath, currentPath))
                        filesSeenAlready[fileHash] = FileMeta(currentLastEditedTime, currentPath)
                    }
                } else {
                    filesSeenAlready[fileHash] = FileMeta(currentLastEditedTime, currentPath)
                }
            }

        }
        duplicateData.value =  duplicates
    }

    private val SAMPLE_SIZE = 4000

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun sampleHashFile(directory:String, path: String): String {

        val imageFullPath = "$directory/$path"

        val totalBytes = app.assets.openFd(imageFullPath).length

        app.assets.open(imageFullPath).use { inputStream ->
            val digest = MessageDigest.getInstance("SHA-512")
            val digestInputStream = DigestInputStream(inputStream, digest)

            if (totalBytes < SAMPLE_SIZE * 3) {
                val bytes = ByteArray(totalBytes.toInt())
                digestInputStream.read(bytes)
            } else {
                val bytes = ByteArray(SAMPLE_SIZE * 3)
                val numBytesBetweenSamples = (totalBytes - SAMPLE_SIZE * 3) / 2

                for (n in 0..2) {
                    digestInputStream.read(bytes, n * SAMPLE_SIZE, SAMPLE_SIZE)
                    digestInputStream.skip(numBytesBetweenSamples)
                }
            }
            return BigInteger(1, digest.digest()).toString(16)
        }
    }

    /**
     * Method to check if the path is a directory [otherwise, a file].
     * As the Asset folder is not part of the Android file system, we do not have access to the File API
     * for this operation.
     *
     *
     * @param path Path to check
     * @param am AssetManager
     */
    private fun isDirectory(startDirectory: String, path: String, am: AssetManager): Boolean {
        //return !path.contains(".")
        var exceptionMessage = ""
        try {
            val desc: AssetFileDescriptor = am.openFd("$startDirectory/$path") // Always throws exception: for directories and for files
            desc.close() // Never executes
        } catch (e: Exception) {
            exceptionMessage = e.toString()
        }

        return exceptionMessage.endsWith(path)
    }
}