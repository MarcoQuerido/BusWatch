package pt.ulp.easybus2_testes

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AutocarrosListActivity : AppCompatActivity(), MyAdapter.OnItemClickListener{
    private lateinit var dbref : DatabaseReference
    private lateinit var autocarroRecyclerview : RecyclerView
    private lateinit var autocarroArrayList : ArrayList<Autocarros>
    private lateinit var autocarroAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autocarros_list)

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String

        findViewById<Button>(R.id.buttonVAAutocarros).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("Username",username)
            intent.putExtra("ID",id)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonFavoritos).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("Username",username)
            intent.putExtra("ID",id)
            startActivity(intent)
            finish()
        }

        autocarroRecyclerview = findViewById(R.id.partilhasList)
        // Set the layoutManager that this RecyclerView will use
        autocarroRecyclerview.layoutManager = LinearLayoutManager(this)
        autocarroRecyclerview.setHasFixedSize(true)
        autocarroArrayList = arrayListOf<Autocarros>()
        getAutocarroData()
        // Adapter class is initialized and list is passed in the params
        autocarroAdapter = MyAdapter(autocarroArrayList,id,username, this)
        // Adapter instance is set to the recyclerView to inflate the items
        autocarroRecyclerview.adapter = autocarroAdapter
        autocarroAdapter.onItemClick = {
            Toast.makeText(this@AutocarrosListActivity,"Item clicado ", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MapsActivity::class.java)
            findViewById<Button>(R.id.buttonAddFavorites).setOnClickListener {
                Log.e("Dados enviados?", "true")
            }
            intent.putExtra("ID",id)
            intent.putExtra("Username",username)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.buttonFavoritos).setOnClickListener {
            val intent = Intent(this, ActivityFavoritos::class.java)
            intent.putExtra("ID",id)
            intent.putExtra("Username",username)
            startActivity(intent)
            finish()
        }
    }

    override fun onItemClick(position: Int){
        Toast.makeText(this@AutocarrosListActivity,"Item $position clicado", Toast.LENGTH_SHORT).show()
    }

    private fun getAutocarroData() {
        dbref = FirebaseDatabase.getInstance().getReference("Autocarros")
        dbref.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (autocarroSnapshot in snapshot.children) {
                        val autocarros = autocarroSnapshot.getValue(Autocarros::class.java)
                        autocarroArrayList.add(autocarros!!)
                    }
                    autocarroAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"Erro", Toast.LENGTH_LONG).show()
            }
        })
    }
}