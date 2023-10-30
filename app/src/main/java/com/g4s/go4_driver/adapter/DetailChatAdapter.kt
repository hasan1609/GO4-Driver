package com.g4s.go4_driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g4s.go4_driver.R

class DetailChatAdapter(
    val context: Context,
    val chat: MutableList<ChatModel>,
    val idSender: String
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_SENT = 2
    val ITEM_RECIVE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 2){
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_to_chat, parent, false)
            return ReceiveViewHolder(view)
        }else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_form_chat, parent, false)
            return ReceiveViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentChat = chat[position]
        if (holder.itemView == SentViewHolder::class.java)
        {
            val viewHolder = holder as SentViewHolder
            holder.sentChat.text = currentChat.pesan
        }else{
            val viewHolder = holder as ReceiveViewHolder
            holder.reciveChat.text = currentChat.pesan
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentChat = chat[position]
        return if(currentChat.senderId!! == idSender){
            ITEM_SENT
        }else{
            ITEM_RECIVE
        }
    }
    override fun getItemCount(): Int {
        return chat.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sentChat = itemView.findViewById<TextView>(R.id.txt_to)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val reciveChat = itemView.findViewById<TextView>(R.id.txt_form)
    }
}