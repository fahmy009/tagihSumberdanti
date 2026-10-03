package com.example.tagihsumberdanti.data.remote

import com.example.tagihsumberdanti.data.model.Pengaturan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.Locale

data class InterfaceTraffic(
    val rxBps: Long = 0, // Download in bps
    val txBps: Long = 0, // Upload in bps
    val interfaceName: String = "ether1"
)

object MikrotikService {

    suspend fun testConnection(config: Pengaturan): Result<String> = withContext(Dispatchers.IO) {
        if (config.mikrotikHost.isEmpty()) {
            return@withContext Result.failure(Exception("Host/IP Public MikroTik belum diisi!"))
        }

        val port = config.mikrotikPort.trim().toIntOrNull() ?: 8728
        val socket = Socket()

        try {
            socket.connect(InetSocketAddress(config.mikrotikHost.trim(), port), 8000)
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            val loginResult = loginRouterOS(inputStream, outputStream, config.mikrotikUser, config.mikrotikPassword)
            if (loginResult.isFailure) {
                socket.close()
                return@withContext Result.failure(loginResult.exceptionOrNull()!!)
            }

            val secrets = fetchSecretNames(inputStream, outputStream)
            socket.close()
            Result.success("Koneksi MikroTik Port $port Berhasil! Ditemukan ${secrets.size} PPP Secret.")
        } catch (e: Exception) {
            try { socket.close() } catch (_: Exception) {}
            Result.failure(Exception("Gagal terhubung ke MikroTik Socket API (${config.mikrotikHost}:$port): ${e.localizedMessage}"))
        }
    }

    suspend fun fetchPppSecrets(config: Pengaturan): List<String> = withContext(Dispatchers.IO) {
        if (!config.mikrotikEnabled || config.mikrotikHost.isEmpty()) return@withContext emptyList()

        val port = config.mikrotikPort.trim().toIntOrNull() ?: 8728
        val socket = Socket()

        try {
            socket.connect(InetSocketAddress(config.mikrotikHost.trim(), port), 8000)
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            val loginResult = loginRouterOS(inputStream, outputStream, config.mikrotikUser, config.mikrotikPassword)
            if (loginResult.isSuccess) {
                val list = fetchSecretNames(inputStream, outputStream)
                socket.close()
                return@withContext list
            }
            socket.close()
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
        emptyList()
    }

    suspend fun fetchInterfaceTraffic(config: Pengaturan, interfaceName: String = "ether1"): InterfaceTraffic = withContext(Dispatchers.IO) {
        if (!config.mikrotikEnabled || config.mikrotikHost.isEmpty()) return@withContext InterfaceTraffic(interfaceName = interfaceName)

        val port = config.mikrotikPort.trim().toIntOrNull() ?: 8728
        val socket = Socket()

        try {
            socket.connect(InetSocketAddress(config.mikrotikHost.trim(), port), 6000)
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            val loginResult = loginRouterOS(inputStream, outputStream, config.mikrotikUser, config.mikrotikPassword)
            if (loginResult.isSuccess) {
                writeSentence(outputStream, listOf("/interface/monitor-traffic", "=interface=$interfaceName", "=once="))
                val responses = readUntilDone(inputStream)

                var rx = 0L
                var tx = 0L

                for (sentence in responses) {
                    for (word in sentence) {
                        if (word.startsWith("=rx-bits-per-second=")) {
                            rx = word.substring("=rx-bits-per-second=".length).toLongOrNull() ?: 0L
                        }
                        if (word.startsWith("=tx-bits-per-second=")) {
                            tx = word.substring("=tx-bits-per-second=".length).toLongOrNull() ?: 0L
                        }
                    }
                }

                socket.close()
                return@withContext InterfaceTraffic(rxBps = rx, txBps = tx, interfaceName = interfaceName)
            }
            socket.close()
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
        InterfaceTraffic(interfaceName = interfaceName)
    }

    suspend fun setPppSecretDisabled(
        config: Pengaturan,
        secretName: String,
        disabled: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!config.mikrotikEnabled || config.mikrotikHost.isEmpty() || secretName.isEmpty()) {
            return@withContext Result.success("Sistem MikroTik nonaktif atau nama secret kosong.")
        }

        val port = config.mikrotikPort.trim().toIntOrNull() ?: 8728
        val socket = Socket()

        try {
            socket.connect(InetSocketAddress(config.mikrotikHost.trim(), port), 8000)
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()

            val loginResult = loginRouterOS(inputStream, outputStream, config.mikrotikUser, config.mikrotikPassword)
            if (loginResult.isFailure) {
                socket.close()
                return@withContext Result.failure(loginResult.exceptionOrNull()!!)
            }

            // 1. Get Secret ID
            writeSentence(outputStream, listOf("/ppp/secret/print", "?name=$secretName"))
            val responses = readUntilDone(inputStream)

            var secretId = ""
            for (sentence in responses) {
                for (word in sentence) {
                    if (word.startsWith("=.id=")) {
                        secretId = word.substring("=.id=".length)
                    }
                }
            }

            val disableStr = if (disabled) "yes" else "no"

            if (secretId.isNotEmpty()) {
                writeSentence(outputStream, listOf("/ppp/secret/set", "=.id=$secretId", "=disabled=$disableStr"))
            } else {
                writeSentence(outputStream, listOf("/ppp/secret/set", "=numbers=$secretName", "=disabled=$disableStr"))
            }

            readUntilDone(inputStream)

            // Kick active PPP connection
            kickActiveSession(inputStream, outputStream, secretName)

            socket.close()
            val statusAction = if (disabled) "di-ISOLIR" else "BUKA ISOLIR (LUNAS)"
            Result.success("MikroTik: PPP Secret '$secretName' berhasil $statusAction!")
        } catch (e: Exception) {
            try { socket.close() } catch (_: Exception) {}
            Result.failure(Exception("Gagal update MikroTik Socket API: ${e.localizedMessage}"))
        }
    }

    // --- Private Helper Functions for RouterOS Binary API Protocol ---

    private fun loginRouterOS(
        inStream: InputStream,
        outStream: OutputStream,
        user: String,
        pass: String
    ): Result<Boolean> {
        writeSentence(outStream, listOf("/login", "=name=$user", "=password=$pass"))
        val sentences = readUntilDone(inStream)

        if (sentences.isEmpty()) {
            return Result.failure(Exception("Tidak ada respon dari RouterOS API"))
        }

        val firstSentence = sentences.first()
        if (firstSentence.contains("!done")) {
            val challengeHex = firstSentence.find { it.startsWith("=ret=") }?.substring("=ret=".length)
            if (challengeHex == null) {
                return Result.success(true)
            } else {
                val hashed = hashMd5Password(pass, challengeHex)
                writeSentence(outStream, listOf("/login", "=name=$user", "=response=00$hashed"))
                val resp = readUntilDone(inStream)
                if (resp.any { it.contains("!done") }) {
                    return Result.success(true)
                }
            }
        }

        val trapMsg = sentences.flatten().find { it.startsWith("=message=") }?.substring("=message=".length)
            ?: "Akses ditolak (not permitted) / User & Password MikroTik Salah!"
        return Result.failure(Exception("Gagal Login MikroTik: $trapMsg"))
    }

    private fun fetchSecretNames(inStream: InputStream, outStream: OutputStream): List<String> {
        writeSentence(outStream, listOf("/ppp/secret/print"))
        val sentences = readUntilDone(inStream)
        val list = mutableListOf<String>()

        for (sentence in sentences) {
            if (sentence.contains("!re")) {
                for (word in sentence) {
                    if (word.startsWith("=name=")) {
                        list.add(word.substring("=name=".length))
                    }
                }
            }
        }
        return list
    }

    private fun kickActiveSession(inStream: InputStream, outStream: OutputStream, secretName: String) {
        try {
            writeSentence(outStream, listOf("/ppp/active/print", "?name=$secretName"))
            val responses = readUntilDone(inStream)
            for (sentence in responses) {
                if (sentence.contains("!re")) {
                    val activeId = sentence.find { it.startsWith("=.id=") }?.substring("=.id=".length)
                    if (activeId != null) {
                        writeSentence(outStream, listOf("/ppp/active/remove", "=.id=$activeId"))
                        readUntilDone(inStream)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun hashMd5Password(password: String, challengeHex: String): String {
        try {
            val challengeBytes = ByteArray(challengeHex.length / 2) {
                challengeHex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
            }
            val md = MessageDigest.getInstance("MD5")
            md.update(0)
            md.update(password.toByteArray(Charsets.UTF_8))
            md.update(challengeBytes)
            val digest = md.digest()
            val sb = StringBuilder()
            for (b in digest) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString()
        } catch (_: Exception) {
            return ""
        }
    }

    private fun writeSentence(outStream: OutputStream, words: List<String>) {
        for (word in words) {
            writeWord(outStream, word)
        }
        writeWord(outStream, "")
    }

    private fun writeWord(outStream: OutputStream, word: String) {
        val bytes = word.toByteArray(Charsets.UTF_8)
        val len = bytes.size

        if (len < 0x80) {
            outStream.write(len)
        } else if (len < 0x4000) {
            val b1 = (len shr 8) or 0x80
            val b2 = len and 0xFF
            outStream.write(b1)
            outStream.write(b2)
        } else if (len < 0x200000) {
            val b1 = (len shr 16) or 0xC0
            val b2 = (len shr 8) and 0xFF
            val b3 = len and 0xFF
            outStream.write(b1)
            outStream.write(b2)
            outStream.write(b3)
        } else {
            val b1 = (len shr 24) or 0xE0
            val b2 = (len shr 16) and 0xFF
            val b3 = (len shr 8) and 0xFF
            val b4 = len and 0xFF
            outStream.write(b1)
            outStream.write(b2)
            outStream.write(b3)
            outStream.write(b4)
        }

        if (len > 0) {
            outStream.write(bytes)
        }
        outStream.flush()
    }

    private fun readUntilDone(inStream: InputStream): List<List<String>> {
        val sentences = mutableListOf<List<String>>()
        while (true) {
            val sentence = readSentence(inStream)
            if (sentence.isEmpty()) break
            sentences.add(sentence)
            if (sentence.contains("!done") || sentence.contains("!trap")) {
                break
            }
        }
        return sentences
    }

    private fun readSentence(inStream: InputStream): List<String> {
        val words = mutableListOf<String>()
        while (true) {
            val word = readWord(inStream)
            if (word.isEmpty()) break
            words.add(word)
        }
        return words
    }

    private fun readWord(inStream: InputStream): String {
        val b1 = inStream.read()
        if (b1 == -1) return ""

        var len = 0
        if (b1 and 0x80 == 0) {
            len = b1
        } else if (b1 and 0xC0 == 0x80) {
            val b2 = inStream.read()
            len = ((b1 and 0x3F) shl 8) or b2
        } else if (b1 and 0xE0 == 0xC0) {
            val b2 = inStream.read()
            val b3 = inStream.read()
            len = ((b1 and 0x1F) shl 16) or (b2 shl 8) or b3
        } else if (b1 and 0xF0 == 0xE0) {
            val b2 = inStream.read()
            val b3 = inStream.read()
            val b4 = inStream.read()
            len = ((b1 and 0x0F) shl 24) or (b2 shl 16) or (b3 shl 8) or b4
        }

        if (len == 0) return ""

        val bytes = ByteArray(len)
        var readBytes = 0
        while (readBytes < len) {
            val count = inStream.read(bytes, readBytes, len - readBytes)
            if (count == -1) break
            readBytes += count
        }
        return String(bytes, 0, readBytes, Charsets.UTF_8)
    }
}

fun Long.toMbps(): String {
    val mbps = this.toDouble() / (1024 * 1024)
    return String.format(Locale.getDefault(), "%.2f Mbps", mbps)
}
