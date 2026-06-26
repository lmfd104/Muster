package com.lmfd.warboss.ui.unit

object KeywordGlossary {

    private val descriptions: Map<String, String> = mapOf(
        // Unit keywords
        "INFANTRY"          to "Core unit type. Can move through and over Obstacles and Barricades. Gains a +1 to their Save characteristic when in cover (to a maximum of 3+).",
        "CHARACTER"         to "Cannot be selected as a target unless they are the closest eligible target, or they have 9 or more wounds. Can be attached to eligible Bodyguard units to form an Attached Unit.",
        "MONSTER"           to "Core unit type. Can shoot and fight even when within Engagement Range of enemy units. Ignores the penalty for shooting into Engagement Range.",
        "VEHICLE"           to "Core unit type. Can shoot and fight even when within Engagement Range of enemy units. Ignores the penalty for shooting into Engagement Range.",
        "WALKER"            to "A VEHICLE that moves and fights like infantry — not blocked by terrain it can physically cross.",
        "BEAST"             to "Core unit type. Can Advance and still shoot or charge.",
        "FLY"               to "Can move over other models and all terrain features as if they were not there. Cannot end a move on top of another model.",
        "PSYKER"            to "Can manifest psychic abilities. Typically has access to a psychic discipline or a specific set of psychic powers.",
        "LEADER"            to "Can be attached to one or more eligible Bodyguard units before the battle (or via Strategic Reserves). While attached, they form a single Attached Unit.",
        "EPIC HERO"         to "A unique CHARACTER. Cannot be attached to a unit that already contains a Leader, and no other Leader can join a unit containing an Epic Hero.",
        "BODYGUARD"         to "Can have a Leader attached to them. While their Leader is alive in the unit, attacks that target the unit can be allocated to the Leader on a 2+ (Look Out, Sir).",
        "DEEP STRIKE"       to "Can be set up in Reserves and arrive using Deep Strike. In the Reinforcements step, can be placed anywhere on the battlefield more than 9\" from all enemy models.",
        "SCOUTS"            to "After both sides have deployed, can make a Normal Move of up to 6\" (or the distance shown on the datasheet) before the battle begins.",
        "INFILTRATORS"      to "Can be set up anywhere on the battlefield that is more than 9\" from the enemy deployment zone and all enemy models.",
        "STEALTH"           to "All ranged attacks that target this unit suffer a -1 penalty to their Hit rolls.",
        "SMOKESCREEN"       to "Once per battle, can use Smoke in the Shooting phase instead of shooting — enemy units cannot draw line of sight through the smoke until next turn.",
        "AIRCRAFT"          to "Can only be set up in the Reinforcements step. Moves a minimum of 20\" each turn. Enemy units must subtract 1 from Hit rolls when targeting this unit.",
        "HOVER"             to "An AIRCRAFT that has chosen to Hover moves as a non-aircraft unit for that turn, losing AIRCRAFT restrictions and benefits.",
        "TRANSPORT"         to "Can carry a number of friendly models (shown on the datasheet). Embarked models cannot do anything while inside. Disembark before the unit moves.",
        "DEADLY DEMISE"     to "When this unit is destroyed, roll one D6 for each model. For each result equal to or greater than the listed value, each unit within 6\" suffers 1 mortal wound.",
        "GRENADES"          to "One model per unit can use a Grenade weapon in the Shooting phase instead of any other ranged weapons.",
        "DAEMON"            to "A supernatural entity. Affected by abilities and rules that reference DAEMONS.",
        "CHAOS"             to "Faction keyword. Affected by abilities and auras that target CHAOS units.",
        "IMPERIUM"          to "Faction keyword. Affected by abilities and auras that target IMPERIUM units.",
        "CORE"              to "A fundamental troop type keyword. Many auras and abilities specifically target CORE units, excluding characters and war engines.",
        "SMOKE"             to "See SMOKESCREEN.",

        // Weapon ability keywords
        "LETHAL HITS"       to "Each unmodified Hit roll of 6 (a Critical Hit) automatically wounds the target — no Wound roll required.",
        "SUSTAINED HITS"    to "Each unmodified Hit roll of 6 (a Critical Hit) scores additional hits equal to the number after the keyword (e.g. SUSTAINED HITS 1 = 1 extra hit).",
        "DEVASTATING WOUNDS" to "Each unmodified Wound roll of 6 (a Critical Wound) inflicts mortal wounds equal to the weapon's Damage characteristic and the attack sequence ends.",
        "TORRENT"           to "This weapon automatically hits its target — do not make Hit rolls. The number of attacks is still determined as normal.",
        "BLAST"             to "Each time this weapon targets a unit of 6+ models, it makes a minimum of 3 attacks. Each time it targets a unit of 11+ models, it makes a minimum of 6 attacks.",
        "MELTA"             to "Each time this weapon targets a unit within half its Range, it gains +2 to its Damage characteristic.",
        "RAPID FIRE"        to "Each time this weapon targets a unit within half its Range, the number of attacks is doubled.",
        "HEAVY"             to "Each time this weapon is used by a unit that has not moved in its Movement phase, it gains +1 to its Hit rolls.",
        "ASSAULT"           to "This weapon can be used even if the bearer's unit Advanced this turn.",
        "PRECISION"         to "Each time an attack is made with this weapon, if a Hit is scored you may choose to allocate the attack to a CHARACTER in the target unit, even if it is not the closest model.",
        "INDIRECT FIRE"     to "Attacks can be made with this weapon even if the target is not visible to the attacker. When firing indirectly, subtract 1 from Hit rolls and the target benefits from Cover.",
        "IGNORES COVER"     to "Each time an attack is made with this weapon, the target does not receive the benefit of Cover against that attack.",
        "LANCE"             to "Each time an attack is made with this weapon, if the bearer's unit made a Charge move this turn, that attack's Armour Penetration is improved by 1.",
        "TWIN-LINKED"       to "Each time an attack is made with this weapon, you may re-roll the Wound roll.",
        "HAZARDOUS"         to "After firing this weapon, for each Hazardous weapon used, roll one D6. On a 1, the bearer suffers 1 mortal wound (or 3 mortal wounds if it is a VEHICLE or MONSTER).",
        "ANTI"              to "Improves the weapon's AP or generates a Critical Wound against units with the specified keyword on an unmodified Wound roll of the stated value or higher (e.g. ANTI-INFANTRY 4+).",
        "ONE SHOT"          to "This weapon can only be used once per battle. Mark it as expended after firing.",
        "EXTRA ATTACKS"     to "This weapon can be used in addition to the bearer's other melee weapons in the Fight phase.",
        "PSYCHIC"           to "This weapon is a psychic attack. It can be used by a PSYKER in the Shooting phase as their psychic ability.",
    )

    fun lookup(keyword: String): String? {
        val normalised = keyword.trim().uppercase()
        // Direct match
        descriptions[normalised]?.let { return it }
        // Prefix match for parameterised keywords like "SUSTAINED HITS 2", "ANTI-INFANTRY 4+"
        for ((key, desc) in descriptions) {
            if (normalised.startsWith(key) && normalised.length > key.length) return desc
        }
        return null
    }
}
