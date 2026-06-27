package com.aman.ainpc.agent.runtime;

import java.util.UUID;

public class AgentState {

    public enum Lifecycle {
        CREATED,
        RUNNING,
        PAUSED,
        STOPPED
    }

    private final UUID agentUUID;
    private Lifecycle lifecycle;

    public AgentState(UUID agentUUID) {
        this.agentUUID = agentUUID;
        this.lifecycle = Lifecycle.CREATED;
    }

    public UUID getAgentUUID() {
        return agentUUID;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }
}
