package com.rkt.snappyrulerset.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rkt.snappyrulerset.presentation.ui.screens.DrawingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testToolSelection() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test ruler selection
        composeTestRule.onNodeWithText("Ruler").performClick()
        composeTestRule.onNodeWithText("Ruler").assertIsSelected()
        
        // Test set square selection
        composeTestRule.onNodeWithText("SetSq 45°").performClick()
        composeTestRule.onNodeWithText("SetSq 45°").assertIsSelected()
        
        // Test protractor selection
        composeTestRule.onNodeWithText("Protractor").performClick()
        composeTestRule.onNodeWithText("Protractor").assertIsSelected()
        
        // Test compass selection
        composeTestRule.onNodeWithText("Compass").performClick()
        composeTestRule.onNodeWithText("Compass").assertIsSelected()
    }
    
    @Test
    fun testModeSelection() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test hand mode
        composeTestRule.onNodeWithText("Hand").performClick()
        composeTestRule.onNodeWithText("Hand").assertIsSelected()
        
        // Test line mode
        composeTestRule.onNodeWithText("Line").performClick()
        composeTestRule.onNodeWithText("Line").assertIsSelected()
        
        // Test freehand mode
        composeTestRule.onNodeWithText("Freehand").performClick()
        composeTestRule.onNodeWithText("Freehand").assertIsSelected()
    }
    
    @Test
    fun testUndoRedoButtons() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test undo button is present and clickable
        composeTestRule.onNodeWithContentDescription("Undo").assertExists()
        composeTestRule.onNodeWithContentDescription("Undo").performClick()
        
        // Test redo button is present and clickable
        composeTestRule.onNodeWithContentDescription("Redo").assertExists()
        composeTestRule.onNodeWithContentDescription("Redo").performClick()
    }
    
    @Test
    fun testSaveButton() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test save button is present and clickable
        composeTestRule.onNodeWithContentDescription("Save").assertExists()
        composeTestRule.onNodeWithContentDescription("Save").performClick()
    }
    
    @Test
    fun testAppTitle() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test app title is displayed
        composeTestRule.onNodeWithText("Snappy Ruler Set").assertExists()
    }
    
    @Test
    fun testToolChipsScrollable() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test that tool chips are scrollable horizontally
        composeTestRule.onNodeWithText("Ruler").assertExists()
        composeTestRule.onNodeWithText("SetSq 45°").assertExists()
        composeTestRule.onNodeWithText("SetSq 30/60°").assertExists()
        composeTestRule.onNodeWithText("Protractor").assertExists()
        composeTestRule.onNodeWithText("Compass").assertExists()
    }
    
    @Test
    fun testModeChipsScrollable() {
        composeTestRule.setContent {
            DrawingScreen()
        }
        
        // Test that mode chips are scrollable horizontally
        composeTestRule.onNodeWithText("Hand").assertExists()
        composeTestRule.onNodeWithText("Freehand").assertExists()
        composeTestRule.onNodeWithText("Line").assertExists()
        composeTestRule.onNodeWithText("RulerLine").assertExists()
        composeTestRule.onNodeWithText("SquareLine").assertExists()
        composeTestRule.onNodeWithText("Protractor").assertExists()
        composeTestRule.onNodeWithText("Compass").assertExists()
    }
}
