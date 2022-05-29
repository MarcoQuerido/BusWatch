package pt.ulp.easybus2_testes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC
}

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String

        findViewById<Button>(R.id.buttonPartilharLoc).setOnClickListener{
            val intent = Intent(this, AutocarrosListActivity::class.java)
            intent.putExtra("Username", username)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonVerLoc).setOnClickListener{
            val intent = Intent(this, ActivityVerLocations::class.java)
            intent.putExtra("Username", username)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonPerfil).setOnClickListener{
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("Username", username)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonLogout).setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, WelcomeScreenActivity::class.java))
            finish()
        }
    }
}