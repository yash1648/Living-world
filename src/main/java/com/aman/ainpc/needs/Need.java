package com.aman.ainpc.needs;

/**
 * A single NPC need with a current value (0.0 = fully satisfied, 1.0 = critical).
 *
 * Value semantics:
 *   0.0 = fully satisfied / not needed right now
 *   1.0 = critical / must be addressed immediately
 *
 * REST is inverted: 0.0 = exhausted, 1.0 = fully rested.
 */
public class Need {

    private final NeedType type;
    private float value;

    public Need(NeedType type) {
        this.type = type;
        this.value = switch (type) {
            case HUNGER    -> 0.1f;   // start slightly hungry
            case SAFETY    -> 0.0f;   // start safe
            case LONELINESS -> 0.2f;  // start slightly lonely
            case CURIOSITY -> 0.3f;   // start slightly curious
            case REST      -> 0.8f;   // start well-rested
        };
    }

    public NeedType getType() { return type; }

    public float getValue() { return value; }

    /** Increase urgency (things getting worse). */
    public void increase(float delta) {
        value = Math.min(1.0f, value + Math.abs(delta));
    }

    /** Decrease urgency (things getting better / need satisfied). */
    public void decrease(float delta) {
        value = Math.max(0.0f, value - Math.abs(delta));
    }

    public void setValue(float value) {
        this.value = Math.max(0.0f, Math.min(1.0f, value));
    }

    /** True if this need is critical and should drive immediate behavior. */
    public boolean isCritical() {
        return switch (type) {
            case REST -> value < 0.15f;         // critically tired (low rest = bad)
            default   -> value > 0.75f;          // high urgency = bad
        };
    }

    /** True if this need is urgent (should be a priority). */
    public boolean isUrgent() {
        return switch (type) {
            case REST -> value < 0.3f;
            default   -> value > 0.55f;
        };
    }

    @Override
    public String toString() {
        return type.name() + "=" + String.format("%.2f", value);
    }
}
