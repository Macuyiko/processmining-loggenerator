package org.processmining.plugins.kutoolbox.visualizators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.util.Cleanable;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableComponent.UpdateListener;
import org.processmining.models.jgraph.ProMJGraph;
import com.fluxicon.slickerbox.factory.SlickerDecorator;

public class GraphViewPanel extends JPanel 
	implements Cleanable, ChangeListener, MouseMotionListener, UpdateListener, MouseWheelListener, MouseListener {
	private static final long serialVersionUID = 6858755708629286614L;
	
	public static final int MAX_ZOOM = 1200;
	protected final ScalableComponent scalable;
	protected JScrollPane scroll;
	protected final Point pp = new Point();
   
	public GraphViewPanel(final ScalableComponent scalableComponent) {
		this.scalable = scalableComponent;
		this.scroll = new JScrollPane(getComponent());
		SlickerDecorator.instance().decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		
		this.scroll.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				scroll.removeComponentListener(this);
				scalable.setScale(1);
				double rx = (scroll.getWidth() - scroll.getVerticalScrollBar().getWidth())
						/ scalable.getComponent().getPreferredSize().getWidth();
				double ry = (scroll.getHeight() - scroll.getHorizontalScrollBar().getHeight())
						/ scalable.getComponent().getPreferredSize().getHeight();
				scalable.setScale(Math.min(rx, ry));
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentHidden(ComponentEvent e) {
			}
		});

		this.setLayout(new BorderLayout());
		this.add(scroll);

		scroll.getViewport().addMouseMotionListener(this);
		scroll.getViewport().addMouseWheelListener(this);
		scroll.getViewport().addMouseListener(this);
		scalable.addUpdateListener(this);
		
		this.redraw();
		this.validate();
		this.repaint();
	}

	public double getScale() {
		return scalable.getScale();
	}

	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	public void cleanUp() {
		if (getComponent() instanceof Cleanable) {
			((Cleanable) getComponent()).cleanUp();
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider) {
			scalable.setScale(((JSlider) source).getValue() / 100.0);
			redraw();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void finalize() throws Throwable {
		try {
			cleanUp();
		} finally {
			super.finalize();
		}
	}

	public void updated() {
		JComponent newComponent = scalable.getComponent();
		if (newComponent != getComponent()) {
			scroll.setViewportView(newComponent);
			if (getComponent() instanceof Cleanable) {
				((Cleanable) getComponent()).cleanUp();
			}
			invalidate();
		}
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public void scaleToFit() {
		scalable.setScale(1);
		double rx = scroll.getViewport().getExtentSize().getWidth()
				/ scalable.getComponent().getPreferredSize().getWidth();
		double ry = scroll.getViewport().getExtentSize().getHeight()
				/ scalable.getComponent().getPreferredSize().getHeight();
		scalable.setScale(Math.min(rx, ry));
	}

	public JComponent getComponent() {
		return this.scalable.getComponent();
	}

	public ScalableComponent getScalable() {
		return this.scalable;
	}

	public void mouseDragged(MouseEvent e) {
		JViewport vport = (JViewport)e.getSource();
        Point cp = e.getPoint();
        Point vp = vport.getViewPosition();
        vp.translate(pp.x-cp.x, pp.y-cp.y);
        getComponent().scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        pp.setLocation(cp);
	}
	
	public void mousePressed(MouseEvent e) {
        pp.setLocation(e.getPoint());
    }

    public void mouseReleased(MouseEvent e) {
    	redraw();
    }

	public synchronized void mouseMoved(MouseEvent e) {
	}
	
	public void redraw() {
		try {
			ProMJGraph graph = (ProMJGraph) this.scalable;
			graph.refresh();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches < 0) {
			getScalable().setScale(getScale() + .1D);
		} else {
			getScalable().setScale(getScale() - .1D);
		}
	}

	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}
}
