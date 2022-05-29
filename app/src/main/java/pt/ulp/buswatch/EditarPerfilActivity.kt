package pt.ulp.easybus2_testes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pt.ulp.easybus2_testes.databinding.ActivityEditarPerfilBinding

class EditarPerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditarPerfilBinding
    private lateinit var database: DatabaseReference
    private lateinit var uid : String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uname = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String

        binding.buttonUpdate.setOnClickListener{
            val firstname = binding.textViewFN.text.toString()
            val lastname = binding.textViewLN.text.toString()
            val username = binding.textViewUN.text.toString()
            updateData(firstname, lastname, username,uname,id)
        }

        binding.buttonVoltar.setOnClickListener{
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("Username", uname)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
    }

    private fun updateData(firstname: String, lastname: String, username: String,uname: String,
    id: String) {
        database = FirebaseDatabase.getInstance().getReference("Utilizadores")
        val utilizadores = mapOf<String, String>(
            "firstName" to firstname,
            "lastName" to lastname,
            "username" to username
        )

        database.child(uid).updateChildren(utilizadores).addOnSuccessListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("Username", uname)
            intent.putExtra("ID", id)
            startActivity(intent)
            finish()
            Toast.makeText(this, "Dados alterados", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(this, "Não foi possivel alterar dados", Toast.LENGTH_SHORT).show()
        }
    }
}