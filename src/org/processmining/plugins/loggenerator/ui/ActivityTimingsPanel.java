package org.processmining.plugins.loggenerator.ui;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;

public class ActivityTimingsPanel extends TwoColumnParameterPanel {
	private static final long serialVersionUID = -1545498489649847L;
	private JCheckBox completeOnly;
	private JTextField avgLead;
	private JTextField stdLead;
	private JTextField avgIdle;
	private JTextField stdIdle;
	
	private GeneratorSettings settings;
	private Transition transition;
	
	public ActivityTimingsPanel(GeneratorSettings settings, Transition transition) {
		super(10);
		
		this.settings = settings;
		this.transition = transition;
		
		boolean conly = true;
		int lAvg = 60;
		int lVar = 0;
		int iAvg = 60;
		int iVar = 0;
		
		if (settings.getTransitionTimings().containsKey(transition)) {
			int[] set = settings.getTransitionTimings().get(transition);
			conly = set[0] == -1;
			lAvg = set[0] == -1 ? 0 : set[0];
			lVar = set[1];
			iAvg = set[2];
			iVar = set[3];
		}		
		
		this.addDoubleLabel("Setting timings for transition: '"+transition.getLabel()+"'", 1);
		completeOnly = this.addCheckbox("Generate 'complete' event only", conly, 3, true);
		
		this.addLabel("Average lead time (duration):", 5);
		avgLead = this.addTextfield(""+lAvg, 5, true, true);
		this.addLabel("Standard deviation:", 6);
		stdLead = this.addTextfield(""+lVar, 6, true, true);
		
		this.addLabel("Average idle time (wait time before activity):", 8);
		avgIdle = this.addTextfield(""+iAvg, 8, true, true);
		this.addLabel("Standard deviation:", 9);
		stdIdle = this.addTextfield(""+iVar, 9, true, true);
		
		updateFields();
	}
	
	@Override
	protected void updateFields() {
		avgLead.setEnabled(!completeOnly.isSelected());
		stdLead.setEnabled(!completeOnly.isSelected());
		
		int lAvg = completeOnly.isSelected() ? -1 : GeneratorSettings.safeInt(avgLead.getText());
		int lVar = GeneratorSettings.safeInt(stdLead.getText());
		int iAvg = GeneratorSettings.safeInt(avgIdle.getText());
		int iVar = GeneratorSettings.safeInt(stdIdle.getText());
		
		settings.getTransitionTimings().put(transition, new int[] {lAvg, lVar, iAvg, iVar});
	}

}
