package org.processmining.plugins.loggenerator.ui;

import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;

public class TraceTimingsPanel extends TwoColumnParameterPanel {
	private static final long serialVersionUID = -19958888354484751L;

	private JRadioButton tracesFollow, tracesEqual;
	private JSpinner anchor;
	private JTextField avg, sd;
	
	protected GeneratorSettings settings;
	protected Petrinet petriNet;

	public TraceTimingsPanel(GeneratorSettings settings) {
		super(14);

		this.settings = settings;
		
		if (settings == null)
			this.settings = new GeneratorSettings();
		else
			this.settings = settings;

		this.init();
	}

	public GeneratorSettings getSettings() {
		settings.setAnchorDate((Date) anchor.getValue());
		settings.setAnchorAvg(Integer.parseInt(avg.getText()));
		settings.setAnchorSd(Integer.parseInt(sd.getText()));
		settings.setTracesFollow(tracesFollow.isSelected());
		
		return this.settings;
	}

	protected void init() {
		this.addHeading("Trace Timing Options", 1);

		this.addDoubleHeading("Anchor Point", 3);

		tracesFollow = this.addRadiobutton("Moving: traces follow each other", true, 4, true);
		tracesEqual = this.addRadiobutton("Fixed: traces start at anchor point", false, 5, true);

		ButtonGroup group = this.addButtongroup();
		group.add(tracesFollow);
		group.add(tracesEqual);
		
		anchor = new JSpinner( new SpinnerDateModel() );
		JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(anchor, "YYYY-MM-DD HH:mm:ss");
		anchor.setEditor(timeEditor);
		anchor.setValue(new Date());
		
		this.addLabel("Anchor date/time:", 8);
		this.addComponent(anchor, 8, true);

		this.addDoubleHeading("Anchor Point Variance", 10);

		this.addLabel("Average (before or after anchor point):", 11);
		avg = this.addTextfield("0", 11, true);
		
		this.addLabel("Standard deviation:", 12);
		sd = this.addTextfield("0", 12, true);
		
		this.updateFields();
	}

	@Override
	protected void updateFields() {
		int iAvg = GeneratorSettings.safeInt(avg.getText());
		int iSd = GeneratorSettings.safeInt(sd.getText());
		avg.setText(iAvg+"");
		sd.setText(iSd+"");
	}

}
