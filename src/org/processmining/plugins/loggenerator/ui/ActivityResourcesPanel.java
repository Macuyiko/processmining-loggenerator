package org.processmining.plugins.loggenerator.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.loggenerator.utils.GeneratorSettings;

public class ActivityResourcesPanel extends JPanel {
	private static final long serialVersionUID = -1598101547847844L;
	
	private GeneratorSettings settings;
	private Transition transition;
	private JList<Pair<String, Integer>> list;
	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ActivityResourcesPanel(final GeneratorSettings settings, final Transition transition) {
		super();
		
		this.settings = settings;
		this.transition = transition;
		
		this.setLayout(new BorderLayout());
		
		JLabel label = new JLabel("Setting resources for transition: '"+transition.getLabel()+"'");
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.add(label, BorderLayout.PAGE_START);
		
		List<Pair<String, Integer>> elements = 
				(settings.getTransitionResourcesWeights().containsKey(transition)) ?
				(settings.getTransitionResourcesWeights().get(transition)) :
				new ArrayList<Pair<String, Integer>>();
				
		DefaultListModel model = new DefaultListModel();
		for (Pair<String, Integer> e : elements)
			model.addElement(e);
		list = new JList(model);
		list.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList arg0,
					Object val, int ind, boolean iss, boolean chf) {
				Pair<String, Integer> v = (Pair<String, Integer>) val;
				JLabel l = new JLabel(v.getFirst()+" ("+v.getSecond()+")");
				if (iss)
					l.setFont(l.getFont().deriveFont(Font.BOLD));
				return l;
			}
		});
		
		this.add(new JScrollPane(list), BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel();
		JButton deleteButton = new JButton("Delete");
		JButton addButton = new JButton("Add");
		final JTextField resourceName = new JTextField();
		resourceName.setPreferredSize(new Dimension(200, 30));
		resourceName.setMinimumSize(new Dimension(200, 30));
		final JTextField resourceWeight = new JTextField("10");
		resourceWeight.setPreferredSize(new Dimension(50, 30));
		resourceWeight.setMinimumSize(new Dimension(50, 30));
		
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Pair<String, Integer> el = new Pair<String, Integer>(
						resourceName.getText(), 
						GeneratorSettings.safeInt(resourceWeight.getText()));
				((DefaultListModel)list.getModel()).addElement(el);
				updateFields();
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list.getSelectedIndex() == -1) return;
				((DefaultListModel)list.getModel()).remove(list.getSelectedIndex());
				updateFields();
			}
		});
		
		
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(deleteButton);
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
		buttonPane.add(Box.createHorizontalStrut(5));
		buttonPane.add(resourceName);
		buttonPane.add(resourceWeight);
		buttonPane.add(addButton);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		this.add(buttonPane, BorderLayout.PAGE_END);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateFields() {
		List<Pair<String, Integer>> arr = new ArrayList<Pair<String, Integer>>();
		for (int i = 0; i < list.getModel().getSize(); i++)
			arr.add((Pair<String, Integer>) ((DefaultListModel)list.getModel()).getElementAt(i));
		
		settings.getTransitionResourcesWeights().put(transition, arr);
	}

}
