package com.aprilarn.washflow.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StringSimilarityUtilsTest {

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, StringSimilarityUtils.levenshteinDistance("John", "John"))
        assertEquals(0, StringSimilarityUtils.levenshteinDistance("John", "john"))
        assertEquals(1, StringSimilarityUtils.levenshteinDistance("John", "Johnn"))
        assertEquals(1, StringSimilarityUtils.levenshteinDistance("John", "Jon"))
        assertEquals(3, StringSimilarityUtils.levenshteinDistance("John", "Jack"))
        
        // Kirto vs Kirito
        assertEquals(1, StringSimilarityUtils.levenshteinDistance("Kirto", "Kirito"))
    }

    @Test
    fun testSimilarityScore() {
        assertEquals(1.0, StringSimilarityUtils.similarityScore("John", "John"), 0.001)
        
        // Slight typo
        val score1 = StringSimilarityUtils.similarityScore("John", "Johnn")
        assertTrue(score1 >= 0.8)
        
        // Trimming
        assertEquals(1.0, StringSimilarityUtils.similarityScore(" John ", "john"), 0.001)
    }

    @Test
    fun testUserReportedCases() {
        // Raphaels vs Raphael
        val scoreRaphael = StringSimilarityUtils.similarityScore("Raphaels", "Raphael")
        assertTrue("Raphaels should match Raphael: $scoreRaphael", scoreRaphael > 0.5)
        
        // 124 vs 123
        val scorePhone = StringSimilarityUtils.similarityScore("124", "123")
        assertTrue("124 should match 123: $scorePhone", scorePhone > 0.5) // Score 1 - 1/3 = 0.66
        
        // Kirto vs Kirito
        val scoreKirito = StringSimilarityUtils.similarityScore("Kirto", "Kirito")
        assertTrue("Kirto should match Kirito: $scoreKirito", scoreKirito > 0.5) // Score 1 - 1/6 = 0.83
    }
}
