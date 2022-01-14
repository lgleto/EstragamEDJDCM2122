package ipca.example.estragame.ui.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import ipca.example.estragame.LoginActivity
import ipca.example.estragame.MainActivity
import ipca.example.estragame.R
import ipca.example.estragame.databinding.FragmentHomeBinding
import ipca.example.estragame.ui.Post
import ipca.example.estragame.ui.User
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var posts = arrayListOf<Post>()
    private lateinit var listViewPhotos : ListView
    private lateinit var adapter: PostsAdapter

    val db = Firebase.firestore
    val usersCollection = db.collection("users")

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


        db.collection("posts")
            //.whereEqualTo("user", FirebaseAuth.getInstance().currentUser.uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                posts.clear()
                for (doc in value!!.documents) {
                    val post = Post.fromHashMap(doc.id,  doc.data!!)
                    posts.add(post)
                }
                adapter.notifyDataSetChanged()
            }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main,menu)
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle("Estragame")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
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
            R.id.action_notification -> {
                sendNotification("Hello world!")
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

            val textViewUserName = rootView.findViewById<TextView>(R.id.textViewUserName)
            val imageViewUserPhoto = rootView.findViewById<ImageView>(R.id.imageViewUserPhoto)

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


            usersCollection.document(posts[position].user.toString()).get()
                .addOnSuccessListener { document ->
                    val user = document.data?.let {
                        User.fromHashMap(it)
                    }
                    user?.let {
                        textViewUserName.text = it.name
                        textViewUserName.text = it.name

                        val photoUserImagesRef = storageRef.child("user_photos/${it.photo}")
                        photoUserImagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { byteArray ->
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            imageViewUserPhoto.setImageBitmap(bitmap)
                        }.addOnFailureListener {

                        }

                    }


                }

            rootView.isClickable = true
            rootView.setOnClickListener {

                val bundle = Bundle().apply {
                    putString("post_id", posts[position].id)
                }

                findNavController().navigate(R.id.action_navigation_home_to_photoFragment,  bundle)
            }

            return rootView
        }
    }
}