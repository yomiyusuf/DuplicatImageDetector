package com.yomi.duplicatedetector.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Yomi Joseph on 2020-04-27.
 */
class DuplicatesListAdapter(private val duplicatesList: MutableList<MutableList<String>>):
        RecyclerView.Adapter<DuplicatesListAdapter.DuplicatesViewHolder>() {

    fun updateData(newDuplicateList: List<MutableList<String>>) {
        duplicatesList.clear()
        duplicatesList.addAll(newDuplicateList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuplicatesViewHolder {
        val duplicatesView = DuplicatesView(parent.context)
        duplicatesView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return DuplicatesViewHolder(duplicatesView)
    }

    override fun onBindViewHolder(holder: DuplicatesViewHolder, position: Int) = holder.bind(duplicatesList[position])

    override fun getItemCount() = duplicatesList.size

    class DuplicatesViewHolder(var view: View): RecyclerView.ViewHolder(view) {
        private val duplicatesView: DuplicatesView = view as DuplicatesView

        fun bind(item: List<String>) {
            duplicatesView.setView(item)
        }
    }
}