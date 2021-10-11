package org.processmining.plugins.kutoolbox.eventlisteners;

import org.processmining.framework.plugin.PluginContext;

public class PluginEventListenerContext implements PluginEventListener {

	private PluginContext context;
	
	public PluginEventListenerContext(PluginContext context) {
		this.context = context;
	}

	@Override
	public void cancel() {
		context.log("*** Cancelled ***");
		context.getProgress().setValue(Integer.MAX_VALUE);
	}

	@Override
	public boolean shouldCancel() {
		return context.getProgress().isCancelled();
	}

	@Override
	public void logMessage(String caption) {
		logMessage("*", caption);
	}

	@Override
	public void logMessage(String eventName, String caption) {
		context.getProgress().setCaption(caption);
		context.log(caption);
	}

	@Override
	public void logMessage(String eventName, String caption, int progress) {
		this.logMessage(eventName, caption);
		this.setProgress(progress);
	}

	@Override
	public void setProgressBounds(int min, int max) {
		this.setProgressMax(max);
		this.setProgressMin(min);
		context.getProgress().setValue(min);
	}

	@Override
	public void setProgress(int progress) {
		context.getProgress().setValue(progress);
	}

	@Override
	public void setProgressMax(int max) {
		context.getProgress().setMaximum(max);
	}

	@Override
	public void setProgressMin(int min) {
		context.getProgress().setMinimum(min);
	}
}
