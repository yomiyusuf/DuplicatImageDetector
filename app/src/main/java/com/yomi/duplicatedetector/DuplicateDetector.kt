package com.yomi.duplicatedetector

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.math.BigInteger
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Created by Yomi Joseph on 2020-04-27.
 *
 *This class handles all the duplication detection algorithm
 * It wraps the duplicatesData in a LiveData so it can be observed by the consumers.
 */
class DuplicateDetector {

    val duplicateData by lazy { MutableLiveData<List<MutableList<String>>>() }

    /**
     *
     * @param assetManager
     * @param startingDirectory the directory within assets directory the search will start from.
     * use empty string("") to use the assets' root. Though this is highly discouraged as Android saves some other files in the assets root
     */
    fun findDuplicateImages(assetManager: AssetManager, startingDirectory: String) {
        val previouslySeenFiles = HashMap<String, MutableList<String>>()
        val stack = ArrayDeque<String>()
        assetManager.list(startingDirectory)?.forEach { stack.push(it) }

        while (!stack.isEmpty()) {

            val currentPath = stack.pop()

            if (isDirectory(assetManager, startingDirectory, currentPath)) {
                assetManager.list("$startingDirectory/$currentPath")?.forEach { stack.push("$currentPath/$it") }
            } else {
                val fileHash: String
                try {
                    fileHash = sampleHashFile(assetManager, startingDirectory, currentPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                    continue
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                    continue
                }

                if (previouslySeenFiles.containsKey(fileHash)) {
                    //add the new path to paths list of the existing entry
                    previouslySeenFiles[fileHash]?.add(currentPath)
                } else {
                    //add a new entry
                    previouslySeenFiles[fileHash] = mutableListOf(currentPath)
                }
            }
        }
        //Filter for entries with multiple paths
        val filtered = previouslySeenFiles.filter { it.value.size > 1 }
        duplicateData.value =  filtered.map {it.value}
    }

    private val SAMPLE_SIZE = 4000

    /**
     *
     * This method creates a fingerprint for the file at the startingDirectory/path
     * This is done by taking 3 samples at the beginning, middle and end of the file. The sample size is
     * set as SAMPLE_SIZE which should be the block size of the file system.
     *
     *
     *@param assetManager
     * @param startingDirectory
     * @param path
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun sampleHashFile(assetManager: AssetManager, startingDirectory:String, path: String): String {

        val imageFullPath = "$startingDirectory/$path"

        val totalBytes = assetManager.openFd(imageFullPath).length

        assetManager.open(imageFullPath).use { inputStream ->
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
    private fun isDirectory(am: AssetManager, startDirectory: String, path: String): Boolean {
        var exceptionMessage = ""
        try {
            val desc: AssetFileDescriptor = am.openFd("$startDirectory/$path") // Always throws exception: for directories and for files
            desc.close()
        } catch (e: Exception) {
            exceptionMessage = e.toString()
        }

        return exceptionMessage.endsWith(path)
    }
}