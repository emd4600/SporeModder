package sporemodder.userinterface.settings.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sporemodder.MainApp;
import sporemodder.userinterface.UIList;
import sporemodder.userinterface.UIList.ElementOptions;
import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.UISourcePreset;
import sporemodder.userinterface.settings.project.UIProjectSettings.SettingsMode;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.Project;
import sporemodder.utilities.SporeGame;
import sporemodder.utilities.Project.GamePathType;

public class UIGeneralSettings extends SettingsCategory {
	
	private JPanel contentPanel;
	
	private JLabel lblName;
	private JTextField tfName;
	private JLabel lblNameError;
	
	private JLabel lblSources;
	private UIList listSources;
	private JButton btnSourcePreset;
	
	private JLabel lblPackPath;
	private ButtonGroup bgPackPath;
	private JRadioButton rbPackPathSpore;
	private JRadioButton rbPackPathGA;
	private JRadioButton rbPackPathCustom;
	private JTextField tfAlternativePackPath;
	private JButton btnPackPathFind;
	
	private boolean defaultPackageName;
	private JLabel lblPackageName;
	private JTextField tfPackageName;
	private JLabel lblPackageNameError;
	
	public UIGeneralSettings(Project project, SettingsMode mode) {
		super(project, mode);
	}

	@Override
	public String getName() {
		return "General Settings";
	}
	
	@Override
	public JPanel createPanel(JDialog parent) {
		
		defaultPackageName = project.useDefaultPackageName();
		
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
		
		//// NAME ////
		
		JPanel namePanel = new JPanel(new BorderLayout());
		lblName = new JLabel("Name: ");
		tfName = new JTextField(project.getProjectName());
		tfName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				tfNameChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				tfNameChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				tfNameChanged();
			}
		});
		lblNameError = new JLabel(" ");
		lblNameError.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		namePanel.add(lblName, BorderLayout.WEST);
		namePanel.add(tfName, BorderLayout.CENTER);
		namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPanel.add(namePanel);
		contentPanel.add(lblNameError);
		
		//////////////
		
		contentPanel.add(Box.createVerticalStrut(10));
		
		//// SOURCES ////
		lblSources = new JLabel("Sources: ");
		lblSources.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		listSources = new UIList(new SourcesElementOptions(), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnSourcePropertiesActivated();
			}
		});
		listSources.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		List<Project> sources = project.getSources();
		for (int i = 0; i < sources.size(); i++) {
			listSources.addElement(sources.get(i).getProjectName());
			if (i ==  sources.size()-1) {
				listSources.setSelectedElement(sources.get(i).getProjectName());
			}
		}
		
		btnSourcePreset = new JButton("Add Preset");
		btnSourcePreset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = UISourcePreset.getPresetsMenu(contentPanel, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						List<String> list = MainApp.getSourcePresets().get(arg0.getActionCommand());
						if (list != null && list.size() > 0) {
							for (String s : list) {
								if (MainApp.getProjectByName(s) != null) {
									listSources.addElement(s);
								}
							}
							listSources.setSelectedElement(list.get(list.size() - 1));
						}
					}
				});
				menu.show(btnSourcePreset, 0, 0);
			}
			
		});
		
		//TODO Add presets
		
		contentPanel.add(lblSources);
		contentPanel.add(listSources);
		contentPanel.add(btnSourcePreset);
		/////////////////
		
		contentPanel.add(Box.createVerticalStrut(10));
		
		//// PACK PATH ////
		
		JPanel customPackPathPanel = new JPanel(new BorderLayout());
		customPackPathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tfAlternativePackPath = new JTextField(project.getCustomPackPath());
		tfAlternativePackPath.setEnabled(project.getPackPathType() == GamePathType.CUSTOM);
		btnPackPathFind = new JButton("Find");
		btnPackPathFind.setEnabled(project.getPackPathType() == GamePathType.CUSTOM);
		btnPackPathFind.addActionListener(new AdvancedFileChooser(tfAlternativePackPath, contentPanel, JFileChooser.DIRECTORIES_ONLY, false, ChooserType.SAVE));
		
		customPackPathPanel.add(tfAlternativePackPath, BorderLayout.CENTER);
		customPackPathPanel.add(btnPackPathFind, BorderLayout.EAST);
		
		lblPackPath = new JLabel("Pack path: ");
		lblPackPath.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel packPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		rbPackPathCustom = new JRadioButton("Custom");
		rbPackPathCustom.setActionCommand("custom");
		rbPackPathCustom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIProjectSettings.enableComponents(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind);
			}
		});
		
		rbPackPathSpore = new JRadioButton("Spore Data");
		rbPackPathSpore.setEnabled(SporeGame.hasSpore());
		rbPackPathSpore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIProjectSettings.enableComponents(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind);
			}
		});
		rbPackPathSpore.setActionCommand("spore");
		
		rbPackPathGA = new JRadioButton("Galactic Adventures Data");
		rbPackPathGA.setEnabled(SporeGame.hasGalacticAdventures());
		rbPackPathGA.setActionCommand("ga");
		rbPackPathGA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UIProjectSettings.enableComponents(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind);
			}
		});
		
		
		switch(project.getPackPathType()) {
		case GALACTIC_ADVENTURES:
			rbPackPathGA.setSelected(true);
			break;
		case SPORE:
			rbPackPathSpore.setSelected(true);
			break;
		case CUSTOM:
			rbPackPathCustom.setSelected(true);
			break;
		default:
			rbPackPathCustom.setSelected(true);
			break;	
		}
		
		bgPackPath = new ButtonGroup();
		bgPackPath.add(rbPackPathSpore);
		bgPackPath.add(rbPackPathGA);
		bgPackPath.add(rbPackPathCustom);
		
		packPathPanel.add(rbPackPathSpore);
		packPathPanel.add(rbPackPathGA);
		packPathPanel.add(rbPackPathCustom);
		packPathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		contentPanel.add(lblPackPath);
		contentPanel.add(packPathPanel);
		contentPanel.add(customPackPathPanel);
		///////////////////
		
		contentPanel.add(Box.createVerticalStrut(10));
		
		//// PACKAGE NAME ////
		lblPackageName = new JLabel("Package name: ");
		lblPackageName.setAlignmentX(Component.LEFT_ALIGNMENT);
		tfPackageName = new JTextField(project.getPackageName());
		tfPackageName.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				tfPackageNameChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				tfPackageNameChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				tfPackageNameChanged();
			}
		});
		tfPackageName.setAlignmentX(Component.LEFT_ALIGNMENT);
		lblPackageNameError = new JLabel(" ");
		lblPackageNameError.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		contentPanel.add(lblPackageName);
		contentPanel.add(tfPackageName);
		contentPanel.add(lblPackageNameError);
		//////////////////////
		
		return contentPanel;
	}

	private void tfNameChanged() {
		if (MainApp.checkProjectName(tfName.getText())) {
			lblNameError.setText("This project name is already in use!");
		} else {
			lblNameError.setText(" ");
		}
		if (defaultPackageName) {
			tfPackageName.setText(Project.getDefaultPackageName(tfName.getText()));
		}
	}
	
	private void tfPackageNameChanged() {
		if (MainApp.checkProjectPackageName(tfPackageName.getText())) {
			lblPackageNameError.setText("This package name is already in use by another project!");
		} else {
			lblPackageNameError.setText(" ");
		}
		defaultPackageName = tfPackageName.getText().equals(Project.getDefaultPackageName(tfName.getText()));
	}
	
	private void btnSourcePropertiesActivated() {
		Project source = MainApp.getProjectByName(listSources.getSelectedElement()); 
		new UIProjectSettings(source, SettingsMode.NONE);
	}
	
	private class SourcesElementOptions implements ElementOptions {
		@Override
		public String[] getOptions(boolean isAdded) {
			List<Project> projects = MainApp.getProjects();
			List<String> sources = listSources.getElements();
			String selectedSource = listSources.getSelectedElement();
			
			// All the projects except the ones already put in the list and the current project; the current value is displayed, however
			List<String> options = new ArrayList<String>();
			
			for (Project p : projects) {
				String name = p.getProjectName();
				boolean cond = false;
				if (!isAdded) {
					cond = name.equals(selectedSource);
				}
				if ((sources.contains(name) && !cond) || name.equals(project.getProjectName())) {
					continue;
				}
				options.add(name);
			}
			
			return (String[]) options.toArray(new String[options.size()]);
		}
	}
	
	
	@Override
	public void saveSettings() {
		if (!tfName.getText().equals(project.getProjectName()) && MainApp.checkProjectName(tfName.getText())) {
				JOptionPane.showMessageDialog(contentPanel, "This project name is already in use!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
		} 
		else if (!tfPackageName.getText().equals(project.getPackageName()) && MainApp.checkProjectPackageName(tfPackageName.getText())) {
			JOptionPane.showMessageDialog(contentPanel, "This project package name is already in use!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (!tfName.getText().equals(project.getProjectName())) {
			if (mode == SettingsMode.NEW) {
				project.setProjectName(tfName.getText());
			} else {
				project.rename(tfName.getText());
			}
		}
		
		List<String> sourceNames = listSources.getElements();
		project.setSources(sourceNames);
		
		GamePathType currentPackPathType = GamePathType.CUSTOM;
		if (rbPackPathSpore.isSelected()) currentPackPathType = GamePathType.SPORE;
		else if (rbPackPathGA.isSelected()) currentPackPathType = GamePathType.GALACTIC_ADVENTURES;
		
		project.setPackPath(currentPackPathType, rbPackPathCustom.isSelected() ? tfAlternativePackPath.getText() : null);
		
		// if the name there is not the default package name
		if (!tfPackageName.getText().equals(Project.getDefaultPackageName(project.getProjectName()))) {
			project.setPackageName(tfPackageName.getText());
		}
	}
}
