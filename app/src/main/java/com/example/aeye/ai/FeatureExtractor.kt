package com.example.aeye.ai

import com.example.aeye.data.model.TestResult

object FeatureExtractor {
    fun fromTestResult(result: TestResult): VisionFeatures {
        return VisionFeatures(
            totalCorrectLetters = result.totalCorrectLetters ?: 0
        )
    }
}