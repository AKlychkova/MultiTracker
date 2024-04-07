package ru.hse.multitracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Corresponds to a table of patients
 * @property id identifier of patient
 * @property name patient's first name
 * @property surname patient's last name
 * @property patronymic patient's patronymic (if exists)
 * @property age patient's age (in years)
 * @property sex patient's sex (true - male, false - female)
 * @property diagnosis patient's diagnosis name
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name="surname") var surname: String,
    @ColumnInfo(name="patronymic") var patronymic: String?,
    @ColumnInfo(name="age") var age: Int?,
    @ColumnInfo(name="sex") var sex: Boolean?,
    @ColumnInfo(name="diagnosis") var diagnosis: String?
    )