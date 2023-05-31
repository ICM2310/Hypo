package com.pontimovil.hypo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pontimovil.hypo.modelo.Usuario

class User_Adapter(private val userList: ArrayList<Usuario>) : RecyclerView.Adapter<User_Adapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_layoutchat, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.txt_name)
        private val chatButton: Button = itemView.findViewById(R.id.btn_chat)

        fun bind(user: Usuario) {
            nameTextView.text = user.email

            chatButton.setOnClickListener {
                // Handle chat button click event
                val clickedUser = userList[adapterPosition]
                // Open chat between the current user and the clicked user
                openChatBetweenUsers(clickedUser)
            }
        }
    }

    private fun openChatBetweenUsers(clickedUser: Usuario) {
        // Implement your logic to open a chat between the current user and the clicked user
        // You can use an Intent to navigate to a new activity or fragment for the chat screen
        // Pass any necessary data to the chat screen, such as user IDs or other identifiers
        // Example:
        /*
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("currentUserId", getCurrentUserId())
        intent.putExtra("clickedUserId", clickedUser.id)
        startActivity(intent)
        */
    }
}
