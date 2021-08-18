package org.apiconlab.mugomusic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.apiconlab.mugomusic.R
import org.apiconlab.mugomusic.SearchDataItem

class HistoryAdapter (private val searchList: ArrayList<SearchDataItem>, private val listener: OnItemClickListener): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        val ivSong: ImageView = view.findViewById(R.id.rsiv1)
        val tvSong: TextView = view.findViewById(R.id.rstv1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                listener.onHistoryItemClick(pos)
            }
        }
    }

    interface OnItemClickListener{
        fun onHistoryItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_song_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSong.text = searchList[position].song_name
        Glide.with(holder.itemView).load(searchList[position].song_image).into(holder.ivSong)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }
}