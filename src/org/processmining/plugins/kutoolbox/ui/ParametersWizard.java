package org.processmining.plugins.kutoolbox.ui;

import java.util.Map;

import javax.swing.JComponent;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.kutoolbox.exceptions.OperationCancelledException;

public class ParametersWizard {

	private UIPluginContext context;
	private UIAttributeConfigurator[] panels;
	private int currentstep;
	
	public ParametersWizard(UIPluginContext context, UIAttributeConfigurator[] panels) {
		this.context = context;
		this.panels = panels;
		this.currentstep = 0;
	}
	
	public void setSettings(int step, Map<String, Object> settings) {
		panels[step].setSettings(settings);
	}

	public Map<String, Object> getSettings(int step) {
		return panels[step].getSettings();
	}

	public void show() throws OperationCancelledException {
		InteractionResult result = InteractionResult.PREV;

		while (!InteractionResult.FINISHED.equals(result)) {
			if (InteractionResult.CANCEL.equals(result))
				throw new OperationCancelledException();
			
			if (InteractionResult.PREV.equals(result))
				if (currentstep > 0) currentstep--;
			
			if (InteractionResult.NEXT.equals(result))
				if (currentstep < panels.length-1) currentstep++;
			
			result = context.showWizard(panels[currentstep].getTitle()+" -- step "+(currentstep+1)+" of "+panels.length,
					currentstep == 0, currentstep == panels.length-1,
					(JComponent) panels[currentstep]);

		}
	}
}
