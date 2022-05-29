package pt.ulp.easybus2_testes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class WelcomeScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid : String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var animation: Animation
    private lateinit var animation2: Animation
    private lateinit var botaoLogin: Button
    private lateinit var botaoRegisto: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        findViewById<Button>(R.id.buttonGoLogin).setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonGoRegisto).setOnClickListener {
            startActivity(Intent(this, ActivityRegisto::class.java))
            finish()
        }

        animation = AnimationUtils.loadAnimation(this,R.anim.animation_welcome_screen)
        animation2 = AnimationUtils.loadAnimation(this,R.anim.animation_welcome_screen_buttons)


        val image = findViewById<ImageView>(R.id.imageBus)
        val title = findViewById<TextView>(R.id.TextViewTitulo)
        val desc = findViewById<TextView>(R.id.textViewDescricao)

        image.animation = animation
        title.animation = animation
        desc.animation = animation

        botaoLogin = findViewById(R.id.buttonGoLogin)
        botaoRegisto = findViewById(R.id.buttonGoRegisto)

        val user = Firebase.auth.currentUser

        botaoLogin.animation = animation2
        botaoRegisto.animation = animation2

        if(user != null){
            botaoLogin.visibility = View.GONE
            botaoRegisto.visibility = View.GONE
        }
    }

    override fun onStart(){
        super.onStart()

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        databaseReference = FirebaseDatabase.getInstance().getReference("Utilizadores")

        val user = Firebase.auth.currentUser
        if(user != null){
            val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
            val reference = database.getReference("Utilizadores")
            reference.child(uid).child("username").get().addOnSuccessListener {
                Log.e("username", "Got value ${it.value}")
                val username = it.value
                val intent = Intent(this, MenuActivity::class.java)
                Toast.makeText(baseContext, "Bem vindo $username", Toast.LENGTH_SHORT).show()
                intent.putExtra("Username", username.toString())
                intent.putExtra("ID", uid)
                startActivity(intent)
                finish()
            }
        }
    }
}