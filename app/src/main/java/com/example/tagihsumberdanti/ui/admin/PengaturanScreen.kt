package com.example.tagihsumberdanti.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagihsumberdanti.data.local.SessionManager
import com.example.tagihsumberdanti.data.model.Paket
import com.example.tagihsumberdanti.data.model.User
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.components.GlassInput
import com.example.tagihsumberdanti.ui.theme.GreenPrimary
import com.example.tagihsumberdanti.ui.theme.Rose
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val pengaturan by viewModel.pengaturan.collectAsState()
    val paketList by viewModel.paketList.collectAsState()
    val userList by viewModel.userList.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Periode, 1 = MikroTik, 2 = Paket, 3 = Akun, 4 = Biometrik

    val daftarBulan = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val daftarTahun = listOf("2025", "2026", "2027", "2028")

    var selectedBulan by remember(pengaturan.bulanAktif) { mutableStateOf(pengaturan.bulanAktif.ifEmpty { "Oktober" }) }
    var selectedTahun by remember(pengaturan.tahunAktif) { mutableStateOf(pengaturan.tahunAktif.ifEmpty { "2026" }) }

    var expandedBulan by remember { mutableStateOf(false) }
    var expandedTahun by remember { mutableStateOf(false) }

    // MikroTik Form States
    var mkHost by remember(pengaturan.mikrotikHost) { mutableStateOf(pengaturan.mikrotikHost) }
    var mkPort by remember(pengaturan.mikrotikPort) { mutableStateOf(pengaturan.mikrotikPort) }
    var mkUser by remember(pengaturan.mikrotikUser) { mutableStateOf(pengaturan.mikrotikUser) }
    var mkPass by remember(pengaturan.mikrotikPassword) { mutableStateOf(pengaturan.mikrotikPassword) }
    var mkEnabled by remember(pengaturan.mikrotikEnabled) { mutableStateOf(pengaturan.mikrotikEnabled) }

    // Biometric Toggle State
    var bioEnabled by remember { mutableStateOf(sessionManager.isBiometricEnabled()) }

    // Modal Bottom Sheet State for User & Paket
    var selectedPaket by remember { mutableStateOf<Paket?>(null) }
    var showPaketSheet by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Pengaturan Sistem",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 5 Sub-Tabs Navigation Header
        val subTabTitles = listOf("📅 Periode", "🔌 MikroTik", "📶 Paket", "👤 Akun", "🔐 Biometrik")
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.White,
            contentColor = TextDark,
            edgePadding = 0.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 16.dp)
        ) {
            subTabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (activeSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeSubTab == index) GreenPrimary else SlateText,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (activeSubTab) {
            0 -> {
                // Tab 1: Periode Penagihan Aktif
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "Periode Penagihan Aktif",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Pilih bulan & tahun aktif untuk meminimalisir kesalahan pengetikan.",
                            fontSize = 13.sp,
                            color = SlateText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        Text(text = "Bulan Aktif:", color = SlateText, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedBulan,
                            onExpandedChange = { expandedBulan = !expandedBulan }
                        ) {
                            OutlinedTextField(
                                value = selectedBulan,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBulan) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GreenPrimary) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedIndicatorColor = GreenPrimary,
                                    unfocusedIndicatorColor = Color(0xFFCBD5E1)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedBulan,
                                onDismissRequest = { expandedBulan = false }
                            ) {
                                daftarBulan.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item, color = TextDark) },
                                        onClick = {
                                            selectedBulan = item
                                            expandedBulan = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Tahun Aktif:", color = SlateText, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = expandedTahun,
                            onExpandedChange = { expandedTahun = !expandedTahun }
                        ) {
                            OutlinedTextField(
                                value = selectedTahun,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTahun) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GreenPrimary) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedIndicatorColor = GreenPrimary,
                                    unfocusedIndicatorColor = Color(0xFFCBD5E1)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedTahun,
                                onDismissRequest = { expandedTahun = false }
                            ) {
                                daftarTahun.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item, color = TextDark) },
                                        onClick = {
                                            selectedTahun = item
                                            expandedTahun = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.savePengaturan(
                                    bulan = selectedBulan,
                                    tahun = selectedTahun,
                                    mikrotikHost = mkHost,
                                    mikrotikPort = mkPort,
                                    mikrotikUser = mkUser,
                                    mikrotikPass = mkPass,
                                    mikrotikEnabled = mkEnabled
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text(text = "Simpan Periode Aktif", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            1 -> {
                // Tab 2: Konfigurasi MikroTik
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Router, contentDescription = null, tint = GreenPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Router MikroTik",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }

                            Switch(
                                checked = mkEnabled,
                                onCheckedChange = { mkEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary)
                            )
                        }

                        Text(
                            text = "Otomatisasi buka isolir saat lunas & isolir saat menunggak via Socket API Port 8728.",
                            fontSize = 13.sp,
                            color = SlateText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        GlassInput(
                            value = mkHost,
                            onValueChange = { mkHost = it },
                            placeholder = "sg-7.hostddns.us",
                            label = "IP Public / Host VPN MikroTik"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassInput(
                                value = mkPort,
                                onValueChange = { mkPort = it },
                                placeholder = "8728",
                                label = "Port Socket API",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            GlassInput(
                                value = mkUser,
                                onValueChange = { mkUser = it },
                                placeholder = "admin",
                                label = "User Router",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GlassInput(
                            value = mkPass,
                            onValueChange = { mkPass = it },
                            placeholder = "Password Router",
                            label = "Password Router",
                            visualTransformation = PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.testMikrotikKoneksi() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Tes Koneksi", color = TextDark, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.savePengaturan(
                                        bulan = selectedBulan,
                                        tahun = selectedTahun,
                                        mikrotikHost = mkHost,
                                        mikrotikPort = mkPort,
                                        mikrotikUser = mkUser,
                                        mikrotikPass = mkPass,
                                        mikrotikEnabled = mkEnabled
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Text(text = "Simpan MikroTik", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 3: Kelola Paket Internet
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kelola Paket Internet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Button(
                                onClick = {
                                    selectedPaket = Paket()
                                    showPaketSheet = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Tambah Paket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "Daftar tarif paket disimpan di Firestore koleksi 'paket'.",
                            fontSize = 13.sp,
                            color = SlateText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        if (paketList.isEmpty()) {
                            Text(text = "Belum ada paket internet", fontSize = 13.sp, color = SlateText)
                        } else {
                            paketList.forEach { pkt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = GreenPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = pkt.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                            Text(text = pkt.nominal.toRupiah(), fontSize = 13.sp, color = SlateText)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            selectedPaket = pkt
                                            showPaketSheet = true
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextDark)
                                        }
                                        IconButton(onClick = { viewModel.hapusPaket(pkt.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Rose)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 4: Kelola Akun User
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kelola Akun Pengguna",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Button(
                                onClick = {
                                    selectedUser = User()
                                    showUserSheet = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Buat Akun", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "Daftar akun Admin & Tukang Tagih di koleksi 'users'.",
                            fontSize = 13.sp,
                            color = SlateText,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        if (userList.isEmpty()) {
                            Text(text = "Belum ada akun terdaftar", fontSize = 13.sp, color = SlateText)
                        } else {
                            userList.forEach { usr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GreenPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = usr.username, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                            Text(text = "Role: ${usr.role} | PIN: ${usr.pin}", fontSize = 12.sp, color = SlateText)
                                        }
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            selectedUser = usr
                                            showUserSheet = true
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit PIN", tint = TextDark)
                                        }
                                        IconButton(onClick = { viewModel.hapusUser(usr.username) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Rose)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Tab 5: Keamanan & Biometrik
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Login Biometrik / Sidik Jari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(text = "Gunakan sensor Fingerprint HP untuk masuk tanpa PIN", fontSize = 12.sp, color = SlateText)
                                }
                            }

                            Switch(
                                checked = bioEnabled,
                                onCheckedChange = {
                                    bioEnabled = it
                                    sessionManager.setBiometricEnabled(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)
        ) {
            Text(text = "Keluar (Logout)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tagih Sumberdanti v1.0\nDeveloped with ❤️ by Muhammad Fahmy",
            fontSize = 12.sp,
            color = SlateText,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Modal Sheet Paket
    if (showPaketSheet && selectedPaket != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showPaketSheet = false
                selectedPaket = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PaketFormBottomSheet(
                paket = selectedPaket!!,
                onSave = { updated ->
                    viewModel.simpanPaket(updated)
                    showPaketSheet = false
                    selectedPaket = null
                },
                onCancel = {
                    showPaketSheet = false
                    selectedPaket = null
                }
            )
        }
    }

    // Modal Sheet User
    if (showUserSheet && selectedUser != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showUserSheet = false
                selectedUser = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            UserFormBottomSheet(
                user = selectedUser!!,
                onSave = { updated ->
                    viewModel.simpanUser(updated)
                    showUserSheet = false
                    selectedUser = null
                },
                onCancel = {
                    showUserSheet = false
                    selectedUser = null
                }
            )
        }
    }
}

@Composable
fun PaketFormBottomSheet(
    paket: Paket,
    onSave: (Paket) -> Unit,
    onCancel: () -> Unit
) {
    var nama by remember { mutableStateOf(paket.nama) }
    var nominalText by remember { mutableStateOf(if (paket.nominal > 0) paket.nominal.toInt().toString() else "100000") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = if (paket.id.isEmpty()) "Tambah Paket Internet" else "Edit Paket Internet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassInput(value = nama, onValueChange = { nama = it }, placeholder = "Contoh: 10 Mbps", label = "Nama Paket")
        Spacer(modifier = Modifier.height(12.dp))
        GlassInput(
            value = nominalText,
            onValueChange = { nominalText = it },
            placeholder = "Contoh: 100000",
            label = "Harga (Rp)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text(text = "Batal", color = TextDark)
            }
            Button(
                onClick = {
                    val nom = nominalText.toDoubleOrNull() ?: 100000.0
                    onSave(paket.copy(nama = nama, nominal = nom))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Simpan Paket", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UserFormBottomSheet(
    user: User,
    onSave: (User) -> Unit,
    onCancel: () -> Unit
) {
    val isEditMode = user.username.isNotEmpty()
    var username by remember { mutableStateOf(user.username) }
    var pin by remember { mutableStateOf(user.pin) }
    var role by remember { mutableStateOf(user.role.ifEmpty { "Tukang Tagih" }) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = if (isEditMode) "Edit PIN Akun (${user.username})" else "Buat Akun Pengguna Baru",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassInput(
            value = username,
            onValueChange = { if (!isEditMode) username = it },
            placeholder = "Username (contoh: penagih2)",
            label = if (isEditMode) "Username (Tidak Dapat Diubah)" else "Username Baru",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassInput(
            value = pin,
            onValueChange = { pin = it },
            placeholder = "PIN / Password",
            label = "PIN Login (Min. 4 Angka)",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Role Akun:", color = SlateText, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = role == "Admin",
                onClick = { role = "Admin" },
                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
            )
            Text(text = "Admin", color = TextDark, modifier = Modifier.padding(end = 16.dp))

            RadioButton(
                selected = role == "Tukang Tagih",
                onClick = { role = "Tukang Tagih" },
                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
            )
            Text(text = "Tukang Tagih", color = TextDark)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text(text = "Batal", color = TextDark)
            }
            Button(
                onClick = { onSave(User(username = username, pin = pin, role = role)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Simpan Akun", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
