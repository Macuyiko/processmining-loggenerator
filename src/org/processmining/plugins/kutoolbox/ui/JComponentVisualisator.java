package org.processmining.plugins.kutoolbox.ui;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "JComponent Visualization", 
	returnLabels = { "Visualization" },
	returnTypes = { JComponent.class }, 
	parameterLabels = { "JComponent" }, 
	userAccessible = true)
@Visualizer

public class JComponentVisualisator {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(final PluginContext context, final JComponent outputFrame) {
		return outputFrame;
	}
}
