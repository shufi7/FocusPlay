package com.example.focusplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.focusplay.R
import com.example.focusplay.model.Anak

class AnakAdapter(
    private val listAnak: List<Anak>,
    private val onClickAnak: (Anak) -> Unit,
    private val onLongClickAnak: (Anak) -> Unit
) : RecyclerView.Adapter<AnakAdapter.AnakViewHolder>() {

    inner class AnakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvItemNamaAnak)
        val tvUmur: TextView = itemView.findViewById(R.id.tvItemUmurAnak)

        fun bind(anak: Anak) {
            tvNama.text = anak.nama_anak
            tvUmur.text = "Umur: ${anak.usia} Tahun"

            itemView.setOnClickListener { onClickAnak(anak) }
            itemView.setOnLongClickListener {
                onLongClickAnak(anak)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnakViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anak, parent, false)
        return AnakViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnakViewHolder, position: Int) {
        holder.bind(listAnak[position])
    }

    override fun getItemCount(): Int = listAnak.size
}