package com.unero.pinar.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unero.pinar.data.model.research.FailReport
import com.unero.pinar.domain.repository.ReportRepository
import com.unero.pinar.utils.REPORT_PATH

class ReportRepositoryImpl: ReportRepository {
    private val db = Firebase.firestore
    private val rootCollection = db.collection(REPORT_PATH)

    override suspend fun newReport(report: FailReport): Task<DocumentReference> =
        rootCollection.add(report)
}