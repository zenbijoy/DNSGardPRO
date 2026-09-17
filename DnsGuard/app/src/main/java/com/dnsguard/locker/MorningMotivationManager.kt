package com.dnsguard.locker

import java.util.Calendar

/**
 * Morning focus directives and mental models (delivered at 8:00 AM).
 * Primes the mind for high productivity, clarity, and intentional living.
 */
object MorningMotivationManager {

    data class MorningDirective(
        val title: String,
        val prompt: String,
        val action: String
    )

    private val DIRECTIVES = listOf(
        MorningDirective(
            title = "The One Thing",
            prompt = "What is the single most important objective you must accomplish today that makes everything else easier or unnecessary?",
            action = "Identify it right now, write it down, and tackle it during your first 90 minutes."
        ),
        MorningDirective(
            title = "Deep Work Protocol",
            prompt = "The ability to focus deeply without distraction is becoming the rarest and most valuable skill in the modern economy.",
            action = "Protect your morning. Silence notifications and work in a state of flow."
        ),
        MorningDirective(
            title = "The Gateway of the Mind",
            prompt = "The thoughts and media you consume in the first hour of waking set the emotional trajectory for your entire day.",
            action = "Feed your mind wisdom, silence, and strategy before allowing the world in."
        ),
        MorningDirective(
            title = "Amor Fati (Love of Fate)",
            prompt = "Expect friction, unexpected demands, and challenges today. They are not obstacles; they are the training ground.",
            action = "Respond to setbacks with calm curiosity instead of frustration."
        ),
        MorningDirective(
            title = "The Craftsman Standard",
            prompt = "Whatever you do today — coding, studying, reading, or training — do it with intentional excellence.",
            action = "Take pride in the unseen details of your work today."
        ),
        MorningDirective(
            title = "Memento Mori",
            prompt = "You have been granted a brand new 24-hour cycle. It is an unrepeatable gift.",
            action = "Live today deliberately so you can look back tonight with pure peace."
        ),
        MorningDirective(
            title = "Internal Locus of Control",
            prompt = "You cannot control the weather, external opinions, or the economy. You control 100% of your effort, focus, and reaction.",
            action = "Direct all your energy solely toward what lies within your power."
        )
    )

    fun getTodayDirective(): MorningDirective {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return DIRECTIVES[dayOfYear % DIRECTIVES.size]
    }
}
