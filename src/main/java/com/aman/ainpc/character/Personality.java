package com.aman.ainpc.character;

/**
 * Personality archetype for an NPC. Influences dialogue tone,
 * decision-making thresholds, and social behavior.
 */
public enum Personality {
    FRIENDLY    ("warm, welcoming, and helpful"),
    STOIC       ("calm, reserved, and matter-of-fact"),
    CURIOUS     ("inquisitive, enthusiastic, always asking questions"),
    CAUTIOUS    ("careful, suspicious of strangers, slow to trust"),
    BRAVE       ("bold, confident, and protective"),
    PLAYFUL     ("witty, lighthearted, and humorous"),
    WISE        ("thoughtful, measured, and philosophical"),
    GRUMPY      ("irritable, blunt, but honest"),
    CHEERFUL    ("energetic, optimistic, and enthusiastic"),
    MYSTERIOUS  ("cryptic, secretive, speaks in riddles");

    private final String description;

    Personality(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
