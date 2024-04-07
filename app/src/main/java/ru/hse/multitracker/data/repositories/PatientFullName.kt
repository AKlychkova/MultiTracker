package ru.hse.multitracker.data.repositories

import ru.hse.multitracker.data.database.entities.Patient

/**
 * Model class to represent patient's full name and identifier
 */
data class PatientFullName (
    val id: Long,
    val name: String,
    val surname: String,
    val patronymic: String?
) {
    constructor(patient: Patient) : this (
        patient.id,
        patient.name,
        patient.surname,
        patient.patronymic
    )
}