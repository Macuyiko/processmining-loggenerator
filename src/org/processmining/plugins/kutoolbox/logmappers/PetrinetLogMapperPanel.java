package org.processmining.plugins.kutoolbox.logmappers;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.kutoolbox.utils.LogUtils;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class PetrinetLogMapperPanel extends JPanel {
	private static final long serialVersionUID = -699953189980632567L;

	@SuppressWarnings("rawtypes")
	private Map<Transition, JComboBox> mapTrans2ComboBox = new HashMap<Transition, JComboBox>();
	private JComboBox<?> classifierSelectionCbBox;
	private XLog log;

	public PetrinetLogMapperPanel(final XLog log, final Petrinet net) {
		super();

		int rowCounter = 0;
		this.log = log;
		SlickerFactory factory = SlickerFactory.instance();

		Object[] availableClassifiers = new Object[4];
		availableClassifiers[0] = XLogInfoImpl.STANDARD_CLASSIFIER;
		availableClassifiers[1] = XLogInfoImpl.NAME_CLASSIFIER;
		availableClassifiers[2] = XLogInfoImpl.LIFECYCLE_TRANSITION_CLASSIFIER;
		availableClassifiers[3] = XLogInfoImpl.RESOURCE_CLASSIFIER;

		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL }, { 80, 70 } };
		TableLayout layout = new TableLayout(size);
		setLayout(layout);

		add(factory.createLabel("<html><h1>Map Transitions to Event Class</h1></html>"), 
				"0, " + rowCounter + ", 1, " + rowCounter);
		rowCounter++;

		classifierSelectionCbBox = factory.createComboBox(availableClassifiers);
		classifierSelectionCbBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] boxOptions = PetrinetLogMapper.extractEventClassList(log,
						(XEventClassifier) classifierSelectionCbBox.getSelectedItem());

				for (Transition transition : mapTrans2ComboBox.keySet()) {
					@SuppressWarnings("unchecked")
					JComboBox<Object> cbBox = mapTrans2ComboBox.get(transition);
					cbBox.removeAllItems();

					for (Object item : boxOptions) {
						cbBox.addItem(item);
					}
					cbBox.setSelectedIndex(
							PetrinetLogMapper.preSelectOption(transition.getLabel(), boxOptions));
				}
			}
		});
		classifierSelectionCbBox.setSelectedIndex(0);
		classifierSelectionCbBox.setPreferredSize(new Dimension(350, 30));
		classifierSelectionCbBox.setMinimumSize(new Dimension(350, 30));

		add(factory.createLabel("Choose classifier"), "0, " + rowCounter + ", l, c");
		add(classifierSelectionCbBox, "1, " + rowCounter + ", l, c");
		rowCounter++;

		Object[] boxOptions = PetrinetLogMapper.extractEventClassList(log, getSelectedClassifier());
		for (Transition transition : PetrinetLogMapper.extractTransitionList(net)) {
			layout.insertRow(rowCounter, 30);
			JComboBox<?> cbBox = factory.createComboBox(boxOptions);
			cbBox.setPreferredSize(new Dimension(350, 30));
			cbBox.setMinimumSize(new Dimension(350, 30));
			mapTrans2ComboBox.put(transition, cbBox);
			cbBox.setSelectedIndex(PetrinetLogMapper.preSelectOption(transition.getLabel(), boxOptions));

			add(factory.createLabel(transition.getLabel()), "0, " + rowCounter + ", l, c");
			add(cbBox, "1, " + rowCounter + ", l, c");
			rowCounter++;
		}

	}

	public PetrinetLogMapper getMap() {
		PetrinetLogMapper map = new PetrinetLogMapper(
				getSelectedClassifier(),
				LogUtils.getEventClasses(log, getSelectedClassifier()),
				mapTrans2ComboBox.keySet());
		for (Transition trans : mapTrans2ComboBox.keySet()) {
			Object selectedValue = mapTrans2ComboBox.get(trans).getSelectedItem();
			if (selectedValue instanceof XEventClass) {
				map.put(trans, (XEventClass) selectedValue);
			}
		}
		return map;
	}

	public XEventClassifier getSelectedClassifier() {
		return (XEventClassifier) classifierSelectionCbBox.getSelectedItem();
	}

}
