package ru.hse.multitracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.hse.multitracker.data.database.entities.TestSession

@Dao
interface TestSessionDao {
    @Insert
    suspend fun insertTestSession(session: TestSession): Long

    @Query("SELECT * FROM sessions WHERE patient_id = :patientId ORDER BY date DESC LIMIT 1")
    suspend fun getLastSession(patientId: Long): TestSession?

    @Query("SELECT * from sessions WHERE id = :id")
    suspend fun getSession(id: Long): TestSession
}