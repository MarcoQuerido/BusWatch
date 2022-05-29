package pt.ulp.easybus2_testes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class ActivityLogin : AppCompatActivity(){
    private var email: String = ""
    private var password: String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var uid : String
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        findViewById<Button>(R.id.button_NewRegisto).setOnClickListener {
            startActivity(Intent(this, ActivityRegisto::class.java))
            finish()
        }

        findViewById<TextView>(R.id.text_view_forget_password).setOnClickListener {
            startActivity(Intent(this, ActivityForgotPassword::class.java))
            finish()
        }

        findViewById<Button>(R.id.button_confirmLogin).setOnClickListener {
            email = findViewById<EditText>(R.id.editText_emailLogin).text.toString()
            password = findViewById<EditText>(R.id.editText_passwordLogin).text.toString()

            // SE UM DOS CAMPOS ESTIVER VAZIO ...
            if(email.isNotEmpty() && password.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener{
                    if (it.isSuccessful){
                        //mostrarNextActivity(it.result?.user?.email?:"",ProviderType.BASIC)
                        auth = FirebaseAuth.getInstance()
                        uid = auth.currentUser?.uid.toString()
                        databaseReference = FirebaseDatabase.getInstance().getReference("Utilizadores")
                        val user = Firebase.auth.currentUser
                        if (user != null){
                            val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
                            val reference = database.getReference("Utilizadores")
                            reference.child(uid).child("username").get().addOnSuccessListener {
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
                    else {
                        Toast.makeText(getApplicationContext(),"Email ou Password errados!" + email, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun mostrarNextActivity(email:String, provider: ProviderType){
        val intent: Intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("email",email)
        intent.putExtra("provider",provider.name)
        startActivity(intent)
        finish()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save UI state changes to the savedInstanceState.
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("Email", email)
        savedInstanceState.putString("Password", password)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        val e_mail = savedInstanceState.getString("Email")
        val pw = savedInstanceState.getString("Password")
        email = e_mail.toString()
        password = pw.toString()
    }
}