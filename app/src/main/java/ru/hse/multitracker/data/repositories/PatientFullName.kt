package ru.hse.multitracker.data.repositories

import ru.hse.multitracker.data.database.entities.Patient


data class PatientFullName (
    val id: Int,
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