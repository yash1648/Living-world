package com.aman.ainpc.perception;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Observation {

    public record Position(double x, double y, double z) {}

    private final long timestamp;
    private final ObservationType type;
    private final UUID sourceUUID;
    private final UUID targetUUID;
    private final Position position;
    private final Map<String, String> metadata;

    public Observation(long timestamp, ObservationType type, UUID sourceUUID, UUID targetUUID, Position position, Map<String, String> metadata) {
        this.timestamp = timestamp;
        this.type = Objects.requireNonNull(type);
        this.sourceUUID = sourceUUID;
        this.targetUUID = targetUUID;
        this.position = position;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ObservationType getType() {
        return type;
    }

    public UUID getSourceUUID() {
        return sourceUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public Position getPosition() {
        return position;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Observation that)) return false;
        return timestamp == that.timestamp
                && type == that.type
                && Objects.equals(sourceUUID, that.sourceUUID)
                && Objects.equals(targetUUID, that.targetUUID)
                && Objects.equals(position, that.position)
                && metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, type, sourceUUID, targetUUID, position, metadata);
    }
}
