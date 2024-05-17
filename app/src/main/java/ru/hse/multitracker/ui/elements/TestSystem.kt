package ru.hse.multitracker.ui.elements

import ru.hse.multitracker.data.database.entities.TestSession

/**
 * @param session current test session
 * @param totalAttemptCount total amount of attempts (default = 5)
 */
class TestSystem(val session: TestSession, val totalAttemptCount: Int = 5) {
    // the number of attempts that have passed
    private var attemptCount: Int = 0

    // the number of right answers
    private var rightAnswersCount: Int = 0

    // sum of reaction time in seconds
    private var sumReactionTime: Int = 0

    // amount of objects have been clicked in this attempt
    private var objectsClicked: Int = 0

    /**
     * Handle right answer
     * @return if attempt has been finished
     */
    fun rightAnswer() : Boolean {
        // increment right answers number
        rightAnswersCount += 1
        // increment objects clicked number
        objectsClicked += 1
        return isFinished()
    }

    /**
     * Handle worse answer
     * @return if attempt has been finished
     */
    fun mistakeAnswer() : Boolean {
        // increment objects clicked number
        objectsClicked += 1
        return isFinished()
    }

    /**
     * Check if attempt has been finished
     */
    private fun isFinished(): Boolean {
        return objectsClicked >= session.targetAmount
    }

    /**
     * @param reactionTime reaction time in finished attempt
     * @return if all attempts are finished
     */
    fun finishAttempt(reactionTime: Int) : Boolean {
        // add reaction time to sumReactionTime
        sumReactionTime += reactionTime
        // increment finished attempts count
        attemptCount += 1
        // clear clicked objects amount
        objectsClicked = 0
        // check if all attempts are finished
        return attemptCount >= totalAttemptCount
    }

    /**
     * @return mean accuracy for finished attempts
     */
    fun getMeanAccuracy() : Double {
        return rightAnswersCount.toDouble() / (session.targetAmount * attemptCount)
    }

    /**
     * @return mean reaction time for finished attempts
     */
    fun getMeanReactionTime() : Int {
        return sumReactionTime / attemptCount
    }
}