package org.petri.nets.service;

import com.google.common.collect.BiMap;
import edu.uci.ics.jung.graph.Graph;
import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.petri.nets.gui.graph.*;
import org.petri.nets.model.DomainModel;
import org.petri.nets.model.Place;
import org.petri.nets.model.reachability.State;
import org.petri.nets.model.Transition;
import org.petri.nets.model.reachability.TransitionEdge;
import org.petri.nets.synhronize.SynchronizePanel;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GraphServiceImpl implements GraphService {
    private final DomainModel model;
    private SynchronizeService syncService;
    private SaveGraphAsFile saveGraphAsFile;
    private BiMap<PlaceGraphCell, Place> placeGUI;
    private BiMap<TransitionGraphCell, Transition> transitonGUI;

    public GraphServiceImpl(DomainModel model, SynchronizePanel synchronizePanel) {
        this.model = model;
        this.syncService = new SynchronizeServiceImpl(model, synchronizePanel);
        saveGraphAsFile = new SaveGraphAsFile(model);
        this.placeGUI = this.model.getSyncModel().getPlaceGUI();
        this.transitonGUI = this.model.getSyncModel().getTransitonGUI();
    }

    @Override
    public void removeFromGraph(Object cell) {
        if (cell instanceof Object[])
            removeFromGraph((Object[]) cell);
        else if (cell instanceof CellView) {
            CellView cellView = (CellView) cell;
            removeFromGraph(new Object[]{cellView.getCell()});
        } else {
            removeFromGraph(new Object[]{cell});
        }
    }

    public void removeFromGraph(Object[] cells) {
        for (Object cell : cells) {
            DefaultGraphCell graphCell = CustomCellViewFactory.tryCastToCell(cell);
            if (graphCell != null) {
                for (Object child : graphCell.getChildren()) {
                    Port port = CustomCellViewFactory.tryCastToPort(child);
                    if (port != null) {
                        port.edges().forEachRemaining(this::removeFromGraph);
                    }
                }
            }
            model.getPetriNetGraph().getGraphLayoutCache().remove(new Object[]{cell});
            synhronizeRemoveFromGraph(cell);
        }
        invalidateReachabilityGraph();
    }

    private void synhronizeRemoveFromGraph(Object cell) {
        if (isPlace(cell)) {
            model.getPetriNet().removePlace(placeGUI.get(cell));
            placeGUI.remove(cell);
        } else if (isTransition(cell)) {
            model.getPetriNet().removeTransition(transitonGUI.get(cell));
            transitonGUI.remove(cell);
        } else {
            removeArc((ArcGraphCell) cell);
        }
    }

    @Override
    public PlaceGraphCell addPlace(Point position) {
        Place place = model.getPetriNet().addPlace();
        PlaceGraphCell cell = new PlaceGraphCell(
                place.getId(),
                position);
        model.getPetriNetGraph().getGraphLayoutCache().insert(cell);
        syncService.addPlace();
        placeGUI.put(cell, place);
        invalidateReachabilityGraph();
        return cell;
    }

    @Override
    public TransitionGraphCell addTransition(Point position) {
        Transition transition = model.getPetriNet().addTransition();
        TransitionGraphCell cell = new TransitionGraphCell(
                transition.getId(),
                position);
        model.getPetriNetGraph().getGraphLayoutCache().insert(cell);
        transitonGUI.put(cell, transition);
        invalidateReachabilityGraph();
        return cell;
    }

    @Override
    public void addArc(PetriNetGraphCell start, PetriNetGraphCell end) {
        ArcGraphCell edge = new ArcGraphCell();
        edge.setSource(start.getFirstChild());
        edge.setTarget(end.getFirstChild());
        edge.setStart(start);
        edge.setEnd(end);
        edge.setPriority(0);
        edge.setValue(0);
        model.getPetriNetGraph().getGraphLayoutCache().insert(edge);

        if (isPlace(start))
            model.getPetriNet().addArc(placeGUI.get(start), transitonGUI.get(end), 1, true);
        else
            model.getPetriNet().addArc(placeGUI.get(end), transitonGUI.get(start), 1, false);

        invalidateReachabilityGraph();
    }

    public DomainModel getModel() {
        return model;
    }

    @Override
    public CellView getLastFocusedCell() {
        PetriNetGraphUI ui = (PetriNetGraphUI) model.getPetriNetGraph().getUI();
        return ui.getLastFocus();
    }


    @Override
    public Object[] getSelectedCells() {
        return model.getPetriNetGraph().getSelectionCells();
    }

    @Override
    public boolean isTransition(Object cell) {
        return CustomCellViewFactory.isTransition(cell);
    }

    @Override
    public boolean isPlace(Object cell) {
        return CustomCellViewFactory.isPlace(cell);
    }

    @Override
    public boolean isVertex(Object cell) {
        return CustomCellViewFactory.isVertex(cell);
    }

    @Override
    public PetriNetGraphCell[] getSelectedVertices() {
        List<PetriNetGraphCell> collect = Arrays.stream(getSelectedCells())
                .filter(this::isVertex)
                .map(c -> (PetriNetGraphCell) c)
                .collect(Collectors.toList());
        return collect.toArray(new PetriNetGraphCell[collect.size()]);
    }

    @Override
    public void saveGraphAsFile(File file) {
        saveGraphAsFile.saveGaph(file);
    }

    @Override
    public void openGraphfromFile(File file) {
        saveGraphAsFile.openGraph(file);
    }

    private int getId(String stringId) {
        int id = Integer.valueOf(stringId.substring(1));
        return id - 1;
    }

    private void removeArc(ArcGraphCell arc) {
        if (isPlace(arc.getStart())) {
            model.getPetriNet().removeArc(placeGUI.get(arc.getStart()), transitonGUI.get(arc.getEnd()), true);
        } else {
            model.getPetriNet().removeArc(placeGUI.get(arc.getEnd()), transitonGUI.get(arc.getStart()), false);
        }
    }

    @Override
    public void setInitialMarking(int placeId, int marking) {
        model.getPetriNet().setInitialMarking(placeId, marking);
        invalidateReachabilityGraph();
    }

    @Override
    public HashMap<Integer, Integer> getInitialMarking() {
        return model.getPetriNet().getInitialMarking();
    }

    @Override
    public int getInitialMarking(int placeId) {
        return model.getPetriNet().getInitialMarking(placeId);
    }

    private void invalidateReachabilityGraph() {
        ReachabilityGraphGenerator reachGraph = new ReachabilityGraphGenerator(model.getPetriNet(), 50);
        Graph<State, TransitionEdge> reachabilityGraph = reachGraph.generateGraph();
        model.setReachabilityGraph(reachabilityGraph);
        syncService.updateReachabilityGraph();
    }

    @Override
    public JGraph getPetriNetGraph() {
        return model.getPetriNetGraph();
    }

    @Override
    public DomainModel getDomainModel() {
        return model;
    }

    @Override
    public Graph<State, TransitionEdge> getReachabilityGraph() {
        return model.getReachabilityGraph();
    }

    @Override
    public SynchronizeService getSynchronizeService() {
        return syncService;
    }
}
