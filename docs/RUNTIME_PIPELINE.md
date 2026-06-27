# Runtime Processing Pipeline

## 1. Observation

**Purpose:** Receive raw perception data from the world. Every tick, the PerceptionBuffer delivers all Observations that accumulated since the last tick. This is the only entry point for external information into the Agent.

**Input:** `PerceptionSnapshot` — a batch of Observations drained from the buffer.

**Output:** A list of raw Observations ready for evaluation.

↓

## 2. Significance Evaluation

**Purpose:** Filter out noise. Not every Observation matters — a block breaking far away, a player crossing the NPC's visual range for an instant, a familiar daily event. This stage decides whether each Observation is worth keeping, discarding, or holding for later comparison.

**Input:** List of raw Observations.

**Output:** A filtered list of Observations deemed significant.

↓

## 3. Event Creation

**Purpose:** Translate a significant Observation into a meaningful, immutable Event. An Observation says "a block broke at position X." An Event says "the blacksmith's house wall collapsed." This is where raw game data gains world context.

**Input:** Significant Observation.

**Output:** An immutable Event with type, timestamp, participants, location, and metadata.

↓

## 4. Life History Update

**Purpose:** Append the Event to the NPC's personal history. The Life History is an append-only, time-stamped record of everything significant the NPC has experienced. Events are never deleted, though they may decay in importance over time.

**Input:** A new Event.

**Output:** Updated Life History with the Event appended.

↓

## 5. Knowledge Update

**Purpose:** Extract semantic facts from the Event. If the Event says "the blacksmith's house wall collapsed," Knowledge Update records "blacksmith's house has a damaged wall" and "the house is at position X." Facts are queryable, overwritable, and shareable.

**Input:** A new Event.

**Output:** Updated Knowledge with new or corrected facts.

↓

## 6. Relationship Update

**Purpose:** Adjust the NPC's relationships based on the Event. If the player helped the NPC, trust increases. If the NPC saw a rival steal, familiarity with the rival decreases. Relationship values drift toward neutral over time.

**Input:** A new Event.

**Output:** Updated Relationship values for any entities involved in the Event.

↓

## 7. Goal Evaluation

**Purpose:** Check whether the current Goal is still valid, completed, or should be replaced. New Events can create urgent needs (flee from danger, investigate a noise) or retire old ones (the resource has been gathered). Also checks if the player has issued a new command.

**Input:** Current Goal, Life History (recent events), Knowledge, Relationships, Perception.

**Output:** A confirmed or updated Current Goal. May be empty.

↓

## 8. Planning

**Purpose:** If the Current Goal has no valid Plan, generate one. The Planner decomposes the Goal into a sequence of Tasks. This stage is skipped entirely if a valid Plan already exists or if there is no Goal.

**Input:** Current Goal, Knowledge (world layout, locations), Relationships (who can help).

**Output:** A Plan — an ordered list of Tasks. May be empty if no Goal exists or planning fails.

↓

## 9. Task Execution

**Purpose:** Execute exactly one Task from the front of the Task Queue. If the Task succeeds, it is removed and the next Task becomes active. If it fails, the failure is noted and may trigger replanning. Only one Task executes per tick.

**Input:** Active Task from the Task Queue.

**Output:** Task outcome (success, failure, or in-progress). Completed Tasks are dequeued.

↓

## 10. End Tick

**Purpose:** Finalize the tick. All state updates are complete. The runtime is ready to begin the next cycle. The Agent's state (lifecycle, buffer, position) is consistent and can be safely persisted.

**Input:** All system states after Task Execution.

**Output:** `AgentTickResult` — SUCCESS, SKIPPED, PAUSED, or STOPPED.
