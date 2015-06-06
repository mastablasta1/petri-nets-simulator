package org.petri.nets.model.reachability;

import com.google.common.collect.Maps;
import org.petri.nets.model.Place;

import java.util.Map;

public class State {
    private Map<Integer, Integer> marking;
    private int depth;

    public State(Map<Integer, Integer> marking) {
        this.marking = Maps.newHashMap(marking);
    }

    public State() {
        marking = Maps.newHashMap();
    }

    public Map<Integer, Integer> getMarking() {
        return Maps.newHashMap(marking);
    }

    public void setMarking(Map<Integer, Integer> marking) {
        this.marking = Maps.newHashMap(marking);
    }

    public int getMarkingForPlace(Place place) {
        return marking.get(place.getId());
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state1 = (State) o;
        return !(marking != null ? !marking.equals(state1.marking) : state1.marking != null);
    }

    @Override
    public String toString() {
        return "State{" +
                "marking=" + marking.values() +
                ", depth=" + depth +
                '}';
    }
}