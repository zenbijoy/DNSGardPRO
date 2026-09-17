package com.dnsguard.locker

import android.content.Context

/**
 * Tracks milestone achievements and journey progression across the 1-year timeline.
 * Focuses purely on consistency, cognitive clarity, and mental discipline.
 */
object MilestoneManager {

    data class Milestone(
        val day: Int,
        val title: String,
        val subtitle: String,
        val badgeEmoji: String,
        val description: String
    )

    val ALL_MILESTONES = listOf(
        Milestone(
            day = 1,
            title = "The Decision",
            subtitle = "Day 1 Complete",
            badgeEmoji = "🌱",
            description = "The courage to begin is the hardest part. You drew a line in the sand."
        ),
        Milestone(
            day = 7,
            title = "The Awakening",
            subtitle = "7 Days of Clarity",
            badgeEmoji = "⚡",
            description = "One full week of uninterrupted mental focus. The fog begins to lift."
        ),
        Milestone(
            day = 14,
            title = "Neural Reset",
            subtitle = "14 Days",
            badgeEmoji = "🧠",
            description = "Dopamine receptors begin natural re-sensitization. Everyday life feels richer."
        ),
        Milestone(
            day = 30,
            title = "Iron Discipline",
            subtitle = "30 Days Milestone",
            badgeEmoji = "🛡️",
            description = "One full month. You have laid the concrete foundation of a new character."
        ),
        Milestone(
            day = 60,
            title = "The New Baseline",
            subtitle = "60 Days",
            badgeEmoji = "🔥",
            description = "Concentration is no longer a struggle; it is becoming your default state."
        ),
        Milestone(
            day = 90,
            title = "Rewired Mind",
            subtitle = "90 Days Complete",
            badgeEmoji = "💎",
            description = "Three months of victory. Prefrontal cortex executive function is restored."
        ),
        Milestone(
            day = 180,
            title = "Master of Self",
            subtitle = "Half-Year Mark",
            badgeEmoji = "⚔️",
            description = "Six months of unbroken self-control. You command your thoughts and actions."
        ),
        Milestone(
            day = 365,
            title = "Transcendent Freedom",
            subtitle = "1 Full Year",
            badgeEmoji = "👑",
            description = "A complete revolution of self. Total autonomy, peace, and permanent mastery."
        )
    )

    /** Computes elapsed days since locking began. */
    fun getElapsedDays(context: Context): Int {
        if (!TimerManager.isStarted(context)) return 0
        val remainingMs = TimerManager.getRemainingTime(context)
        val elapsedMs = (TimerManager.ONE_YEAR_MS - remainingMs).coerceAtLeast(0L)
        return (elapsedMs / (1_000L * 60 * 60 * 24)).toInt()
    }

    /** Returns list of all milestones with their unlocked status. */
    fun getMilestoneProgress(context: Context): List<Pair<Milestone, Boolean>> {
        val elapsedDays = getElapsedDays(context)
        return ALL_MILESTONES.map { milestone ->
            milestone to (elapsedDays >= milestone.day)
        }
    }

    /** Returns the next milestone to be reached. */
    fun getNextMilestone(context: Context): Milestone? {
        val elapsedDays = getElapsedDays(context)
        return ALL_MILESTONES.firstOrNull { it.day > elapsedDays }
    }
}
