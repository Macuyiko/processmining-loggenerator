package org.processmining.plugins.kutoolbox.eventlisteners;

public interface PluginEventListener {
	public void cancel();

	boolean shouldCancel();

	void logMessage(String caption);

	void logMessage(String eventName, String caption);

	void logMessage(String eventName, String caption, int progress);

	void setProgressBounds(int min, int max);

	void setProgressMax(int max);

	void setProgressMin(int min);

	void setProgress(int progress);
}
