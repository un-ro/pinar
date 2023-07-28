package com.unero.pinar.data.model.research

import com.google.firebase.Timestamp

data class FailReport(
    var buildingId: String = "",
    var state: String = "",
    var reason: String = "",
    var mode: String = "",
    var time: Timestamp = Timestamp.now()
)