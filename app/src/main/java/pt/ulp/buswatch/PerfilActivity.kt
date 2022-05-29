package pt.ulp.easybus2_testes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import pt.ulp.easybus2_testes.databinding.ActivityProfileBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var utilizadores: Utilizadores
    private lateinit var uid : String
    private lateinit var buttonBack: Button
    private lateinit var buttonUpdate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().getReference("Utilizadores")
        if(uid.isNotEmpty()){
            getUserData()
        }

        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonUpdate.setOnClickListener{
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("Username", username)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }

        buttonBack = findViewById(R.id.button_voltar)
        buttonBack.setOnClickListener{
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("Username", username)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }
    }

    private fun getUserData() {
        databaseReference.child(uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                utilizadores = snapshot.getValue(Utilizadores::class.java)!!
                binding.textViewFN.setText(utilizadores.firstName)
                binding.textViewLN.setText(utilizadores.lastName)
                binding.textViewEmailAddress.setText(utilizadores.email)
                binding.textViewUN.setText(utilizadores.username)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PerfilActivity,"Impossivel apresentar informação do utilizador", Toast.LENGTH_SHORT).show()
            }
        })
    }
}