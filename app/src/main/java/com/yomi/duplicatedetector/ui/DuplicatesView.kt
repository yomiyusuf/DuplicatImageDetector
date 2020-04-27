package com.yomi.duplicatedetector.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yomi.duplicatedetector.R
import kotlinx.android.synthetic.main.item_duplicate_view.view.*

/**
 * Created by Yomi Joseph on 2020-04-27.
 */
class DuplicatesView: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet):    super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.item_duplicate_view, this, true)
    }

    fun setView(startingDirectory: String, duplicates: List<String>) {
        resetView()
        val duplicateNumber = String.format(resources.getString(R.string.duplicate_number), duplicates.size.toString())
        txt_duplicates_number.text = duplicateNumber

        duplicates.forEach {
            //create TextView
            val pathText = TextView(context).apply {
                text = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextAppearance(R.style.defaultText_body)
                } else {
                    setTextAppearance(context, R.style.defaultText_body)
                }
            }

            //create ImageView
            val image = ImageView(context)
            Glide
                .with(this)
                .load("file:///android_asset/$startingDirectory/$it")
                .apply(RequestOptions().override(500, 500))
                .into(image)

            //Add views to respective containers
            images_paths_container.addView(pathText)
            images_container.addView(image)
        }
    }

    private fun resetView() {
        images_container.removeAllViews()
        images_paths_container.removeAllViews()
    }
}