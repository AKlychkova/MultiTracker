package ru.hse.multitracker

import android.app.Application
import ru.hse.multitracker.data.database.AppDatabase
import ru.hse.multitracker.data.repositories.PatientRepository
import ru.hse.multitracker.data.repositories.TestSessionRepository

class MultiTrackerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val patientRepository by lazy { PatientRepository(database.patientDao()) }
    val testSessionRepository by lazy { TestSessionRepository(database.testSessionDao()) }
}