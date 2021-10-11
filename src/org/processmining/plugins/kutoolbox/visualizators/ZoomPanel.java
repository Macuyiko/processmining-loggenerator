package org.processmining.plugins.kutoolbox.visualizators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ZoomPanel extends JPanel {

	public static final int ZOOMHEIGHT = 200;
	
	private static final long serialVersionUID = 976421878797457195L;
	private final JSlider slider;
	private JLabel sliderMinValue, sliderMaxValue;
	JButton sliderFitValue;
	JLabel sliderValue;

	protected int fitZoom;
	protected final AbstractGraphView mainView;

	public ZoomPanel(AbstractGraphView view, int maximumZoom) {

		super(null);
		this.mainView = view;

		this.slider = SlickerFactory.instance().createSlider(1);

		this.slider.setMinimum(1);
		this.slider.setMaximum(maximumZoom);
		this.slider.setValue(fitZoom);

		this.slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				update();
			}
		});

		this.sliderMinValue = SlickerFactory.instance().createLabel("0%");
		this.sliderMaxValue = SlickerFactory.instance().createLabel(maximumZoom + "%");
		this.sliderFitValue = SlickerFactory.instance().createButton("Fit >");
		this.sliderFitValue.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fit();
			}
		});
		this.sliderValue = SlickerFactory.instance().createLabel(fitZoom + "%");

		this.sliderMinValue.setHorizontalAlignment(SwingConstants.CENTER);
		this.sliderMaxValue.setHorizontalAlignment(SwingConstants.CENTER);
		this.sliderFitValue.setHorizontalAlignment(SwingConstants.RIGHT);
		this.sliderValue.setHorizontalAlignment(SwingConstants.LEFT);

		this.sliderMinValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderMaxValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderFitValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		this.sliderValue.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));

		this.sliderMinValue.setForeground(Color.GRAY);
		this.sliderMaxValue.setForeground(Color.GRAY);
		this.sliderFitValue.setForeground(Color.GRAY);
		this.sliderValue.setForeground(Color.DARK_GRAY);

		this.add(this.slider);
		this.add(this.sliderMinValue);
		this.add(this.sliderMaxValue);
		this.add(this.sliderFitValue);
		this.add(this.sliderValue);

		this.setBackground(Color.LIGHT_GRAY);
		
		this.setValue((int) Math.floor(100 * this.mainView.getScalable().getScale()));
	}

	public void setSize(int width, int height) {
		super.setSize(width, height);
		setHeight(height);
	}

	public void setHeight(int height) {

		int sliderHeight = height - 60;

		this.slider.setBounds(40, 30, 30, sliderHeight);
		this.sliderMaxValue.setBounds(0, 10, 100, 20);
		this.sliderMinValue.setBounds(0, height - 30, 100, 20);

		int value = this.slider.getValue();
		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = 33 + (int) ((float) (this.slider.getMaximum() - this.fitZoom) / (float) span * (sliderHeight - 28));
		this.sliderFitValue.setBounds(0, position, 45, 25);

		if (value == this.fitZoom) {
			this.sliderValue.setBounds(70, position, 60, 20);
		} else {
			position = 33 + (int) ((float) (this.slider.getMaximum() - value) / (float) span * (sliderHeight - 28));
			this.sliderValue.setBounds(70, position, 60, 20);
		}
		
		this.setPreferredSize(new Dimension(140, ZOOMHEIGHT));
	}

	private void update() {

		int value = this.slider.getValue();

		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = 33 + (int) ((float) (this.slider.getMaximum() - value) / (float) span * (this.slider.getBounds().height - 28));

		this.sliderValue.setText(value + "%");
		this.sliderValue.setBounds(70, position, 60, 20);

		this.mainView.getScalable().setScale(getZoomValue());

	}

	public double getZoomValue() {
		return this.slider.getValue() / 100.;
	}

	public void setValue(int value) {
		this.slider.setValue(value);
	}

	public void setFitValue(int value) {

		this.fitZoom = value;

		int span = this.slider.getMaximum() - this.slider.getMinimum();
		int position = (int) (33 + Math.floor(((float) (this.slider.getMaximum() - value) / (float) span * (this.slider
				.getBounds().height - 28))));
		this.sliderFitValue.setBounds(0, position, 45, 25);
	}

	public void fit() {
		setValue(fitZoom);
	}

	protected void computeFitScale() {
		double scale = this.mainView.getScalable().getScale();
		Dimension b = this.mainView.getScalable().getComponent().getPreferredSize();
		double w = b.getWidth() / scale;
		double h = b.getHeight() / scale;
		double rx = this.mainView.getViewport().getExtentSize().getWidth() / w;
		double ry = this.mainView.getViewport().getExtentSize().getHeight() / h;

		setFitValue((int) (Math.min(rx, ry) * 100));
	}
}
