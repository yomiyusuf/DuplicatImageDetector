package com.yomi.duplicatedetector.model

class DuplicateData(val duplicatePath: String, val originalPath: String) {

    override fun toString(): String {
        return String.format("(duplicate: %s, original: %s)", duplicatePath, originalPath)
    }

}