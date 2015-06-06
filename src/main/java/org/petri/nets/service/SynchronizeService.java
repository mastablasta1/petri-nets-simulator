package org.petri.nets.service;

import org.petri.nets.model.Place;
import org.petri.nets.model.Transition;
import org.petri.nets.synhronize.SynchronizePanel;

/**
 * Created by Asia on 2015-05-17.
 */
public interface SynchronizeService {
    void addPlace();
    void updateReachabilityGraph();
    SynchronizePanel getSynchronizePanel();
}
