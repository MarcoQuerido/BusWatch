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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class ActivityRegisto : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_registo)

        findViewById<Button>(R.id.button_voltar).setOnClickListener {
            startActivity(Intent(this, WelcomeScreenActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.button_confirmar).setOnClickListener {
            submit()
        }
    }

    // VERIFICAR E SUBMETER OS DADOS NA BD
    private fun submit(){
        auth= Firebase.auth

        findViewById<Button>(R.id.button_confirmar).setOnClickListener{
            val firstName = findViewById<TextView>(R.id.textView_firstName).text.toString()
            val lastName = findViewById<TextView>(R.id.textView_lastName).text.toString()
            val email = findViewById<EditText>(R.id.editText_EmailAddress).text.toString()
            val username = findViewById<TextView>(R.id.textView_username2).text.toString()
            val password = findViewById<TextView>(R.id.textView_password2).text.toString()

            // SE OS CAMPOS NÃO ESTIVEREM VAZIOS ...
            if(email.isNotEmpty() && password.isNotEmpty()){
                val user = Utilizadores(firstName,lastName, username,email)
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        addToDatabase(user,auth.uid.toString())
                        Log.d("LoginActivity",auth.uid.toString())
                        startActivity(Intent(this, WelcomeScreenActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext,"Email ou password errados", Toast.LENGTH_LONG).show()
                    }
                }
            }
            Toast.makeText(applicationContext,"Preencha todos os campos", Toast.LENGTH_LONG).show()
        }
    }

    // ADICIONAR OS DADOS À BD
    private fun addToDatabase(user: Utilizadores, uid:String){
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Utilizadores")
        reference.child(uid).setValue(user)
    }
}