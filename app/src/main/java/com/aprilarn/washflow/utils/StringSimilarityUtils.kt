package com.aprilarn.washflow.utils

import kotlin.math.max

object StringSimilarityUtils {

    /**
     * Calculates the Levenshtein distance between two strings.
     * The Levenshtein distance is the minimum number of single-character edits 
     * (insertions, deletions, or substitutions) required to change one word into the other.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        val cost = if (s1[i - 1].equals(s2[j - 1], ignoreCase = true)) 0 else 1
                        dp[i][j] = minOf(
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1,
                            dp[i - 1][j - 1] + cost
                        )
                    }
                }
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Calculates a similarity score between two strings based on Levenshtein distance.
     * @return A value between 0.0 (completely different) and 1.0 (identical).
     */
    fun similarityScore(s1: String, s2: String): Double {
        val str1 = s1.trim()
        val str2 = s2.trim()
        
        if (str1.isEmpty() || str2.isEmpty()) return 0.0
        if (str1.equals(str2, ignoreCase = true)) return 1.0
        
        val maxLength = max(str1.length, str2.length)
        if (maxLength == 0) return 1.0
        
        val distance = levenshteinDistance(str1, str2)
        return 1.0 - (distance.toDouble() / maxLength.toDouble())
    }
}
