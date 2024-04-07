package ru.hse.multitracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Corresponds to a table of test sessions
 * @property id identifier of session
 * @property date date and time of test session start
 * @property targetAmount amount of target objects
 * @property totalAmount total amount of objects
 * @property speed speed of objects movement
 * @property movementTime time of objects movement in seconds
 * @property accuracy mean answer accuracy by attempts
 * @property reactionTime mean reaction time by attempts
 * @property patientId identifier of patient who was tested
 */
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
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "date") val date: Date,
    @ColumnInfo(name = "target_amount") val targetAmount: Int,
    @ColumnInfo(name = "total_amount") val totalAmount: Int,
    @ColumnInfo(name = "speed") val speed: Int,
    @ColumnInfo(name = "movement_time") val movementTime: Int,
    @ColumnInfo(name = "accuracy") var accuracy: Double,
    @ColumnInfo(name = "reaction_time") var reactionTime: Int,
    @ColumnInfo(name = "patient_id") val patientId: Long
)