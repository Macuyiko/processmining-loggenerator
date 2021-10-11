package org.processmining.plugins.loggenerator.ui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ActivityConfigurationPanel extends JPanel {
	private static final long serialVersionUID = -699953189980632567L;

	private JFrame frameShowed = null;
	
	public ActivityConfigurationPanel(final GeneratorSettings settings, Petrinet net) {
		super();
		
		frameShowed = null;
		
		BufferedImage timingsIcon = null;
		BufferedImage resourcesIcon = null;
		try {
			timingsIcon = ImageIO.read(this.getClass().getResourceAsStream("timings.png"));
			resourcesIcon = ImageIO.read(this.getClass().getResourceAsStream("resources.png"));
		} catch (IOException e) { e.printStackTrace(); }
		
		int rowCounter = 0;
		
		SlickerFactory factory = SlickerFactory.instance();

		double size[][] = { 
				{	200, 
					220, 
					60, 
					60,
					60,}, 
				{ 70,70,70,70,70 } };
		TableLayout layout = new TableLayout(size);
		setLayout(layout);

		add(factory.createLabel("<html><h1>Setup Activities</h1></html>"),
				"0, " + rowCounter + ", 1, " + rowCounter);
		rowCounter++;

		add(factory.createLabel("Transition"), 			"0, " + rowCounter + ", l, c");
		add(factory.createLabel("Activity Name (leave blank to set invisible)"), 				"1, " + rowCounter + ", l, c");
		add(factory.createLabel("Weight"), 				"2, " + rowCounter + ", l, c");
		add(factory.createLabel("Timings"),	"3, " + rowCounter + ", l, c");
		add(factory.createLabel("Resources"), "4, " + rowCounter + ", l, c");
		rowCounter++;
		
		List<Transition> transitions = new ArrayList<Transition>(net.getTransitions());
        Collections.sort(transitions, new Comparator<Transition>() {
			public int compare(Transition arg0, Transition arg1) {
				String rank1 = arg0.getLabel();
		        String rank2 = arg1.getLabel();
		        return rank1.compareToIgnoreCase(rank2);
			}	
        });

		for (final Transition transition : transitions) {
			
			layout.insertRow(rowCounter, 30);
			
			String sLabel = transition.getLabel()
					.replace("\\ncomplete", "")
					.replace("+complete", "")
					.replace("\\n", "");
			if (sLabel.equals("")) sLabel = "(empty label)";
			if (PetrinetUtils.isInvisibleTransition(transition))
				sLabel += " (invisible)";

			final JTextField tActivity = new JTextField();
			tActivity.setPreferredSize(new Dimension(200, 30));
			tActivity.setMinimumSize(new Dimension(200, 30));
			tActivity.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) { change(); }
				public void removeUpdate(DocumentEvent e) { change(); }
				public void insertUpdate(DocumentEvent e) { change(); }
				public void change() {
					settings.getTransitionNames().put(transition, tActivity.getText());
				}
			});
			
			final JTextField tWeight = new JTextField();
			tWeight.setText("10");
			tWeight.setPreferredSize(new Dimension(50, 30));
			tWeight.setMinimumSize(new Dimension(50, 30));
			tWeight.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) { change(); }
				public void removeUpdate(DocumentEvent e) { change(); }
				public void insertUpdate(DocumentEvent e) { change(); }
				public void change() {
					settings.getTransitionWeights().put(transition, GeneratorSettings.safeInt(tWeight.getText()));
				}
			});
			
			final JButton tTimings = new JButton(new ImageIcon(timingsIcon));
			tTimings.setBorder(BorderFactory.createEmptyBorder());
			tTimings.setContentAreaFilled(false);
			tTimings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (frameShowed != null) frameShowed.dispose();
					frameShowed = new JFrame("Configure Activity Timings");
					frameShowed.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frameShowed.getContentPane().add(new ActivityTimingsPanel(settings, transition), BorderLayout.CENTER);
					frameShowed.pack();
					frameShowed.setVisible(true);
				}
			});
			
			final JButton tResources = new JButton(new ImageIcon(resourcesIcon));
			tResources.setBorder(BorderFactory.createEmptyBorder());
			tResources.setContentAreaFilled(false);
			tResources.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (frameShowed != null) frameShowed.dispose();
					frameShowed = new JFrame("Configure Activity Resources");
					frameShowed.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frameShowed.getContentPane().add(new ActivityResourcesPanel(settings, transition), BorderLayout.CENTER);
					frameShowed.pack();
					frameShowed.setVisible(true);
				}
			});
			
			add(factory.createLabel(sLabel), 	"0, " + rowCounter + ", l, c");
			add(tActivity, 						"1, " + rowCounter + ", l, c");
			add(tWeight, 						"2, " + rowCounter + ", l, c");
			add(tTimings, 						"3, " + rowCounter + ", l, c");
			add(tResources, 					"4, " + rowCounter + ", l, c");
			
			if (sLabel.contains("inv_") 
					|| sLabel.contains("$") 
					|| PetrinetUtils.isInvisibleTransition(transition))
				tActivity.setText("");
			else 
				tActivity.setText(sLabel.replace("*", ""));
			
			// Set these settings for all transitions to defaults at this point
			settings.getTransitionNames().put(transition, tActivity.getText());
			settings.getTransitionWeights().put(transition, GeneratorSettings.safeInt(tWeight.getText()));
			
			rowCounter++;
		}

	}
	
}
