package com.unero.pinar.utils

// Firebase Path
const val BUILDING_PATH = "buildings"
const val POI_PATH = "poi"
const val REPORT_PATH = "monitorTracking"

// Regex Pattern
val pathPattern = "path\\.\\d{1,8}\\.\\d{0,9}".toRegex()
val startPattern = "start\\d{1,8}".toRegex()