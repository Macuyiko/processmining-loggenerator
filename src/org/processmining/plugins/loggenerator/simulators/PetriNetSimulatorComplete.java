package org.processmining.plugins.loggenerator.simulators;

import java.util.List;
import java.util.Random;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.eventlisteners.PluginEventListener;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;
import org.processmining.plugins.loggenerator.utils.LogBuilder;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayState;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public class PetriNetSimulatorComplete extends PetriNetSimulator {

	private Random rand = new Random();

	public PetriNetSimulatorComplete(GeneratorSettings settings, Petrinet petriNet, LogBuilder logBuilder, Marking marking, PluginEventListener eventListener) {
		super(settings, petriNet, logBuilder, marking, eventListener);
	}

	@Override
	public List<Transition> simulateTrace() throws Exception {
		throw new RuntimeException("simulateTrace() unavailable for Complete Simulator!");
	}

	@Override
	public void simulateLog() throws OperationCancelledException {
		initialMarking = PetrinetUtils.getInitialMarking(petriNet, initialMarking);
		
		ReplayStateChain stateChain = new ReplayStateChain();
		ReplayState initialState = new ReplayState(petriNet, initialMarking);
		stateChain.addState(initialState);

		int loopCounter = 0;
		int pathCounter = 0;

		while (true) {
			if (eventListener != null && eventListener.shouldCancel())
				this.cancel();

			loopCounter++;
			if (eventListener != null && loopCounter % 10000 == 0)
				eventListener.logMessage(pathCounter + " ==> " + progressString(stateChain));

			int occurances = stateChain.getCountMarkingOccurances(stateChain.getLastState().getMarking());
			if (occurances > settings.getMaxTimesMarkingSeen())
				if (!stateChain.retractDecision())
					break;

			boolean endReached = PetrinetUtils.isMarkingHasSinglePlace(stateChain.getLastState().getMarking(),
					PetrinetUtils.getEndPlaces(petriNet));
			boolean allConsumed = PetrinetUtils.isMarkingOnlyHasPlaces(stateChain.getLastState().getMarking(),
					PetrinetUtils.getEndPlaces(petriNet));

			if (!stateChain.getLastState().hasNextFireableTransition()) {
				boolean addToSet = true;
				if (settings.isMustReachEnd() && !endReached)
					addToSet = false;
				if (settings.isMustConsumeAll() && !allConsumed)
					addToSet = false;
				if (stateChain.getLastState().getLastDecision() != null)
					addToSet = false;

				if (addToSet) {
					int randomNum = rand.nextInt(settings.getRandomMaxInGroup() - settings.getRandomMinInGroup()
								+ 1)
								+ settings.getRandomMinInGroup();
					pathCounter++;
					for (int i = 0; i < randomNum; i++)
						logBuilder.addTrace(getTransitionSequence(stateChain));
				}
				if (!stateChain.retractDecision())
					break;
			}

			if (stateChain.getLastState().hasNextFireableTransition()) {
				Transition fireCandidate = stateChain.getLastState().nextFireableTransition();
				if (settings.getTransitionWeights().get(fireCandidate) == 0)
					continue;
				stateChain.makeDecision(fireCandidate);
			}

		}
	}
}
