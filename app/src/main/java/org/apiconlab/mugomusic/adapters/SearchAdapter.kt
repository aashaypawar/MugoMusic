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

class SearchAdapter (private val searchList: ArrayList<SearchDataItem>, private val listener: OnItemClickListener): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        val ivSong: ImageView = view.findViewById(R.id.siv1)
        val tvSong: TextView = view.findViewById(R.id.stv1)
        val tvAlbum: TextView = view.findViewById(R.id.stv2)
        val tvSinger: TextView = view.findViewById(R.id.stv3)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION){
                listener.onSearchItemClick(pos)
            }
        }
    }

    interface OnItemClickListener{
        fun onSearchItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSong.text = searchList[position].song_name
        holder.tvAlbum.text = searchList[position].album_name
        holder.tvSinger.text = searchList[position].song_artist
        Glide.with(holder.itemView).load(searchList[position].song_image).into(holder.ivSong)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }
}