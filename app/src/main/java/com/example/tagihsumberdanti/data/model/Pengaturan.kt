package com.example.tagihsumberdanti.data.model

data class Pengaturan(
    val bulanAktif: String = "",
    val tahunAktif: String = "",
    val mikrotikHost: String = "",
    val mikrotikPort: String = "80",
    val mikrotikUser: String = "",
    val mikrotikPassword: String = "",
    val mikrotikEnabled: Boolean = false
)
