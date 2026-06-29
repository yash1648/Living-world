package com.aman.ainpc.perception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks when a PLAYER_SEEN observation was last emitted for each nearby player.
 *
 * Owned by AINPCEntity — one instance per NPC.
 *
 * Purpose:
 *   The entity scans for players every tick (to stay responsive).  Without
 *   throttling, the PerceptionBuffer would receive 20 identical PLAYER_SEEN
 *   observations per second for every player in range.  This class enforces a
 *   per-player cooldown so each player is observed at most once per COOLDOWN_MS.
 *
 * Extensibility:
 *   This pattern generalises to any observation type that can trigger repeatedly
 *   (sounds, damage, block changes, item pickups).  Each new source adds its own
 *   tracker or its own key, without touching AgentRuntime or PerceptionBuffer.
 *
 * Thread safety:
 *   Single-threaded — called from the server tick thread only.
 */
public class PlayerSightTracker {

    /** Minimum gap between two PLAYER_SEEN observations for the same player (ms). */
    private static final long COOLDOWN_MS = 2_000L;

    /** Maps player UUID → timestamp (ms) of the last emitted observation. */
    private final Map<UUID, Long> lastObservedAt = new HashMap<>();

    /**
     * Returns {@code true} if enough time has passed to emit a new PLAYER_SEEN
     * observation for this player, and records the current time so the cooldown
     * restarts.  Returns {@code false} otherwise (no state change).
     */
    public boolean shouldObserve(UUID playerUUID) {
        long now  = System.currentTimeMillis();
        Long last = lastObservedAt.get(playerUUID);
        if (last == null || (now - last) >= COOLDOWN_MS) {
            lastObservedAt.put(playerUUID, now);
            return true;
        }
        return false;
    }

    /**
     * Removes entries for players no longer in range so the map never grows
     * unbounded.  Call this once per scan with the set of currently visible UUIDs.
     */
    public void retainOnly(Set<UUID> presentPlayers) {
        lastObservedAt.keySet().retainAll(presentPlayers);
    }
}
