package org.processmining.plugins.kutoolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XSemanticExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class LogUtils {
	public static XLog newLog(String name) {
		XFactory xFactory = XFactoryRegistry.instance().currentDefault();
		XLog log = xFactory.createLog();
		
		// Set extensions
		log.getExtensions().add(XConceptExtension.instance());
		log.getExtensions().add(XOrganizationalExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		log.getExtensions().add(XSemanticExtension.instance());
		log.getExtensions().add(XTimeExtension.instance());
		
		// Set log classifiers
		log.getClassifiers().add(XLogInfoImpl.STANDARD_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.RESOURCE_CLASSIFIER);
		log.getClassifiers().add(XLogInfoImpl.LIFECYCLE_TRANSITION_CLASSIFIER);
		
		// Set guaranteed attributes
		log.getGlobalTraceAttributes().add((XAttribute)XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute)XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute)XLifecycleExtension.ATTR_TRANSITION.clone());
		
        XConceptExtension.instance().assignName(log, name);
        XLifecycleExtension.instance().assignModel(log, XLifecycleExtension.VALUE_MODEL_STANDARD);
        
        return log;
	}
	
	public static XLog cloneLog(XLog logToClone) {
		XFactory xFactory = XFactoryRegistry.instance().currentDefault();
		XLog newLog = newLog(XConceptExtension.instance().extractName(logToClone));
		newLog.setAttributes((XAttributeMap) logToClone.getAttributes().clone());
		
		for (XTrace t : logToClone) {
			XTrace nt = xFactory.createTrace((XAttributeMap) t.getAttributes().clone());
			for (XEvent e : t) {
				XEvent ne = xFactory.createEvent((XAttributeMap) e.getAttributes().clone());
				nt.add(ne);
			}
			newLog.add(nt);
		}
		
		return newLog;
	}
	
	public static XEvent deriveEventFromClassIdentity(String ec, XEventClassifier classifier, String delimiter) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		String[] keys = classifier.getDefiningAttributeKeys();
		String[] values = ec.split(delimiter);
		XAttributeMap atts = factory.createAttributeMap();
		for (int i = 0; i < keys.length; i++) {
			String val = i < values.length ? values[i] : "";
			atts.put(keys[i], factory.createAttributeLiteral(keys[i], val, null));
		}
		XEvent event = factory.createEvent();
		event.setAttributes(atts);
		return event;
	}
	
	public static Collection<String> getEventClassesAsString(XLog log, XEventClassifier classifier) {
		List<String> classes = new ArrayList<String>();
		for (XEventClass clazz : getEventClasses(log, classifier))
			classes.add(clazz.getId());
		return classes;
	}
	
	public static Collection<XEventClass> getEventClasses(XLog log, XEventClassifier classifier) {
		return getXEventClasses(log, classifier).getClasses();
	}
	
	public static XEventClasses getXEventClasses(XLog log, XEventClassifier classifier) {
		return XEventClasses.deriveEventClasses(classifier, log);
	}
	
	public static List<String> getTraceEventClassSequence(XTrace trace, XEventClassifier classifier) {
		classifier = classifier.equals(null) ? XLogInfoImpl.STANDARD_CLASSIFIER : classifier;
		List<String> sequence = new ArrayList<String>();
		for (XEvent event : trace)
			sequence.add(classifier.getClassIdentity(event));
		return sequence;
	}
	
	public static List<XEventClass> getTraceEventClassSequence(XTrace trace, XEventClasses classes) {
		List<XEventClass> sequence = new ArrayList<XEventClass>();
		for (XEvent event : trace)
			sequence.add(classes.getClassOf(event));
		return sequence;
	}
}
