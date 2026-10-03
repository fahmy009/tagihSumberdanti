package com.example.tagihsumberdanti.data.model

import com.google.firebase.firestore.DocumentId

data class Tagihan(
    @DocumentId
    val id: String = "",
    val idPelanggan: String = "",
    val namaPelanggan: String = "",
    val bulan: String = "",
    val tahun: String = "",
    val nominal: Double = 0.0,
    val status: String = "Belum Bayar", // "Belum Bayar", "Lunas", "Menunggak", "Double", "Double Hutang"
    val tanggalBayar: String = "",
    val ditagihOleh: String = "",
    val keterangan: String = ""
)
