package org.processmining.plugins.kutoolbox.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class PetrinetUtils {
	public static boolean isInvisibleTransition(Transition t) {
		return t.getLabel().equals("") || t.isInvisible();
	}

	public static Set<String> getDistinctLabels(Petrinet petriNet) {
		Set<String> labels = new HashSet<String>();
		for (Transition transition : petriNet.getTransitions()) {
			if (isInvisibleTransition(transition))
				continue;
			labels.add(transition.getLabel());
		}
		return labels;
	}

	public static Set<Place> getPlacesBeforeTransition(Petrinet petriNet, Transition t) {
		Set<Place> places = new HashSet<Place>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aInEdges = petriNet.getInEdges(t);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge : aInEdges)
			places.add((Place) inEdge.getSource());
		return places;
	}

	public static Set<Place> getPlacesAfterTransition(Petrinet petriNet, Transition t) {
		Set<Place> places = new HashSet<Place>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aOutEdges = petriNet.getOutEdges(t);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge : aOutEdges)
			places.add((Place) outEdge.getTarget());
		return places;
	}

	public static Transition getTransitionsById(Petrinet petriNet, String id) {
		for (Transition t : petriNet.getTransitions()) {
			if (t.getId().toString().equals(id))
				return t;
		}
		return null;
	}

	public static Set<Transition> getTransitionsByLabel(Petrinet petriNet, String label) {
		Set<Transition> transitions = new HashSet<Transition>();
		for (Transition t : petriNet.getTransitions()) {
			if (t.getLabel().equals(label))
				transitions.add(t);
		}
		return transitions;
	}

	public static Set<Place> getStartPlacesByLabel(Petrinet petriNet, String label) {
		Set<Transition> transitions = getTransitionsByLabel(petriNet, label);
		Set<Place> startPlaces = getStartPlaces(petriNet);
		for (Transition t : transitions) {
			Set<Place> beforePlaces = getPlacesBeforeTransition(petriNet, t);
			beforePlaces.retainAll(startPlaces);
			if (beforePlaces.size() > 0) {
				return beforePlaces;
			}
		}
		return null;
	}

	public static Set<Place> getStartPlaces(Petrinet petriNet) {
		Set<Place> startSet = new HashSet<Place>();
		for (Place p : petriNet.getPlaces()) {
			if (petriNet.getInEdges(p).size() == 0)
				startSet.add(p);
		}
		return startSet;
	}

	public static Set<Place> getEndPlaces(Petrinet petriNet) {
		Set<Place> endSet = new HashSet<Place>();
		for (Place p : petriNet.getPlaces()) {
			if (petriNet.getOutEdges(p).size() == 0)
				endSet.add(p);
		}
		return endSet;
	}

	public static boolean isMarkingHasSinglePlace(Marking m, Set<Place> places) {
		for (Place p : places) {
			if (m.contains(p))
				return true;
		}
		return false;
	}

	public static boolean isMarkingHasAllPlaces(Marking m, Set<Place> places) {
		for (Place p : places) {
			if (!m.contains(p))
				return false;
		}
		return true;
	}

	public static boolean isMarkingOnlyHasPlaces(Marking m, Set<Place> places) {
		for (Place p : m.toList()) {
			if (!places.contains(p))
				return false;
		}
		return true;
	}
	
	public static Marking getInitialMarking(Petrinet petriNet) {
		return getInitialMarking(petriNet, null);
	}
	
	public static Marking getInitialMarking(Petrinet petriNet, Marking initialMarking) {
		if (initialMarking == null) {
			initialMarking = new Marking();
			initialMarking.addAll(PetrinetUtils.getStartPlaces(petriNet));
		}
		return initialMarking;
	}
	
	public static Marking getFinalMarking(Petrinet petriNet) {
		return getFinalMarking(petriNet, null);
	}
	
	public static Marking getFinalMarking(Petrinet petriNet, Marking finalMarking) {
		if (finalMarking == null) {
			finalMarking = new Marking();
			finalMarking.addAll(PetrinetUtils.getEndPlaces(petriNet));
		}
		return finalMarking;
	}
}
