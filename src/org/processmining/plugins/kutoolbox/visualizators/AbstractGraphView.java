package org.processmining.plugins.kutoolbox.visualizators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.util.Cleanable;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableComponent.UpdateListener;
import org.processmining.framework.util.ui.widgets.Inspector;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public abstract class AbstractGraphView extends InspectorPanel 
	implements Cleanable, ChangeListener, MouseMotionListener, UpdateListener, MouseWheelListener {

	protected Inspector inspector;
	protected HashMap<String, JPanel> inspectorTabs;
	
	private static final long serialVersionUID = 7995897797117234781L;
	public static final int MAX_ZOOM = 1200;
	protected final ScalableComponent scalable;
	private JComponent component;
	protected JScrollPane scroll;

	protected SlickerFactory factory;
	protected SlickerDecorator decorator;

	@SuppressWarnings("deprecation")
	public AbstractGraphView(final ScalableComponent scalableComponent) {
		this.scalable = scalableComponent;
		component = scalableComponent.getComponent();
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();
		scroll = new JScrollPane(getComponent());
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		inspector = this.getInspector();
		inspectorTabs = new HashMap<String, JPanel>();
		
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

		this.addMouseMotionListener(this);
		getComponent().addMouseMotionListener(this);
		getComponent().addMouseWheelListener(this);
		scalable.addUpdateListener(this);
		
		this.redraw();

		this.validate();
		this.repaint();
	}

	public JScrollBar getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	public JScrollBar getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
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
		scalable.removeUpdateListener(this);
		getComponent().removeMouseMotionListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider) {
			scalable.setScale(((JSlider) source).getValue() / 100.0);
			getComponent().repaint();
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
			getComponent().removeMouseMotionListener(this);

			component = newComponent;
			getComponent().addMouseMotionListener(this);
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
		return component;
	}

	public ScalableComponent getScalable() {
		return this.scalable;
	}

	public void mouseDragged(MouseEvent e) {
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

	public DirectedGraphElement getElementForLocation(final ProMJGraph graph, double x, double y) {
		Object cell = graph.getFirstCellForLocation(x, y);
		if (cell instanceof ProMGraphCell) {
			return ((ProMGraphCell) cell).getNode();
		}
		if (cell instanceof ProMGraphEdge) {
			return ((ProMGraphEdge) cell).getEdge();
		}
		return null;
	}

	public Collection<DirectedGraphNode> getSelectedNodes(final ProMJGraph graph) {
		List<DirectedGraphNode> nodes = new ArrayList<DirectedGraphNode>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				nodes.add(((ProMGraphCell) o).getNode());
			}
		}
		return nodes;
	}

	public Collection<DirectedGraphEdge<?, ?>> getSelectedEdges(final ProMJGraph graph) {
		List<DirectedGraphEdge<?, ?>> edges = new ArrayList<DirectedGraphEdge<?, ?>>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphEdge) {
				edges.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return edges;
	}

	public Collection<DirectedGraphElement> getSelectedElements(final ProMJGraph graph) {
		List<DirectedGraphElement> elements = new ArrayList<DirectedGraphElement>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				elements.add(((ProMGraphCell) o).getNode());
			} else if (o instanceof ProMGraphEdge) {
				elements.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return elements;
	}

	public synchronized void addViewPanel(JComponent panel, String title, String tab, boolean open) {
		if (tab.equals("Info")) {
			this.addInfo(title, panel);
		} else {
			JPanel tabPanel;
			if (this.inspectorTabs.containsKey(tab))
				tabPanel = this.inspectorTabs.get(tab);
			else {
				tabPanel = this.inspector.addTab(tab);
				this.inspectorTabs.put(tab, tabPanel);
			}
			this.inspector.addGroup(tabPanel, title, panel, open);
		}
	}

	public void addInteractionViewports(final PIPPanel pip, final ZoomPanel zoom) {
		this.scroll.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent arg0) {
			}
			public void componentMoved(ComponentEvent arg0) {
			}
			public void componentResized(ComponentEvent arg0) {
				if (arg0.getComponent().isValid()) {
					Dimension size = arg0.getComponent().getSize();
					int width = 250, height = 250;
					if (size.getWidth() > size.getHeight())
						height *= size.getHeight() / size.getWidth();
					else
						width *= size.getWidth() / size.getHeight();
					pip.setPreferredSize(new Dimension(width, height));
					pip.initializeImage();
					zoom.computeFitScale();
				}
			}

			public void componentShown(ComponentEvent arg0) {
			}

		});
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches < 0) {
			getScalable().setScale(getScale() + .1D);
		} else {
			getScalable().setScale(getScale() - .1D);
		}
	}
	
	public void setupDefaultPanels() {
		PIPPanel pip = new PIPPanel(this);
		this.addViewPanel(pip, "PIP", "View", true);
		
		ZoomPanel zoom = new ZoomPanel(this, AbstractGraphView.MAX_ZOOM);
		zoom.setHeight(200);
		this.addViewPanel(zoom, "Zoom", "View", true);
		
		ExportPanel export = new ExportPanel(this);
		this.addViewPanel(export, "Options", "Info", false);
		
		this.addInteractionViewports(pip, zoom);
	}

}
