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

class ActivityVerLocations : AppCompatActivity(), AdapterPartilhas.OnItemClickListener{
    private lateinit var dbref : DatabaseReference
    private lateinit var partilhasRecyclerview : RecyclerView
    private lateinit var partilhasArrayList : ArrayList<Partilhas>
    private lateinit var partilhasAdapter: AdapterPartilhas
    private lateinit var idPartilhaa: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partilha_list)

        val username = intent?.extras?.get("Username") as String
        val id = intent?.extras?.get("ID") as String
        Log.e("Username para partilha", id)

        findViewById<Button>(R.id.button_backToMS_Partilhas).setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("Username",username)
            intent.putExtra("ID",id)
            startActivity(intent)
            finish()
        }

        partilhasRecyclerview = findViewById(R.id.partilhasList)
        // Set the layoutManager that this RecyclerView will use
        partilhasRecyclerview.layoutManager = LinearLayoutManager(this)
        partilhasRecyclerview.setHasFixedSize(true)

        partilhasArrayList = arrayListOf()
        idPartilhaa=""
        getData()
        // Adapter class is initialized and list is passed in the params

        Log.e("List",partilhasArrayList.toString())
        partilhasAdapter = AdapterPartilhas(partilhasArrayList,id,username,this)
        // Adapter instance is set to the recyclerView to inflate the items
        partilhasRecyclerview.adapter = partilhasAdapter

        partilhasAdapter.onItemClick = {
            //partilhasRecyclerview.findViewHolderForAdapterPosition()

            Toast.makeText(this@ActivityVerLocations,"Item clicado ", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ActivityMapPartilha::class.java)
            Log.e("IDPartilha----", it.id.toString())
            intent.putExtra("ID",it.id ) //idPartilhaa)
            intent.putExtra("Username",username)
            //val idPartilha = ID(id)
            //intent.putExtra("ID partilha", pid)
            startActivity(intent)
            finish()
        }
    }

    override fun onItemClick(position: Int, idPartilhaa: String){
        Toast.makeText(this@ActivityVerLocations,"Item $position clicado", Toast.LENGTH_SHORT).show()
    }

    private fun getData() {
        dbref = FirebaseDatabase.getInstance().getReference("Partilhas")
        dbref.addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (partilhasSnapshot in snapshot.children) {
                        val partilhas = partilhasSnapshot.getValue(Partilhas::class.java)
                        partilhasArrayList.add(partilhas!!)
                    }
                    partilhasAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"Erro", Toast.LENGTH_LONG).show()
            }
        })
    }
}