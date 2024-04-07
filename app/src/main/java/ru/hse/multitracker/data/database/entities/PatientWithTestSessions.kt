package ru.hse.multitracker.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 *  Corresponds to a combining two tables (patients and test sessions) in a one-to-many relationship
 */
data class PatientWithTestSessions (
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "id",
        entityColumn = "patient_id"
    )
    val sessions: List<TestSession>
)