package com.aman.ainpc.character;

import java.util.UUID;

/**
 * Immutable character definition for an NPC.
 *
 * Generated deterministically from the NPC's UUID so the same NPC
 * always has the same name, personality, and dream across sessions.
 */
public class CharacterProfile {

    private static final String[] NAMES = {
        "Eira", "Torin", "Mira", "Aldric", "Vesper", "Cael", "Liana",
        "Dusk", "Finn", "Sage", "Brynn", "Orin", "Thea", "Riven", "Lyra",
        "Hadric", "Sable", "Wren", "Corvin", "Ashel"
    };

    private final UUID npcUUID;
    private final String name;
    private final Personality personality;
    private final Occupation occupation;
    private final Dream dream;

    public CharacterProfile(UUID npcUUID, String name, Personality personality, Occupation occupation, Dream dream) {
        this.npcUUID = npcUUID;
        this.name = name;
        this.personality = personality;
        this.occupation = occupation;
        this.dream = dream;
    }

    // ── Factory ───────────────────────────────────────────────────

    /**
     * Generates a deterministic character profile from an NPC's UUID.
     * Same UUID always produces the same profile.
     */
    public static CharacterProfile generateFor(UUID uuid) {
        int hash = Math.abs(uuid.hashCode());

        String name = NAMES[hash % NAMES.length];
        Personality personality = Personality.values()[(hash / NAMES.length) % Personality.values().length];
        Occupation occupation = Occupation.values()[(hash / 100) % Occupation.values().length];
        DreamType dreamType = DreamType.values()[(hash / 1000) % DreamType.values().length];
        Dream dream = new Dream(dreamType);

        return new CharacterProfile(uuid, name, personality, occupation, dream);
    }

    // ── Accessors ─────────────────────────────────────────────────

    public UUID getNpcUUID() { return npcUUID; }
    public String getName() { return name; }
    public Personality getPersonality() { return personality; }
    public Occupation getOccupation() { return occupation; }
    public Dream getDream() { return dream; }

    /**
     * Returns a short system prompt describing this NPC for LLM context.
     */
    public String toSystemPrompt() {
        return "You are " + name + ", a " + occupation.name().toLowerCase() + " in a Minecraft village. " +
               "Your personality: " + personality.getDescription() + ". " +
               "Your dream: " + dream.getDescription() + ". " +
               "Respond in character. Generate dialogue ONLY — no action descriptions, no asterisks.";
    }

    @Override
    public String toString() {
        return name + " [" + occupation.name() + ", " + personality.name() + "]";
    }
}
