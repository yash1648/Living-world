package com.aman.ainpc.conversation.session;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owns all active ConversationSessions for the current runtime.
 *
 * Sessions exist only in memory for the duration of an active interaction.
 * No cleanup, no timeout, no persistence, no threading, no LLM.
 */
public class ConversationSessionManager {

    private final Map<UUID, ConversationSession> sessions = new HashMap<>();

    /**
     * Create and register a new session for the given participants.
     *
     * @param participants the entities taking part in this conversation
     * @return the newly created session
     */
    public ConversationSession createSession(List<ConversationParticipant> participants) {
        UUID sessionId = UUID.randomUUID();
        ConversationSession session = new ConversationSession(sessionId, participants, Instant.now());
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * End and remove the session with the given ID.
     * No-op if the session does not exist.
     *
     * @param sessionId the ID of the session to end
     */
    public void endSession(UUID sessionId) {
        ConversationSession session = sessions.remove(sessionId);
        if (session != null) {
            session.end();
        }
    }

    /**
     * Retrieve a session by ID.
     *
     * @param sessionId the session ID
     * @return the session, or null if it does not exist
     */
    public ConversationSession getSession(UUID sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Check whether a session exists and is currently active.
     *
     * @param sessionId the session ID
     * @return true if the session exists and its state is ACTIVE
     */
    public boolean isActive(UUID sessionId) {
        ConversationSession session = sessions.get(sessionId);
        return session != null && session.getState() == ConversationSessionState.ACTIVE;
    }
}
