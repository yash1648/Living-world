package com.aman.ainpc.knowledge;

import com.aman.ainpc.event.Event;
import com.aman.ainpc.event.dispatch.EventConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores semantic facts extracted from events.
 *
 * Implements EventConsumer — whenever an event is dispatched,
 * the extractor derives facts and adds them to the knowledge store.
 */
public class KnowledgeBase implements EventConsumer {

    private final List<Fact> facts = new ArrayList<>();
    private final FactExtractor extractor = new FactExtractor();

    @Override
    public void accept(Event event) {
        List<Fact> extracted = extractor.extract(event);
        facts.addAll(extracted);
    }

    // ── Queries ───────────────────────────────────────────────────

    public List<Fact> getFacts() {
        return Collections.unmodifiableList(facts);
    }

    public List<Fact> getFactsAbout(String subject) {
        return facts.stream()
                .filter(f -> f.getSubject().equals(subject))
                .collect(Collectors.toList());
    }

    public List<Fact> getFactsByPredicate(Fact.Predicate predicate) {
        return facts.stream()
                .filter(f -> f.getPredicate() == predicate)
                .collect(Collectors.toList());
    }

    public List<Fact> getFactsWithObject(String object) {
        return facts.stream()
                .filter(f -> f.getObject().contains(object))
                .collect(Collectors.toList());
    }

    /**
     * Returns a short text summary of the most recent facts, suitable for
     * providing as LLM context during conversation.
     */
    public String summarize(int maxFacts) {
        if (facts.isEmpty()) return "No known facts.";
        return facts.stream()
                .skip(Math.max(0, facts.size() - maxFacts))
                .map(Fact::toString)
                .collect(Collectors.joining("; "));
    }

    public int size() {
        return facts.size();
    }

    public void clear() {
        facts.clear();
    }
}
