package org.processmining.plugins.yapetrinetreplayer.replayers.utils;

import java.util.ArrayList;
import java.util.List;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.kutoolbox.logmappers.LogMapper;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

public class LogReplayUtils extends LogUtils {
	public int getMappedSequenceSize(XTrace trace, PetrinetLogMapper mapper) {
		int size = 0;
		for (int i = 0; i < trace.size(); i++) {
			XEvent event = trace.get(i);
			if (mapper.eventHasTransition(event))
				size++;
		}
		return size;
	}
	
	public static List<String> getClassSequence(List<XEvent> sequence, LogMapper mapper) {
		List<String> history = new ArrayList<String>();
		for (XEvent event : sequence) {
			if (!mapper.eventHasTransition(event)) continue;
			history.add(mapper.getEventClassifier().getClassIdentity(event));
		}
		return history;
	}
	
	public static List<XEvent> getEventSequence(XTrace trace,  LogMapper mapper, int untilPosition, boolean skipUnmapped) {
		List<XEvent> history = new ArrayList<XEvent>();
		int position = 0;
		while (position < untilPosition && position < trace.size()) {
			XEvent event = trace.get(position);
			if (mapper.eventHasTransition(event) || !skipUnmapped)
				history.add(event);
			position++;
		}
		return history;
	}
}
