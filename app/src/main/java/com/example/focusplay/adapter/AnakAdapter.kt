package com.example.focusplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.focusplay.R
import com.example.focusplay.model.Anak
import com.example.focusplay.utils.AvatarHelper

/**
 * Adapter untuk menghubungkan data Anak dengan tampilan item_anak di RecyclerView.
 *
 * Klik dan tekan lama diteruskan melalui callback ke Activity yang memakai adapter.
 */
class AnakAdapter(
    // Daftar data yang akan ditampilkan oleh RecyclerView.
    private val listAnak: List<Anak>,
    // Callback saat satu kartu anak ditekan biasa.
    private val onClickAnak: (Anak) -> Unit,
    // Callback saat satu kartu anak ditekan lama.
    private val onLongClickAnak: (Anak) -> Unit
) : RecyclerView.Adapter<AnakAdapter.AnakViewHolder>() {

    // ==================== BAGIAN VIEWHOLDER DAN ISI KARTU ====================
    inner class AnakViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Menghubungkan ImageView avatar dari item_anak.xml.
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatarAnak)
        // Menghubungkan TextView nama anak.
        val tvNama: TextView = itemView.findViewById(R.id.tvItemNamaAnak)
        // Menghubungkan TextView umur anak.
        val tvUmur: TextView = itemView.findViewById(R.id.tvItemUmurAnak)

        fun bind(anak: Anak) {
            // Mengubah ID avatar berbentuk teks menjadi drawable melalui AvatarHelper.
            imgAvatar.setImageResource(AvatarHelper.getAvatarResource(anak.avatar))
            // Memberi deskripsi avatar untuk aksesibilitas.
            imgAvatar.contentDescription = "Avatar ${anak.nama_anak}"
            // Menampilkan nama dari model Anak.
            tvNama.text = anak.nama_anak
            // Menampilkan usia dengan format teks.
            tvUmur.text = "Umur: ${anak.usia} Tahun"

            // Meneruskan data anak yang diklik ke Activity.
            itemView.setOnClickListener { onClickAnak(anak) }
            // Meneruskan data anak yang ditekan lama ke Activity.
            itemView.setOnLongClickListener {
                onLongClickAnak(anak)
                true
            }
        }
    }

    // ==================== BAGIAN PEMBUATAN ITEM ====================
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnakViewHolder {
        // Tampilan setiap kartu anak diambil dari layout item_anak.xml.
        // Membuat View baru dari file item_anak.xml.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anak, parent, false)
        // Membungkus View menjadi AnakViewHolder.
        return AnakViewHolder(view)
    }

    // ==================== BAGIAN DATA ADAPTER ====================
    override fun onBindViewHolder(holder: AnakViewHolder, position: Int) {
        // Mengisi ViewHolder menggunakan data sesuai posisi RecyclerView.
        holder.bind(listAnak[position])
    }

    // Memberi tahu RecyclerView jumlah item yang perlu ditampilkan.
    override fun getItemCount(): Int = listAnak.size
}
