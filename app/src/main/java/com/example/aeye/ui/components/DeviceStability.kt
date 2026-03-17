package com.example.aeye.ui.components

import android.content.Context
import android.content.res.Configuration

private const val MIN_SUPPORTED_PX_PER_MM = 12.0

fun isTabletDevice(context: Context): Boolean {
    return (context.resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

fun isSupportedForLogmar(
    context: Context,
    pxPerMm: Double?
): Boolean {
    if (isTabletDevice(context)) return false
    if (pxPerMm == null || pxPerMm <= 0.0) return true
    return pxPerMm >= MIN_SUPPORTED_PX_PER_MM
}

fun logmarSupportReason(
    context: Context,
    pxPerMm: Double?
): String? {
    return when {
        isTabletDevice(context) ->
            "This prototype currently supports smartphones only for LogMAR testing."

        pxPerMm == null || pxPerMm <= 0.0 ->
            "Screen calibration is required before LogMAR testing."

        pxPerMm < MIN_SUPPORTED_PX_PER_MM ->
            "This device screen density is too low for reliable LogMAR testing."

        else -> null
    }
}