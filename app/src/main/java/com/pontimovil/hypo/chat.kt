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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [chat.newInstance] factory method to
 * create an instance of this fragment.
 */
class chat : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentChatBinding
    private lateinit var userList: ArrayList<Usuario>
    private lateinit var adapter: UserAdapter
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Initialize the userList and adapter
        userList = ArrayList()
        adapter = UserAdapter(userList)

        // Set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        db.collection("chatusuario")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val name = document.getString("name") ?: ""
                    val email = document.getString("email") ?: ""
                    val user = Usuario(email, "0", "0")
                    userList.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

                binding = FragmentChatBinding.inflate(inflater, container, false)
                val topBar = binding.topBar
                val fragmentName = "Chat" // replace with the fragment name you want to use
                val bundle = Bundle().apply {
                    putString("fragmentName", fragmentName)
                }
                val topBarFragment = topBar().apply {
                    arguments = bundle
                }
                childFragmentManager.beginTransaction().apply {
                    add(topBar.id, topBarFragment, "topBar")
                    commit()
                }

                return view
            }


        companion object {
            /**
             * Use this factory method to create a new instance of
             * this fragment using the provided parameters.
             *
             * @param param1 Parameter 1.
             * @param param2 Parameter 2.
             * @return A new instance of fragment chat.
             */
            // TODO: Rename and change types and number of parameters
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                chat().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }

}
