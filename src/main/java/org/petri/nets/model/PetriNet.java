package org.petri.nets.model;

import java.util.*;

public interface PetriNet {
    LinkedHashMap<Integer, Integer> getInitialMarking();

    Integer getInitialMarking(Integer placeId);

    void setInitialMarking(Integer placeId, int marking);

    HashMap<Integer, Place> getPlaceMap();
    HashMap<Integer, Transition> getTransitionMap();

    Collection<Transition> getTransitions();

    // add

    Place addPlace();

    Transition addTransition();

    Arc addArc(Place place, Transition transition, int value, boolean startsInPlace);

    // remov
    void removeArc(Place place, Transition transition, boolean isPlaceStart);

    void removePlace(Place place);

    void removePlace(int id);

    void removeTransition(Transition transition);

    void removeTransition(int id);

    boolean hasArc(Place place, Transition transition, boolean startsInPlace);

    Map<Integer, Place> getPlaces();

    Place getPlace(int id);
}
