package com.aman.ainpc.relationship;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks this NPC's relationship with another entity (player or NPC).
 *
 * All scores are clamped to [-1.0, 1.0] except familiarity (0.0 to 1.0).
 *
 *   trust       — How much the NPC trusts this entity. Increases on help/gifts,
 *                 decreases on attacks. Decays slowly over time.
 *   familiarity — How well the NPC knows this entity. Grows with each interaction.
 *   respect     — Whether the NPC looks up to or down on this entity.
 *   fear        — How afraid the NPC is of this entity. 0=fearless, 1=terrified.
 *   friendship  — Overall warmth of the relationship.
 */
public class RelationshipRecord {

    private final UUID entityId;
    private final Instant firstSeen;
    private int interactionCount;

    private float trust;       // -1.0 to 1.0
    private float familiarity; //  0.0 to 1.0
    private float respect;     // -1.0 to 1.0
    private float fear;        //  0.0 to 1.0
    private float friendship;  // -1.0 to 1.0

    public RelationshipRecord(UUID entityId, Instant firstSeen) {
        this.entityId = entityId;
        this.firstSeen = firstSeen;
        this.interactionCount = 0;
        this.trust = 0.0f;
        this.familiarity = 0.0f;
        this.respect = 0.0f;
        this.fear = 0.0f;
        this.friendship = 0.0f;
    }

    // ── Score updates from events ──────────────────────────────────

    /** Called when the NPC meets/sees this entity. */
    public void onEncountered() {
        familiarity = clamp01(familiarity + 0.08f);
        interactionCount++;
    }

    /** Called when this entity attacked or hurt the NPC. */
    public void onAttackedBy() {
        trust = clamp(trust - 0.3f);
        fear = clamp01(fear + 0.25f);
        friendship = clamp(friendship - 0.2f);
        interactionCount++;
    }

    /** Called when this entity helped the NPC. */
    public void onHelped() {
        trust = clamp(trust + 0.2f);
        friendship = clamp(friendship + 0.15f);
        respect = clamp(respect + 0.1f);
        interactionCount++;
    }

    /** Called when this entity gave an item to the NPC. */
    public void onGaveItem() {
        trust = clamp(trust + 0.12f);
        friendship = clamp(friendship + 0.10f);
        familiarity = clamp01(familiarity + 0.05f);
        interactionCount++;
    }

    /** Called when this entity took an item from the NPC. */
    public void onTookItem() {
        trust = clamp(trust - 0.15f);
        friendship = clamp(friendship - 0.08f);
        interactionCount++;
    }

    /** Called when the NPC had a conversation with this entity. */
    public void onConversation() {
        familiarity = clamp01(familiarity + 0.12f);
        friendship = clamp(friendship + 0.05f);
        trust = clamp(trust + 0.03f);
        interactionCount++;
    }

    /**
     * Slowly decay scores back toward neutral over time.
     * Call periodically (e.g. once per game day).
     */
    public void decay() {
        trust = decay(trust, 0.001f);
        fear = clamp01(fear - 0.002f);
        familiarity = clamp01(familiarity - 0.0005f);
        // friendship and respect decay very slowly
        friendship = decay(friendship, 0.0003f);
        respect = decay(respect, 0.0003f);
    }

    // ── Label ─────────────────────────────────────────────────────

    /** Human-readable relationship label for this entity. */
    public String getRelationshipLabel() {
        if (friendship > 0.6f && trust > 0.5f) return "Close Friend";
        if (friendship > 0.3f) return "Friend";
        if (trust < -0.4f && fear > 0.3f) return "Enemy";
        if (fear > 0.5f) return "Feared";
        if (familiarity < 0.1f) return "Stranger";
        return "Acquaintance";
    }

    // ── Accessors ─────────────────────────────────────────────────

    public UUID getEntityId() { return entityId; }
    public Instant getFirstSeen() { return firstSeen; }
    public int getInteractionCount() { return interactionCount; }
    public float getTrust() { return trust; }
    public float getFamiliarity() { return familiarity; }
    public float getRespect() { return respect; }
    public float getFear() { return fear; }
    public float getFriendship() { return friendship; }

    // ── Helpers ───────────────────────────────────────────────────

    private static float clamp(float v) { return Math.max(-1.0f, Math.min(1.0f, v)); }
    private static float clamp01(float v) { return Math.max(0.0f, Math.min(1.0f, v)); }
    private static float decay(float v, float rate) {
        if (v > 0) return Math.max(0, v - rate);
        if (v < 0) return Math.min(0, v + rate);
        return 0;
    }

    @Override
    public String toString() {
        return entityId + " [" + getRelationshipLabel() + "] trust=" +
               String.format("%.2f", trust) + " famil=" + String.format("%.2f", familiarity);
    }
}
