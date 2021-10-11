package org.processmining.plugins.kutoolbox.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DurationPanel extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 4591213704473324755L;
	private long duration;
	private JSpinner syears, smonths, sdays, shours, sminutes, sseconds, smseconds;
	
	public DurationPanel() {
		this(1 * 60 * 60 * 1000);
	}
	
	public DurationPanel(long duration) {
		initUI();
		setDuration(duration);
	}
		
	private void initUI() {
		this.setPreferredSize(new Dimension(500, 70));
		
		SpinnerModel years =  new SpinnerNumberModel(0, 0, 10, 1);
		SpinnerModel months =  new SpinnerNumberModel(0, 0, 12, 1);
		SpinnerModel days =  new SpinnerNumberModel(0, 0, 31, 1);
		SpinnerModel hours =  new SpinnerNumberModel(1, 0, 24, 1);
		SpinnerModel minutes =  new SpinnerNumberModel(0, 0, 60, 1);
		SpinnerModel seconds =  new SpinnerNumberModel(0, 0, 60, 1);
		SpinnerModel mseconds =  new SpinnerNumberModel(0, 0, 1000, 1);
		
		syears = new JSpinner(years);
		smonths = new JSpinner(months);
		sdays = new JSpinner(days);
		shours = new JSpinner(hours);
		sminutes = new JSpinner(minutes);
		sseconds = new JSpinner(seconds);
		smseconds = new JSpinner(mseconds);

		syears.addChangeListener(this);
		smonths.addChangeListener(this);
		sdays.addChangeListener(this);
		shours.addChangeListener(this);
		sminutes.addChangeListener(this);
		sseconds.addChangeListener(this);
		smseconds.addChangeListener(this);
		
		this.add(new JLabel("Y:"));
		this.add(syears);
		this.add(new JLabel("M:"));
		this.add(smonths);
		this.add(new JLabel("D:"));
		this.add(sdays);
		this.add(new JLabel("h:"));
		this.add(shours);
		this.add(new JLabel("m:"));
		this.add(sminutes);
		this.add(new JLabel("s:"));
		this.add(sseconds);
		this.add(new JLabel("ms:"));
		this.add(smseconds);
		
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
		long d = 0;
		
		d = (long) Math.floor(duration / (1000 * 60 * 60 * 24 * 31 * 12));
		syears.setValue(d);
		duration -= d * (1000 * 60 * 60 * 24 * 31 * 12);
		
		d = (long) Math.floor(duration / (1000 * 60 * 60 * 24 * 31));
		smonths.setValue(d);
		duration -= d * (1000 * 60 * 60 * 24 * 31);
		
		d = (long) Math.floor(duration / (1000 * 60 * 60 * 24));
		sdays.setValue(d);
		duration -= d * (1000 * 60 * 60 * 24);
		
		d = (long) Math.floor(duration / (1000 * 60 * 60));
		shours.setValue(d);
		duration -= d * (1000 * 60 * 60);
		
		d = (long) Math.floor(duration / (1000 * 60));
		sminutes.setValue(d);
		duration -= d * (1000 * 60);
		
		d = (long) Math.floor(duration / (1000));
		sseconds.setValue(d);
		duration -= d * (1000);
		
		smseconds.setValue(d);
		duration -= d * (1000 * 60 * 60 * 24 * 31 * 12);
	}
	
	public long getDuration() {
		return duration;
	}

	public void stateChanged(ChangeEvent arg0) {
		long d  = Long.parseLong(syears.getValue().toString()) 		* 1000L * 60L * 60L * 24L * 31L * 12L
				+ Long.parseLong(smonths.getValue().toString()) 	* 1000L * 60L * 60L * 24L * 31L
				+ Long.parseLong(sdays.getValue().toString()) 		* 1000L * 60L * 60L * 24L
				+ Long.parseLong(shours.getValue().toString())  	* 1000L * 60L * 60L
				+ Long.parseLong(sminutes.getValue().toString()) 	* 1000L * 60L
				+ Long.parseLong(sseconds.getValue().toString()) 	* 1000L
				+ Long.parseLong(smseconds.getValue().toString());
		duration = d;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame("DurationPanel test");
		f.setSize(500, 70);
		f.getContentPane().add(BorderLayout.CENTER, new DurationPanel());
		f.setVisible(true);
		System.err.println();
	}
	
}
