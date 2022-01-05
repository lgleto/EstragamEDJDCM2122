package ipca.example.estragame.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import ipca.example.estragame.LoginActivity
import ipca.example.estragame.R
import ipca.example.estragame.databinding.FragmentHomeBinding
import ipca.example.estragame.ui.Post
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var posts = arrayListOf<Post>()
    private lateinit var listViewPhotos : ListView
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddPhoto.setOnClickListener {
          findNavController().navigate(R.id.action_navigation_home_to_photoFragment)
        }

        listViewPhotos = binding.listViewPhotos
        adapter = PostsAdapter()
        listViewPhotos.adapter = adapter

        val db = Firebase.firestore
        db.collection("posts")
            //.whereEqualTo("user", FirebaseAuth.getInstance().currentUser.uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                posts.clear()
                for (doc in value!!.documents) {
                    val post = Post.fromHashMap(doc.data!!)
                    posts.add(post)
                }
                adapter.notifyDataSetChanged()
            }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main,menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Firebase.auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PostsAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return posts.size
        }

        override fun getItem(position: Int): Any {
            return posts[position]
        }

        override fun getItemId(p0: Int): Long {
            return 0L
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
            var rootView = layoutInflater.inflate(R.layout.row_photo, viewGroup, false)
            val textViewDescription = rootView.findViewById<TextView>(R.id.textViewDescription)
            val imageViewPhoto = rootView.findViewById<ImageView>(R.id.imageViewPhoto)
            textViewDescription.text = posts[position].description

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val photoName = posts[position].photo
            val photoImagesRef = storageRef.child("photos/${photoName}")

            val ONE_MEGABYTE: Long = 1024 * 1024
            photoImagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                imageViewPhoto.setImageBitmap(bitmap)
            }.addOnFailureListener {

            }

            return rootView
        }
    }
}