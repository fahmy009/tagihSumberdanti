package com.example.tagihsumberdanti.ui.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagihsumberdanti.data.model.Pelanggan
import com.example.tagihsumberdanti.data.model.Tagihan
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.components.GlassInput
import com.example.tagihsumberdanti.ui.theme.Amber
import com.example.tagihsumberdanti.ui.theme.Indigo
import com.example.tagihsumberdanti.ui.theme.LeafGreen
import com.example.tagihsumberdanti.ui.theme.Rose
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagihanScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val tagihanList by viewModel.tagihanList.collectAsState()
    val pelangganList by viewModel.pelangganList.collectAsState()
    val pengaturan by viewModel.pengaturan.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    var selectedTagihan by remember { mutableStateOf<Tagihan?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showSingleInvoiceSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filterOptions = listOf("Semua", "Belum Bayar", "Lunas", "Menunggak", "Double", "Double Hutang")

    val filteredList = tagihanList.filter { tagihan ->
        val matchesSearch = tagihan.namaPelanggan.contains(searchQuery, ignoreCase = true) ||
                tagihan.bulan.contains(searchQuery, ignoreCase = true) ||
                tagihan.tahun.contains(searchQuery, ignoreCase = true)
        val matchesFilter = if (selectedFilter == "Semua") true else tagihan.status == selectedFilter
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kelola Tagihan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Button(
                onClick = { showSingleInvoiceSheet = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Tagihan Satuan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        GlassInput(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Cari nama pelanggan / bulan...",
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = LeafGreen)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { option ->
                val isSelected = selectedFilter == option
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = option },
                    label = { Text(text = option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LeafGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = SlateText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada tagihan ditemukan",
                    color = SlateText,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { tagihan ->
                    val customerNoHp = pelangganList.find { it.id == tagihan.idPelanggan }?.noHp ?: ""
                    TagihanCard(
                        tagihan = tagihan,
                        onEditClick = {
                            selectedTagihan = tagihan
                            showEditSheet = true
                        },
                        onDeleteClick = {
                            viewModel.hapusTagihan(tagihan.id)
                        },
                        onSendWaClick = {
                            kirimNotaWhatsApp(context, tagihan, customerNoHp)
                        }
                    )
                }
            }
        }
    }

    if (showEditSheet && selectedTagihan != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showEditSheet = false
                selectedTagihan = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            EditTagihanStatusBottomSheet(
                tagihan = selectedTagihan!!,
                onSaveStatus = { newStatus ->
                    viewModel.updateStatusTagihan(selectedTagihan!!.id, newStatus, ditagihOleh = "Admin")
                    showEditSheet = false
                    selectedTagihan = null
                },
                onCancel = {
                    showEditSheet = false
                    selectedTagihan = null
                }
            )
        }
    }

    if (showSingleInvoiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSingleInvoiceSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            SingleInvoiceBottomSheet(
                pelangganList = pelangganList,
                defaultMonth = pengaturan.bulanAktif,
                defaultYear = pengaturan.tahunAktif,
                onCreateInvoice = { pelanggan, bulan, tahun, nominal ->
                    viewModel.buatTagihanSatuan(pelanggan, bulan, tahun, nominal)
                    showSingleInvoiceSheet = false
                },
                onCancel = { showSingleInvoiceSheet = false }
            )
        }
    }
}

@Composable
fun TagihanCard(
    tagihan: Tagihan,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSendWaClick: () -> Unit
) {
    val statusColor = when (tagihan.status) {
        "Lunas" -> LeafGreen
        "Double" -> Indigo
        "Menunggak" -> Rose
        "Double Hutang" -> Amber
        else -> Amber // Belum Bayar
    }

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
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = statusColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tagihan.namaPelanggan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tagihan.status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Periode: ${tagihan.bulan} ${tagihan.tahun}", fontSize = 14.sp, color = SlateText)
            Text(text = "Nominal: ${tagihan.nominal.toRupiah()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LeafGreen)

            if (tagihan.tanggalBayar.isNotEmpty()) {
                Text(text = "Dibayar: ${tagihan.tanggalBayar} (${tagihan.ditagihOleh})", fontSize = 12.sp, color = SlateText)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSendWaClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Kirim Nota WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Status", tint = TextDark)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Rose)
                    }
                }
            }
        }
    }
}

@Composable
fun EditTagihanStatusBottomSheet(
    tagihan: Tagihan,
    onSaveStatus: (String) -> Unit,
    onCancel: () -> Unit
) {
    val statuses = listOf("Belum Bayar", "Lunas", "Menunggak", "Double", "Double Hutang")
    var currentStatus by remember { mutableStateOf(tagihan.status) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Ubah Status Tagihan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "${tagihan.namaPelanggan} - ${tagihan.bulan} ${tagihan.tahun}",
            fontSize = 14.sp,
            color = SlateText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        statuses.forEach { statusOption ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentStatus == statusOption,
                    onClick = { currentStatus = statusOption },
                    colors = RadioButtonDefaults.colors(selectedColor = LeafGreen)
                )
                Text(
                    text = statusOption,
                    color = TextDark,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
                onClick = { onSaveStatus(currentStatus) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
            ) {
                Text(text = "Simpan Status", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleInvoiceBottomSheet(
    pelangganList: List<Pelanggan>,
    defaultMonth: String,
    defaultYear: String,
    onCreateInvoice: (Pelanggan, String, String, Double) -> Unit,
    onCancel: () -> Unit
) {
    var expandedCustomer by remember { mutableStateOf(false) }
    var selectedPelanggan by remember { mutableStateOf<Pelanggan?>(pelangganList.firstOrNull()) }
    var bulan by remember { mutableStateOf(defaultMonth.ifEmpty { "Oktober" }) }
    var tahun by remember { mutableStateOf(defaultYear.ifEmpty { "2026" }) }
    var nominalInput by remember { mutableStateOf("100000") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Buat Tagihan Satuan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Buat tagihan khusus untuk 1 pelanggan tanpa tagihan massal.",
            fontSize = 13.sp,
            color = SlateText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Select Customer Dropdown
        Text(text = "Pilih Pelanggan:", color = SlateText, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expandedCustomer,
            onExpandedChange = { expandedCustomer = !expandedCustomer }
        ) {
            OutlinedTextField(
                value = selectedPelanggan?.nama ?: "Pilih Pelanggan",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomer) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedIndicatorColor = LeafGreen,
                    unfocusedIndicatorColor = Color(0xFFCBD5E1)
                )
            )
            ExposedDropdownMenu(
                expanded = expandedCustomer,
                onDismissRequest = { expandedCustomer = false }
            ) {
                pelangganList.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(text = "${p.nama} (${p.paket})", color = TextDark) },
                        onClick = {
                            selectedPelanggan = p
                            expandedCustomer = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GlassInput(
            value = bulan,
            onValueChange = { bulan = it },
            placeholder = "Bulan Tagihan",
            label = "Bulan"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassInput(
            value = tahun,
            onValueChange = { tahun = it },
            placeholder = "Tahun Tagihan",
            label = "Tahun"
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassInput(
            value = nominalInput,
            onValueChange = { nominalInput = it },
            placeholder = "Nominal Tagihan (Rp)",
            label = "Nominal (Rp)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

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
                    if (selectedPelanggan != null) {
                        val nom = nominalInput.toDoubleOrNull() ?: 100000.0
                        onCreateInvoice(selectedPelanggan!!, bulan, tahun, nom)
                    }
                },
                enabled = selectedPelanggan != null,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
            ) {
                Text(text = "Buat Tagihan", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun kirimNotaWhatsApp(context: android.content.Context, tagihan: Tagihan, rawPhone: String) {
    if (rawPhone.isEmpty()) {
        Toast.makeText(context, "Nomor HP pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
        return
    }

    var cleanPhone = rawPhone.replace("[^0-9]".toRegex(), "")
    if (cleanPhone.startsWith("0")) {
        cleanPhone = "62" + cleanPhone.substring(1)
    }

    val pesan = """
        Halo Kak *${tagihan.namaPelanggan}*,
        Berikut info tagihan internet Anda:
        - Periode: *${tagihan.bulan} ${tagihan.tahun}*
        - Nominal: *${tagihan.nominal.toRupiah()}*
        - Status: *${tagihan.status}*
        
        Terima kasih telah menggunakan layanan internet kami!
    """.trimIndent()

    val encodedMsg = Uri.encode(pesan)
    val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
