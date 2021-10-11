package org.processmining.plugins.kutoolbox.logging;

public class SimpleDebugLogger implements DebugLogger {
	private boolean showDebug = true;
	private String standardHeader = "DebugLogger";

	public final void debug(String message) {
		debug(standardHeader, message);
	}

	public final void debug(String header, String message) {
		if (showDebug)
			System.out.print(" --DEBUG-- [" + header + "] " + message);
	}
	
	public final void debugln(String message) {
		debugln(standardHeader, message);
	}

	public final void debugln(String header, String message) {
		if (showDebug)
			debug(header, message+"\n");
	}

	public boolean isShowDebug() {
		return showDebug;
	}

	public void setShowDebug(boolean showDebug) {
		this.showDebug = showDebug;
	}

	public String getStandardHeader() {
		return standardHeader;
	}

	public void setStandardHeader(String standardHeader) {
		this.standardHeader = standardHeader;
	}
}
