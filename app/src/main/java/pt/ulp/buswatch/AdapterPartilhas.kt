package pt.ulp.easybus2_testes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AdapterPartilhas(
    private val partilhasList : ArrayList<Partilhas>,
    val id: String, val username: String,
    val listener: OnItemClickListener) : RecyclerView.Adapter<AdapterPartilhas.MyViewHolder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var idPartilhaa : String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.partilhas_item, parent,false)
        return MyViewHolder(itemView)
    }

    var onItemClick: ((Partilhas) -> Unit)? = null

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = partilhasList[position]
        holder.empresa.text = currentitem.empresa
        holder.saida.text = currentitem.saida
        holder.chegada.text = currentitem.chegada
        holder.via.text = currentitem.via
        holder.horaPartida.text = currentitem.hora_partida
        holder.nome.text = currentitem.user
        holder.id.text = currentitem.id
        holder.alert.text = currentitem.alert

        auth = Firebase.auth
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentitem)
            val idPartilha = currentitem.id
            if (idPartilha != null) {
                idPartilhaa = idPartilha
            }
        }
    }

    override fun getItemCount(): Int {
        return partilhasList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener{
        val empresa : TextView = itemView.findViewById(R.id.textviewEmpresa)
        val saida : TextView = itemView.findViewById(R.id.textviewSaida)
        val chegada : TextView = itemView.findViewById(R.id.textviewChegada)
        val via : TextView = itemView.findViewById(R.id.textviewVia)
        val horaPartida : TextView = itemView.findViewById(R.id.textviewHoraPartida)
        val nome: TextView = itemView.findViewById(R.id.textviewUsernamePartilha)
        val id: TextView = itemView.findViewById(R.id.textviewID)
        val alert: TextView = itemView.findViewById(R.id.textviewAlert)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position,idPartilhaa)
            }
        }
    }
    interface OnItemClickListener{
        fun onItemClick(position: Int, idPartilhaa: String)
    }
}