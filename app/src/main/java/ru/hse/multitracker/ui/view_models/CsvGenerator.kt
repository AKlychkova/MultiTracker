package ru.hse.multitracker.ui.view_models

import ru.hse.multitracker.data.database.entities.TestSession

class CsvGenerator {
    companion object {
        private fun toCsvString(session: TestSession): String {
            return "${session.id}," +
                    "${session.date}," +
                    "${session.totalAmount}," +
                    "${session.targetAmount}," +
                    "${session.speed}," +
                    "${session.movementTime}," +
                    "${session.accuracy}," +
                    "${session.reactionTime}\n"
        }

        fun generateCsv(sessions: List<TestSession>): String {
            val csv = StringBuilder()
            csv.append("id,date,total,target,speed,mov_time,accuracy,react_time\n")
            for (session in sessions) {
                csv.append(toCsvString(session))
            }
            return csv.toString()
        }
    }
}