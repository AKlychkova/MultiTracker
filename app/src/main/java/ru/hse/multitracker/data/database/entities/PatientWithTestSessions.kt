package ru.hse.multitracker.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PatientWithTestSessions (
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "id",
        entityColumn = "patient_id"
    )
    val sessions: List<TestSession>
)