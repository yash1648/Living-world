package com.aman.ainpc.conversation;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Server-side singleton that tracks which players are currently in conversation
 * with which NPC.
 *
 * Thread-safe — the main server thread and background AI-call threads both access it.
 */
public class ConversationManager {

    private static final ConversationManager INSTANCE = new ConversationManager();

    public static ConversationManager getInstance() { return INSTANCE; }

    /** playerUUID → npcUUID */
    private final ConcurrentMap<UUID, UUID> active = new ConcurrentHashMap<>();

    private ConversationManager() {}

    /**
     * Start a conversation between a player and an NPC.
     * Ends any existing conversation the player was in first.
     */
    public void startConversation(UUID playerUUID, UUID npcUUID) {
        active.put(playerUUID, npcUUID);
    }

    /** End the player's current conversation (if any). */
    public void endConversation(UUID playerUUID) {
        active.remove(playerUUID);
    }

    /** True if this player is currently in conversation with an NPC. */
    public boolean isInConversation(UUID playerUUID) {
        return active.containsKey(playerUUID);
    }

    /** Returns the UUID of the NPC this player is talking to, if any. */
    public Optional<UUID> getConversationNPC(UUID playerUUID) {
        return Optional.ofNullable(active.get(playerUUID));
    }

    /** End all conversations (e.g. on server stop). */
    public void clear() {
        active.clear();
    }
}
