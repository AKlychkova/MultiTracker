package ru.hse.multitracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.hse.multitracker.data.database.entities.TestSession

@Dao
interface TestSessionDao {
    /**
     * insert test session to the database
     * @param session test session to insert
     */
    @Insert
    suspend fun insertTestSession(session: TestSession): Long

    /**
     * delete test session from the database
     * @param session test session to delete
     */
    @Delete
    suspend fun deleteTestSession(session: TestSession)

    /**
     * update test session data in the database
     * @param session test session to update
     */
    @Update
    suspend fun updateTestSession(session: TestSession)

    /**
     * @param patientId patient's identifier
     * @return patient's last test session or null if there are not this patient's test sessions
     */
    @Query("SELECT * FROM sessions WHERE patient_id = :patientId ORDER BY date DESC LIMIT 1")
    suspend fun getLastSession(patientId: Long): TestSession?

    /**
     * @param id test session identifier
     * @return test session associated with this identifier
     */
    @Query("SELECT * from sessions WHERE id = :id")
    suspend fun getSession(id: Long): TestSession
}