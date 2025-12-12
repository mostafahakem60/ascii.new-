package com.example.androidmultimodule.ui.workspace

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.arabic_explainer.data.model.ArabicWord
import org.junit.Rule
import org.junit.Test

class CodeWorkspaceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyWorkspaceInitialState() {
        composeTestRule.setContent {
            CodeWorkspaceScreen(
                state = CodeWorkspaceUiState(),
                onCodeChange = {},
                onAnalyze = {}
            )
        }

        composeTestRule.onNodeWithText("Code Workspace").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter Smali code here...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Analyze").assertIsDisplayed()
    }

    @Test
    fun verifyLoadingState() {
        composeTestRule.setContent {
            CodeWorkspaceScreen(
                state = CodeWorkspaceUiState(isLoading = true, smaliCode = "some code"),
                onCodeChange = {},
                onAnalyze = {}
            )
        }

        composeTestRule.onNodeWithText("Analyzing...").assertIsDisplayed()
    }

    @Test
    fun verifyResultsDisplay() {
        val translation = "public class Test { }"
        val explanation = ArabicWord(
            word = "Test",
            translation = "TestTranslation",
            pronunciation = "TestPronunciation",
            definition = "TestDefinition"
        )
        
        composeTestRule.setContent {
            CodeWorkspaceScreen(
                state = CodeWorkspaceUiState(
                    smaliCode = "some code",
                    javaTranslation = translation,
                    explainedTerms = listOf(explanation)
                ),
                onCodeChange = {},
                onAnalyze = {}
            )
        }

        // Check Tabs
        composeTestRule.onNodeWithText("Java Translation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arabic Explanation").assertIsDisplayed()

        // Check Translation content (default tab)
        composeTestRule.onNodeWithText(translation).assertIsDisplayed()

        // Switch Tab
        composeTestRule.onNodeWithText("Arabic Explanation").performClick()
        
        // Check Explanation content
        composeTestRule.onNodeWithText("Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Translation: TestTranslation").assertIsDisplayed()
    }
}
