package com.example.androidmultimodule.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arabic_explainer.data.model.ArabicWord

@Composable
fun CodeWorkspaceRoute(
    viewModel: CodeWorkspaceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    CodeWorkspaceScreen(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onAnalyze = viewModel::analyzeCode
    )
}

@Composable
fun CodeWorkspaceScreen(
    state: CodeWorkspaceUiState,
    onCodeChange: (String) -> Unit,
    onAnalyze: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Java Translation", "Arabic Explanation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Code Workspace",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Paste your Smali code below to see the Java equivalent and learn about the terms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Code Editor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                if (state.smaliCode.isEmpty()) {
                    Text(
                        text = "Enter Smali code here...",
                        color = Color.Gray,
                        style = TextStyle(fontFamily = FontFamily.Monospace)
                    )
                }
                BasicTextField(
                    value = state.smaliCode,
                    onValueChange = { onCodeChange(it) },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onAnalyze() },
            modifier = Modifier.align(Alignment.End),
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Analyzing..." else "Analyze")
        }

        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (state.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results Section
        if (state.javaTranslation != null || state.explainedTerms.isNotEmpty()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    // Java Translation
                    CodeView(
                        code = state.javaTranslation ?: "No translation available",
                        language = "java"
                    )
                }
                1 -> {
                    // Arabic Explanation
                    Column {
                        if (state.explainedTerms.isEmpty()) {
                             Text("No explanations available for the terms found.")
                        } else {
                            state.explainedTerms.forEach { word ->
                                ExplanationCard(word)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeView(code: String, language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Text(
            text = if (language == "java") highlightJava(code) else AnnotatedString(code),
            modifier = Modifier.padding(16.dp),
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        )
    }
}

@Composable
fun ExplanationCard(word: ArabicWord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Translation: ${word.translation}")
            Text(text = "Pronunciation: ${word.pronunciation}")
            Text(text = "Definition: ${word.definition}")
        }
    }
}

// Simple syntax highlighting helpers
fun highlightJava(code: String): AnnotatedString {
    val keywords = listOf("public", "class", "void", "return", "int", "String", "if", "else", "for", "while")
    val builder = AnnotatedString.Builder(code)
    
    // This is a very naive implementation, just for demo
    keywords.forEach { keyword ->
        var startIndex = code.indexOf(keyword)
        while (startIndex >= 0) {
            val endIndex = startIndex + keyword.length
            // Check boundaries to ensure it's a whole word (basic check)
            val isStartBoundary = startIndex == 0 || !code[startIndex - 1].isLetterOrDigit()
            val isEndBoundary = endIndex == code.length || !code[endIndex].isLetterOrDigit()
            
            if (isStartBoundary && isEndBoundary) {
                 builder.addStyle(
                    style = SpanStyle(color = Color(0xFF000080), fontWeight = FontWeight.Bold),
                    start = startIndex,
                    end = endIndex
                )
            }
            startIndex = code.indexOf(keyword, endIndex)
        }
    }
    return builder.toAnnotatedString()
}
