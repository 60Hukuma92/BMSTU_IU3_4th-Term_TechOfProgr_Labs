package com.bmstu.iu3.automanagement.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bmstu.iu3.automanagement.R.font.game_font
import com.bmstu.iu3.automanagement.models.MarketViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyComponentsScreen(onBack: () -> Unit) {
    val marketViewModel: MarketViewModel = viewModel()
    val components by marketViewModel.availableComponents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Buy Components", fontFamily = FontFamily(Font(game_font))) })
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
                items(components) { component ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = component.getName(), style = MaterialTheme.typography.titleMedium)
                                Text(text = "${component.getPrice()} $", style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(onClick = { marketViewModel.buyComponent(component) }) {
                                Text("Buy")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Back to Menu")
            }
        }
    }
}
