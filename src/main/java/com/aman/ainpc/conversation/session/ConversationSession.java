package com.aman.ainpc.conversation.session;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Runtime state for one active conversation between participants.
 *
 * A ConversationSession exists only while entities are actively interacting.
 * It has no message history, no memory, and no AI — it is a pure identity
 * and lifecycle record that future features (timeouts, interruption,
 * multiplayer, LLM context windows) will build on.
 */
public final class ConversationSession {

    private final UUID sessionId;
    private ConversationSessionState state;
    private final List<ConversationParticipant> participants;
    private final Instant startedAt;

    public ConversationSession(UUID sessionId,
                               List<ConversationParticipant> participants,
                               Instant startedAt) {
        this.sessionId    = sessionId;
        this.participants = Collections.unmodifiableList(participants);
        this.startedAt    = startedAt;
        this.state        = ConversationSessionState.ACTIVE;
    }

    // ── State transitions ─────────────────────────────────────────

    /** Mark this session as ended. */
    void end() {
        this.state = ConversationSessionState.ENDED;
    }

    // ── Accessors ─────────────────────────────────────────────────

    public UUID getSessionId()                          { return sessionId; }
    public ConversationSessionState getState()          { return state; }
    public List<ConversationParticipant> getParticipants() { return participants; }
    public Instant getStartedAt()                       { return startedAt; }
}
