package com.example.aeye.ai

object VisionRiskPredictor {

    fun predict(features: VisionFeatures): VisionRiskClass {
        return when {
            features.totalCorrectLetters <= 20 ->
                VisionRiskClass.MARKED_REDUCTION_ON_CHART

            features.totalCorrectLetters <= 32 ->
                VisionRiskClass.REDUCED_ON_CHART

            else ->
                VisionRiskClass.BEST_ON_CHART
        }
    }
}