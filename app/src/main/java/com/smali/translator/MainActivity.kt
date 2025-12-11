package com.smali.translator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smali.translator.ui.theme.SmaliTranslatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmaliTranslatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smali Translator") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Smali to Java Translator",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "Features:\n" +
                        "• Import Smali files using Storage Access Framework\n" +
                        "• Translate Smali bytecode to Java\n" +
                        "• Batch process multiple files\n" +
                        "• Export results with explanations\n" +
                        "• Cached translations for faster access",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = { /* Launch file picker */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Smali File")
            }
            
            Button(
                onClick = { /* Launch batch picker */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Multiple Files (Batch)")
            }
        }
    }
}
