package org.processmining.plugins.yapetrinetreplayer.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.PetrinetReplayUtils;

public class ReplayState implements Cloneable {
	private Petrinet petriNet;
	private Marking marking;
	private PetrinetSemantics semantics;
	
	private boolean isDecisionMade = false;
	private boolean isDecisionForced = false;
	private boolean isDecisionInvisible = false;
	private boolean isDecisionRepeat = false;
	
	private Transition lastDecision;
	private Set<Place> lastInsertedTokens;
	
	private ArrayList<Transition> fireableTransitions;
	private ArrayList<Transition> forceableTransitions;
	
	private int lastDecisionIndex = 0;
	private int lastForcedIndex = 0;
	
	public ReplayState(Petrinet petriNet, Marking marking) {
		this.petriNet = petriNet;
		this.marking = marking;
		this.semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		semantics.initialize(petriNet.getTransitions(), marking);
		Collection<Transition> fireables = semantics.getExecutableTransitions();
		this.fireableTransitions = new ArrayList<Transition>(fireables);
		Collections.sort(this.fireableTransitions);
	}
	
	public int getDecisionIndex() {
		return lastDecisionIndex;
	}

	public boolean isFireableTransition(Transition t) {
		return fireableTransitions.contains(t);
	}
	
	public Collection<Transition> getFireableTransitions() {
		return fireableTransitions;
	}
	
	public boolean hasNextFireableTransition() {
		if (this.fireableTransitions == null) return false;
		return lastDecisionIndex < this.fireableTransitions.size();
	}

	public Transition nextFireableTransition() {
		Transition next = this.fireableTransitions.get(lastDecisionIndex);
		lastDecisionIndex++;
		return next;
	}
	
	public void resetFireableTransitions() {
		lastDecisionIndex = 0;
	}
	
	public Collection<Transition> getForceableTransitions() {
		return forceableTransitions;
	}
	
	public void setForceableTransitions(Collection<Transition> transitions) {
		forceableTransitions = new ArrayList<Transition>(transitions);
	}
	
	public boolean hasNextForceableTransition() {
		if (this.forceableTransitions == null) return false;
		return lastForcedIndex < this.forceableTransitions.size();
	}

	public Transition nextForceableTransition() {
		Transition next = this.forceableTransitions.get(lastForcedIndex);
		lastForcedIndex++;
		return next;
	}
	
	public void resetForceableTransitions() {
		lastForcedIndex = 0;
	}
	
	public ReplayState getNextState(Transition transitionToFire) {
		Marking realMarking = new Marking(semantics.getCurrentState());
		
		Marking requiredMarking = new Marking();
		Set<Place> beforePlaces = new HashSet<Place>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> aInEdges = 
				petriNet.getInEdges(transitionToFire);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> inEdge : aInEdges) {
			Place before = (Place) inEdge.getSource();
			beforePlaces.add(before);
			int weight = ((Arc) inEdge).getWeight();
			requiredMarking.add(before, weight);
		}
		
		Marking forcedMarking = new Marking(semantics.getCurrentState());
		Set<Place> missingPlaces = new HashSet<Place>();
		for (Place p : beforePlaces) {
			int missing = requiredMarking.occurrences(p) - forcedMarking.occurrences(p);
			if (missing > 0) {
				forcedMarking.add(p, missing);
				missingPlaces.add(p);
			}
		}
		semantics.setCurrentState(forcedMarking);
		
		if (missingPlaces.size() > 0)
			lastInsertedTokens = missingPlaces;
		lastDecision = transitionToFire;
		
		isDecisionMade = true;
		isDecisionRepeat = false;
		isDecisionInvisible = PetrinetReplayUtils.isInvisibleTransition(lastDecision);
		isDecisionForced = missingPlaces.size() > 0;
		
		try {
			semantics.executeExecutableTransition(transitionToFire);
		} catch (IllegalTransitionException e) {
			return null;
		}
		
		Marking newMarking = semantics.getCurrentState();
		semantics.setCurrentState(realMarking);
		
		return new ReplayState(petriNet, newMarking);
	}

	public ReplayState repeatState(boolean asForced, boolean asInvisible) {
		lastDecision = null;
		isDecisionMade = false;
		isDecisionRepeat = true;
		isDecisionInvisible = asInvisible;
		isDecisionForced = asForced;
		
		return new ReplayState(petriNet, semantics.getCurrentState());
	}

	public Marking getMarking() {
		return marking;
	}

	public void setMarking(Marking marking) {
		this.marking = marking;
	}

	public Transition getLastDecision() {
		return lastDecision;
	}
	
	public void resetDecision() {
		lastInsertedTokens = null;
		lastDecision = null;
		isDecisionMade = false;
		isDecisionInvisible = false;
		isDecisionRepeat = false;
		isDecisionForced = false;
	}

	public Set<Place> getInsertedTokens() {
		return lastInsertedTokens == null ? new HashSet<Place>() : lastInsertedTokens;
	}
	
	public boolean isDecisionInvisible() {
		return isDecisionInvisible;
	}
	
	public boolean isDecisionMade() {
		return isDecisionMade;
	}
	
	public boolean isDecisionRepeated() {
		return isDecisionRepeat;
	}
	
	public boolean isDecisionForced() {
		return isDecisionForced;
	}

	public String toString() {
		String repr = "";
		if (this.isDecisionRepeated())
			repr += "<NR>";
		else if (!this.isDecisionMade())
			repr += "<N>";
		
		if (this.isDecisionForced())
			repr += "<F>";
		if (this.isDecisionInvisible())
			repr += "<I>";
		
		repr += (this.isDecisionMade()) ? this.getLastDecision().getLabel() : "*none*";
		return repr;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplayState other = (ReplayState) obj;
		if (marking == null) {
			if (other.marking != null)
				return false;
		} else if (!marking.equals(other.marking))
			return false;
		if (petriNet == null) {
			if (other.petriNet != null)
				return false;
		} else if (!petriNet.equals(other.petriNet))
			return false;
		return true;
	}

	public ReplayState clone() {
		Marking newMarking = new Marking(this.marking.toList());
		ReplayState clone = new ReplayState(petriNet, newMarking);
		
		clone.lastDecision = this.lastDecision;
		
		if (this.lastInsertedTokens != null)
			clone.lastInsertedTokens = new HashSet<Place>(this.lastInsertedTokens);
	
		if (this.forceableTransitions != null)
			clone.forceableTransitions = new ArrayList<Transition>(this.forceableTransitions);
		
		clone.lastDecisionIndex = this.lastDecisionIndex;
		clone.lastForcedIndex = this.lastForcedIndex;
		
		clone.isDecisionMade = this.isDecisionMade;
		clone.isDecisionForced = this.isDecisionForced;
		clone.isDecisionInvisible = this.isDecisionInvisible;
		clone.isDecisionRepeat = this.isDecisionRepeat;
		
		return clone;
	}
}
