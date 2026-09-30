package com.sos.studentonstudy.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.School
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
fun DashboardScreen(studentName: String, onMenu: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SosPurple, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            .padding(start = 14.dp, end = 24.dp, top = 20.dp, bottom = 30.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onMenu) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Main Menu", tint = Color.White)
                            }
                            Text("Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(22.dp))
                        Text("Your study overview", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Keep track of your learning, $studentName", color = Color.White.copy(alpha = .86f), fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                        SectionTitle("Overview")
                        Spacer(Modifier.height(18.dp))
                        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(52.dp).background(SosAqua, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = SosPurple, modifier = Modifier.size(28.dp))
                                }
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text("Student on Study", color = SosText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Study information at a glance", color = SosMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            SosBottomBar(onMenu = onMenu, onDashboard = {}, selectedDashboard = true)
        }
    }
}
