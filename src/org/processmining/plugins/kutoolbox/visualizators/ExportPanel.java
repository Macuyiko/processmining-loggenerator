package org.processmining.plugins.kutoolbox.visualizators;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import com.fluxicon.slickerbox.components.SlickerButton;

public class ExportPanel extends JPanel {

	private static final long serialVersionUID = 7153768335241741777L;

	private SlickerButton exportButton;
	
	protected final AbstractGraphView mainView;

	public ExportPanel(AbstractGraphView view){
		
		this.mainView = view;
		
		double size[][] = { { 10, TableLayoutConstants.FILL, 10 }, { 10, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 10 } };
		setLayout(new TableLayout(size));
		
		exportButton = new SlickerButton("Export view...");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		this.add(exportButton, "1, 1");
		/*
		resetButton = new SlickerButton("Recalculate view...");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		this.add(resetButton, "1, 2");
		*/
		this.setPreferredSize(new Dimension(100,100));
	}
	/*
	private void reset() {
		ProMJGraph jgraph = (ProMJGraph) this.mainView.getScalable();
		DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> graph = jgraph.getProMGraph();
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(false);
		layout.setCompactLayout(false);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);
		layout.setOrientation(map.get(graph, AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

		JGraphFacade facade = new JGraphFacade(jgraph);
		facade.setOrdered(false);
		facade.setEdgePromotion(true);
		facade.setIgnoresCellsInGroups(false);
		facade.setIgnoresHiddenCells(false);
		facade.setIgnoresUnconnectedCells(false);
		facade.setDirected(true);
		facade.resetControlPoints();
		facade.run(layout, true);
	
		jgraph.setUpdateLayout(layout);
	}
	*/
	private void export() {
		//ExportDialog export = new ExportDialog();
		//export.showExportDialog(this, "Export view as ...", this.mainView.getScalable().getComponent(), "View");
	}
}
