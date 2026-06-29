package com.aman.ainpc.decision;

import com.aman.ainpc.character.CharacterProfile;
import com.aman.ainpc.knowledge.KnowledgeBase;
import com.aman.ainpc.memory.history.LifeHistory;
import com.aman.ainpc.needs.Need;
import com.aman.ainpc.needs.NeedType;
import com.aman.ainpc.needs.NeedsManager;
import com.aman.ainpc.relationship.RelationshipManager;

/**
 * Decides the NPC's current goal based on all available context.
 *
 * Priority order:
 *   1. Critical safety (flee / hide)
 *   2. Critical hunger (find food)
 *   3. Critical tiredness (rest)
 *   4. Urgent loneliness (socialize)
 *   5. Curiosity + dream pursuit
 *   6. Do occupation work (all needs satisfied)
 *   7. Idle / wander
 */
public class DecisionEngine {

    public Goal decide(
            NeedsManager needs,
            KnowledgeBase knowledge,
            RelationshipManager relationships,
            LifeHistory history,
            CharacterProfile profile
    ) {
        Need safety = needs.getNeed(NeedType.SAFETY);
        Need hunger = needs.getNeed(NeedType.HUNGER);
        Need rest = needs.getNeed(NeedType.REST);
        Need loneliness = needs.getNeed(NeedType.LONELINESS);
        Need curiosity = needs.getNeed(NeedType.CURIOSITY);

        // ── 1. Critical safety ────────────────────────────────────
        if (safety.isCritical()) {
            return new Goal(GoalType.SEEK_SAFETY,
                    "Danger! Safety need is " + String.format("%.0f%%", safety.getValue() * 100),
                    1.0f);
        }

        // ── 2. Critical hunger ────────────────────────────────────
        if (hunger.isCritical()) {
            return new Goal(GoalType.FIND_FOOD,
                    "Starving! Hunger at " + String.format("%.0f%%", hunger.getValue() * 100),
                    0.95f);
        }

        // ── 3. Critical rest ──────────────────────────────────────
        if (rest.isCritical()) {
            return new Goal(GoalType.REST,
                    "Exhausted! Rest at " + String.format("%.0f%%", rest.getValue() * 100),
                    0.85f);
        }

        // ── 4. Urgent loneliness ──────────────────────────────────
        if (loneliness.isUrgent()) {
            return new Goal(GoalType.SOCIALIZE,
                    "Lonely — need to interact with someone",
                    0.70f);
        }

        // ── 5. Curiosity + dream ──────────────────────────────────
        if (curiosity.isUrgent()) {
            if (profile.getDream() != null && !profile.getDream().isAchieved()) {
                return new Goal(GoalType.PURSUE_DREAM,
                        "Pursuing dream: " + profile.getDream().getDescription(),
                        0.55f);
            }
            return new Goal(GoalType.EXPLORE,
                    "Curious — exploring the world",
                    0.45f);
        }

        // ── 6. Moderate hunger → find food ────────────────────────
        if (hunger.isUrgent()) {
            return new Goal(GoalType.FIND_FOOD,
                    "Getting hungry",
                    0.50f);
        }

        // ── 7. Occupation work ─────────────────────────────────────
        if (isContentEnough(needs)) {
            return new Goal(GoalType.DO_WORK,
                    "Content — doing " + profile.getOccupation().name().toLowerCase() + " work",
                    0.30f);
        }

        // ── 8. Default: idle ──────────────────────────────────────
        return new Goal(GoalType.IDLE, "Nothing pressing — wandering", 0.10f);
    }

    /** All needs are at a comfortable level — safe to do occupation work. */
    private boolean isContentEnough(NeedsManager needs) {
        return needs.getNeed(NeedType.HUNGER).getValue() < 0.4f
                && needs.getNeed(NeedType.SAFETY).getValue() < 0.3f
                && needs.getNeed(NeedType.REST).getValue() > 0.4f;
    }
}
