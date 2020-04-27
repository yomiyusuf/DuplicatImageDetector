package com.yomi.duplicatedetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.yomi.duplicatedetector.viewModel.DuplicateViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: DuplicateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(DuplicateViewModel::class.java)

        viewModel.duplicateData.observe(this, Observer {
            Log.e("DUP", it.toString())
        })
        viewModel.findDuplicateImages("pictures")
    }

}
