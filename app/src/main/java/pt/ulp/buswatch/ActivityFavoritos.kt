package pt.ulp.easybus2_testes

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ActivityFavoritos : AppCompatActivity(), AdapterFavoritos.OnItemClickListener {
    private lateinit var dbref : DatabaseReference
    private lateinit var favoritosRecyclerview : RecyclerView
    private lateinit var favoritosArrayList : ArrayList<Favoritos>
    private lateinit var favoritosAdapter: AdapterFavoritos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String
        Log.e("Username para partilha", id)

        findViewById<Button>(R.id.buttonVAAutocarros).setOnClickListener {
            val intent = Intent(this, AutocarrosListActivity::class.java)
            intent.putExtra("Username",username)
            intent.putExtra("ID",id)
            startActivity(intent)
            finish()
        }

        favoritosRecyclerview = findViewById(R.id.favoritosList)
        // Set the layoutManager that this RecyclerView will use
        favoritosRecyclerview.layoutManager = LinearLayoutManager(this)
        favoritosRecyclerview.setHasFixedSize(true)
        favoritosArrayList = arrayListOf<Favoritos>()
        getAutocarroData(id)
        // Adapter class is initialized and list is passed in the params
        favoritosAdapter = AdapterFavoritos(favoritosArrayList,id,username, this)
        // Adapter instance is set to the recyclerView to inflate the items
        favoritosRecyclerview.adapter = favoritosAdapter
        favoritosAdapter.onItemClick = {
            Toast.makeText(this@ActivityFavoritos,"Item clicado ", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("ID",id)
            intent.putExtra("Username",username)
            startActivity(intent)
            finish()
        }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this@ActivityFavoritos,"Item $position clicado", Toast.LENGTH_SHORT).show()
    }

    private fun getAutocarroData(uid: String) {
        dbref = FirebaseDatabase.getInstance().getReference("Utilizadores").child(uid).child("Favoritos")
        dbref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (favoritosSnapshot in snapshot.children) {
                        val favoritos = favoritosSnapshot.getValue(Favoritos::class.java)
                        favoritosArrayList.add(favoritos!!)
                    }
                    favoritosAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"Erro", Toast.LENGTH_LONG).show()
            }
        })
    }
}