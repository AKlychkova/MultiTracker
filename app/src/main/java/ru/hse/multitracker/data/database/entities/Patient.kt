package ru.hse.multitracker.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name="surname") var surname: String,
    @ColumnInfo(name="patronymic") var patronymic: String?,
    @ColumnInfo(name="age") var age: Int?,
    @ColumnInfo(name="sex") var sex: Boolean?,
    @ColumnInfo(name="diagnosis") var diagnosis: String?
    )