package com.example.tagihsumberdanti.ui.penagih

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihsumberdanti.data.model.Pelanggan
import com.example.tagihsumberdanti.data.model.Pengaturan
import com.example.tagihsumberdanti.data.model.Tagihan
import com.example.tagihsumberdanti.data.remote.MikrotikService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TukangTagihViewModel : ViewModel() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _tagihanList = MutableStateFlow<List<Tagihan>>(emptyList())
    val tagihanList: StateFlow<List<Tagihan>> = _tagihanList.asStateFlow()

    private val _pelangganList = MutableStateFlow<List<Pelanggan>>(emptyList())
    val pelangganList: StateFlow<List<Pelanggan>> = _pelangganList.asStateFlow()

    private val _pengaturan = MutableStateFlow(Pengaturan(bulanAktif = "Oktober", tahunAktif = "2026"))
    val pengaturan: StateFlow<Pengaturan> = _pengaturan.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var tagihanListener: ListenerRegistration? = null
    private var pelangganListener: ListenerRegistration? = null
    private var pengaturanListener: ListenerRegistration? = null

    init {
        listenToPengaturan()
        listenToTagihan()
        listenToPelanggan()
    }

    private fun listenToPengaturan() {
        pengaturanListener = firestore.collection("pengaturan").document("app_config")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val p = snapshot.toObject(Pengaturan::class.java)
                    if (p != null) {
                        _pengaturan.value = p
                    }
                }
            }
    }

    private fun listenToTagihan() {
        tagihanListener = firestore.collection("tagihan")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Tagihan::class.java)?.copy(id = doc.id)
                    }
                    _tagihanList.value = list
                }
            }
    }

    private fun listenToPelanggan() {
        pelangganListener = firestore.collection("pelanggan")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Pelanggan::class.java)?.copy(id = doc.id)
                    }
                    _pelangganList.value = list
                }
            }
    }

    fun updateStatusTagihan(
        tagihanId: String,
        newStatus: String,
        collectorName: String
    ) {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val updateMap = mutableMapOf<String, Any>(
            "status" to newStatus,
            "ditagihOleh" to collectorName
        )
        if (newStatus == "Lunas" || newStatus == "Double") {
            updateMap["tanggalBayar"] = dateStr
        }

        firestore.collection("tagihan").document(tagihanId)
            .update(updateMap)
            .addOnSuccessListener {
                _message.value = "Berhasil memproses pembayaran: $newStatus"

                // MikroTik Auto Buka Isolir / Isolir
                val tagihan = _tagihanList.value.find { it.id == tagihanId }
                val pppSecret = _pelangganList.value.find { it.id == tagihan?.idPelanggan }?.pppSecret ?: ""
                if (pppSecret.isNotEmpty() && _pengaturan.value.mikrotikEnabled) {
                    viewModelScope.launch {
                        val isDisable = (newStatus == "Menunggak")
                        val result = MikrotikService.setPppSecretDisabled(_pengaturan.value, pppSecret, isDisable)
                        result.onSuccess { msg -> _message.value = msg }
                    }
                }
            }
            .addOnFailureListener {
                _message.value = "Gagal memproses pembayaran: ${it.message}"
            }
    }

    fun updateOwnPin(username: String, newPin: String) {
        val cleanPin = newPin.trim()
        if (cleanPin.length < 4 || !cleanPin.all { it.isDigit() }) {
            _message.value = "PIN baru harus berupa minimal 4 digit angka!"
            return
        }

        firestore.collection("users").document(username)
            .update("pin", cleanPin)
            .addOnSuccessListener {
                _message.value = "PIN berhasil diperbarui!"
            }
            .addOnFailureListener {
                _message.value = "Gagal mengupdate PIN: ${it.message}"
            }
    }

    fun clearMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tagihanListener?.remove()
        pelangganListener?.remove()
        pengaturanListener?.remove()
    }
}
