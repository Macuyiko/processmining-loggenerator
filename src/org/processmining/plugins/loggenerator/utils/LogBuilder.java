package org.processmining.plugins.loggenerator.utils;

import java.util.List;
import java.util.Random;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XSemanticExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.randomcollection.RandomCollection;

public class LogBuilder {
	private GeneratorSettings settings;

	private XLog log;
	private Random rand = new Random();
	private XFactory xFactory = XFactoryRegistry.instance().currentDefault();
	private int traceCounter;

	private long latestTimeStamp;

	public LogBuilder(GeneratorSettings settings) {
		this.settings = settings;

		this.newLog();
	}

	public void newLog() {
		traceCounter = 0;
		log = null;
		log = xFactory.createLog();
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
		log.getGlobalTraceAttributes().add((XAttribute) XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute) XConceptExtension.ATTR_NAME.clone());
		log.getGlobalEventAttributes().add((XAttribute) XLifecycleExtension.ATTR_TRANSITION.clone());

		XConceptExtension.instance().assignName(log, "Simulated event log");
		XLifecycleExtension.instance().assignModel(log, XLifecycleExtension.VALUE_MODEL_STANDARD);

		latestTimeStamp = settings.getAnchorDate().getTime();
	}

	public XLog getLog() {
		return log;
	}

	public void addTrace(List<Transition> transitionSequence) {
		XTrace trace = xFactory.createTrace();

		long curTimestamp = settings.getAnchorDate().getTime();
		if (settings.isTracesFollow())
			curTimestamp = latestTimeStamp;

		long tstar = (long) Math
				.round((double) settings.getAnchorSd() * rand.nextGaussian() + (double) settings.getAnchorAvg());
		if (settings.isTracesUniformStart()) {
			tstar = settings.getAnchorAvg() + rand.nextInt(settings.getAnchorSd() * 2) - settings.getAnchorSd();
		}
		tstar *= (60 * 1000);
		curTimestamp = curTimestamp + tstar;

		long previousTime = curTimestamp;
		long previousNonParTime = curTimestamp;
		long completeTime = curTimestamp;
		long startTime = curTimestamp;

		XConceptExtension.instance().assignName(trace, "trace_" + traceCounter);

		for (Transition t : transitionSequence) {
			// Activity name
			String activity = settings.getTransitionNames().get(t);
			if (activity == null || activity.equals(""))
				continue;

			boolean createStart = true;
			int[] timings = settings.getTransitionTimings().containsKey(t) ? settings.getTransitionTimings().get(t)
					: new int[] { -1, 0, 60, 0 };
			createStart = timings[0] != -1;
			boolean isPar = createStart && timings.length > 4;
			long previousTimeToUse = isPar ? previousNonParTime : previousTime;

			// Idle time
			long duration = (long) Math.round((double) timings[3] * rand.nextGaussian() + (double) timings[2]);
			if (duration < 0)
				duration = 0;
			duration *= (60 * 1000);
			startTime = previousTimeToUse + duration;

			// Lead time
			completeTime = startTime;
			if (createStart) {
				duration = (long) Math.round((double) timings[1] * rand.nextGaussian() + (double) timings[0]);
				if (duration < 0)
					duration = 0;
				duration *= (60 * 1000);
				completeTime = startTime + duration;
			}

			// Resources
			String resource = "NONE";
			if (settings.getTransitionResourcesWeights().containsKey(t)) {
				RandomCollection<String> randCol = new RandomCollection<String>(rand);
				for (Pair<String, Integer> p : settings.getTransitionResourcesWeights().get(t)) {
					randCol.add(p.getSecond(), p.getFirst());
				}
				resource = randCol.next();
			}

			if (createStart) {
				XEvent start = xFactory.createEvent();
				XConceptExtension.instance().assignName(start, activity);
				XOrganizationalExtension.instance().assignResource(start, resource);
				XTimeExtension.instance().assignTimestamp(start, startTime);
				XLifecycleExtension.instance().assignStandardTransition(start, StandardModel.START);
				trace.add(start);
			}

			XEvent complete = xFactory.createEvent();
			XConceptExtension.instance().assignName(complete, activity);
			XOrganizationalExtension.instance().assignResource(complete, resource);
			XTimeExtension.instance().assignTimestamp(complete, completeTime);
			XLifecycleExtension.instance().assignStandardTransition(complete, StandardModel.COMPLETE);
			trace.add(complete);

			if (!isPar && completeTime > previousNonParTime)
				previousNonParTime = completeTime;
			if (completeTime > previousTime)
				previousTime = completeTime;
			if (completeTime > latestTimeStamp)
				latestTimeStamp = completeTime;

		}
		traceCounter++;
		log.add(trace);

	}

	public void finalizeLog() {
		/*
		 * XesXmlSerializer xSer = new XesXmlSerializer(); try {
		 * xSer.serialize(this.log, new FileOutputStream("C:\\temp\\tmp_output.xes")); }
		 * catch (Exception e) { System.err.println("Exception while writing log"); }
		 */
	}
}
