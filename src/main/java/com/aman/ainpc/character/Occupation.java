package com.aman.ainpc.character;

/**
 * The NPC's occupation / role in the world.
 * Influences their daily routine, dialogue, and decision priorities.
 */
public enum Occupation {
    FARMER      ("tends crops and livestock"),
    BLACKSMITH  ("forges tools and weapons"),
    TRADER      ("buys and sells goods"),
    GUARD       ("protects the village"),
    HEALER      ("tends to the sick and wounded"),
    SCHOLAR     ("studies lore and history"),
    HUNTER      ("tracks and hunts animals"),
    BUILDER     ("constructs buildings and roads"),
    COOK        ("prepares meals and brews"),
    HERBALIST   ("gathers plants and makes potions"),
    WANDERER    ("travels without a fixed home");

    private final String description;

    Occupation(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
