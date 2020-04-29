package com.yomi.duplicatedetector.ui

import android.os.Build
import android.widget.LinearLayout
import com.yomi.duplicatedetector.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class DuplicatesViewTest {

    private lateinit var activity: MainActivity
    private lateinit var duplicateView: DuplicatesView
    private val duplicateData = listOf("testImages/3.jpg", "testImages/3b.jpg")

    private lateinit var pathsContainer: LinearLayout
    private lateinit var imagesContainer: LinearLayout

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(MainActivity::class.java).create().get()
        duplicateView = DuplicatesView(activity)

        pathsContainer = duplicateView.findViewById(R.id.images_paths_container)
        imagesContainer = duplicateView.findViewById(R.id.images_container)
    }

    @Test
    fun `verify duplicateView displays correctly`() {
        duplicateView.setView(duplicateData)

        assertEquals(duplicateData.size, pathsContainer.childCount)
        assertEquals(duplicateData.size, imagesContainer.childCount)
    }

}