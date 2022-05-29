package pt.ulp.easybus2_testes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterVerLocation(private val partilhasList : ArrayList<Partilhas>,
        private val listener: OnItemClickListener)
    : RecyclerView.Adapter<AdapterVerLocation.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.partilhas_item, parent,false)

        Log.e("List2",partilhasList.toString())
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = partilhasList[position]
        holder.empresa.text = currentitem.empresa
        holder.saida.text = currentitem.saida
        holder.chegada.text = currentitem.chegada
        holder.via.text = currentitem.via
        holder.horaPartida.text = currentitem.hora_partida
        holder.nome.text = currentitem.user
        holder.alert.text = currentitem.alert
    }

    override fun getItemCount(): Int {
        return partilhasList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val empresa : TextView = itemView.findViewById(R.id.textviewEmpresa)
        val saida : TextView = itemView.findViewById(R.id.textviewSaida)
        val chegada : TextView = itemView.findViewById(R.id.textviewChegada)
        val via : TextView = itemView.findViewById(R.id.textviewVia)
        val horaPartida : TextView = itemView.findViewById(R.id.textviewHoraPartida)
        val nome: TextView = itemView.findViewById(R.id.textviewUsernamePartilha)
        val alert: TextView = itemView.findViewById(R.id.textviewAlert)

        init {
            itemView.setOnClickListener {  }
        }
    }

    interface OnItemClickListener{
        fun onItemClick()
    }
}