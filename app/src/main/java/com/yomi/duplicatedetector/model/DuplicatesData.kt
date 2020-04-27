package com.yomi.duplicatedetector.model

class DuplicatesData(val duplicatePath: String, val originalPath: String) {

    override fun toString(): String {
        return String.format("(duplicate: %s, original: %s)", duplicatePath, originalPath)
    }
}