package org.processmining.plugins.kutoolbox.utils;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.logmodel.LogPetrinetConnection;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;

public class MappingUtils {
	
	public static TransEvClassMapping getTransEvClassMapping(PetrinetLogMapper mapping) {
		TransEvClassMapping transEvClassMapping = new TransEvClassMapping(
				mapping.getEventClassifier(),
				EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		for (Transition transition : mapping.getTransitions()) {
			if (mapping.transitionIsInvisible(transition))
				continue;
			mapping.get(transition);
			transEvClassMapping.put(transition, mapping.get(transition));
			if (mapping.get(transition).equals(PetrinetLogMapper.BLOCKING_CLASS))
				transEvClassMapping.put(transition, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		}
		return transEvClassMapping;
	}

	public static EvClassLogPetrinetConnection getEvClassLogPetrinetConnection(PetrinetLogMapper mapping, Petrinet net, XLog log) {
		EvClassLogPetrinetConnection connection = new EvClassLogPetrinetConnection("Mapping", 
						(PetrinetGraph) net, log, mapping.getEventClassifier(), getTransEvClassMapping(mapping));
		return connection;
	}

	public static LogPetrinetConnection getLogPetrinetConnection(PetrinetLogMapper mapping, Petrinet net, XLog log) {
		XEventClasses classes = XEventClasses.deriveEventClasses(mapping.getEventClassifier(), log);
		LogPetrinetConnection newConnection = new LogPetrinetConnectionImpl(log, classes, net, getLogPetrinetMapping(mapping));
		return newConnection;
	}

	public static Set<Pair<Transition, XEventClass>> getLogPetrinetMapping(PetrinetLogMapper mapping) {
		TransEvClassMapping evMapping = getTransEvClassMapping(mapping);
		Set<Pair<Transition, XEventClass>> m = new HashSet<Pair<Transition, XEventClass>>();
		for (Transition transition : mapping.getTransitions()) {
			if (evMapping.containsKey(transition) && !evMapping.get(transition).equals(EvClassLogPetrinetConnectionFactoryUI.DUMMY)) {
				Pair<Transition, XEventClass> newPair = new Pair<Transition, XEventClass>(transition, evMapping.get(transition));
				m.add(newPair);
			}
		}
		return m;
	}
}
