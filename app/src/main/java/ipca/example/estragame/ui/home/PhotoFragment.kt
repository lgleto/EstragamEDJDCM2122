package ipca.example.estragame.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ipca.example.estragame.databinding.FragmentPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*


class PhotoFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var binding : FragmentPhotoBinding

    private var bitmap : Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabTakePhoto.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        binding.fabSend.setOnClickListener {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val photoName = UUID.randomUUID().toString()+".jpg"
            val photoImagesRef = storageRef.child("photos/${photoName}")

            val baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = photoImagesRef.putBytes(data)
            uploadTask.addOnFailureListener {
                //error uploading photo
                Toast.makeText(requireContext(),"error uploading photo", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                Toast.makeText(requireContext(),"Photo upload with success", Toast.LENGTH_LONG).show()

                val db = Firebase.firestore
                val post = hashMapOf(
                    "description" to binding.editTextDescription.text.toString(),
                    "photo" to photoName,
                    "date" to Timestamp(Date()),
                    "user" to FirebaseAuth.getInstance().currentUser?.uid
                )

                db.collection(FirebaseAuth.getInstance().uid.toString())
                    .add(post)
                    .addOnSuccessListener { documentReference ->
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(),"error uploading photo", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageViewPhoto.setImageBitmap(imageBitmap)
            bitmap = imageBitmap
            binding.fabSend.visibility = View.VISIBLE
        }
    }


}