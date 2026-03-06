package com.example.aeye.data.model

data class TestResult(
    val id: String = "",
    val testType: String = "",
    val createdAt: Long = 0L,          // store as epoch millis when reading
    val distanceCm: Int? = null,
    val eye: String? = null,
    val createdAtMillis: Long? = null,

    // logMAR-specific
    val finalLogmar: Double? = null,
    val snellenApprox: String? = null,
    val totalCorrectLetters: Int? = null,
    val totalLetters: Int? = null,

    // calibration/debug
    val pxPerMm: Double? = null
)