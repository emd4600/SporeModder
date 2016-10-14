package sporemodder.userinterface.fileview;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import sporemodder.MainApp;

public abstract class FileView {
	protected String path;
	protected File file;
	
	protected int tabIndex;
	protected JTabbedPane parentTabbedPane;
	protected String name;
	
	public FileView(String path) {
		this.path = path;
		file = MainApp.getCurrentProject().getFile(path);
		name = file.getName();
	}
	
	public FileView(String path, File file) {
		this.path = path;
		this.file = file;
		name = file.getName();
	}
	
	public abstract JComponent getPanel();
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public void setParentTabbedPane(JTabbedPane parentTabbedPane) {
		this.parentTabbedPane = parentTabbedPane;
	}
	
	public JTabbedPane getParentTabbedPane() {
		return parentTabbedPane;
	}
	
	public void setTabIndex(int tabIndex) {
		//TODO update parent tabbed pane!!!
		this.tabIndex = tabIndex;
	}
	
	public int getTabIndex() {
		return tabIndex;
	}

	public abstract boolean isEditable();

	public abstract void setEditable(boolean editable);
	
	
	public abstract int getViewType();
}
