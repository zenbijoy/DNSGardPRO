package com.dnsguard.locker

import java.util.Calendar

/**
 * Curated repository of 50 transformative stories across 5 core categories:
 *  1. 🛡️ Breaking the Chains (Overcoming Addiction)
 *  2. 🧠 The Power of Discipline
 *  3. 🔥 Resilience and Falling Down
 *  4. 🧘 Mindset and Self-Control
 *  5. 🌱 Rebirth and Purpose
 *
 * Designed with real-time emojis, narrative parables, and punchy daily morals.
 */
object NightlyMotivationManager {

    data class Story(
        val id: Int,
        val category: String,
        val categoryEmoji: String,
        val title: String,
        val emoji: String,
        val body: String,
        val moral: String
    )

    val ALL_STORIES: List<Story> = listOf(
        // ── Category 1: Breaking the Chains ────────────────────────────────────
        Story(
            id = 1,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Moth and the Flame",
            emoji = "🕯️",
            body = "A moth kept flying toward a bright candle flame, singing its delicate wings. An elder moth called out: 'The flame looks dazzling and warm, but its nature is only to consume and destroy.' The young moth paused, turned back into the cool night air, and lived to fly beneath the stars.",
            moral = "Your addiction dresses up as pleasure, but it only destroys your potential. Stay far away from the flame."
        ),
        Story(
            id = 2,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Elephant and the Rope",
            emoji = "🐘",
            body = "As a small baby, an elephant was tied to a wooden peg by a thick rope. He pulled and strained for weeks but could not break free. Years later, as a 5-ton adult capable of uprooting trees, he remained tied to the same tiny rope without ever attempting to escape — because he still believed he was powerless.",
            moral = "You are immeasurably stronger today than when you first fell. The invisible rope is an illusion. Break free."
        ),
        Story(
            id = 3,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Two Wolves",
            emoji = "🐺",
            body = "An elder told his grandson: 'There is a fierce battle waging inside every human heart between two wolves. One wolf is impulsive indulgence, weakness, and regret. The other wolf is discipline, clarity, and peace.' The boy asked: 'Grandfather, which wolf wins?' The elder answered: 'The one you feed.'",
            moral = "Every single time you resist an urge, you starve the destructive wolf and nourish your master self."
        ),
        Story(
            id = 4,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Empty Bottle",
            emoji = "🍾",
            body = "A man carried an empty glass bottle in his bag wherever he journeyed. When curious strangers asked why, he smiled gently: 'This was the last bottle of poison I drank before I took my life back. I keep it empty to remind myself that I emptied it — it did not empty me.'",
            moral = "Let your past mistakes stand as a badge of your endurance, never as the definition of your identity."
        ),
        Story(
            id = 5,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Monk and the River",
            emoji = "🌊",
            body = "Two traveling monks arrived at a swollen river where a woman was unable to cross. The senior monk lifted her onto his shoulders, carried her through the rapids, placed her gently on dry ground, and walked on. Hours later, the younger monk burst out: 'Brother, our vows forbid physical contact!' The master smiled: 'I set her down hours ago by the riverbank. Why are you still carrying her in your mind?'",
            moral = "Your past urges and mistakes belong on yesterday's riverbank. Put them down and walk forward in peace."
        ),
        Story(
            id = 6,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Forest Trail",
            emoji = "🌲",
            body = "A traveler walked through a thick forest every single day for five years, stamping down the earth until a deep, wide path formed. One day, he resolved never to walk that direction again. Over seasons of rain and sunshine, thick grass, wildflowers, and shrubs grew over the dirt. Soon, the path vanished completely.",
            moral = "Your neural pathways for compulsive behavior will wither and vanish if you simply refuse to walk down them."
        ),
        Story(
            id = 7,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Fence and the Nails",
            emoji = "🪵",
            body = "A father gave his son a hammer and told him to drive a nail into the wooden fence every time he gave in to an impulse. Weeks later, the father instructed him to pull out one nail for every day of complete self-control. When the fence was cleared, the father said: 'The nails are gone, but look at the holes. The scars remain.'",
            moral = "Giving in damages your sacred self-respect. You cannot undo the past, but you have the power to stop hammering new scars."
        ),
        Story(
            id = 8,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Lone King",
            emoji = "♟️",
            body = "In a championship chess match, a grandmaster lost every piece except his solitary King against an army of pawns and rooks. Instead of tipping his king in resignation, he calculated with supreme calmness, maneuvering around traps for 50 grueling moves until his opponent blundered, securing an impossible victory.",
            moral = "Even when you feel exhausted and stripped of momentum, as long as you refuse to surrender, victory remains yours."
        ),
        Story(
            id = 9,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Relentless Water Drop",
            emoji = "💧",
            body = "A single water drop landed upon a scorching desert boulder and evaporated in a hiss of steam. Another drop fell, and another, day after day, year after year. Slowly, the relentless microscopic impact carved a groove, seeped into the grain, and split the granite wide open.",
            moral = "Consistency crushes intensity every time. Win today with a single drop of discipline."
        ),
        Story(
            id = 10,
            category = "Breaking the Chains",
            categoryEmoji = "🛡️",
            title = "The Unlocked Cell",
            emoji = "🚪",
            body = "A prisoner spent twenty years sitting inside a dark cell, convinced the heavy iron door was locked from the outside. One morning, a guard whispered through the bars: 'Friend, the bolt was never turned. The latch has been open since the day you arrived.' The man pushed the door gently — and stepped out into the sunlight.",
            moral = "You are not a helpless prisoner to your urges. The power to walk into freedom has been in your hands all along."
        ),

        // ── Category 2: The Power of Discipline ────────────────────────────────
        Story(
            id = 11,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Chinese Bamboo",
            emoji = "🎋",
            body = "After a farmer plants a Chinese bamboo seed, he waters and fertilizes it for four years without seeing a single sprout. But in the fifth year, a green shoot breaks through the earth and skyrockets 90 feet into the air in just six weeks. For four years, it was silently engineering an underground root fortress.",
            moral = "Your brain is actively rewiring itself right now, even when you cannot see the surface results. Trust the roots."
        ),
        Story(
            id = 12,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The 101st Hammer Strike",
            emoji = "🔨",
            body = "A stonecutter hammers against a massive granite rock one hundred times without producing a single visible hairline crack. Yet on the hundred and first blow, the stone suddenly splits into two clean halves. He knows it was not the last strike that split the boulder, but the one hundred silent strikes before it.",
            moral = "Every time you say 'NO' to a momentary urge, you are delivering another decisive blow against the rock."
        ),
        Story(
            id = 13,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Unshakable Conductor",
            emoji = "🚄",
            body = "A bullet train conductor in Japan maintained an unbroken 30-year record of never departing or arriving even sixty seconds behind schedule. When asked for his secret philosophy, he replied simply: 'I do not rely on mood or inspiration. I perform the exact same standard regardless of whether it rains, snows, or shines.'",
            moral = "Discipline is the superpower of doing the essential work even when your feelings argue against it."
        ),
        Story(
            id = 14,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Master Painter",
            emoji = "🎨",
            body = "An ancient painter sat before the same pine tree every single dawn for ten years, painting it upon rice paper. On the 3,650th morning, his brushstrokes were so alive with truth and depth that wild songbirds flew down from the sky and attempted to perch upon the painted branches.",
            moral = "Transcendent mastery is born from monotonous, daily fidelity — never from sporadic bursts of enthusiasm."
        ),
        Story(
            id = 15,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "Running in the Downpour",
            emoji = "🌧️",
            body = "An Olympic marathoner was spotted doing intervals at 4:30 AM in freezing rain. A passerby asked why he didn't train in a warm gym instead. The runner wiped rain from his eyes and answered: 'Because right now, my competition is choosing the comfort of their bed.'",
            moral = "When the temptation to quit strikes hardest, that is precisely where the championship of your life is won."
        ),
        Story(
            id = 16,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Morning Standard",
            emoji = "🛏️",
            body = "A celebrated naval commander was asked how high-stakes commanders are forged. He answered: 'It begins the moment your eyes open. If you cannot discipline yourself to make your bed with tight military corners, how can you expect to command a warship through a tempest?'",
            moral = "Monumental victories are built on microscopic habits. Master the small actions first."
        ),
        Story(
            id = 17,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Tree-to-Tree Rule",
            emoji = "🌳",
            body = "A man who struggled to jog even a quarter-mile committed to running a 26-mile marathon. When his chest burned, he told himself: 'Just run to that oak tree ahead.' When he reached it, he said: 'Now run to the lamppost.' Step by step, tree by tree, twelve months later he crossed the finish line.",
            moral = "Do not stare at the towering 365-day mountain. Narrow your sight solely to conquering today."
        ),
        Story(
            id = 18,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "Three Feet from Water",
            emoji = "⛏️",
            body = "A pioneer dug a well for weeks in parched earth. Exhausted and discouraged by dry dust, he threw down his shovel and walked away. The next traveler picked up the shovel, dug merely three feet deeper, and struck a roaring subterranean spring of crystal clear water.",
            moral = "Never surrender when you are exhausted. Breakthroughs often hide directly behind your greatest frustration."
        ),
        Story(
            id = 19,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The 10-Minute Scholar",
            emoji = "⏱️",
            body = "A young student committed to studying advanced mathematics for only ten undisturbed minutes every single morning, while his classmates pulled chaotic all-nighters before exams. After four years, his mathematical depth far outstripped all his peers combined.",
            moral = "The compound interest of unbroken daily discipline is a force that nothing on earth can stop."
        ),
        Story(
            id = 20,
            category = "The Power of Discipline",
            categoryEmoji = "🧠",
            title = "The Foggy Summit",
            emoji = "🧗",
            body = "A mountaineer climbed through dense alpine fog. If he looked up, he saw only vertigo and sheer cliff faces. Instead, he fixed his gaze entirely on the six inches where his boot met the rock. When he finally looked up, he was standing above the clouds upon the sunlit peak.",
            moral = "Focus entirely on placing your next step with honor. The summit will take care of itself."
        ),

        // ── Category 3: Resilience and Falling Down ────────────────────────────
        Story(
            id = 21,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "Edison's 10,000 Attempts",
            emoji = "💡",
            body = "Thomas Edison conducted nearly ten thousand unsuccessful experiments while trying to invent the incandescent light bulb. When a reporter asked if he felt like a failure, Edison looked astonished: 'I have not failed once! I have successfully discovered 9,999 materials that do not work.'",
            moral = "Every past slip is intelligence and data. Study what triggered it, fortify your defense, and rise."
        ),
        Story(
            id = 22,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Toddler's First Steps",
            emoji = "👶",
            body = "A baby learning to walk stumbles and falls to the carpet over a hundred times. Not once does the child sit down, fold its arms, and conclude: 'Walking is simply not for me; I am destined to crawl forever.' The child laughs, grasps the table leg, and stands again.",
            moral = "Falling is the natural anatomy of learning. Never let a stumble convince you to crawl."
        ),
        Story(
            id = 23,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Flight of the Phoenix",
            emoji = "🦅",
            body = "In ancient myth, the noble Phoenix reaches the end of its life cycle, willingly builds a nest of aromatic cinnamon twigs, and allows itself to be consumed by flame. Out of the smoldering ash, a younger, stronger, and more luminous creature rises into the sky.",
            moral = "Your old destructive habits have burned you to the ground. Now rise from those ashes as a sovereign man."
        ),
        Story(
            id = 24,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The 9,000 Missed Shots",
            emoji = "🏀",
            body = "Michael Jordan, the greatest basketball player in history, admitted: 'I have missed more than 9,000 shots in my career. I have lost almost 300 games. Twenty-six times, I have been trusted to take the game-winning shot and missed. I have failed over and over again. That is why I succeed.'",
            moral = "Resilience in the wake of hardship is the golden currency required for true mastery."
        ),
        Story(
            id = 25,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Blown-Off Course",
            emoji = "⛵",
            body = "A merchant ship lost its compass in a fierce hurricane and drifted aimlessly across unknown seas for weeks. The captain thought all was lost until the ship bumped gently against the shores of a lush, undiscovered island brimming with spices and gold.",
            moral = "Your past hardships and detours can lead directly to the deepest breakthroughs of your life."
        ),
        Story(
            id = 26,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Kintsugi Vessel",
            emoji = "🏺",
            body = "When a priceless ceramic bowl shatters in Japan, master craftsmen do not throw the shards away. They mend every fracture with a lacquer dusted with pure powdered gold. The repaired bowl becomes significantly more beautiful and prized than when it was unbroken.",
            moral = "Your battle with weakness does not make you flawed. Your hard-won victory makes your spirit pure gold."
        ),
        Story(
            id = 27,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Ten Defeats",
            emoji = "🤼",
            body = "A wrestler suffered ten straight tournament defeats in a single season. Sitting defeated in the locker room, his coach gripped his shoulder: 'Now that you know every possible way a man can be thrown, you are finally ready to learn how to throw.' The following year, he won the national title.",
            moral = "Defeat is a masterclass in strategy, never a permanent verdict on your potential."
        ),
        Story(
            id = 28,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Churned Cream",
            emoji = "🐸",
            body = "Two frogs accidentally leaped into a deep pail of heavy cream. Seeing the slippery metal walls, the first frog despaired: 'We cannot climb out,' stopped swimming, and sank. The second frog kicked his legs vigorously without stopping. By morning, his frantic kicking had churned the cream into solid butter, and he hopped out.",
            moral = "Never stop kicking. Relentless effort will eventually create solid ground beneath your feet."
        ),
        Story(
            id = 29,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The Slip on the Rung",
            emoji = "🪜",
            body = "A carpenter working on a church roof slipped his boot off a wet ladder rung. He did not let go of both hands and hurl himself to the ground in despair; he tightened his grip on the side rails, found his footing, and continued climbing upward.",
            moral = "A hard day or close call is merely a slip of the foot. Never let a momentary slip become a reckless slide."
        ),
        Story(
            id = 30,
            category = "Resilience and Falling Down",
            categoryEmoji = "🔥",
            title = "The River's Blade",
            emoji = "🏞️",
            body = "Deep in the canyon, a river carves through hundreds of feet of solid granite. It does not accomplish this feat through sudden explosive force, but through centuries of quiet, unbroken, patient persistence.",
            moral = "Quiet, persistent consistency will dissolve the hardest obstacles in your character."
        ),

        // ── Category 4: Mindset and Self-Control ────────────────────────────────
        Story(
            id = 31,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Empty Boat",
            emoji = "🛶",
            body = "A fisherman was resting on a misty lake when another boat slammed violently into his hull. Enraged, he stood up shouting, ready to fight the reckless oarsman. But when he looked through the mist, he saw the other vessel was completely empty, simply drifting with the current. His anger vanished in a second.",
            moral = "Your urges are empty drifting boats. They are meaningless physiological ripples, not commands you must obey."
        ),
        Story(
            id = 32,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Sacks of Gravel",
            emoji = "🎒",
            body = "A traveler trudged along a mountain trail groaning under the crushing weight of a backpack filled with jagged rocks. When a sage asked why he didn't unclip the straps, the man looked bewildered: 'I have carried this bag so long, I forgot I was the one who packed it.'",
            moral = "Compulsive habits are heavy sacks of dead weight. Unclip the buckle, drop the bag, and breathe."
        ),
        Story(
            id = 33,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Samurai's Ten Counts",
            emoji = "⚔️",
            body = "A legendary swordsman lived by a golden discipline: whenever provoked or filled with sudden adrenaline, he paused and counted to ten before his hand even touched the hilt of his blade. In fifty years of combat, he never made a foolish stroke.",
            moral = "Intense cravings crest and die within 5 to 10 minutes. If you have the wisdom to pause, you win."
        ),
        Story(
            id = 34,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Two Travelers and the Tiger",
            emoji = "🐅",
            body = "Two men resting by a jungle path heard the low roar of a tiger. The first man froze in terror. The second calmly opened his pack and laced up his running shoes. 'You fool, you cannot outrun a tiger!' cried the first. The second replied: 'I do not need to outrun the tiger. I just need to move forward with purpose.'",
            moral = "Stop obsessing over other people or the scale of the obstacle. Focus purely on your personal forward motion."
        ),
        Story(
            id = 35,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "Stillness in the Storm",
            emoji = "🏹",
            body = "In the middle of a screaming battlefield where dust choked the sky and arrows rained from every quadrant, a Zen master sat in lotus posture beneath a tree, breathing slow and rhythmic. The arrows struck the earth around him, but not a single hair of his head trembled.",
            moral = "A calm, centered mind is the ultimate armor. When chaos screams, anchor yourself in quiet stillness."
        ),
        Story(
            id = 36,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Merchant's Serenity",
            emoji = "🏡",
            body = "A billionaire merchant owned palaces, merchant fleets, and silk warehouses, yet was tormented by insomnia and anxiety. Finally, he gave away the businesses that poisoned his soul and retired to a quiet cabin by a stream. There, for the first time, he slept in deep peace.",
            moral = "Letting go of toxic attachments is the only true gateway to deep, unshakable serenity."
        ),
        Story(
            id = 37,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The King's Inscription",
            emoji = "💍",
            body = "A wise monarch ordered his jewelers to forge a ring engraved with a truth that would hold across all situations. In sorrow, looking at the inscription comforted his spirit; in moments of intoxicating triumph, it kept him humble: 'This too shall pass.'",
            moral = "Both the discomfort of an urge and the agony of resistance are temporary. Breathe — this too shall pass."
        ),
        Story(
            id = 38,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Date Palm Legacy",
            emoji = "🌴",
            body = "An eighty-year-old grandfather was carefully planting date palm saplings in the desert oasis. A young traveler laughed: 'Old man, date palms take fifty years to bear fruit! You will never taste their sweetness.' The old man smiled: 'My whole life I ate sweet dates from trees planted by ancestors I never knew. I plant today for those to come.'",
            moral = "Endure today's discipline as a sacred gift to the dignified future self you are becoming."
        ),
        Story(
            id = 39,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The Supreme Conquest",
            emoji = "🛡️",
            body = "A conqueror returned from sacking foreign kingdoms and asked a philosopher: 'Who is mightier than I?' The philosopher answered: 'A man who can subdue thousands in battle is indeed great. But he who conquers his own appetites and thoughts is the greatest conqueror in the universe.'",
            moral = "Mastering your own mind is the highest, most noble campaign a human being can wage."
        ),
        Story(
            id = 40,
            category = "Mindset and Self-Control",
            categoryEmoji = "🧘",
            title = "The 24-Hour Horizon",
            emoji = "⏳",
            body = "An ancient fortress commander under siege did not calculate the food reserves for three years. Every dawn at muster, he told his garrison: 'Stand firm upon the battlements today. We do not need to win the war today; we only need to hold the gate until sunset.'",
            moral = "You do not need to conquer a whole year right now. Just guard the gate of your mind until tonight."
        ),

        // ── Category 5: Rebirth and Purpose ────────────────────────────────────
        Story(
            id = 41,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Horse in the Marble",
            emoji = "🗿",
            body = "A visitor stood in Michelangelo's studio gasping at a stunningly lifelike stallion sculpted out of Carrara marble. The visitor asked how he engineered such perfection. The master smiled: 'The horse was already living inside the stone. I merely chipped away everything that was not the horse.'",
            moral = "Self-mastery is not about adding new tricks. It is stripping away everything that is not the sovereign man you were born to be."
        ),
        Story(
            id = 42,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Chrysalis Struggle",
            emoji = "🦋",
            body = "A man saw a butterfly struggling through a tiny opening in its cocoon. Pitying its labor, he snipped the cocoon open with scissors. The butterfly emerged easily, but its body was bloated and its wings were limp and shriveled. It crawled on the dirt, never able to fly.",
            moral = "The intense struggle against the cocoon is the biological necessity that pumps strength into your wings."
        ),
        Story(
            id = 43,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "Acres of Diamonds",
            emoji = "💎",
            body = "A farmer sold his homestead and spent his life traveling across continents hunting for diamond mines, dying penniless and broken. The man who bought his old farm knelt by the garden creek one morning and discovered the Golconda diamond mine — the richest deposit of gems on earth.",
            moral = "The power, peace, and greatness you seek are not hidden in far-off pleasures. They reside inside you."
        ),
        Story(
            id = 44,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "Buried in the Dark",
            emoji = "🌱",
            body = "A walnut seed was shoved deep into the damp, freezing dirt beneath the forest floor. It felt suffocated, crushed by weight, and abandoned in total darkness. Yet it was in that very darkness that the shell cracked, roots anchored downward, and a giant tree reached for the sky.",
            moral = "When you feel buried by difficulty, remember: you have not been buried, you have been planted."
        ),
        Story(
            id = 45,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Leaking Chalice",
            emoji = "🏺",
            body = "A master asked his student to fetch river water in a porous clay bowl. As the student ran, water streamed through tiny cracks. By the time he reached the temple, the bowl was dry. The master said: 'Your uncontrolled desires are the cracks leaking your mental energy into the dirt.'",
            moral = "Seal the leaks in your attention with steady discipline, and watch your mental power overflow."
        ),
        Story(
            id = 46,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Living Candle",
            emoji = "🕯️",
            body = "A stick of wax sits safely in a dark cupboard for decades, completely preserved yet entirely useless. But when placed in a bronze holder and ignited, its wick burns bright, melting its own wax to banish darkness and illuminate an entire library of scholars.",
            moral = "Sacrifice cheap, fleeting comfort to ignite the enduring light of your true potential."
        ),
        Story(
            id = 47,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Citadel Walls",
            emoji = "🏰",
            body = "A benevolent king built massive, impenetrable stone walls around his city. Critics mocked him for paranoia. The king walked among laughing children and bustling markets: 'These walls do not exist out of fear. They exist so that everything pure, peaceful, and productive inside may flourish untouched.'",
            moral = "This app and your DNS lock are your citadel walls. They exist solely to protect your internal peace."
        ),
        Story(
            id = 48,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The True Hero",
            emoji = "🦸",
            body = "Ancient legends never praise a hero who walked through life without hardship, scars, or doubt. The hero is the one who was ambushed by demons, thrown into the abyss, felt the bite of cold iron, yet rose up, drew his sword, and conquered his destiny.",
            moral = "You are not a victim of your past. You are the triumphant hero of your own life's comeback story."
        ),
        Story(
            id = 49,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Desert Mirage",
            emoji = "🏜️",
            body = "A thirsty lion saw the shimmering reflection of an antelope in the heat waves of the desert. He charged across the burning sand for miles, panting with exhaustion, only to bite into dry gravel. He finally lifted his gaze and turned toward the real river in the valley.",
            moral = "Destructive digital cravings are mirages. They promise water, but leave you parched. Turn to real life."
        ),
        Story(
            id = 50,
            category = "Rebirth and Purpose",
            categoryEmoji = "🌱",
            title = "The Day 1 Principle",
            emoji = "🌅",
            body = "A master who had lived ten years of total mental clarity was asked by an admirer: 'How does it feel to have permanently arrived at the mountain peak?' The master shook his head gently: 'I have not arrived. Every morning when my feet touch the floor, I whisper to my heart: Today is Day 1.'",
            moral = "Yesterday's victories are history. Approach today with the fresh, humble, fearless fire of Day 1."
        )
    )

    fun getTodayStory(): Story {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return ALL_STORIES[dayOfYear % ALL_STORIES.size]
    }

    fun getAllCategories(): List<String> = listOf(
        "All (50)",
        "Breaking the Chains",
        "The Power of Discipline",
        "Resilience and Falling Down",
        "Mindset and Self-Control",
        "Rebirth and Purpose"
    )

    fun getStoriesByCategory(cat: String): List<Story> {
        if (cat == "All (50)") return ALL_STORIES
        return ALL_STORIES.filter { it.category.equals(cat, ignoreCase = true) }
    }
}
