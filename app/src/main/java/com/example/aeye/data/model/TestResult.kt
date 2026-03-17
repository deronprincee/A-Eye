package com.example.aeye.data.model

data class TestResult(
    val id: String = "",
    val testType: String = "",
    val createdAt: Long = 0L,
    val eye: String? = null,
    val createdAtMillis: Long? = null,

    // logMAR-specific
    val finalLogmar: Double? = null,
    val snellenApprox: String? = null,
    val totalCorrectLetters: Int? = null,
    val totalLetters: Int? = null,
    val correctPerLine: List<Int> = emptyList(),
    val inputMode: String? = null,
    val lastAttemptedRowLogmar: Double? = null,
    val lastPassedRowLogmar: Double? = null,

    // calibration/debug
    val pxPerMm: Double? = null
)