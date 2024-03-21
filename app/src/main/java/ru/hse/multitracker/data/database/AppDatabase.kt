package ru.hse.multitracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.hse.multitracker.data.database.entities.Patient
import ru.hse.multitracker.data.database.entities.TestSession

@Database(entities = [Patient::class, TestSession::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun testSessionDao(): TestSessionDao
}