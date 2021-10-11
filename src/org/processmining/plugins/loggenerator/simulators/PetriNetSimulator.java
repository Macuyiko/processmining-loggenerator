package org.processmining.plugins.loggenerator.simulators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.eventlisteners.PluginEventListener;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;
import org.processmining.plugins.loggenerator.utils.LogBuilder;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayState;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public abstract class PetriNetSimulator {
	protected Petrinet petriNet;
	protected GeneratorSettings settings;
	protected Marking initialMarking;
	protected PluginEventListener eventListener;
	protected LogBuilder logBuilder;

	public PetriNetSimulator(GeneratorSettings settings, Petrinet petriNet, LogBuilder logBuilder, Marking initialMarking, PluginEventListener eventListener) {
		this.settings = settings;
		this.petriNet = petriNet;
		this.initialMarking = initialMarking;
		this.eventListener = eventListener;
		this.logBuilder = logBuilder;
	}

	public PetriNetSimulator(GeneratorSettings settings, Petrinet petriNet, LogBuilder logBuilder, PluginEventListener eventListener) {
		this.settings = settings;
		this.petriNet = petriNet;
		this.initialMarking = null;
		this.eventListener = eventListener;
		this.logBuilder = logBuilder;
	}

	public abstract void simulateLog() throws Exception;

	public abstract List<Transition> simulateTrace() throws Exception;

	protected void cancel() throws OperationCancelledException {
		if (eventListener != null)
			eventListener.cancel();
		throw new OperationCancelledException();
	}
	
	protected List<Transition> getTransitionSequence(ReplayStateChain chain) {
		List<Transition> sequence = new ArrayList<Transition>();
		for (ReplayState state : chain.getStateChain()) {
			Transition fired = state.getLastDecision();
			if (fired == null)
				continue;
			sequence.add(fired);
		}
		return sequence;
	}

	protected List<String> getLabelSequence(ReplayStateChain chain, Map<Transition,String> mapping) {
		List<String> sequence = new ArrayList<String>();
		for (ReplayState state : chain.getStateChain()) {
			Transition fired = state.getLastDecision();
			if (fired == null)
				continue;
			if (mapping.get(fired).equals(""))
				continue;
			sequence.add(mapping.get(fired));
		}
		return sequence;
	}
	
	protected String getLabelSequenceID(ReplayStateChain chain, Map<Transition,String> mapping) {
		String sequence = "";
		for (ReplayState state : chain.getStateChain()) {
			Transition fired = state.getLastDecision();
			if (fired == null)
				continue;
			if (mapping.get(fired).equals(""))
				continue;
			sequence += mapping.get(fired);
		}
		return sequence;
	}
	
	protected String progressString(ReplayStateChain chain) {
		String repr = "";
		for (ReplayState state : chain.getStateChain()) {
			repr += "[" + state.getDecisionIndex() + "/" + state.getFireableTransitions().size() + "] ";
		}
		return repr;
	}
}
