package com.example.tagihsumberdanti.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val username: String = "",
    val pin: String = "",
    val role: String = "" // "Admin" or "Tukang Tagih"
)
