package org.processmining.plugins.yapetrinetreplayer.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.PetrinetReplayUtils;

public class ReplayStateChain implements Cloneable {
	private ArrayList<ReplayState> stateChain;
	
	public ReplayStateChain() {
		this.stateChain = new ArrayList<ReplayState>();
	}
	
	public String toString() {
		String repr = "";
		for (ReplayState state : stateChain) {
			repr += state.toString();
			repr += ", ";
		}
		return repr;
	}
	
	public List<String> getLabelSequence() {
		List<String> sequence = new ArrayList<String>();
		for (ReplayState state : stateChain) {
			Transition fired = state.getLastDecision();
			if (fired == null) continue;
			if (PetrinetReplayUtils.isInvisibleTransition(fired)) continue;
			sequence.add(fired.getLabel());
		}
		return sequence;
	}
	
	public List<String> getClassSequence(PetrinetLogMapper mapper) {
		ArrayList<String> sequence = new ArrayList<String>();
		for (ReplayState state : stateChain) {
			Transition fired = state.getLastDecision();
			if (fired == null) continue;
			if (PetrinetReplayUtils.isInvisibleTransition(fired, mapper)) continue;
			sequence.add(mapper.get(fired).getId());
		}
		return sequence;
	}
	
	public int getCountMarkingOccurances(Marking m) {
		int count = 0;
		for (ReplayState state : stateChain) {
			if (state.getMarking().equals(m))
				count++;
		}
		return count;
	}
	
	public int getCountMarkingAfterLastInvisibleChainOccurances(Marking m) {
		int count = 0;
		for (int k = stateChain.size()-1; k>=0; k--) {
			ReplayState state = stateChain.get(k);
			if (state.isDecisionMade() && !state.isDecisionInvisible())
				break;
			if (state.getMarking().equals(m))
				count++;
		}
		return count;
	}
	
	public int getCountForced() {
		int count = 0;
		for (ReplayState state : stateChain) {
			if (state.isDecisionForced())
				count++;
		}
		return count;
	}
	
	public int getCountInvisible() {
		int count = 0;
		for (ReplayState state : stateChain) {
			if (state.isDecisionInvisible())
				count++;
		}
		return count;
	}

	public boolean isNeverForced() {
		return getCountForced() == 0;
	}

	public boolean isNeverInvisible() {
		return getCountInvisible() == 0;
	}

	public Transition getLastNonInvisibleDecision() {
		for (int i = stateChain.size()-1; i>=0; i--) {
			ReplayState state = stateChain.get(i);
			if (state.isDecisionMade() && !state.isDecisionInvisible())
				return state.getLastDecision();
		}
		return null;
	}
	
	public Collection<Transition> getDecisions() {
		return getLastState().getFireableTransitions();
	}
	
	public void makeDecision(Transition transitionToFire) {
		ReplayState newState = getLastState().getNextState(transitionToFire);
		addState(newState);
	}
	
	public void repeatDecision(boolean asForced, boolean asInvisible) {
		ReplayState newState = getLastState().repeatState(asForced, asInvisible);
		addState(newState);
	}

	public boolean retractDecision() {
		stateChain.remove(stateChain.size()-1);
		if (stateChain.size() == 0)
			return false;
		getLastState().resetDecision();
		return true;
	}
	
	public void addState(ReplayState newState) {
		stateChain.add(newState);
	}
	
	public ReplayState getPreviousState() {
		if (stateChain.size() >= 2)
			return stateChain.get(stateChain.size()-2);
		return null;
	}
	
	public ReplayState getLastState() {
		return stateChain.get(stateChain.size()-1);
	}
	
	public ReplayStateChain clone() {
		ReplayStateChain clone = new ReplayStateChain();
		for (ReplayState rs : stateChain)
			clone.addState(rs.clone());
		return clone;
	}

	public ArrayList<ReplayState> getStateChain() {
		return stateChain;
	}

	public void clear() {
		this.stateChain.clear();
	}
	
	public int size() {
		return this.stateChain.size();
	}
}
