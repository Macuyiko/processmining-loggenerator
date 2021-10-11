package org.processmining.plugins.loggenerator.simulators;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.eventlisteners.PluginEventListener;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.kutoolbox.randomcollection.RandomCollection;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;
import org.processmining.plugins.loggenerator.utils.LogBuilder;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayState;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public class PetriNetSimulatorRandom extends PetriNetSimulator {
	private Random rand = new Random();

	public PetriNetSimulatorRandom(GeneratorSettings settings, Petrinet petriNet, LogBuilder logBuilder, Marking marking, PluginEventListener eventListener) {
		super(settings, petriNet, logBuilder, marking, eventListener);
	}

	@Override
	public void simulateLog() throws Exception {
		if (eventListener != null) {
			eventListener.logMessage("Generating traces (randomly)...");
			eventListener.setProgressBounds(0, settings.getNrTraces() + 1);
		}
		
		for (int i = 0; i < settings.getNrTraces(); i++) {
			if (eventListener!= null && i % 100 == 0)
				eventListener.logMessage("At trace #"+i);

			if (eventListener != null)
				eventListener.setProgress(i);
			List<Transition> tryTrace = simulateTrace();
			while (tryTrace == null) {
				if (eventListener != null && eventListener.shouldCancel())
					this.cancel();
				tryTrace = simulateTrace();
			}
			
			int randomNum = rand.nextInt(settings.getRandomMaxInGroup() 
					- settings.getRandomMinInGroup() + 1)
					+ settings.getRandomMinInGroup();
			
			for (int j = 0; j < randomNum; j++)
				logBuilder.addTrace(tryTrace);
		}
	}
	
	@Override
	public List<Transition> simulateTrace() throws IllegalTransitionException, OperationCancelledException {
		initialMarking = PetrinetUtils.getInitialMarking(petriNet, initialMarking);
		
		ReplayStateChain stateChain = new ReplayStateChain();
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);

		Collection<Transition> exTransitions = initialState.getFireableTransitions();
		while (exTransitions.size() > 0) {
			if (eventListener != null && eventListener.shouldCancel())
				this.cancel();

			Transition exTransition = getRandomWeightedTransition(exTransitions);
			stateChain.makeDecision(exTransition);

			int occurances = stateChain.getCountMarkingOccurances(stateChain.getLastState().getMarking());
			if (occurances > settings.getMaxTimesMarkingSeen()) {
				if (settings.isAlsoConsiderPartial()) {
					break;
				} else {
					return null;
				}
			}

			exTransitions = stateChain.getDecisions();
		}

		boolean endReached = PetrinetUtils.isMarkingHasSinglePlace(stateChain.getLastState().getMarking(),
				PetrinetUtils.getEndPlaces(petriNet));
		boolean allConsumed = PetrinetUtils.isMarkingOnlyHasPlaces(stateChain.getLastState().getMarking(),
				PetrinetUtils.getEndPlaces(petriNet));

		if (settings.isMustReachEnd() && !endReached)
			return null;
		if (settings.isMustConsumeAll() && !allConsumed)
			return null;

		return getTransitionSequence(stateChain);
	}

	public Transition getRandomWeightedTransition(Collection<Transition> exTransitions) {
		RandomCollection<Transition> randCol = new RandomCollection<Transition>(rand);
		for (Transition t : exTransitions) {
			randCol.add(settings.getTransitionWeights().get(t), t);
		}
		return randCol.next();
	}

	public static <T> T selectRandom(final Iterator<T> iter, final Random random) {
		if (!iter.hasNext()) {
			throw new IllegalArgumentException();
		}
		if (random == null) {
			throw new NullPointerException();
		}
		T selected = iter.next();
		int count = 1;
		while (iter.hasNext()) {
			final T current = iter.next();
			++count;
			if (random.nextInt(count) == 0) {
				selected = current;
			}
		}
		return selected;
	}
}
