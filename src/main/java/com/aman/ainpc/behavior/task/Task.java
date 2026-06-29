package com.aman.ainpc.behavior.task;

/**
 * Immutable description of a single atomic action for an NPC.
 *
 * Created by the Planner. Executed by the ActionExecutor.
 * Never holds Minecraft objects — pure data only.
 */
public final class Task {

    private static final int DEFAULT_DURATION_TICKS = 200; // 10 s

    private final TaskType type;
    private final boolean hasPosition;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int maxDurationTicks;

    // ── Constructors ──────────────────────────────────────────────

    private Task(TaskType type, boolean hasPosition,
                 double x, double y, double z, int maxDurationTicks) {
        this.type = type;
        this.hasPosition = hasPosition;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.maxDurationTicks = maxDurationTicks;
    }

    // ── Factory methods ───────────────────────────────────────────

    /** Task with no fixed position — ActionExecutor resolves at runtime. */
    public static Task of(TaskType type) {
        return new Task(type, false, 0, 0, 0, defaultDuration(type));
    }

    /** Task that walks to a specific world position. */
    public static Task walkTo(double x, double y, double z) {
        return new Task(TaskType.WALK_TO, true, x, y, z, 300);
    }

    /** Task with a custom timeout (ticks). */
    public static Task ofWithDuration(TaskType type, int durationTicks) {
        return new Task(type, false, 0, 0, 0, durationTicks);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static int defaultDuration(TaskType type) {
        return switch (type) {
            case WALK_TO     -> 300;
            case EXPLORE     -> 300;
            case INVESTIGATE -> 200;
            case GREET       -> 120;
            case SOCIALIZE   -> 200;
            case EAT         ->  80;
            case SLEEP       -> 240;
            case WORK        -> 400;
            case IDLE        -> 100;
        };
    }

    // ── Accessors ─────────────────────────────────────────────────

    public TaskType getType()           { return type; }
    public boolean hasPosition()        { return hasPosition; }
    public double getTargetX()          { return targetX; }
    public double getTargetY()          { return targetY; }
    public double getTargetZ()          { return targetZ; }
    public int getMaxDurationTicks()    { return maxDurationTicks; }

    @Override
    public String toString() {
        return hasPosition
                ? String.format("Task[%s → (%.1f, %.1f, %.1f)]", type, targetX, targetY, targetZ)
                : String.format("Task[%s]", type);
    }
}
