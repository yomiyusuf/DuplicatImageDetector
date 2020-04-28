package com.yomi.duplicatedetector.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.yomi.duplicatedetector.R
import com.yomi.duplicatedetector.viewModel.DuplicateViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val startingDirectory = "pictures"
    private lateinit var listAdapter: DuplicatesListAdapter

    private lateinit var viewModel: DuplicateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listAdapter = DuplicatesListAdapter(arrayListOf())
        viewModel = ViewModelProviders.of(this).get(DuplicateViewModel::class.java)

        initRecyclerView()
        registerObservers()
        viewModel.getDuplicateImages(assets, startingDirectory)
    }

    private fun initRecyclerView() {
        rv_duplicates.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    private fun registerObservers() {
        viewModel.duplicateData.observe(this, Observer {
            listAdapter.updateData(it)
        })
    }
}
