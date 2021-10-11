package org.processmining.plugins.kutoolbox.logging;

public interface DebugLogger {
	public void debug(String message);

	public void debug(String header, String message);
	
	public void debugln(String message);

	public void debugln(String header, String message);
}
