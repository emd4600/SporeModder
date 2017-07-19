package sporemodder.userinterface.settings.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import sporemodder.MainApp;
import sporemodder.utilities.Project;

public class UIProjectSettings extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5600026923057068830L;
	
	public static enum SettingsMode { NONE, NEW, UNPACK }
	
	private Project project;
	private SettingsMode mode;
	
	private JTabbedPane tabbedPane;

	private final List<SettingsCategory> categories = new ArrayList<SettingsCategory>();
	
	
	public UIProjectSettings(Project project, SettingsMode mode) {
		super(MainApp.getUserInterface());
		
		this.mode = mode;
		this.project = project;
		
		if (project == null) throw new UnsupportedOperationException("ProjectSettings cannot be built from a null Project.");
		
		
		categories.add(new UIGeneralSettings(project, mode));
		categories.add(new UIPackingSettings(project, mode));
		categories.add(new UIDebugSettings(project, mode));
		
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Project Settings (" + project.getProjectName() + ")");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		for (SettingsCategory category : categories) {
			
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JButton btnOkay = new JButton("Accept");
			btnOkay.addActionListener(new ALOkay());
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ALCancel());
			
			buttonsPanel.add(btnOkay);
			buttonsPanel.add(btnCancel);
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			containerPanel.add(category.createPanel(this), BorderLayout.NORTH);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab(category.getName(), containerPanel);
		}
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	
	public static void enableComponents(JRadioButton radioButton, Component ... components) {
		if (radioButton != null && components != null) {
			boolean isSelected = radioButton.isSelected();
			for (Component c : components) {
				c.setEnabled(isSelected);
			}
		}
	}
	
	
	private class ALOkay implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			for (SettingsCategory category : categories) {
				category.saveSettings();
			}
			
			
			// Unpacked projects must only be created when they are unpacked
			if (mode != SettingsMode.UNPACK) {
				if (!MainApp.projectExists(project) || mode == SettingsMode.NEW) {
					project.createNewProject();
//					MainApp.setCurrentProject(project);
				}
				
				project.writeProperties();
			}
			
			//TODO only do this if the sources were updated?
			MainApp.setCurrentProject(project);
			
			dispose();
		}
	}
	
	private class ALCancel implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dispose();
		}
	}
	
}
