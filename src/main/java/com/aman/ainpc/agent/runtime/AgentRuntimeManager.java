package com.aman.ainpc.agent.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRuntimeManager {

    private static final AgentRuntimeManager INSTANCE = new AgentRuntimeManager();

    private final Map<UUID, AgentRuntime> runtimes = new ConcurrentHashMap<>();

    private AgentRuntimeManager() {}

    public static AgentRuntimeManager getInstance() {
        return INSTANCE;
    }

    public AgentRuntime register(UUID agentUUID) {
        return runtimes.computeIfAbsent(agentUUID, AgentRuntime::new);
    }

    public void unregister(UUID agentUUID) {
        AgentRuntime runtime = runtimes.remove(agentUUID);
        if (runtime != null) {
            runtime.getState().setLifecycle(AgentState.Lifecycle.STOPPED);
        }
    }

    public AgentRuntime getRuntime(UUID agentUUID) {
        return runtimes.get(agentUUID);
    }

    public boolean isRegistered(UUID agentUUID) {
        return runtimes.containsKey(agentUUID);
    }
}
