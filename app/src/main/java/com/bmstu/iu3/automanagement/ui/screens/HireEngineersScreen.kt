package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmstu.iu3.automanagement.ui.theme.PixelButton
import com.bmstu.iu3.automanagement.R.font.press_start2p
import com.bmstu.iu3.automanagement.models.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HireEngineersScreen(onBack: () -> Unit) {
    val viewModel: MarketViewModel = viewModel()
    val engineers = viewModel.availableEngineers

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hire Engineers", fontFamily = FontFamily(Font(press_start2p))) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(engineers) { engineer ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = engineer.getName(), style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily(Font(press_start2p)))
                                Text(text = "Skill: ${engineer.getSkill()}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily(Font(press_start2p)))
                                Text(text = "Salary: ${engineer.getSalary()} $", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily(Font(press_start2p)))
                            }
                            PixelButton(
                                text = "Hire",
                                onClick = { viewModel.hireEngineer(engineer) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PixelButton(
                text = "Back to Menu",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
