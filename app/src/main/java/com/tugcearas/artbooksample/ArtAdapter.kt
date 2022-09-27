package com.tugcearas.artbooksample

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tugcearas.artbooksample.databinding.RecyclerRowBinding

class ArtAdapter(val artList: ArrayList<ArtModel>):RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
       val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {

        holder.binding.recyclerRowText.text = artList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,ArtBookDetailsActivity::class.java)
            intent.putExtra("info","oldArt")
            intent.putExtra("id",artList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = artList.size
}