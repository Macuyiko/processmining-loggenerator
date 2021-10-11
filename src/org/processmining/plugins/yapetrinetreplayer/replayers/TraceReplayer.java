package org.processmining.plugins.yapetrinetreplayer.replayers;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.LogReplayUtils;
import org.processmining.plugins.yapetrinetreplayer.replayers.utils.PetrinetReplayUtils;
import org.processmining.plugins.yapetrinetreplayer.types.ReplayStateChain;

public abstract class TraceReplayer {
	protected Petrinet petriNet;
	protected PetrinetLogMapper mapping;	
	
	public TraceReplayer(Petrinet net, PetrinetLogMapper mapping) throws Exception {
		this.petriNet = net;
		this.mapping = mapping;
		PetrinetReplayUtils.setInvisibleTransitions(net, mapping);
	}
	
	public PetrinetLogMapper getMapping() {
		return mapping;
	}

	public void setMapping(PetrinetLogMapper mapping) {
		this.mapping = mapping;
	}
	
	public Set<ReplayStateChain> getStatePathsForSequence(XTrace sequence, Marking initialMarking) {
		List<XEvent> history = 
				LogReplayUtils.getEventSequence(sequence, mapping, sequence.size(), false);
		return getStatePathsForSequence(history, initialMarking);
	}

	public abstract Set<ReplayStateChain> getStatePathsForSequence(List<XEvent> history, Marking initialMarking);
}
