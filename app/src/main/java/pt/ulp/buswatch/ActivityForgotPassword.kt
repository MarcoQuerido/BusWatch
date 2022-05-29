package pt.ulp.easybus2_testes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ActivityForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        findViewById<Button>(R.id.buttonConfirmarFP).setOnClickListener {
            val email = findViewById<EditText>(R.id.textViewFPEmail).text.toString().trim { it <= ' '}
            if (email.isEmpty()){
                Toast.makeText(this, "Por favor introduza um endereço de email.", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            Toast.makeText(this, "Email enviado com sucesso para alteração de password.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        findViewById<Button>(R.id.button_voltarFP).setOnClickListener {
            startActivity(Intent(this, ActivityLogin::class.java))
            finish()
        }
    }
}