package com.dnsguard.locker

import java.util.Calendar

/**
 * Feature 8: Daily motivational self-control quotes.
 *
 * Returns a different quote for each calendar day (changes at midnight).
 * The same quote is shown all day for consistency.
 */
object QuoteManager {

    private val QUOTES = listOf(
        "The first step to getting what you want is having the courage to focus on what matters." to "Zig Ziglar",
        "Discipline is choosing between what you want now and what you want most." to "Abraham Lincoln",
        "You don't have to see the whole staircase, just take the first step." to "Martin Luther King Jr.",
        "Self-control is strength. Calmness is mastery." to "Morgan Freeman",
        "The secret of getting ahead is getting started." to "Mark Twain",
        "It does not matter how slowly you go as long as you do not stop." to "Confucius",
        "Strength does not come from physical capacity. It comes from an indomitable will." to "Mahatma Gandhi",
        "The mind is everything. What you think, you become." to "Buddha",
        "We are what we repeatedly do. Excellence is not an act, but a habit." to "Aristotle",
        "Your future self is watching you right now through your memories." to "Aubrey de Grey",
        "Patience and persistence are the true marks of a craftsman." to "Proverb",
        "Focus is saying no to a hundred other good ideas to make one great." to "Steve Jobs",
        "One day or day one — you decide." to "Paulo Coelho",
        "Every single morning brings an unwritten page." to "Unknown",
        "Mastering others is strength; mastering yourself is true power." to "Lao Tzu",
        "Small disciplines repeated with consistency every day lead to monumental achievements." to "John C. Maxwell",
        "Between stimulus and response there is a space. In that space is our power to choose." to "Viktor Frankl",
        "Deep work is the ability to focus without distraction on a cognitively demanding task." to "Cal Newport",
        "You may have to fight a battle more than once to win it." to "Margaret Thatcher",
        "Nothing in the world is worth having unless it means effort, learning, and grit." to "Theodore Roosevelt",
        "The only way to achieve greatness is through deep, undivided attention." to "Seneca",
        "Fall seven times, stand up eight." to "Japanese Proverb",
        "Do what is meaningful, not what is expedient." to "Jordan Peterson",
        "Continuous improvement is better than delayed perfection." to "Mark Twain",
        "Every moment is a fresh beginning to build something meaningful." to "T.S. Eliot",
        "Curiosity and focus are the keys that unlock any door in life." to "Richard Feynman",
        "What lies within us is far greater than what lies behind or before us." to "Ralph Waldo Emerson",
        "The beautiful thing about learning is that no one can take it away from you." to "B.B. King",
        "You are the master of your fate, the captain of your soul." to "William Ernest Henley",
        "Your life does not get better by chance. It gets better by intentional change." to "Jim Rohn"
    )

    data class Quote(val text: String, val author: String)

    /**
     * Returns today's quote, rotating through the list by calendar day.
     * The same quote persists all day; it changes automatically at midnight.
     */
    fun todayQuote(): Quote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val (text, author) = QUOTES[dayOfYear % QUOTES.size]
        return Quote(text, author)
    }

    /** Returns the 0–1 progress through the full quote list (for a small indicator). */
    fun todayIndex(): Int {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return dayOfYear % QUOTES.size
    }

    val totalQuotes: Int get() = QUOTES.size
}
