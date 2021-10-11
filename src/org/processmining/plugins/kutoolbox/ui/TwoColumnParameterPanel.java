package org.processmining.plugins.kutoolbox.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public abstract class TwoColumnParameterPanel extends RoundedPanel {
	private static final long serialVersionUID = 1953855415963248544L;
	private final SlickerFactory slickerFactory = SlickerFactory.instance();
	private int layoutRows;

	public TwoColumnParameterPanel(int rows) {
		this.layoutRows = rows;
		this.setLayout();
	}

	protected void setLayout() {
		RowSpec[] rowSpecs = new RowSpec[layoutRows];
		for (int row = 0; row < layoutRows; row++) {
			rowSpecs[row] = RowSpec.decode("top:20px");
		}
		setLayout(new FormLayout(
				new ColumnSpec[] {
						ColumnSpec.decode("default:grow"),
						ColumnSpec.decode("default:grow") }, 
				rowSpecs));
	}

	abstract protected void updateFields();

	public JLabel addHeading(String text, int row) {
		JLabel label = slickerFactory.createLabel(text);
		label.setForeground(Color.DARK_GRAY);
		label.setFont(new Font("Tahoma", Font.BOLD, 18));
		add(label, "1, " + row + ", 2, 1, left, top");
		return label;
	}
	
	public JLabel addDoubleHeading(String text, int row) {
		JLabel label = slickerFactory.createLabel(text);
		label.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(label, "1, " + row + ", 2, 1, left, top");
		return label;
	}

	public JLabel addLabel(String text, int row) {
		JLabel label = slickerFactory.createLabel(text);
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		add(label, "1, " + row + ", right, top");
		return label;
	}

	public JLabel addDoubleLabel(String text, int row) {
		JLabel label = slickerFactory.createLabel(text);
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		add(label, "1, " + row + ", 2, 1, left, top");
		return label;
	}

	public JCheckBox addCheckbox(String text, boolean checked, int row, boolean updatesFields) {
		JCheckBox checkbox = slickerFactory.createCheckBox(text, checked);
		if (updatesFields) {
			checkbox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateFields();
				}
			});
		}
		add(checkbox, "1, " + row + ", 2, 1, left, top");
		return checkbox;
	}

	public JComboBox<?> addCombobox(int selected, Object[] values, int row, boolean updatesFields) {
		JComboBox<?> combobox = slickerFactory.createComboBox(values);
		combobox.setSelectedIndex(selected);
		if (updatesFields) {
			combobox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateFields();
				}
			});
		}
		add(combobox, "2, " + row + ", left, top");
		return combobox;
	}

	public JRadioButton addRadiobutton(String text, boolean checked, int row, boolean updatesFields) {
		JRadioButton radiobutton = slickerFactory.createRadioButton(text);
		if (updatesFields) {
			radiobutton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateFields();
				}
			});
		}
		radiobutton.setSelected(checked);
		add(radiobutton, "1, " + row + ", 2, 1, left, top");
		return radiobutton;
	}

	public ButtonGroup addButtongroup() {
		ButtonGroup group = new ButtonGroup();
		return group;
	}

	public JTextArea addTextarea(String text, int row) {
		JTextArea textarea = new JTextArea("", 15, 45);
		textarea.setLineWrap(true);
		add(textarea, "1, " + row + ", 2, 1, left, top");
		return textarea;
	}
	
	public JTextField addTextfield(String text, int row, boolean updatesFields) {
		return addTextfield(text, row, updatesFields, false);
	}

	public JTextField addTextfield(String text, int row, boolean updatesFields, boolean updatesImmediately) {
		JTextField textfield = new JTextField(text, text.length() + 2);
		if (updatesFields) {
			if (updatesImmediately) {
				textfield.getDocument().addDocumentListener(new DocumentListener() {
					public void changedUpdate(DocumentEvent e) { change(); }
					public void removeUpdate(DocumentEvent e) { change(); }
					public void insertUpdate(DocumentEvent e) { change(); }
					public void change() {
						updateFields();
					}
				});
			} else {
				textfield.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						updateFields();
					}
				});
			}
		}
		textfield.setFont(new Font("Tahoma", Font.PLAIN, 12));
		add(textfield, "2, " + row + ", left, top");
		return textfield;
	}

	public FancyIntegerSlider addIntegerSlider(int min, int def, int max, int row) {
		FancyIntegerSlider integerslider = new FancyIntegerSlider(min, def, max);
		add(integerslider, "2, " + row);
		return integerslider;
	}
	
	public FancyDoubleSlider addDoubleSlider(double min, double def, double max, int row) {
		FancyDoubleSlider doubleslider = new FancyDoubleSlider(min, def, max);
		add(doubleslider, "2, " + row);
		return doubleslider;
	}
	
	public JSpinner addSpinner(double min, double def, double max, double step, int row) {
		SpinnerModel model = new SpinnerNumberModel(def, min, max, step);
		JSpinner spinner = new JSpinner(model);
		add(spinner, "2, " + row);
		return spinner;
	}
	
	public JComponent addComponent(JComponent component, int row) {
		return addComponent(component, row, false);
	}
	
	public JComponent addComponent(JComponent component, int row, boolean right) {
		String c = (right) ? "2" : "1";
		add(component, c + ", " + row);
		return component;
	}
	
	public JComponent addDoubleComponent(JComponent component, int row) {
		add(component, "1, " + row + ", 2, 1, left, top");
		return component;
	}

}
