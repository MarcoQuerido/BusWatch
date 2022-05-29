package pt.ulp.easybus2_testes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class AdapterFavoritos(
    private val favoritosList : ArrayList<Favoritos>,
    val id: String, val username: String,
    val listener: OnItemClickListener) : RecyclerView.Adapter<AdapterFavoritos.MyViewHolder>() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.autocarros_item2, parent,false)
        return MyViewHolder(itemView)
    }

    var onItemClick: ((Favoritos) -> Unit)? = null

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = favoritosList[position]
        holder.empresa.text = currentitem.empresa
        holder.saida.text = currentitem.saida
        holder.chegada.text = currentitem.chegada
        holder.via.text = currentitem.via
        holder.horaPartida.text = currentitem.hora_partida

        auth = Firebase.auth
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentitem)
            val dados = Partilhas(currentitem.empresa, currentitem.saida, currentitem.chegada,
                currentitem.via, currentitem.hora_partida,0.0,0.0,0.0,0.0,username,id,"")
            Log.e("dados", dados.toString())
            sendToPartilha(dados,id)
        }

        holder.buttonFav.setOnClickListener {
            deleteFavorite(id)
            holder.buttonFav.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return favoritosList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener{
        val empresa : TextView = itemView.findViewById(R.id.textviewEmpresa)
        val saida : TextView = itemView.findViewById(R.id.textviewSaida)
        val chegada : TextView = itemView.findViewById(R.id.textviewChegada)
        val via : TextView = itemView.findViewById(R.id.textviewVia)
        val horaPartida : TextView = itemView.findViewById(R.id.textviewHoraPartida)
        val buttonFav: Button = itemView.findViewById(R.id.buttonRemoveFavorite)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    private fun deleteFavorite(id: String) {
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Utilizadores")
        val key = database.getReference("Favoritos").push().key
        if (key != null) {
            reference.child(id).child("Favoritos").child(key).removeValue().addOnSuccessListener {
                // Write was successful!
                // ...
                Log.e("Base de Dados", "Passou para a base de dados")
            }
                .addOnFailureListener {
                    // Write failed
                    // ...
                    Log.e("Base de Dados", "Não passou para a base de dados")
                }
        }
    }

    private fun sendToPartilha(partilha: Partilhas, id: String) {
        val database = FirebaseDatabase.getInstance("https://buswatch-90a50-default-rtdb.firebaseio.com/")
        val reference = database.getReference("Partilhas")
        Log.e("Base de Dados", partilha.toString())
        reference.child(id).setValue(partilha).addOnSuccessListener {
            // Write was successful!
            // ...
            Log.e("Base de Dados", "Passou para a base de dados")
        }
            .addOnFailureListener {
                // Write failed
                // ...
                Log.e("Base de Dados", "Não passou para a base de dados")
            }
    }
}