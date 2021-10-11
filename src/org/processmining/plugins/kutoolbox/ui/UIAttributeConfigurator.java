package org.processmining.plugins.kutoolbox.ui;

import java.util.Map;

public interface UIAttributeConfigurator {
	public String getTitle();
	public void resetSettings();
	public void setSettings(Map<String, Object> settings);
	public Map<String, Object> getSettings();
}
