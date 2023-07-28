package com.unero.pinar.domain.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.unero.pinar.data.model.research.FailReport

interface ReportRepository {
    suspend fun newReport(report: FailReport): Task<DocumentReference>
}