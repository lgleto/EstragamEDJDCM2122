package ipca.example.estragame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (FirebaseAuth.getInstance().currentUser == null ){
            val intent = Intent ( this, LoginActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent ( this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}