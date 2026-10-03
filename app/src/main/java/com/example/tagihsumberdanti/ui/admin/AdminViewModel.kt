package com.example.tagihsumberdanti.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagihsumberdanti.data.model.Paket
import com.example.tagihsumberdanti.data.model.Pelanggan
import com.example.tagihsumberdanti.data.model.Pengaturan
import com.example.tagihsumberdanti.data.model.Tagihan
import com.example.tagihsumberdanti.data.model.User
import com.example.tagihsumberdanti.data.remote.MikrotikService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminViewModel : ViewModel() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _pelangganList = MutableStateFlow<List<Pelanggan>>(emptyList())
    val pelangganList: StateFlow<List<Pelanggan>> = _pelangganList.asStateFlow()

    private val _tagihanList = MutableStateFlow<List<Tagihan>>(emptyList())
    val tagihanList: StateFlow<List<Tagihan>> = _tagihanList.asStateFlow()

    private val _pengaturan = MutableStateFlow(Pengaturan(bulanAktif = "Oktober", tahunAktif = "2026"))
    val pengaturan: StateFlow<Pengaturan> = _pengaturan.asStateFlow()

    private val _paketList = MutableStateFlow<List<Paket>>(emptyList())
    val paketList: StateFlow<List<Paket>> = _paketList.asStateFlow()

    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList.asStateFlow()

    private val _mikrotikSecrets = MutableStateFlow<List<String>>(emptyList())
    val mikrotikSecrets: StateFlow<List<String>> = _mikrotikSecrets.asStateFlow()

    private val _interfaceTraffic = MutableStateFlow(com.example.tagihsumberdanti.data.remote.InterfaceTraffic())
    val interfaceTraffic: StateFlow<com.example.tagihsumberdanti.data.remote.InterfaceTraffic> = _interfaceTraffic.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var pelangganListener: ListenerRegistration? = null
    private var tagihanListener: ListenerRegistration? = null
    private var pengaturanListener: ListenerRegistration? = null
    private var paketListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null

    init {
        listenToPengaturan()
        listenToPelanggan()
        listenToTagihan()
        listenToPaket()
        listenToUsers()
    }

    private fun listenToPengaturan() {
        pengaturanListener = firestore.collection("pengaturan").document("app_config")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val p = snapshot.toObject(Pengaturan::class.java)
                    if (p != null) {
                        _pengaturan.value = p
                        syncMikrotikSecrets(p)
                    }
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

    private fun listenToPaket() {
        paketListener = firestore.collection("paket")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    if (snapshot.isEmpty) {
                        val initialPakets = listOf(
                            Paket(nama = "10 Mbps", nominal = 100000.0),
                            Paket(nama = "20 Mbps", nominal = 150000.0),
                            Paket(nama = "50 Mbps", nominal = 250000.0)
                        )
                        for (p in initialPakets) {
                            val doc = firestore.collection("paket").document()
                            doc.set(p.copy(id = doc.id))
                        }
                    } else {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Paket::class.java)?.copy(id = doc.id)
                        }
                        _paketList.value = list
                    }
                }
            }
    }

    private fun listenToUsers() {
        userListener = firestore.collection("users")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.copy(username = doc.id)
                    }
                    _userList.value = list
                }
            }
    }

    fun syncMikrotikSecrets(config: Pengaturan = _pengaturan.value) {
        viewModelScope.launch {
            if (config.mikrotikHost.isNotEmpty()) {
                val secrets = MikrotikService.fetchPppSecrets(config)
                _mikrotikSecrets.value = secrets
                fetchTrafficData()
            }
        }
    }

    fun fetchTrafficData(interfaceName: String = "ether1") {
        viewModelScope.launch {
            if (_pengaturan.value.mikrotikHost.isNotEmpty()) {
                val traffic = MikrotikService.fetchInterfaceTraffic(_pengaturan.value, interfaceName)
                _interfaceTraffic.value = traffic
            }
        }
    }

    fun savePengaturan(
        bulan: String,
        tahun: String,
        mikrotikHost: String = _pengaturan.value.mikrotikHost,
        mikrotikPort: String = _pengaturan.value.mikrotikPort,
        mikrotikUser: String = _pengaturan.value.mikrotikUser,
        mikrotikPass: String = _pengaturan.value.mikrotikPassword,
        mikrotikEnabled: Boolean = _pengaturan.value.mikrotikEnabled
    ) {
        val config = Pengaturan(
            bulanAktif = bulan,
            tahunAktif = tahun,
            mikrotikHost = mikrotikHost,
            mikrotikPort = mikrotikPort,
            mikrotikUser = mikrotikUser,
            mikrotikPassword = mikrotikPass,
            mikrotikEnabled = mikrotikEnabled
        )
        firestore.collection("pengaturan").document("app_config")
            .set(config)
            .addOnSuccessListener {
                _pengaturan.value = config
                _message.value = "Pengaturan berhasil disimpan!"
                syncMikrotikSecrets(config)
            }
            .addOnFailureListener {
                _message.value = "Gagal menyimpan pengaturan: ${it.message}"
            }
    }

    fun testMikrotikKoneksi() {
        viewModelScope.launch {
            val result = MikrotikService.testConnection(_pengaturan.value)
            result.onSuccess { msg ->
                _message.value = msg
                val updatedConfig = _pengaturan.value.copy(mikrotikEnabled = true)
                _pengaturan.value = updatedConfig
                firestore.collection("pengaturan").document("app_config").set(updatedConfig)
                syncMikrotikSecrets(updatedConfig)
            }.onFailure {
                _message.value = it.message
            }
        }
    }

    fun simpanPaket(paket: Paket) {
        if (paket.nama.trim().isEmpty()) {
            _message.value = "Nama paket tidak boleh kosong!"
            return
        }
        if (paket.nominal <= 0) {
            _message.value = "Harga paket harus lebih besar dari Rp 0!"
            return
        }

        val docRef = if (paket.id.isEmpty()) {
            firestore.collection("paket").document()
        } else {
            firestore.collection("paket").document(paket.id)
        }
        val dataToSave = paket.copy(id = docRef.id)
        docRef.set(dataToSave)
            .addOnSuccessListener {
                _message.value = "Paket internet berhasil disimpan!"
            }
            .addOnFailureListener {
                _message.value = "Gagal menyimpan paket: ${it.message}"
            }
    }

    fun hapusPaket(paketId: String) {
        firestore.collection("paket").document(paketId)
            .delete()
            .addOnSuccessListener {
                _message.value = "Paket berhasil dihapus!"
            }
            .addOnFailureListener {
                _message.value = "Gagal menghapus paket: ${it.message}"
            }
    }

    fun simpanUser(user: User) {
        val cleanUser = user.username.trim()
        val cleanPin = user.pin.trim()

        if (cleanUser.isEmpty()) {
            _message.value = "Username tidak boleh kosong!"
            return
        }
        if (cleanPin.length < 4 || !cleanPin.all { it.isDigit() }) {
            _message.value = "PIN/Password harus berupa minimal 4 digit angka!"
            return
        }

        val userObj = user.copy(username = cleanUser, pin = cleanPin)
        firestore.collection("users").document(cleanUser)
            .set(userObj)
            .addOnSuccessListener {
                _message.value = "Akun $cleanUser (${user.role}) berhasil disimpan!"
            }
            .addOnFailureListener {
                _message.value = "Gagal menyimpan akun: ${it.message}"
            }
    }

    fun hapusUser(username: String) {
        firestore.collection("users").document(username)
            .delete()
            .addOnSuccessListener {
                _message.value = "Akun $username berhasil dihapus!"
            }
            .addOnFailureListener {
                _message.value = "Gagal menghapus akun: ${it.message}"
            }
    }

    fun buatTagihanMassal() {
        viewModelScope.launch {
            val activeMonth = _pengaturan.value.bulanAktif
            val activeYear = _pengaturan.value.tahunAktif
            val activeCustomers = _pelangganList.value.filter { it.statusAktif == "Aktif" }

            if (activeCustomers.isEmpty()) {
                _message.value = "Tidak ada pelanggan aktif untuk dibuatkan tagihan!"
                return@launch
            }

            val batch = firestore.batch()
            var addedCount = 0

            for (p in activeCustomers) {
                val exists = _tagihanList.value.any {
                    it.idPelanggan == p.id &&
                            it.bulan.equals(activeMonth, ignoreCase = true) &&
                            it.tahun.equals(activeYear, ignoreCase = true)
                }
                if (!exists) {
                    val docRef = firestore.collection("tagihan").document()
                    val nominalPaket = _paketList.value.find { it.nama == p.paket }?.nominal ?: 100000.0
                    val newTagihan = Tagihan(
                        id = docRef.id,
                        idPelanggan = p.id,
                        namaPelanggan = p.nama,
                        bulan = activeMonth,
                        tahun = activeYear,
                        nominal = nominalPaket,
                        status = "Belum Bayar",
                        tanggalBayar = "",
                        ditagihOleh = "",
                        keterangan = "Tagihan otomatis $activeMonth $activeYear"
                    )
                    batch.set(docRef, newTagihan)
                    addedCount++
                }
            }

            if (addedCount > 0) {
                batch.commit()
                    .addOnSuccessListener {
                        _message.value = "Berhasil membuat $addedCount tagihan massal untuk $activeMonth $activeYear!"
                    }
                    .addOnFailureListener {
                        _message.value = "Gagal membuat tagihan massal: ${it.message}"
                    }
            } else {
                _message.value = "Tagihan untuk semua pelanggan aktif di $activeMonth $activeYear sudah ada!"
            }
        }
    }

    fun buatTagihanSatuan(
        pelanggan: Pelanggan,
        bulan: String,
        tahun: String,
        nominal: Double
    ) {
        if (bulan.trim().isEmpty() || tahun.trim().isEmpty()) {
            _message.value = "Bulan dan tahun tagihan tidak boleh kosong!"
            return
        }
        if (nominal <= 0) {
            _message.value = "Nominal tagihan harus lebih besar dari Rp 0!"
            return
        }

        val docRef = firestore.collection("tagihan").document()
        val tagihanBaru = Tagihan(
            id = docRef.id,
            idPelanggan = pelanggan.id,
            namaPelanggan = pelanggan.nama,
            bulan = bulan,
            tahun = tahun,
            nominal = nominal,
            status = "Belum Bayar",
            tanggalBayar = "",
            ditagihOleh = "",
            keterangan = "Tagihan satuan $bulan $tahun"
        )
        docRef.set(tagihanBaru)
            .addOnSuccessListener {
                _message.value = "Tagihan satuan untuk ${pelanggan.nama} berhasil dibuat!"
            }
            .addOnFailureListener {
                _message.value = "Gagal membuat tagihan: ${it.message}"
            }
    }

    fun updateStatusTagihan(tagihanId: String, newStatus: String, ditagihOleh: String = "Admin") {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        firestore.collection("tagihan").document(tagihanId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "tanggalBayar" to if (newStatus == "Lunas" || newStatus == "Double") dateStr else "",
                    "ditagihOleh" to ditagihOleh
                )
            )
            .addOnSuccessListener {
                _message.value = "Status tagihan diperbarui ke $newStatus"

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
                _message.value = "Gagal memperbarui tagihan: ${it.message}"
            }
    }

    fun hapusTagihan(tagihanId: String) {
        firestore.collection("tagihan").document(tagihanId)
            .delete()
            .addOnSuccessListener {
                _message.value = "Tagihan berhasil dihapus"
            }
            .addOnFailureListener {
                _message.value = "Gagal menghapus tagihan: ${it.message}"
            }
    }

    fun simpanPelanggan(pelanggan: Pelanggan) {
        val cleanNama = pelanggan.nama.trim()
        val cleanAlamat = pelanggan.alamat.trim()
        val rawHp = pelanggan.noHp.trim()
        val cleanDigits = rawHp.replace("[^0-9]".toRegex(), "")

        if (cleanNama.isEmpty()) {
            _message.value = "Nama pelanggan tidak boleh kosong!"
            return
        }
        if (cleanAlamat.isEmpty()) {
            _message.value = "Alamat pelanggan tidak boleh kosong!"
            return
        }

        var formattedHp = "-"
        if (cleanDigits.isNotEmpty()) {
            if (cleanDigits.length < 10) {
                _message.value = "Jika diisi, nomor WhatsApp harus berformat angka valid (minimal 10 digit)!"
                return
            }
            formattedHp = cleanDigits
            if (formattedHp.startsWith("0")) {
                formattedHp = "62" + formattedHp.substring(1)
            }
        }

        val docRef = if (pelanggan.id.isEmpty()) {
            firestore.collection("pelanggan").document()
        } else {
            firestore.collection("pelanggan").document(pelanggan.id)
        }

        val dataToSave = pelanggan.copy(
            id = docRef.id,
            nama = cleanNama,
            alamat = cleanAlamat,
            noHp = formattedHp
        )

        docRef.set(dataToSave)
            .addOnSuccessListener {
                _message.value = "Data pelanggan berhasil disimpan"
            }
            .addOnFailureListener {
                _message.value = "Gagal menyimpan pelanggan: ${it.message}"
            }
    }

    fun hapusPelanggan(id: String) {
        firestore.collection("pelanggan").document(id)
            .delete()
            .addOnSuccessListener {
                _message.value = "Pelanggan berhasil dihapus"
            }
            .addOnFailureListener {
                _message.value = "Gagal menghapus pelanggan: ${it.message}"
            }
    }

    fun clearMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pelangganListener?.remove()
        tagihanListener?.remove()
        pengaturanListener?.remove()
        paketListener?.remove()
        userListener?.remove()
    }
}

fun Double.toRupiah(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(this).replace(",00", "")
}
