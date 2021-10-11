package org.processmining.plugins.kutoolbox.utils;

public class FakeProgress implements org.processmining.framework.plugin.Progress {

	private boolean cancelled = false;

	public FakeProgress() {
	}

	@Override
	public void cancel() {
		cancelled = true;
	}

	@Override
	public String getCaption() {
		return "";
	}

	@Override
	public int getMaximum() {
		return 0;
	}

	@Override
	public int getMinimum() {
		return 0;
	}

	@Override
	public int getValue() {
		return 0;
	}

	@Override
	public void inc() {
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isIndeterminate() {
		return true;
	}

	@Override
	public void setCaption(String message) {
	}

	@Override
	public void setIndeterminate(boolean makeIndeterminate) {
	}

	@Override
	public void setMaximum(int value) {
	}

	@Override
	public void setMinimum(int value) {
	}

	@Override
	public void setValue(int value) {
	}
}
