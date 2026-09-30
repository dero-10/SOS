package com.sos.studentonstudy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(studentName: String, onDashboard: () -> Unit, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SosPurple, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .padding(start = 24.dp, end = 16.dp, top = 28.dp, bottom = 32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SOS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.height(26.dp))
                                Text("Hello, $studentName", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Welcome to Student on Study", color = Color.White.copy(alpha = .84f), fontSize = 13.sp)
                            }
                            IconButton(onClick = onLogout) {
                                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Log out", tint = Color.White)
                            }
                        }
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                        SectionTitle("Main Menu")
                        Spacer(Modifier.height(18.dp))
                        SurfaceCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onDashboard)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(SosAqua, RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.GridView, contentDescription = null, tint = SosPurple)
                                }
                                Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                                    Text("Dashboard", color = SosText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(3.dp))
                                    Text("See your study overview", color = SosMuted, fontSize = 12.sp)
                                }
                                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, tint = SosMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            SosBottomBar(onMenu = {}, onDashboard = onDashboard, selectedDashboard = false)
        }
    }
}
