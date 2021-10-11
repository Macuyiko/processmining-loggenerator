package org.processmining.plugins.yapetrinetreplayer.replayers.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayState;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public class PetrinetReplayUtils extends PetrinetUtils {
	public static boolean isInvisibleTransition(Transition t, PetrinetLogMapper m) {
		return isInvisibleTransition(t) || m.get(t) == null;
	}

	public static void setInvisibleTransitions(Petrinet petriNet, PetrinetLogMapper m) {
		for (Transition t : petriNet.getTransitions()) {
			t.setInvisible(isInvisibleTransition(t, m));
		}
	}

	public static Transition isExistsInvisibleStatePathToTask(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking) {
		return isExistsInvisibleStatePathToTask(
				petriNet,
				mapping,
				nextActivity,
				initialMarking,
				1000000,
				true,
				true);
	}
	
	public static Transition isExistsInvisibleStatePathToTask(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking,
			int maxStates,
			boolean shortBreathFirst,
			boolean useBreathFirst) {
		
		if (shortBreathFirst) {
			ReplayStateChain oneChain = getOneInvisibleStatePathToTask(petriNet, mapping, nextActivity, initialMarking);
			if (oneChain != null) {
				for (Transition t : oneChain.getLastState().getFireableTransitions()) {
					if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) 
							&& nextActivity.equals(mapping.get(t).getId())) {
						return t;
					}
				}
			}
		}
		
		if (!useBreathFirst)
			return isExistsInvisibleStatePathToTaskDFS(petriNet, mapping, nextActivity, initialMarking, maxStates);
		else
			return isExistsInvisibleStatePathToTaskBFS(petriNet, mapping, nextActivity, initialMarking, maxStates);
	}
	
	public static Transition isExistsInvisibleStatePathToTaskBFS(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking,
			int maxStates) {
		
		initialMarking = getInitialMarking(petriNet, initialMarking);
		
		Set<Marking> visitedMarkings = new HashSet<Marking>();
		visitedMarkings.add(initialMarking);
		Set<Marking> queuedMarkings = new HashSet<Marking>();
		queuedMarkings.add(initialMarking);
		
		int statesChecked = 0;
		
		while (queuedMarkings.size() > 0) {
			statesChecked++;
			Marking markingTodo = queuedMarkings.iterator().next();
			queuedMarkings.remove(markingTodo);
			
			PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
			semantics.initialize(petriNet.getTransitions(), markingTodo);
			Collection<Transition> fireables = semantics.getExecutableTransitions();
			
			if (maxStates <= 0 || statesChecked > maxStates) {
				System.err.println("* Max state exploration limit reached: "+nextActivity);
				return null;
			}
		
			for (Transition t : fireables) {
				if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) 
						&& nextActivity.equals(mapping.get(t).getId()))
					return t;
				if (PetrinetReplayUtils.isInvisibleTransition(t, mapping)) {
					try {
						semantics.executeExecutableTransition(t);
					} catch (IllegalTransitionException e) {
						continue;
					}
					
					Marking newMarking = semantics.getCurrentState();
					if (!visitedMarkings.contains(newMarking))
						queuedMarkings.add(newMarking);
					visitedMarkings.add(newMarking);
				}
			}

		} // end while
		
		return null;
	}
	
	public static Transition isExistsInvisibleStatePathToTaskDFS(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking,
			int maxStates) {
		
		ReplayStateChain stateChain = new ReplayStateChain();
		initialMarking = getInitialMarking(petriNet, initialMarking);
		
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);
		int statesChecked = 0;
		
		while (true) {
			statesChecked++;
			if (maxStates <= 0 || statesChecked > maxStates){
				System.err.println("* Max state exploration limit reached: "+nextActivity);
				return null;
			}
			
			/* Escape self imposed looping */
			if (stateChain.getPreviousState() != null 
					&& stateChain.getPreviousState().isDecisionInvisible()) {
				int occurances = stateChain.getCountMarkingAfterLastInvisibleChainOccurances(
						stateChain.getLastState().getMarking());
				if (occurances > 1) {
					if (!stateChain.retractDecision()) // <-- RETRACT
						break;
					continue;
				}
			}

			/* Fireable reached */
			for (Transition t : stateChain.getLastState().getFireableTransitions()) {
				if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) 
						&& nextActivity.equals(mapping.get(t).getId())) {
					return t;
				}
			}
			
			/* Still possible to fire invisibles */
			if (stateChain.getLastState().hasNextFireableTransition()) {
				Transition fireCandidate = stateChain.getLastState().nextFireableTransition();
				boolean isInvisible = PetrinetReplayUtils.isInvisibleTransition(fireCandidate, mapping);
				
				if (isInvisible) // ADVANCE -->
					stateChain.makeDecision(fireCandidate);
				continue;
			}
			
			if (!stateChain.retractDecision()) // <-- RETRACT
				break;
		} // end while
		
		return null;
	}
	
	public static ReplayStateChain getShortestInvisibleStatePathToTask(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking) {
		return getShortestInvisibleStatePathToTask(
				petriNet,
				mapping,
				nextActivity,
				initialMarking,
				10000,
				true);
	}
	
	public static ReplayStateChain getOneInvisibleStatePathToTask(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking) {
		ReplayStateChain stateChain = new ReplayStateChain();
		
		initialMarking = getInitialMarking(petriNet, initialMarking);
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);
		ReplayState whileState = stateChain.getLastState();
		while (whileState.hasNextFireableTransition()) {
			Transition fireCandidate = stateChain.getLastState().nextFireableTransition();
			boolean isInvisible = PetrinetReplayUtils.isInvisibleTransition(fireCandidate, mapping);	
			if (!isInvisible)
				continue;
			stateChain.makeDecision(fireCandidate);
			for (Transition t : stateChain.getLastState().getFireableTransitions()) {
				if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) 
						&& nextActivity.equals(mapping.get(t).getId())) {
					return stateChain.clone();
				}
			}
			stateChain.retractDecision();
		}
		return null;
	}
	
	public static ReplayStateChain getShortestInvisibleStatePathToTask(
			Petrinet petriNet,
			PetrinetLogMapper mapping,
			String nextActivity,
			Marking initialMarking,
			int maxStates,
			boolean shortBreathFirst) {
		
		ReplayStateChain oneChain = getOneInvisibleStatePathToTask(petriNet, mapping, nextActivity, initialMarking);
		if (oneChain != null)
			return oneChain;
		
		ReplayStateChain stateChain = new ReplayStateChain();
		
		initialMarking = getInitialMarking(petriNet, initialMarking);
		
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);
		
		int bestLength = -1;
		int statesChecked = 0;
		ReplayStateChain bestPath = null;
		
		while (true) {
			statesChecked++;
			if (maxStates <= 0 || statesChecked > maxStates) {
				System.err.println("* Max state exploration limit reached: "+bestLength);
				return bestPath;
			}
			
			/* Escape self imposed looping */
			if (stateChain.getPreviousState() != null 
					&& stateChain.getPreviousState().isDecisionInvisible()) {
				int occurances = stateChain.getCountMarkingAfterLastInvisibleChainOccurances(
						stateChain.getLastState().getMarking());
				if (occurances > 1) {
					if (!stateChain.retractDecision()) // <-- RETRACT
						break;
					continue;
				}
			}

			/* Fireable reached */
			boolean endFound = false;
			
			for (Transition t : stateChain.getLastState().getFireableTransitions()) {
				if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) 
						&& nextActivity.equals(mapping.get(t).getId())) {
					endFound = true;
					break;
				}
			}
			
			if (endFound && (stateChain.size() < bestLength || bestLength == -1)) {
				bestPath = stateChain.clone();
				bestLength = stateChain.size();
			}
			
			/* Still possible to fire invisibles */
			if (stateChain.getLastState().hasNextFireableTransition()) {
				Transition fireCandidate = stateChain.getLastState().nextFireableTransition();
				boolean isInvisible = PetrinetReplayUtils.isInvisibleTransition(fireCandidate, mapping);
				
				if (isInvisible && (stateChain.size() < bestLength || bestLength == -1)) {
					// ADVANCE -->
					stateChain.makeDecision(fireCandidate);
				}
				continue;
			}
			
			if (!stateChain.retractDecision()) // <-- RETRACT
				break;
		} // end while
		
		return bestPath;
	}
	
}
