package com.aman.ainpc.knowledge;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.perception.Observation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Extracts semantic facts from Events based on their type.
 *
 * Routes on EventType and uses observation metadata to build
 * subject-predicate-object triples.
 */
public class FactExtractor {

    public List<Fact> extract(Event event) {
        List<Fact> facts = new ArrayList<>();
        Instant now = event.getTimestamp();

        switch (event.getType()) {
            case MET_PLAYER -> extractMetPlayer(event, facts, now);
            case NPC_ENCOUNTERED -> extractNpcEncountered(event, facts, now);
            case PLAYER_ATTACKED_NPC, ENTITY_DAMAGED -> extractAttack(event, facts, now);
            case PLAYER_GAVE_ITEM -> extractGaveItem(event, facts, now);
            case PLAYER_TOOK_ITEM -> extractTookItem(event, facts, now);
            case ITEM_OBSERVED -> extractItemObserved(event, facts, now);
            case BLOCK_CHANGED -> extractBlockChanged(event, facts, now);
            case CONVERSATION_HAD -> extractConversation(event, facts, now);
            case NPC_HELPED -> extractHelped(event, facts, now);
            default -> {}
        }

        return facts;
    }

    private void extractMetPlayer(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null && obs.getTargetUUID() != null) {
                String npc = "npc:" + obs.getSourceUUID();
                String player = "player:" + obs.getTargetUUID();
                String playerName = obs.getMetadata().getOrDefault("name", obs.getTargetUUID().toString());

                facts.add(new Fact(UUID.randomUUID(), npc, Fact.Predicate.MET, playerName, 1.0f, now));
            }
        }
    }

    private void extractNpcEncountered(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null && obs.getTargetUUID() != null) {
                String self = "npc:" + obs.getSourceUUID();
                String other = "npc:" + obs.getTargetUUID();
                facts.add(new Fact(UUID.randomUUID(), self, Fact.Predicate.MET, other, 0.9f, now));
            }
        }
    }

    private void extractAttack(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null && obs.getTargetUUID() != null) {
                String attacker = obs.getMetadata().getOrDefault("attacker_type", "entity") + ":" + obs.getSourceUUID();
                String target = "entity:" + obs.getTargetUUID();
                facts.add(new Fact(UUID.randomUUID(), attacker, Fact.Predicate.ATTACKED, target, 1.0f, now));
                // Imply fear of attacker
                String victim = "entity:" + obs.getTargetUUID();
                facts.add(new Fact(UUID.randomUUID(), victim, Fact.Predicate.FEARS, attacker, 0.8f, now));
            }
        }
    }

    private void extractGaveItem(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            String giver = "player:" + (obs.getSourceUUID() != null ? obs.getSourceUUID() : "unknown");
            String item = obs.getMetadata().getOrDefault("item", "unknown_item");
            facts.add(new Fact(UUID.randomUUID(), giver, Fact.Predicate.GAVE, item, 1.0f, now));
        }
    }

    private void extractTookItem(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            String taker = "player:" + (obs.getSourceUUID() != null ? obs.getSourceUUID() : "unknown");
            String item = obs.getMetadata().getOrDefault("item", "unknown_item");
            facts.add(new Fact(UUID.randomUUID(), taker, Fact.Predicate.FOUND, item, 0.9f, now));
        }
    }

    private void extractItemObserved(Event event, List<Fact> facts, Instant now) {
        if (!event.getSourceObservations().isEmpty()) {
            Observation obs = event.getSourceObservations().get(0);
            String observer = "npc:" + (obs.getSourceUUID() != null ? obs.getSourceUUID() : "unknown");
            String item = obs.getMetadata().getOrDefault("item", "item:" + event.getMetadata().getOrDefault("item_type", "unknown"));
            facts.add(new Fact(UUID.randomUUID(), observer, Fact.Predicate.SAW, item, 0.7f, now));
        }
    }

    private void extractBlockChanged(Event event, List<Fact> facts, Instant now) {
        if (!event.getSourceObservations().isEmpty()) {
            Observation obs = event.getSourceObservations().get(0);
            String observer = "npc:" + (obs.getSourceUUID() != null ? obs.getSourceUUID() : "unknown");
            String block = "block_change:" + obs.getMetadata().getOrDefault("block", "unknown");
            facts.add(new Fact(UUID.randomUUID(), observer, Fact.Predicate.SAW, block, 0.5f, now));
        }
    }

    private void extractConversation(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null && obs.getTargetUUID() != null) {
                String npc = "npc:" + obs.getSourceUUID();
                String other = obs.getMetadata().getOrDefault("speaker_type", "entity") + ":" + obs.getTargetUUID();
                facts.add(new Fact(UUID.randomUUID(), npc, Fact.Predicate.MET, other, 0.95f, now));
            }
        }
    }

    private void extractHelped(Event event, List<Fact> facts, Instant now) {
        for (Observation obs : event.getSourceObservations()) {
            if (obs.getSourceUUID() != null && obs.getTargetUUID() != null) {
                String helper = "npc:" + obs.getSourceUUID();
                String helped = "entity:" + obs.getTargetUUID();
                facts.add(new Fact(UUID.randomUUID(), helper, Fact.Predicate.HELPED, helped, 1.0f, now));
            }
        }
    }
}
