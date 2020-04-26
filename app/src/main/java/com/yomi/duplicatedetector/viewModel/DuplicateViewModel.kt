package com.yomi.duplicatedetector.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.yomi.duplicatedetector.model.FileMeta
import com.yomi.duplicatedetector.model.DuplicateData
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

    val duplicateData by lazy { MutableLiveData<MutableList<DuplicateData>>() }

    fun findDuplicateImages(directory: String) {
        val previouslySeenImages = HashMap<String, FileMeta>()
        val stack = ArrayDeque<String>()
        app.assets.list(directory)?.forEach {
            stack.push(it)
        }

        val duplicates = ArrayList<DuplicateData>()

        while (!stack.isEmpty()) {

            val currentPath = stack.pop()
            val currentFile = File(currentPath.toString())

            val fileHash: String
            try {
                fileHash = sampleHashFile(directory, currentPath)
            } catch (e: IOException) {

                e.printStackTrace()
                continue
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                continue
            }

            val currentLastEditedTime = currentFile.lastModified()

            if (previouslySeenImages.containsKey(fileHash)) {

                val fileInfo = previouslySeenImages[fileHash]
                val existingLastEditedTime = fileInfo!!.timeLastEdited
                val existingPath = fileInfo.path

                if (currentLastEditedTime > existingLastEditedTime) {
                    duplicates.add(DuplicateData(currentPath, existingPath))
                } else {
                    duplicates.add(DuplicateData(existingPath, currentPath))
                    previouslySeenImages[fileHash] = FileMeta(currentLastEditedTime, currentPath)
                }
            } else {
                previouslySeenImages[fileHash] = FileMeta(currentLastEditedTime, currentPath)
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
}