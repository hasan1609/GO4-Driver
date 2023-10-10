package com.g4s.go4_driver.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.g4s.go4_driver.R
import com.g4s.go4_driver.model.ProdukOrderModel
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class ItemLogOrderRestoAdapter (
    private val listData :MutableList<ProdukOrderModel>,
    private val context: Context
) : RecyclerView.Adapter<ItemLogOrderRestoAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return listData.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        var foto: ImageView
        var nama: TextView
        var harga: TextView
        var keterangan: TextView
        var total: TextView

        init {
            foto = view.findViewById(R.id.foto)
            nama = view.findViewById(R.id.txt_nama)
            harga = view.findViewById(R.id.txt_harga)
            total = view.findViewById(R.id.txt_total)
            keterangan = view.findViewById(R.id.txt_ket)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_produk_log_order, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listItem = listData[position]
        val urlImage = context.getString(R.string.urlImage)
        val foto = listItem.produk!!.fotoProduk.toString()
        var def = "/public/images/no_image.png"
        val jml = listItem.jumlah.toString()

        val hargax = listItem.produk.harga!!.toDoubleOrNull() ?: 0.0
        val totalx = listItem.total!!.toDoubleOrNull() ?: 0.0
        val formatter = DecimalFormat.getCurrencyInstance() as DecimalFormat
        val symbols = formatter.decimalFormatSymbols
        symbols.currencySymbol = "Rp. "
        formatter.decimalFormatSymbols = symbols

        val totals = formatter.format(totalx)
        val hargas = formatter.format(hargax)

        holder.nama.text = listItem.produk.namaProduk.toString().toUpperCase()
        holder.harga.text = "$hargas x $jml"
        holder.total.text = totals

        if (listItem.catatan == null) {
            holder.keterangan.visibility = View.GONE
        } else {
            holder.keterangan.visibility = View.VISIBLE
            holder.keterangan.text = listItem.catatan.toString()
        }
        if (listItem.produk.fotoProduk  != null) {
            Picasso.get()
                .load(urlImage + foto)
                .into(holder.foto)
        } else {
            Picasso.get()
                .load(urlImage + def)
                .into(holder.foto)
        }
    }
}