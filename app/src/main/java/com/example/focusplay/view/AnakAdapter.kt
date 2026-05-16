package com.example.focusplay.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.focusplay.R
import com.example.focusplay.model.Anak

class AnakAdapter(
    private val listAnak: List<Anak>,
    private val onLongClickAnak: (Anak) -> Unit // Jalur komunikasi ke Dashboard
) : RecyclerView.Adapter<AnakAdapter.AnakViewHolder>() {

    class AnakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaAnakItem)
        val tvUsia: TextView = itemView.findViewById(R.id.tvUsiaAnakItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnakViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anak, parent, false)
        return AnakViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnakViewHolder, position: Int) {
        val anak = listAnak[position]
        holder.tvNama.text = anak.nama_anak
        holder.tvUsia.text = "Usia: ${anak.usia} Tahun"

        // Sensor ketika kotak nama ditekan lama
        holder.itemView.setOnLongClickListener {
            onLongClickAnak(anak)
            true // Mengembalikan 'true' artinya aksi tahan klik berhasil dieksekusi
        }
    }

    override fun getItemCount(): Int = listAnak.size
}