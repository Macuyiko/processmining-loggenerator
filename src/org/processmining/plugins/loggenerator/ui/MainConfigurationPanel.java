package org.processmining.plugins.loggenerator.ui;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.ui.FancyIntegerSlider;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings.SimulationAlgorithm;

public class MainConfigurationPanel extends TwoColumnParameterPanel {
	private static final long serialVersionUID = -1899951988854484751L;

	private FancyIntegerSlider sliderNrTraces;
	private FancyIntegerSlider sliderRandomMaxInGroup, sliderRandomMinInGroup, sliderMaxTimesMarkingSeen;
	private JRadioButton radioRandom, radioComplete, radioDistinct;
	private JRadioButton radioReachEnd, radioNoRemaining, radioAlsoAccept;

	protected GeneratorSettings settings;
	protected Petrinet petriNet;

	public MainConfigurationPanel(GeneratorSettings settings, Petrinet petriNet) {
		super(22);

		this.settings = settings;
		this.petriNet = petriNet;

		if (settings == null)
			this.settings = new GeneratorSettings();
		else
			this.settings = settings;

		this.init();
	}

	public GeneratorSettings getSettings() {
		settings.setNrTraces(sliderNrTraces.getValue());
		settings.setRandomMinInGroup(sliderRandomMinInGroup.getValue());
		settings.setRandomMaxInGroup(sliderRandomMaxInGroup.getValue());
		settings.setMaxTimesMarkingSeen(sliderMaxTimesMarkingSeen.getValue());

		settings.setMustReachEnd(radioReachEnd.isSelected());
		settings.setMustConsumeAll(radioNoRemaining.isSelected());
		settings.setAlsoConsiderPartial(radioAlsoAccept.isSelected());

		if (radioRandom.isSelected())
			settings.setSimulationMethod(SimulationAlgorithm.Random);
		if (radioComplete.isSelected())
			settings.setSimulationMethod(SimulationAlgorithm.Complete);
		if (radioDistinct.isSelected())
			settings.setSimulationMethod(SimulationAlgorithm.Distinct);

		return this.settings;
	}

	protected void init() {
		this.addHeading("Event Log Generation Options", 1);

		this.addDoubleHeading("Simulation Method", 3);

		radioRandom = this.addRadiobutton("Random path generation", true, 4, true);
		radioComplete = this.addRadiobutton("Complete generation", false, 5, true);
		radioDistinct = this.addRadiobutton("Grouped path generation", false, 6, true);

		ButtonGroup group = this.addButtongroup();
		group.add(radioRandom);
		group.add(radioComplete);
		group.add(radioDistinct);

		this.addDoubleHeading("Options", 8);

		this.addLabel("Nr. of generated traces:", 9);
		sliderNrTraces = this.addIntegerSlider(1, 2000, 500000, 9);
		this.addLabel("Min. traces to add for each generated sequence:", 10);
		sliderRandomMinInGroup = this.addIntegerSlider(1, 1, 1000, 10);
		this.addLabel("Max. traces to add for each generated sequence:", 11);
		sliderRandomMaxInGroup = this.addIntegerSlider(1, 1, 1000, 11);

		this.addLabel("Max. times marking seen:", 14);
		sliderMaxTimesMarkingSeen = this.addIntegerSlider(1, 3, 50, 14);

		radioReachEnd = this.addRadiobutton("Only include traces that reach end state", true, 16, true);
		radioNoRemaining = this.addRadiobutton("Only include traces without remaining tokens", true, 17, true);
		radioAlsoAccept = this.addRadiobutton("Also accept traces that exceeded number of same markings seen", false, 18, true);

		this.updateFields();
	}

	@Override
	protected void updateFields() {
		sliderNrTraces.setEnabled(!radioComplete.isSelected());
	}

}
