package org.processmining.plugins.loggenerator;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.eventlisteners.PluginEventListener;
import org.processmining.plugins.kutoolbox.eventlisteners.PluginEventListenerUI;
import org.processmining.plugins.loggenerator.simulators.PetriNetSimulator;
import org.processmining.plugins.loggenerator.simulators.PetriNetSimulatorComplete;
import org.processmining.plugins.loggenerator.simulators.PetriNetSimulatorDistinct;
import org.processmining.plugins.loggenerator.simulators.PetriNetSimulatorRandom;
import org.processmining.plugins.loggenerator.ui.ParametersWizard;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings.SimulationAlgorithm;
import org.processmining.plugins.loggenerator.utils.LogBuilder;

public class PetriNetLogGenerator {
	@Plugin(name = "Generate Event Log From Petri Net", 
			parameterLabels = { "Petri net", "Marking" }, 
			returnLabels = { "Event Log" },
			returnTypes = { XLog.class }, 
			help = "Simulate an event log based on a given Petri net and initial marking")
	@UITopiaVariant(affiliation = "KU Leuven", 
		author = "Seppe vanden Broucke",
		email = "seppe.vandenbroucke@econ.kuleuven.be", 
		website = "http://econ.kuleuven.be")
	public static XLog main(UIPluginContext context, Petrinet petriNet, Marking marking) {
		ParametersWizard conf = new ParametersWizard(context, petriNet);
		PluginEventListener eventListener = new PluginEventListenerUI(context);
		
		try {
			conf.show();
			GeneratorSettings settings = conf.getSettings();
			eventListener.logMessage("Simulating...");
			XLog log = generate(petriNet, marking, settings, eventListener);
			eventListener.logMessage("Finishing...");
			return log;
		} catch (Exception e) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
	
	public static XLog generate(Petrinet petriNet, Marking marking, GeneratorSettings settings, PluginEventListener optionalListener) throws Exception {
		LogBuilder logBuilder = new LogBuilder(settings);
		PetriNetSimulator petriSimulator = null;
		
		SimulationAlgorithm selectedAlgorithm = settings.getSimulationMethod();

		switch (selectedAlgorithm) {
			case Random :
				petriSimulator = new PetriNetSimulatorRandom(settings, petriNet, logBuilder, marking, optionalListener);
				break;
			case Complete :
				petriSimulator = new PetriNetSimulatorComplete(settings, petriNet, logBuilder, marking, optionalListener);
				break;
			case Distinct :
				petriSimulator = new PetriNetSimulatorDistinct(settings, petriNet, logBuilder, marking, optionalListener);
				break;
			default :
				return null;
		}
		petriSimulator.simulateLog();
		logBuilder.finalizeLog();
		return logBuilder.getLog();
	}

}
