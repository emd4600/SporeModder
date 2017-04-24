package sporemodder.userinterface.settings.project;

import javax.swing.JPanel;

import sporemodder.userinterface.settings.project.UIProjectSettings.SettingsMode;
import sporemodder.utilities.Project;

public abstract class SettingsCategory {
	
	protected Project project;
	protected SettingsMode mode;
	
	public SettingsCategory(Project project, SettingsMode mode) {
		this.project = project;
		this.mode = mode;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	public Project getProject() {
		return project;
	}
	
	public void setSettingsMode(SettingsMode mode) {
		this.mode = mode;
	}
	
	public SettingsMode getSettingsMode() {
		return mode;
	}
	
	public abstract String getName();
	
	public abstract JPanel createPanel();
	
	public abstract void saveSettings();
}
