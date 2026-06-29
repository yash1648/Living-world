package com.aman.ainpc.knowledge;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable semantic fact extracted from an Event.
 *
 * Format: subject [predicate] object
 * Example: "player:UUID MET npc:UUID" or "npc:UUID SAW item:diamond"
 *
 * Confidence ranges from 0.0 (uncertain) to 1.0 (certain).
 */
public final class Fact {

    public enum Predicate {
        MET,        // subject MET object (encounter)
        ATTACKED,   // subject ATTACKED object
        HELPED,     // subject HELPED object
        GAVE,       // subject GAVE object (item)
        SAW,        // subject SAW object (observation)
        FOUND,      // subject FOUND object (item discovered)
        FEARS,      // subject FEARS object
        TRUSTS,     // subject TRUSTS object
        LIVED_AT,   // subject LIVED_AT object (location)
        KNOWS,      // subject KNOWS object (knowledge meta)
    }

    private final UUID factId;
    private final String subject;
    private final Predicate predicate;
    private final String object;
    private final float confidence;
    private final Instant timestamp;

    public Fact(UUID factId, String subject, Predicate predicate, String object, float confidence, Instant timestamp) {
        this.factId = factId;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
        this.timestamp = timestamp;
    }

    public UUID getFactId() { return factId; }
    public String getSubject() { return subject; }
    public Predicate getPredicate() { return predicate; }
    public String getObject() { return object; }
    public float getConfidence() { return confidence; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return subject + " " + predicate.name() + " " + object + " (conf=" + String.format("%.2f", confidence) + ")";
    }
}
