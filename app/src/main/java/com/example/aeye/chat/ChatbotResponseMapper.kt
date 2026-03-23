object ChatbotResponseMapper {

    fun responseForTotalCorrect(totalCorrectLetters: Int): String {
        return when {
            totalCorrectLetters <= 20 ->
                "Your result suggests a significant reduction in visual performance. Consider booking an eye examination soon."

            totalCorrectLetters <= 32 ->
                "Your result suggests some reduction in visual performance. A routine eye examination may be advisable."

            else ->
                "Your result suggests the best visual performance measurable with this test. If you still have symptoms, consider consulting an optician."
        }
    }
}