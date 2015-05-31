package org.petri.nets.service;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import org.petri.nets.model.*;

import java.util.*;

/**
 * Created by Małgorzata on 2015-05-24.
 */
public class ReachGraph {
    public Graph<HashMap<Integer,Integer>, Transition> reachGraph;
    private PetriNet petriNet;

    public ReachGraph(PetriNet pN) {
        petriNet = new ListPetriNet();
        if (pN.getPlacesCount() > 0) {
            petriNet.setPlaceMap(pN.getPlaceMap());
            petriNet.setTransitionMap(pN.getTransitionMap());
            petriNet.setInitialMarking(pN.getInitialMarking());
        }
        reachGraph = new SparseMultigraph<>();
    }

    public void RunReachGraph() {
        GenerateGraph(petriNet.getInitialMarking());
    }

    public void GenerateGraph(HashMap<Integer,Integer> marking) {
        petriNet.setInitialMarking(marking);
        HashMap<Integer,Integer> prevMarking = new HashMap<>();
        prevMarking.putAll(marking);
        //jesli graf nie ma wierzcholka, dodajemy go
        if (reachGraph.getVertexCount() <= 0) {
            reachGraph.addVertex(prevMarking); //przeciazyc toString() dla List<Integer>, zeby wyswietlalo {1,0,0} w wezle
        }
        //tworzymy liste mozliwych przejsc przy tym znakowaniu
        List<Transition> possibleTransitions = new ArrayList<>(); //inicjacja listy przejsc mozliwych do wykonania przy tym znakowaniu
        for(Map.Entry<Integer,Transition> allTransitionsEntry:petriNet.getTransitionMap().entrySet()){ //iterujemy sie po wszystkich przejsciach
            Transition transition = allTransitionsEntry.getValue(); //przejscie
            if(IsTransitionDoable(transition)) { //jesli przejcie jest mozliwe do wykonania przy tym znakowaniu
                possibleTransitions.add(transition); //dodajemy do listy mozliwych przejsc
            }
        }

        //TODO: uporzadkowac liste wedlug priorytetow przejsc
        for(int i=0;possibleTransitions.size()>i; i++){ //iterujemy sie po mozliwych do wykonania przejsciach
            //stworzyc nowe przejscie bo sie wykrzacza?
            Transition possibleTransition=new Transition(-1,0);
            possibleTransition=possibleTransitions.get(i); //pobieramy przejscie
            HashMap<Integer,Integer> newMarking = new HashMap<>(); //inicjalizujemy nowe znakowanie
            newMarking.putAll(DoTransition(possibleTransition)); //wykonujemy przejscie, pobieramy nowe znakowanie po jego wykonaniu
            reachGraph.addVertex(newMarking); //dodajemy nowy wierzcholek
            reachGraph.addEdge(possibleTransition, prevMarking, newMarking); //dodajemy nowa krakwedz
            GenerateGraph(newMarking); //wywolujemy funkcje dla nowego znakowania
        }
    }

    public Transition ChooseHighestPriorityTransition(HashMap<Integer,Transition> transitionMap){
        //wez wszystkie miejsca, znajdz to, ktorego luk wychodzacy ma najwyzszy priorytet
        //sprawdz
        Transition highestPriorityTransition=null;
        int priority=-1;
        for(Map.Entry<Integer,Transition> transitionEntry:transitionMap.entrySet()){
            Transition transition=transitionEntry.getValue();
            if(priority< transition.getPriority()){
                priority=transition.getPriority();
                highestPriorityTransition=transition;
            }
        }
        return highestPriorityTransition;
    }

    //jesli we wszystkich miejscach, ktore wchodza do przejscia jest wystarczajaco duzo znacznikow
    //mozemy wykonac przejscie
    public boolean IsTransitionDoable(Transition transition) {
        boolean isTransitionDoable = false;
        HashMap<Place, Arc> startingPlacesMap = new HashMap<>();
        startingPlacesMap.putAll(transition.getPlaceFrom());
        for (Map.Entry<Place, Arc> startingPlacesSet : startingPlacesMap.entrySet()) {
            int placeMarking = startingPlacesSet.getKey().getMarking();
            int arcValue = startingPlacesSet.getValue().getValue();
            if (placeMarking < arcValue) return false;
            isTransitionDoable = true;
        }
        return isTransitionDoable;
    }

    //jesli wybralismy juz przejscie o najwyzszym priorytecie
    public HashMap<Integer,Integer> DoTransition(Transition transition) {
        HashMap<Place, Arc> startingPlaces = new HashMap<>();
        startingPlaces.putAll(transition.getPlaceFrom());
        //zabieramy znaczniki z miejsc wejsciowych w zaleznosci od wartosci lukow z nich wychodzacych
        int takenMarkingCount = 0;
        for (Map.Entry<Place, Arc> startingPlacesSet : startingPlaces.entrySet()) {
            Place startPlace = startingPlacesSet.getKey();
            Arc arcLeavingStartPlace = startingPlacesSet.getValue();
            takenMarkingCount += arcLeavingStartPlace.getValue();
            int newMarkingForStartPlace=startPlace.getMarking() - arcLeavingStartPlace.getValue();   //odejmujemy znaczniki z miejsca startowego (tyle ile wartość łuku wychodzacego)
            petriNet.setInitialMarking(startPlace.getIdPlace(),newMarkingForStartPlace);
            startPlace.setMarking(newMarkingForStartPlace);
        }
        for (Map.Entry<Place, Arc> endPlaceSet : transition.getPlaceTo().entrySet()) //iterujemy sie po miejscach, do ktorych prowadzi przejscie
        {
            //wykonanie przejscia
            Place endPlace = endPlaceSet.getKey();              //miejsce, do ktorego trafilismy z przejscia
            Arc arcEnteringEndPlace = endPlaceSet.getValue();   //luk, ktory prowadzi do miejsca
            if (takenMarkingCount >= arcEnteringEndPlace.getValue())     //jesli nie, przejscia nie mozna wykonac - chyba wtedy jest niezywotne
            {
                int newMarkingForEndPlace=endPlace.getMarking() + arcEnteringEndPlace.getValue(); //dodajemy znaczniki do miejsca koncowego (tyle ile wartość łuku wchodzacego)
                petriNet.setInitialMarking(endPlace.getIdPlace(),newMarkingForEndPlace);
                endPlace.setMarking(newMarkingForEndPlace);
            }
        }
        return petriNet.getInitialMarking();
    }
}