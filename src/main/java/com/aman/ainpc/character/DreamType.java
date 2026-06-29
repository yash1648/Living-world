package com.aman.ainpc.character;

/**
 * Long-term ambitions an NPC can hold.
 * Dreams rarely change and heavily influence the Decision Engine.
 */
public enum DreamType {
    BUILD_A_HOME        ("Build a home and settle down"),
    PROTECT_THE_VILLAGE ("Protect the village from all threats"),
    BECOME_A_MASTER     ("Master their craft and be recognized for it"),
    EXPLORE_THE_WORLD   ("See every corner of this world"),
    FIND_FAMILY         ("Find or reunite with lost family"),
    ACCUMULATE_WEALTH   ("Amass great wealth and resources"),
    DEFEND_THE_WEAK     ("Defend those who cannot defend themselves"),
    DISCOVER_SECRETS    ("Uncover the hidden truths of the world"),
    EARN_RESPECT        ("Earn the respect and admiration of others"),
    LIVE_IN_PEACE       ("Find a quiet, peaceful life away from conflict");

    private final String description;

    DreamType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
