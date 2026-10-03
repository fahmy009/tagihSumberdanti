package com.example.tagihsumberdanti.ui.login

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagihsumberdanti.R
import com.example.tagihsumberdanti.data.model.User
import com.example.tagihsumberdanti.ui.components.GlassCard
import com.example.tagihsumberdanti.ui.components.GlassInput
import com.example.tagihsumberdanti.ui.theme.GreenLight
import com.example.tagihsumberdanti.ui.theme.GreenPrimary
import com.example.tagihsumberdanti.ui.theme.IndigoLight
import com.example.tagihsumberdanti.ui.theme.LightBackground
import com.example.tagihsumberdanti.ui.theme.SlateText
import com.example.tagihsumberdanti.ui.theme.TextDark

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val username by viewModel.username.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val loginResult by viewModel.loginResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is LoginResult.Success -> {
                onLoginSuccess(result.user)
                viewModel.resetState()
            }
            is LoginResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        // Soft Green Background Accent Circle 1
        val blurModifier1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(100.dp)
        } else Modifier

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = (-120).dp)
                .clip(CircleShape)
                .background(GreenLight)
                .then(blurModifier1)
        )

        // Soft Indigo Accent Circle 2
        val blurModifier2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur(100.dp)
        } else Modifier

        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (100).dp, y = (140).dp)
                .clip(CircleShape)
                .background(IndigoLight)
                .then(blurModifier2)
        )

        // Login Card Form
        GlassCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo Aplikasi",
                    modifier = Modifier
                        .size(88.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "Tagih Sumberdanti",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aplikasi Manajemen Tagihan Internet",
                    fontSize = 14.sp,
                    color = SlateText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                GlassInput(
                    value = username,
                    onValueChange = viewModel::onUsernameChange,
                    placeholder = "Username",
                    label = "Username",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username Icon",
                            tint = GreenPrimary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                GlassInput(
                    value = pin,
                    onValueChange = viewModel::onPinChange,
                    placeholder = "PIN",
                    label = "PIN",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "PIN Icon",
                            tint = GreenPrimary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.login() },
                    enabled = loginResult !is LoginResult.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (loginResult is LoginResult.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Masuk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
