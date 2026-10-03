package com.example.tagihsumberdanti.data.model

import com.google.firebase.firestore.DocumentId

data class Paket(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val nominal: Double = 0.0
)
