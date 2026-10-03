package com.example.tagihsumberdanti.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagihsumberdanti.data.model.Pelanggan
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.components.GlassInput
import com.example.tagihsumberdanti.ui.theme.GreenPrimary
import com.example.tagihsumberdanti.ui.theme.LeafGreen
import com.example.tagihsumberdanti.ui.theme.LightBackground
import com.example.tagihsumberdanti.ui.theme.Rose
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PelangganScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val pelangganList by viewModel.pelangganList.collectAsState()
    val paketList by viewModel.paketList.collectAsState()
    val mikrotikSecrets by viewModel.mikrotikSecrets.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    var selectedPelanggan by remember { mutableStateOf<Pelanggan?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredList = pelangganList.filter {
        it.nama.contains(searchQuery, ignoreCase = true) ||
                it.alamat.contains(searchQuery, ignoreCase = true) ||
                it.noHp.contains(searchQuery, ignoreCase = true) ||
                it.pppSecret.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val defaultPaket = paketList.firstOrNull()?.nama ?: "10 Mbps"
                    selectedPelanggan = Pelanggan(statusAktif = "Aktif", paket = defaultPaket)
                    showBottomSheet = true
                },
                containerColor = LeafGreen,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Pelanggan")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Daftar Pelanggan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            GlassInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Cari nama, alamat, atau secret MikroTik...",
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = LeafGreen)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data pelanggan",
                        color = SlateText,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        PelangganCard(
                            pelanggan = item,
                            onEditClick = {
                                selectedPelanggan = item
                                showBottomSheet = true
                            },
                            onDeleteClick = {
                                viewModel.hapusPelanggan(item.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedPelanggan != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                selectedPelanggan = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PelangganFormBottomSheet(
                pelanggan = selectedPelanggan!!,
                paketList = paketList.map { it.nama },
                mikrotikSecrets = mikrotikSecrets,
                onSave = { updated ->
                    viewModel.simpanPelanggan(updated)
                    showBottomSheet = false
                    selectedPelanggan = null
                },
                onCancel = {
                    showBottomSheet = false
                    selectedPelanggan = null
                }
            )
        }
    }
}

@Composable
fun PelangganCard(
    pelanggan: Pelanggan,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = LeafGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pelanggan.nama,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // Status Chip
                val isAktif = pelanggan.statusAktif == "Aktif"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isAktif) LeafGreen.copy(alpha = 0.15f) else Rose.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = pelanggan.statusAktif,
                        color = if (isAktif) LeafGreen else Rose,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Alamat: ${pelanggan.alamat}", fontSize = 14.sp, color = SlateText)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SlateText, modifier = Modifier.padding(end = 4.dp))
                Text(text = pelanggan.noHp, fontSize = 14.sp, color = SlateText)
            }
            Text(text = "Paket: ${pelanggan.paket}", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))

            if (pelanggan.pppSecret.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(imageVector = Icons.Default.Router, contentDescription = null, tint = GreenPrimary, modifier = Modifier.padding(end = 4.dp))
                    Text(text = "MikroTik Secret: ${pelanggan.pppSecret}", fontSize = 13.sp, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = TextDark)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Rose)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PelangganFormBottomSheet(
    pelanggan: Pelanggan,
    paketList: List<String>,
    mikrotikSecrets: List<String>,
    onSave: (Pelanggan) -> Unit,
    onCancel: () -> Unit
) {
    var nama by remember { mutableStateOf(pelanggan.nama) }
    var alamat by remember { mutableStateOf(pelanggan.alamat) }
    var noHp by remember { mutableStateOf(pelanggan.noHp) }
    var pppSecret by remember { mutableStateOf(pelanggan.pppSecret) }

    // Uses packages configured by Admin in Pengaturan -> Kelola Paket Internet
    val availablePakets = if (paketList.isNotEmpty()) paketList else listOf("10 Mbps", "20 Mbps", "50 Mbps")
    val currentPaket = if (availablePakets.contains(pelanggan.paket)) pelanggan.paket else availablePakets.first()
    var paket by remember { mutableStateOf(currentPaket) }
    var statusAktif by remember { mutableStateOf(pelanggan.statusAktif) }

    var expandedPaket by remember { mutableStateOf(false) }
    var expandedSecret by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = if (pelanggan.id.isEmpty()) "Tambah Pelanggan Baru" else "Edit Data Pelanggan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassInput(value = nama, onValueChange = { nama = it }, placeholder = "Nama Pelanggan", label = "Nama Pelanggan")
        Spacer(modifier = Modifier.height(12.dp))
        GlassInput(value = alamat, onValueChange = { alamat = it }, placeholder = "Alamat Rumah", label = "Alamat")
        Spacer(modifier = Modifier.height(12.dp))
        GlassInput(
            value = noHp,
            onValueChange = { noHp = it },
            placeholder = "Contoh: 0812345678",
            label = "Nomor WhatsApp (Angka Valid)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Always Guaranteed Dropdown for Paket
        Text(text = "Pilih Paket Internet:", color = SlateText, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedPaket,
            onExpandedChange = { expandedPaket = !expandedPaket }
        ) {
            OutlinedTextField(
                value = paket,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaket) },
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
                expanded = expandedPaket,
                onDismissRequest = { expandedPaket = false }
            ) {
                availablePakets.forEach { pName ->
                    DropdownMenuItem(
                        text = { Text(text = pName, color = TextDark) },
                        onClick = {
                            paket = pName
                            expandedPaket = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // PPP Secret MikroTik Select / Type
        if (mikrotikSecrets.isNotEmpty()) {
            Text(text = "Pilih PPP Secret MikroTik:", color = SlateText, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedSecret,
                onExpandedChange = { expandedSecret = !expandedSecret }
            ) {
                OutlinedTextField(
                    value = pppSecret.ifEmpty { "Pilih Secret MikroTik" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSecret) },
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
                    expanded = expandedSecret,
                    onDismissRequest = { expandedSecret = false }
                ) {
                    mikrotikSecrets.forEach { sec ->
                        DropdownMenuItem(
                            text = { Text(text = sec, color = TextDark) },
                            onClick = {
                                pppSecret = sec
                                expandedSecret = false
                            }
                        )
                    }
                }
            }
        } else {
            GlassInput(value = pppSecret, onValueChange = { pppSecret = it }, placeholder = "Contoh: ahmad_home", label = "MikroTik PPP Secret Name")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Status Langganan:", color = SlateText, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = statusAktif == "Aktif",
                onClick = { statusAktif = "Aktif" },
                colors = RadioButtonDefaults.colors(selectedColor = LeafGreen)
            )
            Text(text = "Aktif", color = TextDark, modifier = Modifier.padding(end = 16.dp))

            RadioButton(
                selected = statusAktif == "Nonaktif",
                onClick = { statusAktif = "Nonaktif" },
                colors = RadioButtonDefaults.colors(selectedColor = Rose)
            )
            Text(text = "Nonaktif", color = TextDark)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Batal", color = TextDark)
            }
            Button(
                onClick = {
                    onSave(
                        pelanggan.copy(
                            nama = nama,
                            alamat = alamat,
                            noHp = noHp,
                            paket = paket,
                            pppSecret = pppSecret,
                            statusAktif = statusAktif
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
            ) {
                Text(text = "Simpan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
