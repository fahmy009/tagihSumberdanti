package com.example.tagihsumberdanti.data.model

import com.google.firebase.firestore.DocumentId

data class Pelanggan(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val noHp: String = "",
    val paket: String = "",
    val pppSecret: String = "", // Nama Secret PPPoE di MikroTik
    val tanggalDaftar: String = "",
    val statusAktif: String = "Aktif" // "Aktif" or "Nonaktif"
)
