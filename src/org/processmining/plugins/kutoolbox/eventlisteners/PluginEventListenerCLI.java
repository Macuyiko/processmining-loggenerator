package org.processmining.plugins.kutoolbox.eventlisteners;

public class PluginEventListenerCLI implements PluginEventListener {

	int min, max;
	
	public PluginEventListenerCLI() {
		min = 0;
		max = 0;
	}

	@Override
	public void cancel() {
		System.out.println("*** Operation cancelled ***");
	}

	@Override
	public boolean shouldCancel() {
		return false;
	}

	@Override
	public void logMessage(String caption) {
		logMessage("*", caption);
	}

	@Override
	public void logMessage(String eventName, String caption) {
		System.out.println("[" + eventName + "] " + caption);
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
	}

	@Override
	public void setProgress(int progress) {
		if (progress % 10 == 0)
			System.out.println(" [progress] " + min + " / " + progress + " / " + max);
	}

	@Override
	public void setProgressMax(int max) {
		this.max = max;
	}

	@Override
	public void setProgressMin(int min) {
		this.min = min;
	}
}
