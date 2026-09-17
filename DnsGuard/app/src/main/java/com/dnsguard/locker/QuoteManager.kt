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
        "Discipline is choosing between what you want now and what you want most." to "Abraham Lincoln",
        "Between stimulus and response there is a space. In that space is our power to choose." to "Viktor Frankl",
        "We must all suffer one of two things: the pain of discipline or the pain of regret." to "Jim Rohn",
        "He who conquers himself is the mightiest warrior." to "Confucius",
        "No man is free who is not master of himself." to "Epictetus",
        "You have power over your mind — not outside events. Realize this, and you will find strength." to "Marcus Aurelius",
        "Dopamine is not about pleasure; it is about the anticipation of pleasure. Master the trigger, master your brain." to "Dr. Andrew Huberman",
        "The urge is just a wave. You do not have to drown in it; you can surf it." to "Dr. Alan Marlatt",
        "First say to yourself what you would be; and then do what you have to do." to "Epictetus",
        "Do not pray for an easy life; pray for the strength to endure a difficult one." to "Bruce Lee",
        "The secret of freedom lies in educating people, whereas the secret of tyranny is in keeping them ignorant." to "Maximilien Robespierre",
        "Self-control is strength. Right thought is mastery. Calmness is power." to "James Allen",
        "We are what we repeatedly do. Excellence, then, is not an act, but a habit." to "Aristotle",
        "If you want to conquer the world, you must first conquer yourself." to "Fyodor Dostoevsky",
        "The soul becomes dyed with the color of its thoughts." to "Marcus Aurelius",
        "The greatest victory is victory over oneself." to "Plato",
        "What man actually needs is not a tensionless state but the striving and struggling for a worthwhile goal." to "Viktor Frankl",
        "Comfort is the enemy of achievement." to "Farrah Gray",
        "Waste no more time arguing about what a good man should be. Be one." to "Marcus Aurelius",
        "It is not death that a man should fear, but he should fear never beginning to live." to "Marcus Aurelius",
        "The chains of habit are too light to be felt until they are too heavy to be broken." to "Warren Buffett",
        "Don't count the days, make the days count." to "Muhammad Ali",
        "Freedom is not the absence of commitments, but the ability to choose — and commit to — what is best for me." to "Paulo Coelho",
        "Difficulties strengthen the mind, as labor does the body." to "Seneca",
        "There is nothing outside of yourself that can ever enable you to get better, stronger, richer, quicker, or smarter. Everything is within." to "Miyamoto Musashi",
        "A man who suffers before it is necessary, suffers more than is necessary." to "Seneca",
        "You don't rise to the level of your goals, you fall to the level of your systems." to "James Clear",
        "Every action you take is a vote for the type of person you wish to become." to "James Clear",
        "Cravings will pass whether you give in to them or not. The difference is who you become on the other side." to "Recovery Truth",
        "True freedom is impossible without a mind made free by discipline." to "Mortimer J. Adler",
        "When you control your mind, you control your life." to "David Goggins",
        "In the midst of chaos, there is also opportunity." to "Sun Tzu",
        "The first and best victory is to conquer self." to "Plato",
        "Fall seven times, stand up eight." to "Japanese Proverb",
        "Do what is meaningful, not what is expedient." to "Jordan Peterson",
        "I am not what happened to me, I am what I choose to become." to "Carl Jung",
        "Hold yourself responsible for a higher standard than anybody else expects of you." to "Henry Ward Beecher",
        "Continuous improvement is better than delayed perfection." to "Mark Twain",
        "The cave you fear to enter holds the treasure you seek." to "Joseph Campbell",
        "Only the disciplined ones in life are free. If you are undisciplined, you are a slave to your moods and passions." to "Eliud Kipchoge",
        "Mastering others is strength; mastering yourself is true power." to "Lao Tzu",
        "He who has a why to live can bear almost any how." to "Friedrich Nietzsche",
        "Your future self is depending on you not to quit today." to "Unknown",
        "One day or day one — you decide." to "Paulo Coelho",
        "Nothing in the world can take the place of persistence." to "Calvin Coolidge",
        "If you are going through hell, keep going." to "Winston Churchill",
        "Small disciplines repeated with consistency every day lead to monumental achievements." to "John C. Maxwell",
        "The happiness of your life depends upon the quality of your thoughts." to "Marcus Aurelius",
        "Strength does not come from winning. Your struggles develop your strengths." to "Arnold Schwarzenegger",
        "It always seems impossible until it's done." to "Nelson Mandela",
        "You must be willing to give up who you are to become who you want to be." to "Albert Einstein",
        "Patience is bitter, but its fruit is sweet." to "Aristotle",
        "Do not let your fire go out, spark by irreplaceable spark in the hopeless swamps of the not-quite, the not-yet, and the not-at-all." to "Ayn Rand",
        "A river cuts through rock not because of its power, but because of its persistence." to "Jim Watkins",
        "Self-discipline is the magic power that makes you virtually unstoppable." to "Dan Kennedy",
        "The mind is a superb instrument if used rightly. Used wrongly, however, it becomes very destructive." to "Eckhart Tolle",
        "Character is doing the right thing when nobody is looking." to "J.C. Watts",
        "You cannot change your destination overnight, but you can change your direction overnight." to "Jim Rohn",
        "He who fears he will suffer, already suffers because he fears." to "Michel de Montaigne",
        "Victory comes from finding opportunities in problems." to "Sun Tzu",
        "You are today where your thoughts have brought you; you will be tomorrow where your thoughts take you." to "James Allen",
        "The impediment to action advances action. What stands in the way becomes the way." to "Marcus Aurelius",
        "Today I will do what others won't, so tomorrow I can accomplish what others can't." to "Jerry Rice",
        "The man who moves a mountain begins by carrying away small stones." to "Confucius",
        "If you want light to come into your life, you need to stand where it is shining." to "Guy Finley",
        "To enjoy good health, to bring true happiness to one's family, to bring peace to all, one must first discipline and control one's own mind." to "Buddha",
        "Act as if what you do makes a difference. It does." to "William James",
        "The only person you are destined to become is the person you decide to be." to "Ralph Waldo Emerson",
        "Our greatest glory is not in never falling, but in rising every time we fall." to "Confucius",
        "Do not spoil what you have by desiring what you have not." to "Epicurus",
        "Never surrender what you want most for what you want now." to "Anonymous",
        "A disciplined mind brings happiness." to "Buddha"
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

    fun getRandomQuote(): Quote {
        val randomIndex = (0 until QUOTES.size).random()
        val (text, author) = QUOTES[randomIndex]
        return Quote(text, author)
    }

    val totalQuotes: Int get() = QUOTES.size
}
