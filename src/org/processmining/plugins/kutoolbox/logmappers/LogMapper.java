package org.processmining.plugins.kutoolbox.logmappers;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;

public interface LogMapper {
	public XEventClassifier getEventClassifier();
	public Collection<XEventClass> getEventClasses();
	public Collection<XEventClass> getMappedEventClasses();
	public boolean eventHasTransition(XEvent event);					
	public boolean eventHasTransition(XEventClass eventClass);
	public boolean eventHasTransition(String eventName);
}
