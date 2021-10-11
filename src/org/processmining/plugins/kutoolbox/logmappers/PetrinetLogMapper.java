package org.processmining.plugins.kutoolbox.logmappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.processmining.framework.util.ArrayUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class PetrinetLogMapper extends HashMap<Transition, XEventClass> implements LogMapper {
	private static final long serialVersionUID = -4344051692440782096L;
	private XEventClassifier eventClassifier;
	private Collection<XEventClass> eventClasses;
	private Collection<Transition> transitions;
	
	public static final XEventClass BLOCKING_CLASS = new XEventClass("BLOCKED VISIBLE", -101);
	
	public PetrinetLogMapper(XEventClassifier eventClassifier, 
			XLog log, 
			Collection<Transition> transitions){
		this(eventClassifier, 
				XEventClasses.deriveEventClasses(eventClassifier, log).getClasses(), 
				transitions);
	}
	
	public PetrinetLogMapper(XEventClassifier eventClassifier, 
			XEventClasses eventClasses, 
			Collection<Transition> transitions){
		this(eventClassifier, eventClasses.getClasses(), transitions);
	}
	
	public PetrinetLogMapper(XEventClassifier eventClassifier, 
			Collection<XEventClass> eventClasses, 
			Collection<Transition> transitions){
		this.eventClassifier = eventClassifier;
		this.eventClasses = eventClasses;
		this.transitions = transitions;
	}
	
	public PetrinetLogMapper(XEventClassifier eventClassifier, XLog log, Petrinet net) {
		this(eventClassifier, XEventClasses.deriveEventClasses(eventClassifier, log).getClasses(), net.getTransitions());
	}

	public XEventClassifier getEventClassifier(){
		return this.eventClassifier;
	}

	public Collection<XEventClass> getEventClasses() {
		return this.eventClasses;
	}

	public Collection<XEventClass> getMappedEventClasses() {
		return this.values();
	}

	public boolean eventHasTransition(XEvent event){
		for (Transition t : this.keySet()) {
			if (eventClassifier.getClassIdentity(event).equals(this.get(t).getId()))
				return true;
		}
		return false;					
	}

	public boolean eventHasTransition(XEventClass eventClass) {
		return this.containsValue(eventClass);
	}

	public boolean eventHasTransition(String eventName) {
		for (Transition t : this.keySet()) {
			if (eventName.equals(this.get(t).getId()))
				return true;
		}
		return false;
	}

	public XEvent makeEvent(String classId) {
		XEvent event = new XEventImpl();
		String[] keys = getEventClassifier().getDefiningAttributeKeys();
		String[] values = classId.split("\\+");
		for (int i = 0; i < keys.length; i++)
			event.getAttributes().put(keys[i], new XAttributeLiteralImpl(keys[i], values[i]));
		return event;
	}
	
	public Collection<Transition> getTransitions() {
		return this.transitions;
	}
	
	public boolean transitionEqualsEvent(Transition transition, XEvent event){
		XEventClass eventClass = this.get(transition);
		if (eventClass == null)
			return false;
		return eventClassifier.getClassIdentity(event).equals(eventClass.getId());
	}
	
	public boolean transitionHasEvent(Transition transition) {
		return this.containsKey(transition) && !this.get(transition).equals(BLOCKING_CLASS);
	}
	
	public boolean transitionIsInvisible(Transition transition) {
		return !this.containsKey(transition);
	}

	public Collection<Transition> getTransitionsForActivity(String currentActivity) {
		HashSet<Transition> toReturn = new HashSet<Transition>();
		for (Transition t : this.keySet()) {
			XEventClass eventClass = this.get(t);
			if (currentActivity.equals(eventClass.getId()))
				toReturn.add(t);
		}
		return toReturn;
	}
	
	public XEventClass getEventClass(String s) {
		for (XEventClass eventClass : this.eventClasses) {
			if (eventClass.getId().equals(s))
				return eventClass;
		}
		return null;
	}

	public void applyMappingOnTransitions() {
		for (Transition t : this.transitions) {
			t.setInvisible(transitionIsInvisible(t));
		}
	}
	
	public String toString() {
		String repr = "PetrinetLogMapper (eventClassifier=" + eventClassifier + ")\n";
		for (Transition t : this.keySet()) {
			repr += "  " + t.getLabel() + " --> "+this.get(t).getId()+"\n";
		}
		for (XEventClass xclass : eventClasses) {
			if (!eventHasTransition(xclass)) {
				repr += "  " + "!! unmapped !!" + " --> "+xclass.getId()+"\n";
			}
		}
		for (Transition transition : transitions) {
			if (!transitionHasEvent(transition)) {
				repr += "  " + transition.getLabel() + " --> * unmapped *\n";
			}
		}
		return repr;
	}

	public static PetrinetLogMapper getStandardMap(final XLog log, final Petrinet net) {
		PetrinetLogMapper map = new PetrinetLogMapper(XLogInfoImpl.STANDARD_CLASSIFIER,
				LogUtils.getEventClasses(log, XLogInfoImpl.STANDARD_CLASSIFIER),
				net.getTransitions());
		Object[] boxOptions = extractEventClassList(log, XLogInfoImpl.STANDARD_CLASSIFIER);
		for (Transition transition : net.getTransitions()) {
			Object sEventClass = boxOptions[preSelectOption(transition.getLabel(), boxOptions)];
			if (sEventClass instanceof XEventClass){
				XEventClass eventClass = (XEventClass) sEventClass;
				map.put(transition, eventClass);
			}
		}
		return map;
	}
	
	public static Object[] extractEventClassList(XLog log, XEventClassifier classifier) {
		Collection<XEventClass> classes = LogUtils.getEventClasses(log, classifier);

		Object[] arrEvClass = classes.toArray();
		Arrays.sort(arrEvClass);
		Object[] notMappedAct = { "NONE" };
		Object[] blockedAct = { BLOCKING_CLASS };
		Object[] boxOptions = ArrayUtils.concatAll(notMappedAct, blockedAct, arrEvClass);

		return boxOptions;
	}
	
	public static Transition[] extractTransitionList(Petrinet net) {
		List<Transition> transitions = new ArrayList<Transition>(net.getTransitions());
		Collections.sort(transitions, new Comparator<Transition>() {
			@Override
			public int compare(Transition a, Transition b) {
				return a.getLabel().compareToIgnoreCase(b.getLabel());
			}
		});
		
		Transition[] arrTransitions = transitions.toArray(new Transition[]{});
		return arrTransitions;
	}

	public static int preSelectOption(String transition, Object[] events) {
		AbstractStringMetric metric = new Levenshtein();

		int index = 0;
		float simOld = metric.getSimilarity(transition, "none");
		simOld = Math.max(simOld, metric.getSimilarity(transition, "invisible"));
		simOld = Math.max(simOld, metric.getSimilarity(transition, "skip"));
		simOld = Math.max(simOld, metric.getSimilarity(transition, "tau"));
		simOld = Math.max(simOld, metric.getSimilarity(transition, "inv"));

		for (int i = 1; i < events.length; i++) {
			String event = ((XEventClass) events[i]).toString();
			
			if (event.indexOf("+completeRejected") >= 0) continue;
			if (event.indexOf("+rejected") >= 0) continue;
			
			if (transition.equals(event)) {
				index = i;
				break;
			}
			
			if (event.replace("+complete", "").replace("\\ncomplete", "").replace("\\n", "")
					.equals(
				transition.replace("\\ncomplete", "").replace("+complete", "").replace("\\n", "")
					)) {
				index = i;
				break;
			}
		}

		return index;
	}
	
}
