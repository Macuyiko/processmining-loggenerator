package org.processmining.plugins.kutoolbox.groupedlog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;

/**
 * @author Seppe vanden Broucle (seppe.vandenbroucke@econ.kuleuven.be)
 * @author Niels Lambrigts (niels.lambrigts@gmail.com)
 * @author Jochen De Weerdt (Jochen.DeWeerdt@econ.kuleuven.be)
 * @date 2011-07-29
 */
public class GroupedXLog implements Cloneable {
	private XEventClassifier classifier;
	private List<List<XTrace>> groups;
	private XAttributeMap attributeMap;
	private int nbTraces = 0;
	private String name;
	private Map<String, Integer> trace2Index;

	private GroupedXLog(XEventClassifier c) {
		classifier = c;
	}
	
	public GroupedXLog(XLog log) {
		this(log, XLogInfoImpl.STANDARD_CLASSIFIER, true);
	}

	public GroupedXLog(XLog log, XEventClassifier c, boolean useHashMap) {
		trace2Index = new HashMap<String, Integer>();
		classifier = c;
		attributeMap = log.getAttributes();
		groups = new ArrayList<List<XTrace>>();
		
		{
			XAttribute nameAttribute = attributeMap.get("concept:name");
			name = nameAttribute == null ? "---" : nameAttribute.toString();
			int length = name.lastIndexOf('.');
			if (length >= 0) {
				if (name.subSequence(length, name.length()).equals(".gz")) {
					length = name.lastIndexOf('.', length - 1);
				}
				if (length != 0) {
					name = name.substring(0, length);
				}
			}
		}
		
		for (XTrace trace : log) {
			nbTraces++;
			int index = -1;
			if (useHashMap) {
				String traceHash = traceHash(trace);
				if (trace2Index.containsKey(traceHash)) {
					index = trace2Index.get(traceHash);
				}
			} else {
				for (int i = 0; i < groups.size(); i++) {
					if (tracesSimilar(trace, groups.get(i).get(0))) {
						index = i;
						break;
					}
				}
			}
			if (index == -1) {
				index = groups.size();
				groups.add(new ArrayList<XTrace>());
				trace2Index.put(traceHash(trace), index);
			}
			groups.get(index).add(trace);
		}
		
		Collections.sort(groups, new Comparator<List<XTrace>>() {
			@Override
			public int compare(List<XTrace> a, List<XTrace> b) {
				return b.size() - a.size();
			}
		});
	}

	@Override
	public Object clone() {
		GroupedXLog output = new GroupedXLog(classifier);
		output.groups = new ArrayList<List<XTrace>>();
		for (int i = 0; i < groups.size(); i++) {
			XTrace[] sub_array = groups.get(i).toArray(new XTrace[0]);
			output.groups.add(new ArrayList<XTrace>());
			for (int j = 0; j < sub_array.length; j++) {
				output.groups.get(i).add(sub_array[j]);
			}
		}
		output.attributeMap = (XAttributeMap) attributeMap.clone();
		output.nbTraces = nbTraces;
		output.name = name;
		return output;
	}

	public List<XTrace> get(int index) {
		return groups.get(index);
	}

	public XAttributeMap getAttributeMap() {
		return attributeMap;
	}

	public XLog getLog() {
		XLogImpl log = new XLogImpl(attributeMap);
		for (int i = 0; i < groups.size(); i++)
			log.addAll(groups.get(i));
		return log;
	}

	public XLog getLog(Collection<Integer> indices) {
		XLogImpl log = new XLogImpl(attributeMap);
		for (Integer i : indices)
			log.addAll(groups.get(i));
		return log;
	}

	public XLog getLog(int... indices) {
		XLogImpl log = new XLogImpl(attributeMap);
		for (int i = 0; i < indices.length; i++)
			log.addAll(groups.get(indices[i]));
		return log;
	}
	
	public XLog getGroupedLog() {
		XLogImpl log = new XLogImpl(attributeMap);
		for (int i = 0; i < groups.size(); i++)
			log.add(groups.get(i).get(0));
		return log;
	}

	public String getName() {
		return name;
	}

	public int getNbTraces() {
		return nbTraces;
	}

	public void remove(int index) {
		nbTraces -= groups.get(index).size();
		groups.remove(index);
	}

	public void remove(XTrace trace) throws IllegalArgumentException {
		for (int i = 0; i < groups.size(); i++) {
			if (tracesSimilar(trace, groups.get(i).get(0))) {
				nbTraces -= groups.get(i).size();
				groups.remove(i);
				return;
			}
		}
		throw new IllegalArgumentException("No similar group found");
	}

	public int size() {
		return groups.size();
	}

	private boolean tracesSimilar(XTrace one, XTrace two) {
		return tracesSimilar(one, two, classifier);
	}
	
	public static boolean tracesSimilar(XTrace one, XTrace two, XEventClassifier c) {
		if (one.size() != two.size())
			return false;
		for (int i = 1; i < one.size(); i++) {
			if (!c.sameEventClass(one.get(i), two.get(i)))
				return false;
		}
		if (!c.sameEventClass(one.get(0), two.get(0)))
			return false;
		return true;
	}
	
	public String traceHash(XTrace trace) {
		String th = "";
		for (XEvent e : trace)
			th += classifier.getClassIdentity(e) + "+++++";
		return th;
	}
}
