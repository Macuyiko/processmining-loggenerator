package org.processmining.plugins.kutoolbox.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author Niels Lambrigts (niels.lambrigts@gmail.com)
 * @author Jochen De Weerdt (Jochen.DeWeerdt@econ.kuleuven.be)
 * @date 2011-07-29
 */
public class FancyIntegerSlider extends JPanel {

	private static final long serialVersionUID = -4719482627218339778L;

	private JTextField textfield;
	private JSlider slider;
	int current;
	int min;
	int max;
	boolean logarithmic;
	double log_base;

	private ChangeListener keepLabelConsistent = new ChangeListener() {

		@SuppressWarnings("deprecation")
		@Override
		public void stateChanged(ChangeEvent event) {
			textfield.getDocument()
					.removeDocumentListener(keepSliderConsistent);
			current = getValueFromSlider();
			textfield.setText(new Integer(current).toString());
			textfield.getDocument().addDocumentListener(keepSliderConsistent);
		}
	};

	private KeyListener keepLabelTextConsistent = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// do nothing
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// do nothing
		}

		@Override
		public void keyTyped(KeyEvent event) {
			if (event.getKeyChar() < '0' || event.getKeyChar() > '9') {
				event.consume();
				return;
			}
		}
	};

	private DocumentListener keepSliderConsistent = new DocumentListener() {

		@Override
		public void changedUpdate(DocumentEvent event) {
			updateLabel();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			updateLabel();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			updateLabel();
		}

		public void updateLabel() {
			slider.removeChangeListener(keepLabelConsistent);
			try {
				current = Integer.parseInt(textfield.getText());
			} catch (Exception e) {
				current = min;
			}

			if (current >= max) {
				slider.setValue(slider.getMaximum());
				slider.addChangeListener(keepLabelConsistent);
				return;
			} else if (current <= min) {
				slider.setValue(slider.getMinimum());
				slider.addChangeListener(keepLabelConsistent);
				return;
			}

			slider.setValue(getSliderFromValue());
			slider.addChangeListener(keepLabelConsistent);
		}
	};

	/**
	 * Create a new FancyIntegerSlider with a linear scale.
	 * 
	 * @param min
	 *            The minimal value which this slider can have.
	 * @param current
	 *            The current value of this slider.
	 * @param max
	 *            The maximal value which this slider can have.
	 */
	public FancyIntegerSlider(int min, int current, int max) {
		this(min, current, max, false);
	}

	/**
	 * Create a new FancyIntegerSlider.
	 * 
	 * @param min
	 *            The minimal value which this slider can have.
	 * @param current
	 *            The current value of this slider.
	 * @param max
	 *            The maximal value which this slider can have.
	 * @param logarithmic
	 *            True if you want a logarithmic slider, false for a linear
	 *            slider.
	 */
	public FancyIntegerSlider(int min, int current, int max, boolean logarithmic) {
		SlickerFactory slicker = SlickerFactory.instance();
		this.setOpaque(false);
		textfield = new JTextField();
		slider = slicker.createSlider(SwingConstants.HORIZONTAL);

		slider.setMinimum(0);
		if (logarithmic)
			slider.setMaximum(Integer.MAX_VALUE);
		else
			slider.setMaximum(max - min);

		this.current = current;
		this.min = min;
		this.max = max;
		this.logarithmic = logarithmic;
		if (logarithmic) {
			log_base = Math.pow(Math.E,
					Math.log(max + 1 - min) / slider.getMaximum());
		}

		slider.setPreferredSize(new Dimension(250,
				slider.getPreferredSize().height));
		textfield.setPreferredSize(new Dimension(40, textfield
				.getPreferredSize().height));
		slider.setMinimumSize(slider.getPreferredSize());
		textfield.setMinimumSize(textfield.getPreferredSize());

		textfield.setOpaque(false);
		textfield.setBorder(null);
		textfield.setFont(slicker.createLabel("").getFont());
		textfield.setDisabledTextColor(textfield.getForeground());

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.BASELINE_LEADING;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(slider, c);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.BELOW_BASELINE_LEADING;
		c.gridx = 1;
		c.gridy = 0;
		this.add(textfield, c);

		slider.addChangeListener(keepLabelConsistent);
		textfield.addKeyListener(keepLabelTextConsistent);
		textfield.getDocument().addDocumentListener(keepSliderConsistent);

		setValue(current);
	}

	private int getSliderFromValue() {
		double position = current;
		if (logarithmic) {
			position = Math.log(position + 1 - min) / Math.log(log_base);
		}
		position -= min;
		return (int) position;
	}

	public int getValue() {
		return current;
	}
	
	public int getMax() {
		return slider.getMaximum();
	}

	public int getMin() {
		return slider.getMinimum();
	}

	public void setMax(int maximum) {
		slider.setMaximum(maximum);
	}

	public void setMin(int minimum) {
		slider.setMinimum(minimum);
	}
	
	private int getValueFromSlider() {
		double position = slider.getValue();
		if (logarithmic) {
			position += min;
			position = Math.pow(log_base, position) - 1;
		}
		position += min;
		return (int) position;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		slider.setEnabled(enabled);
		textfield.setEnabled(enabled);
	}

	@SuppressWarnings("deprecation")
	public void setValue(int value) {
		current = value;
		textfield.setText(new Integer(current).toString());
	}
}
