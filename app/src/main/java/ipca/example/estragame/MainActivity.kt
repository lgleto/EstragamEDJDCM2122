package ipca.example.estragame

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import ipca.example.estragame.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val db = Firebase.firestore

    var notificationReceiver : NotificationReceiver? = null

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.getString(MyFirebaseMessagingService.NOTIFICATION_MESSAGE)?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()

        val user = hashMapOf(
            "online" to true,
            "date" to Timestamp(Date()),
        )
        db.collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .set(user)

        notificationReceiver = NotificationReceiver()
        this.registerReceiver(notificationReceiver, IntentFilter(MyFirebaseMessagingService.BROADCAST_NET_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        val user = hashMapOf(
            "online" to false,
            "date" to Timestamp(Date()),
        )
        db.collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .set(user)

        notificationReceiver?.let {
            this.unregisterReceiver(it)
        }

    }
}