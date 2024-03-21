package ru.hse.multitracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patient_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TestSession(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "target_amount") val targetAmount: Int,
    @ColumnInfo(name = "total_amount") val totalAmount: Int,
    @ColumnInfo(name = "speed") val speed: Int,
    @ColumnInfo(name = "movement_time") val movementTime: Int,
    @ColumnInfo(name = "accuracy") val accuracy: Double,
    @ColumnInfo(name = "reaction_time") val reactionTime: Int,
    @ColumnInfo(name = "patient_id") val patientId: Int
)