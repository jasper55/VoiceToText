package com.example.app.jasper.voicetotext.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.jasper.voicetotext.R
import com.example.app.jasper.voicetotext.ViewModel
import com.example.app.jasper.voicetotext.model.Language


class RecyclerViewAdapter(
        var userActionClickListener: UserActionClickListener,
        var resultList: List<Language>,
        private val viewModel: ViewModel
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var currentPosition: Int = 0

    fun setList(resultList: List<Language>) {
        this.resultList = resultList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.result_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val resultItem = resultList[position]
        viewHolder.result_language.text = "${resultItem.displayName}"
        viewHolder.itemListContainer.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                userActionClickListener.onItemClick(position)
                currentPosition = position
            }
        })
    }


    override fun getItemCount() = resultList.size

    fun getItem(position: Int) = resultList[position]

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var result_language: TextView
        var itemListContainer: LinearLayout

        init {
            itemListContainer = itemView.findViewById(R.id.item_list_container)
            result_language = itemView.findViewById(R.id.list_item_language)
        }
    }

    interface UserActionClickListener {
        fun onItemClick(position: Int)
    }
}
