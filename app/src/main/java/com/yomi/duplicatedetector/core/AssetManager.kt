package com.yomi.duplicatedetector.core

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import java.util.*

/**
 * Created by Yomi Joseph on 2020-04-28.
 */

/**
 * Extension method on AssetManager to extract the path of all the files within the starting directory
 * including sub-directories.
 * @param startingDirectory
 * Examples
 * "" - to start from root of assets directory,
 * "pictures" - to start from the directory called pictures in root of assets folder
 * "pictures/work" - for deeper directories
 */
fun AssetManager.allFiles(startingDirectory: String): List<String>{
    val files = mutableListOf<String>()
    val stack = ArrayDeque<String>()
    this.list(startingDirectory)?.forEach { stack.push(it) }

    while (!stack.isEmpty()) {
        val currentPath = stack.pop()
        val completePath = "$startingDirectory/$currentPath"

        //if current path is is directory, open it up and add content to the stack
        if (isDirectory(completePath)) {
            this.list(completePath)
                ?.forEach { stack.push("$currentPath/$it") }
        }
        //if current path is a file add it to files
        else {
            files.add(completePath)
        }
    }
    return files
}

/**
 * Extension Method to check if the path is a directory [otherwise, a file].
 * As the Asset folder is not part of the Android file system, we do not have access to the File API
 * for this operation.
 *
 *
 * @param path Path to check
 */
fun AssetManager.isDirectory(path: String): Boolean {
    var exceptionMessage = ""
    try {
        val desc: AssetFileDescriptor =
            this.openFd(path) // Always throws exception: for directories and for files
        desc.close()
    } catch (e: Exception) {
        exceptionMessage = e.toString()
    }

    return exceptionMessage.endsWith(path)
}
