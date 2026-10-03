package com.example.tagihsumberdanti.ui.penagih

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.tagihsumberdanti.data.model.Tagihan
import com.example.tagihsumberdanti.ui.admin.kirimNotaWhatsApp
import com.example.tagihsumberdanti.ui.admin.toRupiah
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.components.GlassInput
import com.example.tagihsumberdanti.ui.theme.Amber
import com.example.tagihsumberdanti.ui.theme.GreenPrimary
import com.example.tagihsumberdanti.ui.theme.Indigo
import com.example.tagihsumberdanti.ui.theme.LightBackground
import com.example.tagihsumberdanti.ui.theme.Rose
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TukangTagihScreen(
    collectorUsername: String,
    viewModel: TukangTagihViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagihanList by viewModel.tagihanList.collectAsState()
    val pelangganList by viewModel.pelangganList.collectAsState()
    val pengaturan by viewModel.pengaturan.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Belum Tagih, 1 = Sudah Lunas
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagihan by remember { mutableStateOf<Tagihan?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showPinSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // Filter by active month & year (case-insensitive)
    val activeMonthInvoices = tagihanList.filter {
        it.bulan.equals(pengaturan.bulanAktif, ignoreCase = true) &&
                it.tahun.equals(pengaturan.tahunAktif, ignoreCase = true)
    }

    // Unpaid vs Paid lists
    val belumLunasInvoices = activeMonthInvoices.filter {
        it.status == "Belum Bayar" || it.status == "Menunggak" || it.status == "Double Hutang"
    }
    val sudahLunasInvoices = activeMonthInvoices.filter {
        it.status == "Lunas" || it.status == "Double"
    }

    // Header Setoran Statistics for current collector
    val collectedByMe = activeMonthInvoices.filter { it.ditagihOleh == collectorUsername }
    val totalUangSaya = collectedByMe.sumOf {
        when (it.status) {
            "Lunas" -> it.nominal
            "Double" -> it.nominal * 2
            else -> 0.0
        }
    }
    val rumahSayaDitagih = collectedByMe.size

    val listForSelectedTab = if (selectedTab == 0) belumLunasInvoices else sudahLunasInvoices
    val filteredList = listForSelectedTab.filter {
        it.namaPelanggan.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Layar Penagih",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Petugas: $collectorUsername (${pengaturan.bulanAktif} ${pengaturan.tahunAktif})",
                        fontSize = 13.sp,
                        color = SlateText
                    )
                }

                Row {
                    IconButton(onClick = { showPinSheet = true }) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Ubah PIN & Keamanan", tint = GreenPrimary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = Rose)
                    }
                }
            }

            // Summary Setoran Header GlassCard
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                cornerRadius = 18.dp
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Setoran Anda ($collectorUsername)", fontSize = 14.sp, color = SlateText)
                        Text(text = totalUangSaya.toRupiah(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rumah Ditagih: $rumahSayaDitagih", fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
                        Text(text = "Belum Tagih: ${belumLunasInvoices.size}", fontSize = 13.sp, color = Amber, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Option A Tab Row: Belum Tagih vs Sudah Lunas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = TextDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "📋 Belum Tagih (${belumLunasInvoices.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) GreenPrimary else SlateText
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "✅ Sudah Lunas (${sudahLunasInvoices.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) GreenPrimary else SlateText
                        )
                    }
                )
            }

            // Search Input
            GlassInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Cari nama pelanggan...",
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GreenPrimary)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagihan List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "Semua tagihan periode ini sudah lunas!" else "Belum ada tagihan lunas",
                        color = SlateText,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { tagihan ->
                        PenagihTagihanCard(
                            tagihan = tagihan,
                            onClick = {
                                selectedTagihan = tagihan
                                showPaymentSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPaymentSheet && selectedTagihan != null) {
        val currentPhone = pelangganList.find { it.id == selectedTagihan!!.idPelanggan }?.noHp ?: ""
        ModalBottomSheet(
            onDismissRequest = {
                showPaymentSheet = false
                selectedTagihan = null
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PaymentActionBottomSheet(
                tagihan = selectedTagihan!!,
                onAction = { newStatus ->
                    viewModel.updateStatusTagihan(
                        tagihanId = selectedTagihan!!.id,
                        newStatus = newStatus,
                        collectorName = collectorUsername
                    )
                    showPaymentSheet = false
                    selectedTagihan = null
                },
                onSendWa = {
                    kirimNotaWhatsApp(context, selectedTagihan!!, currentPhone)
                }
            )
        }
    }

    if (showPinSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPinSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ChangePinBottomSheet(
                collectorUsername = collectorUsername,
                onSavePin = { newPin ->
                    viewModel.updateOwnPin(collectorUsername, newPin)
                    showPinSheet = false
                },
                onCancel = { showPinSheet = false }
            )
        }
    }
}

@Composable
fun PenagihTagihanCard(
    tagihan: Tagihan,
    onClick: () -> Unit
) {
    val statusColor = when (tagihan.status) {
        "Lunas" -> GreenPrimary
        "Double" -> Indigo
        "Menunggak" -> Rose
        "Double Hutang" -> Amber
        else -> Amber
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tagihan.namaPelanggan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

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

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Nominal: ${tagihan.nominal.toRupiah()}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                if (tagihan.ditagihOleh.isNotEmpty()) {
                    Text(text = "Ditagih: ${tagihan.ditagihOleh}", fontSize = 12.sp, color = SlateText)
                } else {
                    Text(text = "Klik untuk bayar", fontSize = 12.sp, color = SlateText)
                }
            }
        }
    }
}

@Composable
fun PaymentActionBottomSheet(
    tagihan: Tagihan,
    onAction: (String) -> Unit,
    onSendWa: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Proses Pembayaran",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = tagihan.namaPelanggan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GreenPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "Tagihan: ${tagihan.nominal.toRupiah()} (${tagihan.bulan} ${tagihan.tahun})",
            fontSize = 14.sp,
            color = SlateText,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Button(
            onClick = { onAction("Lunas") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Bayar Lunas", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onAction("Double") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo, contentColor = Color.White)
        ) {
            Icon(imageVector = Icons.Default.Payments, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Bayar Double (2 Bulan)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSendWa,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = TextDark)
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = GreenPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Hubungi / Ingatkan via WhatsApp", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onAction("Menunggak") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Rose, contentColor = Color.White)
            ) {
                Text(text = "Menunggak", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onAction("Double Hutang") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Color.White)
            ) {
                Text(text = "Double Hutang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChangePinBottomSheet(
    collectorUsername: String,
    onSavePin: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var newPin by remember { mutableStateOf("") }
    var bioEnabled by remember { mutableStateOf(sessionManager.isBiometricEnabled()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Pengaturan Keamanan Penagih",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Akun: $collectorUsername (Username tidak dapat diubah)",
            fontSize = 13.sp,
            color = SlateText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GlassInput(
            value = collectorUsername,
            onValueChange = {},
            label = "Username (Tetap)",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SlateText) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GlassInput(
            value = newPin,
            onValueChange = { newPin = it },
            placeholder = "Masukkan PIN Baru",
            label = "PIN Baru (Min. 4 Digit)",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Biometric Toggle Switch for Penagih
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Login Biometrik / Sidik Jari", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = "Fingerprint Unlock saat buka aplikasi", fontSize = 11.sp, color = SlateText)
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text(text = "Batal", color = TextDark)
            }
            Button(
                onClick = { onSavePin(newPin) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(text = "Simpan PIN", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
