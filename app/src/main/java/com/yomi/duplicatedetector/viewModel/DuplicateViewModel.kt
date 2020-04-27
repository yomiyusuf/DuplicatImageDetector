package com.yomi.duplicatedetector.viewModel

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import com.yomi.duplicatedetector.DuplicateDetector

/**
 * Created by Yomi Joseph on 2020-04-26.
 */
class DuplicateViewModel: ViewModel() {

    private val duplicateDetector = DuplicateDetector()

    val duplicateData = duplicateDetector.duplicateData

    fun getDuplicateImages(am: AssetManager, startingDirectory: String) {
        duplicateDetector.findDuplicateImages(am, startingDirectory)
    }
}