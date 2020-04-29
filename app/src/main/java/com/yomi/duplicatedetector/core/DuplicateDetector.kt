package com.yomi.duplicatedetector.core

import android.content.res.AssetManager
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Exception
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
     * use empty string("") to use the assets' root. Though this is highly discouraged as Android
     * saves some other files in the assets root
     * Run in a coroutine in case there is delay in completion due to large number of files
     */
    suspend fun findDuplicateImages(assetManager: AssetManager, startingDirectory: String) {
        withContext(Dispatchers.IO){
            val previouslySeenFiles = HashMap<String, MutableList<String>>()
            val allFiles = assetManager.allFiles(startingDirectory)
            allFiles.forEach {
                val fileHash: String
                try {
                    fileHash = sampleHashFile(assetManager, it)

                    if (previouslySeenFiles.containsKey(fileHash)) {
                        //add the new path to paths list of the existing entry
                        previouslySeenFiles[fileHash]?.add(it)
                    } else {
                        //add a new entry
                        previouslySeenFiles[fileHash] = mutableListOf(it)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            //Filter for entries with multiple paths
            val filtered = previouslySeenFiles.filter { it.value.size > 1 }
            duplicateData.postValue(filtered.map { it.value })
        }
    }

    private val BLOCK_SIZE = 4000

    /**
     *
     * This method creates a fingerprint for the file at the startingDirectory/path
     * This is done by taking 3 samples at the beginning, middle and end of the file. The sample size is
     * set as BLOCK_SIZE which should be the block size of the file system.
     *
     *
     *@param assetManager
     * @param path
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun sampleHashFile(assetManager: AssetManager, path: String): String {

        val totalBytes = assetManager.openFd(path).length

        assetManager.open(path).use { inputStream ->
            val digest = MessageDigest.getInstance("SHA-512")
            val digestInputStream = DigestInputStream(inputStream, digest)

            if (totalBytes < BLOCK_SIZE * 3) {
                val bytes = ByteArray(totalBytes.toInt())
                digestInputStream.read(bytes)
            } else {
                val bytes = ByteArray(BLOCK_SIZE * 3)
                val numBytesBetweenSamples = (totalBytes - BLOCK_SIZE * 3) / 2

                for (n in 0..2) {
                    digestInputStream.read(bytes, n * BLOCK_SIZE, BLOCK_SIZE)
                    digestInputStream.skip(numBytesBetweenSamples)
                }
            }
            return BigInteger(1, digest.digest()).toString(16)
        }
    }


}