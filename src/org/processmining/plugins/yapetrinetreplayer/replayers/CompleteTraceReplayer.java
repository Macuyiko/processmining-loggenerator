package org.processmining.plugins.yapetrinetreplayer.replayers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.LogReplayUtils;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.PetrinetReplayUtils;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayState;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public class CompleteTraceReplayer extends TraceReplayer {

	private List<XEvent> history;
	private Marking initialMarking;
	private boolean mustReachEndPlace;
	private boolean mustConsumeAllTokens;
	private boolean allowForceFire;
	private boolean lazyInvisibles;
	private int invisibleDepth;
	private boolean useUpperBound;
	private boolean useSortedInvisible;
	
	private ReplayStateChain stateChain;
	private int currentSequencePosition;
	private int currentUpperBound;
	
	private Transition FAKE_REPEAT_TRANSITION = PetrinetFactory.newPetrinet("fake").addTransition("fake");

	public CompleteTraceReplayer(Petrinet net, PetrinetLogMapper mapping) throws Exception {
		super(net, mapping);
	}

	public void resetReplayer(List<XEvent> history, Marking initialMarking) {
		resetReplayer(history, initialMarking, false, false, true, false, -1, false, false);
	}

	public void resetReplayer(List<XEvent> history, Marking initialMarking, 
			boolean mustReachEndPlace,
			boolean mustConsumeAllTokens, 
			boolean allowForceFire, 
			boolean lazyInvisibles, 
			int invisibleDepth,
			boolean useUpperBound,
			boolean useSortedInvisible) {
	
		initialMarking = PetrinetUtils.getInitialMarking(petriNet, initialMarking);
		
		this.history = history;
		this.initialMarking = initialMarking;
		this.mustReachEndPlace = mustReachEndPlace;
		this.mustConsumeAllTokens = mustConsumeAllTokens;
		this.allowForceFire = allowForceFire;
		this.lazyInvisibles = lazyInvisibles;
		this.invisibleDepth = invisibleDepth;
		this.useUpperBound = useUpperBound;
		this.useSortedInvisible = useSortedInvisible;
		
		this.stateChain = new ReplayStateChain();
	
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);
	
		this.currentSequencePosition = 0;
		this.currentUpperBound = -1;
	}

	public Set<ReplayStateChain> getStatePathsForSequence(List<XEvent> history, Marking initialMarking) {
		return getAllStatePathsForSequence(history, initialMarking, 
				false, false, true, false, -1, true, true, 2); // use at least some heuristics
	}

	public Set<ReplayStateChain> getAllStatePathsForSequence(List<XEvent> history, Marking initialMarking,
			boolean mustReachEndPlace, 
			boolean mustConsumeAllTokens, 
			boolean allowForceFire, 
			boolean lazyInvisibles,
			int invisibleDepth, 
			boolean useUpperBound,
			boolean useSortedInvisible,
			int returnFirstPath) {
	
		Set<ReplayStateChain> statePaths = new HashSet<ReplayStateChain>();
	
		resetReplayer(history, initialMarking, 
				mustReachEndPlace, 
				mustConsumeAllTokens, 
				allowForceFire, 
				lazyInvisibles,
				invisibleDepth,
				useUpperBound,
				useSortedInvisible);
		
		ReplayStateChain bestPath = null;
		while (true) {
			ReplayStateChain path = getNextStatePath();
			if (path == null)
				break;
			bestPath = path;
			// 0 1 -> add
			//   1 -> return first
			//     2 -> return first non forced
			if (returnFirstPath < 2)
				statePaths.add(path);
			if (returnFirstPath == 1)
				break;
			if (returnFirstPath == 2 && path.getCountForced() == 0) {
				statePaths.add(path);
				break;
			}
		}
		if (statePaths.size() == 0 && bestPath != null)
			// If no single 0-forced found and one path requested, add best next
			statePaths.add(bestPath);
		return statePaths;
	}

	public ReplayStateChain getNextStatePath() {
		ReplayStateChain toReturn = null;
	
		if (stateChain == null)
			return null;
	
		if (history.size() == 0) {
			stateChain = null;
			ReplayStateChain emptyChain = new ReplayStateChain();
			ReplayState initialState = new ReplayState(petriNet, initialMarking);
			emptyChain.addState(initialState);
			return emptyChain;
		}
		
		while (true) {
			if (toReturn != null)
				break;
	
			boolean rewindOnRetraction = stateChain.getPreviousState() != null
					&& !stateChain.getPreviousState().isDecisionInvisible();
	
			String currentActivity = null;
			if (currentSequencePosition < history.size())
				currentActivity = mapping.getEventClassifier().getClassIdentity(
						history.get(currentSequencePosition));
	
			if (currentActivity != null && stateChain.getLastState().getForceableTransitions() == null)
				stateChain.getLastState().setForceableTransitions(mapping.getTransitionsForActivity(currentActivity));
			
			if (currentActivity != null && !mapping.eventHasTransition(currentActivity)) {
				Set<Transition> fakeForceSet = new HashSet<Transition>();
				fakeForceSet.add(FAKE_REPEAT_TRANSITION);
				stateChain.getLastState().setForceableTransitions(fakeForceSet);
			}
	
			// Escape self imposed looping
			if (stateChain.getPreviousState() != null 
					&& stateChain.getPreviousState().isDecisionInvisible()) {
				int occurances = stateChain.getCountMarkingAfterLastInvisibleChainOccurances(
						stateChain.getLastState().getMarking());
				if (occurances > 1) {
					if (!stateChain.retractDecision())
						break;
					if (rewindOnRetraction)
						currentSequencePosition--;
					continue;
				}
			}
	
			boolean endReached = PetrinetReplayUtils.isMarkingHasSinglePlace(
					stateChain.getLastState().getMarking(),
					PetrinetReplayUtils.getEndPlaces(petriNet));
			boolean allConsumed = PetrinetReplayUtils.isMarkingOnlyHasPlaces(
					stateChain.getLastState().getMarking(),
					PetrinetReplayUtils.getEndPlaces(petriNet));
			boolean sequenceDone = stateChain.getClassSequence(mapping).equals(
					LogReplayUtils.getClassSequence(history, mapping));
						
			if (sequenceDone) {
				boolean addToSet = !((mustReachEndPlace && !endReached) 
						|| (mustConsumeAllTokens && !allConsumed)
						|| (stateChain.getLastState().isDecisionMade())
						|| (!sequenceDone));
				if (addToSet)
					toReturn = stateChain.clone();
			}
			
			// End reached: no fire and no force
			if (!stateChain.getLastState().hasNextFireableTransition()
					&& (!allowForceFire || !stateChain.getLastState().hasNextForceableTransition())) {
				if (!stateChain.retractDecision())
					break;
				if (rewindOnRetraction)
					currentSequencePosition--;
				continue;
			}
	
			// Still possible to fire invisibles / enabled
			if (stateChain.getLastState().hasNextFireableTransition()) {
				Transition fireCandidate = stateChain.getLastState().nextFireableTransition();
				
				int currentInvisibleDepth = this.getCountEndingInvisibleDecisions(stateChain);
				boolean hasEnabled = this.isFireableSetHasEnabled(
						stateChain.getLastState().getFireableTransitions(), currentActivity);
				boolean isInvisible = PetrinetReplayUtils.isInvisibleTransition(fireCandidate, mapping);
				
				boolean orderCheck = stateChain.getPreviousState() == null  
						|| !useSortedInvisible 
						|| !stateChain.getPreviousState().isFireableTransition(fireCandidate)
						|| !stateChain.getPreviousState().isDecisionInvisible()
						|| !isInvisible
						|| fireCandidate.compareTo(stateChain.getPreviousState().getLastDecision()) > 0;
				boolean invisibleCheck = isInvisible 
						&& (currentActivity == null 
							|| invisibleDepth == -1 
							|| currentInvisibleDepth < invisibleDepth);
				boolean enabledCheck = !isInvisible 
						&& currentActivity != null 
						&& currentActivity.equals(mapping.get(fireCandidate).getId());
	
				if (enabledCheck || (orderCheck && invisibleCheck && (!hasEnabled || !lazyInvisibles))) {
					if (currentUpperBound > 0 
							&& useUpperBound 
							&& currentUpperBound <= stateChain.getCountForced())
							continue;
					stateChain.makeDecision(fireCandidate);
					if (!isInvisible)
						currentSequencePosition++;
				}
				continue;
			}
	
			/* Still possible to force fire*/
			if (allowForceFire && stateChain.getLastState().hasNextForceableTransition()) {
				// Nothing to fire, try to force
				Transition fireCandidate = stateChain.getLastState().nextForceableTransition();
				if (fireCandidate.equals(FAKE_REPEAT_TRANSITION)) {
					stateChain.repeatDecision(true, false);
					currentSequencePosition++;
					continue;
				}
				if (!stateChain.getLastState().getFireableTransitions().contains(fireCandidate)) {
					if (currentUpperBound > 0 
							&& useUpperBound 
							&& currentUpperBound <= stateChain.getCountForced())
							continue;
					stateChain.makeDecision(fireCandidate);
					currentSequencePosition++;
				}
				continue;
			}
		} // end while
		
		if ((toReturn != null) 
				&& (currentUpperBound == -1 || toReturn.getCountForced() < currentUpperBound))
			currentUpperBound = toReturn.getCountForced();
		
		return toReturn;
	}

	private int getCountEndingInvisibleDecisions(ReplayStateChain stateChain) {
		int count = 0;
		for (int i = stateChain.size() - 1; i >= 0; i--) {
			ReplayState state = stateChain.getStateChain().get(i);
			if (state.isDecisionMade() && state.isDecisionInvisible())
				count++;
			if (state.isDecisionMade() && !state.isDecisionInvisible())
				break;
		}
		return count;
	}

	private boolean isFireableSetHasEnabled(Collection<Transition> fireableTransitions, String currentActivity) {
		if (currentActivity == null)
			return false;
		for (Transition t : fireableTransitions) {
			if (!PetrinetReplayUtils.isInvisibleTransition(t, mapping) && currentActivity.equals(mapping.get(t).getId()))
				return true;
		}
		return false;
	}

}
