package org.petri.nets.service;

import edu.uci.ics.jung.graph.Graph;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.petri.nets.gui.graph.PetriNetGraphCell;
import org.petri.nets.gui.graph.PlaceGraphCell;
import org.petri.nets.gui.graph.TransitionGraphCell;
import org.petri.nets.model.DomainModel;
import org.petri.nets.model.Transition;
import org.petri.nets.synhronize.SynchronizePanel;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface GraphService {

    void removeFromGraph(Object cell);


    PlaceGraphCell addPlace(Point position);

    TransitionGraphCell addTransition(Point position);

    void addArc(PetriNetGraphCell start, PetriNetGraphCell end);

    void setInitialMarking(int placeId, int marking);

    CellView getLastFocusedCell();

    Object[] getSelectedCells();

    boolean isVertex(Object cell);

    PetriNetGraphCell[] getSelectedVertices();

    boolean isTransition(Object cell);

    boolean isPlace(Object cell);
    public void saveGraphAsFile();

    HashMap<Integer, Integer> getInitialMarking();

    int getInitialMarking(int placeId);

    JGraph getPetriNetGraph();

    DomainModel getDomainModel();

    Graph<HashMap<Integer,Integer>,Transition> getReachabilityGraph();

    SynchronizeService getSynchronizeService();
}
