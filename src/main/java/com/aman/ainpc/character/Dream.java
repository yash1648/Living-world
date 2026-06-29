package com.aman.ainpc.character;

/**
 * An NPC's long-term ambition.
 *
 * Progress tracks how close the NPC is to achieving their dream (0.0 to 1.0).
 * Dreams rarely change — they are set at character creation and only shift
 * due to significant life events.
 */
public class Dream {

    private final DreamType type;
    private float progress; // 0.0 = just started, 1.0 = achieved

    public Dream(DreamType type) {
        this.type = type;
        this.progress = 0.0f;
    }

    public DreamType getType() { return type; }

    public String getDescription() { return type.getDescription(); }

    public float getProgress() { return progress; }

    public void advanceProgress(float delta) {
        progress = Math.min(1.0f, progress + Math.abs(delta));
    }

    public boolean isAchieved() { return progress >= 1.0f; }

    @Override
    public String toString() {
        return type.getDescription() + " (" + String.format("%.0f", progress * 100) + "% progress)";
    }
}
