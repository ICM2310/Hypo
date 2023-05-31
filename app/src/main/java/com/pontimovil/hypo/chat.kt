package com.pontimovil.hypo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pontimovil.hypo.databinding.FragmentChatBinding
import com.pontimovil.hypo.modelo.Usuario

class chat : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var userList: ArrayList<Usuario>
    private lateinit var adapter: UserAdapter
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userList = ArrayList()
        adapter = UserAdapter(userList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView1)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        db.collection("chatusuario")
            .get().addOnSuccessListener { querySnapshot -> for (document in querySnapshot) {
                    val email = document.getString("email") ?: ""
                    val user = Usuario(email, "0", "0")
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = chat()
    }
}
