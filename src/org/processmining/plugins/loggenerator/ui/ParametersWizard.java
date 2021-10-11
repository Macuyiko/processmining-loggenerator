package org.processmining.plugins.loggenerator.ui;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;

public class ParametersWizard {

	private UIPluginContext context;
	private GeneratorSettings settings;
	private Petrinet petriNet;

	public ParametersWizard(UIPluginContext context, Petrinet petriNet) {
		this.context = context;
		this.petriNet = petriNet;
	}

	public ParametersWizard(UIPluginContext context, GeneratorSettings settings, Petrinet petriNet) {
		this(context, petriNet);
		this.settings = settings;
	}

	public GeneratorSettings getSettings() {
		return settings;
	}

	public void show() throws OperationCancelledException {
		InteractionResult result = InteractionResult.PREV;
		int step = 0;
		
		while (!InteractionResult.FINISHED.equals(result)) {
			if (InteractionResult.CANCEL.equals(result))
				throw new OperationCancelledException();
			
			if (step == 0) { // Step 1
				MainConfigurationPanel parameterPanel = new MainConfigurationPanel(settings, petriNet);
				result = context.showWizard("Log Generation Settings", true, false, parameterPanel);
				this.settings = parameterPanel.getSettings();
			}
			
			if (step == 1) { // Step 1
				TraceTimingsPanel parameterPanel = new TraceTimingsPanel(settings);
				result = context.showWizard("Trace Timing Settings", false, false, parameterPanel);
				this.settings = parameterPanel.getSettings();
			}
			
			if (step == 2) { // Step 3
				ActivityConfigurationPanel mappingPanel = new ActivityConfigurationPanel(settings, petriNet);
				result = context.showWizard("Activity Mapping and Weights", false, true, mappingPanel);
			}
			
			if (InteractionResult.NEXT.equals(result))
				step++;
			if (InteractionResult.PREV.equals(result))
				step--;
		}
	}
}
