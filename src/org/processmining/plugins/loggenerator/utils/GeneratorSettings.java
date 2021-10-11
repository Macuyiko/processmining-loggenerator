package org.processmining.plugins.loggenerator.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class GeneratorSettings {
	protected int nrTraces = 20;
	protected int randomMaxInGroup = 5;
	protected int randomMinInGroup = 3;
	protected int maxTimesMarkingSeen = 5;
	protected boolean mustReachEnd = true;
	protected boolean mustConsumeAll = true;
	protected boolean alsoConsiderPartial = false;
	
	protected boolean tracesFollow = true;
	protected boolean tracesUniformStart = false;
	protected Date anchorDate = new Date();
	protected int anchorAvg = 0;
	protected int anchorSd = 0;
	
	protected Map<Transition, Integer> transitionWeights = new HashMap<Transition, Integer>();
	protected Map<Transition, String> transitionNames = new HashMap<Transition, String>();
	protected Map<Transition, int[]> transitionTimings = new HashMap<Transition, int[]>();
	protected Map<Transition, List<Pair<String, Integer>>> transitionResourcesWeights = 
			new HashMap<Transition, List<Pair<String, Integer>>>();

	protected int restartAfter = 10;
	protected float skipChance = 0.85F;

	public enum SimulationAlgorithm {
		Random, Complete, Distinct
	}

	private SimulationAlgorithm simulationMethod = SimulationAlgorithm.Random;
	
	public SimulationAlgorithm getSimulationMethod() {
		return simulationMethod;
	}

	public void setSimulationMethod(SimulationAlgorithm simulationMethod) {
		this.simulationMethod = simulationMethod;
	}

	public int getNrTraces() {
		return nrTraces;
	}

	public void setNrTraces(int nrTraces) {
		this.nrTraces = nrTraces;
	}

	public int getRandomMaxInGroup() {
		return randomMaxInGroup;
	}

	public void setRandomMaxInGroup(int randomMaxInGroup) {
		this.randomMaxInGroup = randomMaxInGroup;
	}

	public int getRandomMinInGroup() {
		return randomMinInGroup;
	}

	public void setRandomMinInGroup(int randomMinInGroup) {
		this.randomMinInGroup = randomMinInGroup;
	}

	public boolean isMustReachEnd() {
		return mustReachEnd;
	}

	public void setMustReachEnd(boolean mustReachEnd) {
		this.mustReachEnd = mustReachEnd;
	}

	public boolean isMustConsumeAll() {
		return mustConsumeAll;
	}

	public void setMustConsumeAll(boolean mustConsumeAll) {
		this.mustConsumeAll = mustConsumeAll;
	}

	public int getMaxTimesMarkingSeen() {
		return maxTimesMarkingSeen;
	}

	public void setMaxTimesMarkingSeen(int maxTimesMarkingSeen) {
		this.maxTimesMarkingSeen = maxTimesMarkingSeen;
	}

	public int getRestartAfter() {
		return restartAfter;
	}

	public void setRestartAfter(int restartAfter) {
		this.restartAfter = restartAfter;
	}

	public float getSkipChance() {
		return skipChance;
	}

	public void setSkipChance(float skipChance) {
		this.skipChance = skipChance;
	}

	public boolean isTracesUniformStart() {
		return tracesUniformStart;
	}

	public void setTracesUniformStart(boolean tracesUniformStart) {
		this.tracesUniformStart = tracesUniformStart;
	}

	public Map<Transition, Integer> getTransitionWeights() {
		return transitionWeights;
	}

	public Map<Transition, String> getTransitionNames() {
		return transitionNames;
	}

	public Map<Transition, int[]> getTransitionTimings() {
		return transitionTimings;
	}

	public Map<Transition, List<Pair<String, Integer>>> getTransitionResourcesWeights() {
		return transitionResourcesWeights;
	}
	
	public Date getAnchorDate() {
		return anchorDate;
	}

	public void setAnchorDate(Date anchorDate) {
		this.anchorDate = anchorDate;
	}

	public boolean isTracesFollow() {
		return tracesFollow;
	}

	public void setTracesFollow(boolean tracesFollow) {
		this.tracesFollow = tracesFollow;
	}

	public int getAnchorAvg() {
		return anchorAvg;
	}

	public void setAnchorAvg(int anchorAvg) {
		this.anchorAvg = anchorAvg;
	}

	public int getAnchorSd() {
		return anchorSd;
	}

	public void setAnchorSd(int anchorSd) {
		this.anchorSd = anchorSd;
	}

	public boolean isAlsoConsiderPartial() {
		return alsoConsiderPartial;
	}

	public void setAlsoConsiderPartial(boolean alsoConsiderPartial) {
		this.alsoConsiderPartial = alsoConsiderPartial;
	}
	
	public static int safeInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	

}
