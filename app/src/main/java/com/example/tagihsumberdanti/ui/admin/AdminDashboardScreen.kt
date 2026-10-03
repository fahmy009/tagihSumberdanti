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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagihsumberdanti.data.remote.toMbps
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.theme.Amber
import com.example.tagihsumberdanti.ui.theme.GreenPrimary
import com.example.tagihsumberdanti.ui.theme.Indigo
import com.example.tagihsumberdanti.ui.theme.LeafGreen
import com.example.tagihsumberdanti.ui.theme.Rose
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val pelangganList by viewModel.pelangganList.collectAsState()
    val tagihanList by viewModel.tagihanList.collectAsState()
    val pengaturan by viewModel.pengaturan.collectAsState()
    val mikrotikSecrets by viewModel.mikrotikSecrets.collectAsState()

    // 1. Total Pendapatan = Lunas + (2 * Double)
    val totalPendapatan = tagihanList.sumOf { tagihan ->
        when (tagihan.status) {
            "Lunas" -> tagihan.nominal
            "Double" -> tagihan.nominal * 2
            else -> 0.0
        }
    }

    // 2. Count Pelanggan
    val pelangganAktif = pelangganList.count { it.statusAktif == "Aktif" }
    val pelangganNonaktif = pelangganList.count { it.statusAktif == "Nonaktif" }

    // 3. Tagihan Belum Lunas
    val belumLunasList = tagihanList.filter {
        it.status == "Belum Bayar" || it.status == "Menunggak" || it.status == "Double Hutang"
    }
    val countBelumLunas = belumLunasList.size
    val totalNominalBelumLunas = belumLunasList.sumOf { it.nominal }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Dashboard Beranda",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Bulan Active: ${pengaturan.bulanAktif} ${pengaturan.tahunAktif}",
            fontSize = 14.sp,
            color = SlateText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Router Status & Connection Card (Mini Winbox)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Status MikroTik Router",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val isMikrotikOnline = (pengaturan.mikrotikEnabled || mikrotikSecrets.isNotEmpty()) && pengaturan.mikrotikHost.isNotEmpty()
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isMikrotikOnline) GreenPrimary.copy(alpha = 0.15f) else Rose.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isMikrotikOnline) "Online/API" else "Offline",
                                        color = if (isMikrotikOnline) GreenPrimary else Rose,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = if (pengaturan.mikrotikHost.isNotEmpty()) "${pengaturan.mikrotikHost}:${pengaturan.mikrotikPort} (${mikrotikSecrets.size} Secret)" else "Host/IP belum dikonfigurasi",
                                fontSize = 12.sp,
                                color = SlateText
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.testMikrotikKoneksi() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Tes Router", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }

                if (pengaturan.mikrotikEnabled && pengaturan.mikrotikHost.isNotEmpty()) {
                    val traffic by viewModel.interfaceTraffic.collectAsState()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📊 Traffic (${traffic.interfaceName}):", fontSize = 12.sp, color = SlateText, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "⬇️ ${traffic.rxBps.toMbps()}", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                            Text(text = "⬆️ ${traffic.txBps.toMbps()}", fontSize = 12.sp, color = Indigo, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Card 1: Total Pendapatan
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = LeafGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Total Pendapatan",
                        fontSize = 14.sp,
                        color = SlateText
                    )
                    Text(
                        text = totalPendapatan.toRupiah(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = LeafGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Jumlah Pelanggan
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Jumlah Pelanggan",
                        fontSize = 14.sp,
                        color = SlateText
                    )
                    Text(
                        text = "${pelangganList.size} Pelanggan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Aktif: $pelangganAktif",
                            fontSize = 13.sp,
                            color = LeafGreen,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Nonaktif: $pelangganNonaktif",
                            fontSize = 13.sp,
                            color = Rose,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Tagihan Belum Lunas
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Tagihan Belum Lunas",
                        fontSize = 14.sp,
                        color = SlateText
                    )
                    Text(
                        text = "$countBelumLunas Tagihan (${totalNominalBelumLunas.toRupiah()})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Tombol Buat Tagihan Massal
        Button(
            onClick = { viewModel.buatTagihanMassal() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LeafGreen,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.AddCard,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Buat Tagihan Massal (${pengaturan.bulanAktif} ${pengaturan.tahunAktif})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
