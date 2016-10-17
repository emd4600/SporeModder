package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.userinterface.UIList;
import sporemodder.userinterface.UIList.ElementOptions;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.Project;
import sporemodder.utilities.Project.GamePathType;
import sporemodder.utilities.SporeGame;

public class UIProjectSettings extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5600026923057068830L;
	
	public static enum Mode { NONE, NEW, UNPACK }
	
	private static final FileNameExtensionFilter FILEFILTER_EXE = new FileNameExtensionFilter("Executable (*.exe)", "exe");
	
	private Project project;
	
	private JTabbedPane tabbedPane;
	private JPanel contentPanel;
	
	private JLabel lblName;
	private JTextField tfName;
	private JLabel lblNameError;
	
	private JLabel lblSources;
	private UIList listSources;
	private JButton btnSourcePreset;
	
	private JLabel lblPackPath;
//	private GamePathType currentPackPathType;
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
	
	private JPanel packingPanel;
	private JCheckBox cbConvertPROP;
	private JCheckBox cbConvertRW4;
	private JCheckBox cbConvertSPUI;
	private JCheckBox cbConvertTLSA;
	private JCheckBox cbConvertPCTP;
	private JCheckBox cbConvertRAST;
	//TODO GAIT
	private JCheckBox cbConvertEffects;
	private JCheckBox cbCompression;
	private JSpinner spinnerCompression;
	
	private JPanel buttonsPanel;
	private JButton btnOkay;
	private JButton btnCancel;
	
	
	private JPanel debugPanel;
	private JLabel lblGamePath;
	private JCheckBox cbDefaultGamePath;
	private ButtonGroup bgGamePath;
	private JRadioButton rbGamePathSpore;
	private JRadioButton rbGamePathGA;
	private JRadioButton rbGamePathCustom;
	private JTextField tfGamePath;
	private JButton btnGamePath;
	private JLabel lblCommand;
	private JTextField tfGameCommandLine;
	
//	private boolean newProject;
//	// unpacked projects can override existing projects, just for convenience...
//	private boolean unpackedProject;
	private Mode mode;
	
	public UIProjectSettings(Project project, Mode mode) {
		super(MainApp.getUserInterface());
		
//		this.newProject = newProject;
//		this.unpackedProject = unpackedProject;
		
		this.mode = mode;
		
		if (project == null) throw new UnsupportedOperationException("ProjectSettings cannot be built from a null Project.");
		
		this.project = project;
		defaultPackageName = project.useDefaultPackageName();
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Project Settings (" + project.getProjectName() + ")");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		//// OPTIONS PANEL ////
		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnOkay = new JButton("Accept");
		btnOkay.addActionListener(new ALOkay());
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ALCancel());
		
		buttonsPanel.add(btnOkay);
		buttonsPanel.add(btnCancel);
		///////////////////////
			
		{
			contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
			
			//// NAME ////
			JPanel namePanel = new JPanel(new BorderLayout());
			lblName = new JLabel("Name: ");
			tfName = new JTextField(project.getProjectName());
			tfName.getDocument().addDocumentListener(new DLTextFieldName());
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
			
			listSources = new UIList(new SourcesElementOptions(), new ALSourceProperties());
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
					JPopupMenu menu = UISourcePreset.getPresetsMenu(UIProjectSettings.this, new ActionListener() {
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
			btnPackPathFind.addActionListener(new AdvancedFileChooser(tfAlternativePackPath, this, JFileChooser.DIRECTORIES_ONLY, false, ChooserType.SAVE));
			
			customPackPathPanel.add(tfAlternativePackPath, BorderLayout.CENTER);
			customPackPathPanel.add(btnPackPathFind, BorderLayout.EAST);
			
			lblPackPath = new JLabel("Pack path: ");
			lblPackPath.setAlignmentX(Component.LEFT_ALIGNMENT);
			JPanel packPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			rbPackPathCustom = new JRadioButton("Custom");
			rbPackPathCustom.setActionCommand("custom");
			rbPackPathCustom.addActionListener(new ALGamePathButton(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind));
			rbPackPathSpore = new JRadioButton("Spore Data");
			rbPackPathSpore.setEnabled(SporeGame.hasSpore());
			rbPackPathSpore.addActionListener(new ALGamePathButton(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind));
			rbPackPathSpore.setActionCommand("spore");
			rbPackPathGA = new JRadioButton("Galactic Adventures Data");
			rbPackPathGA.setEnabled(SporeGame.hasGalacticAdventures());
			rbPackPathGA.setActionCommand("ga");
			rbPackPathGA.addActionListener(new ALGamePathButton(rbPackPathCustom, tfAlternativePackPath, btnPackPathFind));
			
			
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
			tfPackageName.getDocument().addDocumentListener(new DLTextFieldPackageName());
			tfPackageName.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblPackageNameError = new JLabel(" ");
			lblPackageNameError.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			contentPanel.add(lblPackageName);
			contentPanel.add(tfPackageName);
			contentPanel.add(lblPackageNameError);
			//////////////////////
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			containerPanel.add(contentPanel, BorderLayout.NORTH);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab("General settings", containerPanel);
		}
		
		{
			packingPanel = new JPanel();
			packingPanel.setLayout(new BoxLayout(packingPanel, BoxLayout.Y_AXIS));
			packingPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
			
			JLabel label = new JLabel("Convert files when packing: ");
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
			packingPanel.add(label);
			
			{
				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout(4, 2));
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				cbConvertPROP = new JCheckBox("Convert PROP files", project.isConvertPROP());
				cbConvertRW4 = new JCheckBox("Convert RW4 files", project.isConvertRW4());
				cbConvertSPUI = new JCheckBox("Convert SPUI files", project.isConvertSPUI());
				cbConvertTLSA = new JCheckBox("Convert TLSA files", project.isConvertTLSA());
				cbConvertPCTP = new JCheckBox("Convert PCTP files", project.isConvertPCTP());
				cbConvertRAST = new JCheckBox("Convert RAST files", project.isConvertRAST());
				//TODO GAIT
				cbConvertEffects = new JCheckBox("Convert Effects", project.isConvertEffects());
				
				panel.add(cbConvertPROP);
				panel.add(cbConvertRW4);
				panel.add(cbConvertSPUI);
				panel.add(cbConvertTLSA);
				panel.add(cbConvertPCTP);
				panel.add(cbConvertRAST);
				//TODO GAIT
				panel.add(cbConvertEffects);
				
				packingPanel.add(panel);
			}
			
			{
				JPanel panel = new JPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				int limit = project.getCompressingLimit();
				cbCompression = new JCheckBox("Compress files larger than: ", limit != -1);
				cbCompression.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {
						spinnerCompression.setEnabled(arg0.getStateChange() == ItemEvent.SELECTED);
					}
				});
				
				SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
				spinnerModel.setMinimum(0);
				spinnerModel.setMaximum(Integer.MAX_VALUE);
				spinnerModel.setStepSize(1024);
				
				spinnerCompression = new JSpinner();
				spinnerCompression.setModel(spinnerModel);
				spinnerCompression.setEnabled(limit != -1);
				if (limit != -1) spinnerCompression.setValue(limit);
				
				panel.add(cbCompression);
				panel.add(spinnerCompression);
				
				packingPanel.add(panel);
			}
			
			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton btnOkay = new JButton("Accept");
			btnOkay.addActionListener(new ALOkay());
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ALCancel());
			buttonsPanel.add(btnOkay);
			buttonsPanel.add(btnCancel);
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			
			containerPanel.add(packingPanel, BorderLayout.NORTH);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab("Packing settings", containerPanel);
		}
		
		/* -- DEBUG PANEL -- */
		{
			debugPanel = new JPanel();
			debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
			debugPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
			
			final JPanel customGamePathPanel = new JPanel(new BorderLayout());
			customGamePathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			tfGamePath = new JTextField(project.getCustomGamePath());
			tfGamePath.setEnabled(project.getGamePathType() == GamePathType.CUSTOM);
			btnGamePath = new JButton("Find");
			btnGamePath.setEnabled(project.getGamePathType() == GamePathType.CUSTOM);
			btnGamePath.addActionListener(new AdvancedFileChooser(tfGamePath, this, JFileChooser.FILES_ONLY, false, ChooserType.OPEN, FILEFILTER_EXE));
			
			customGamePathPanel.add(tfGamePath, BorderLayout.CENTER);
			customGamePathPanel.add(btnGamePath, BorderLayout.EAST);
			
			lblGamePath = new JLabel("Game path: ");
			lblGamePath.setAlignmentX(Component.LEFT_ALIGNMENT);
			final JPanel gamePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			rbGamePathCustom = new JRadioButton("Custom");
			rbGamePathCustom.setActionCommand("custom");
			rbGamePathCustom.addActionListener(new ALGamePathButton(rbGamePathCustom, tfGamePath, btnGamePath));
			rbGamePathSpore = new JRadioButton("Spore");
			rbGamePathSpore.setEnabled(SporeGame.hasSpore());
			rbGamePathSpore.addActionListener(new ALGamePathButton(rbGamePathCustom, tfGamePath, btnGamePath));
			rbGamePathSpore.setActionCommand("spore");
			rbGamePathGA = new JRadioButton("Galactic Adventures");
			rbGamePathGA.setEnabled(SporeGame.hasGalacticAdventures());
			rbGamePathGA.setActionCommand("ga");
			rbGamePathGA.addActionListener(new ALGamePathButton(rbGamePathCustom, tfGamePath, btnGamePath));
			
			switch(project.getGamePathType()) {
			case GALACTIC_ADVENTURES:
				rbGamePathGA.setSelected(true);
				break;
			case SPORE:
				rbGamePathSpore.setSelected(true);
				break;
			case CUSTOM:
				rbGamePathCustom.setSelected(true);
				break;
			default:
				rbGamePathCustom.setSelected(true);
				break;	
			}
			
			bgGamePath = new ButtonGroup();
			bgGamePath.add(rbGamePathSpore);
			bgGamePath.add(rbGamePathGA);
			bgGamePath.add(rbGamePathCustom);
			
			gamePathPanel.add(rbGamePathSpore);
			gamePathPanel.add(rbGamePathGA);
			gamePathPanel.add(rbGamePathCustom);
			gamePathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			
			lblCommand = new JLabel("Command line arguments:");
			lblCommand.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			tfGameCommandLine = new JTextField(project.getGameCommandLine());
			tfGameCommandLine.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			cbDefaultGamePath = new JCheckBox("Use default debugging settings");
			cbDefaultGamePath.setSelected(project.isDefaultGamePath());
			updateDebuggingEnabled();
			cbDefaultGamePath.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateDebuggingEnabled();
				}
			});
			
			debugPanel.add(cbDefaultGamePath);
			debugPanel.add(lblGamePath);
			debugPanel.add(gamePathPanel);
			debugPanel.add(customGamePathPanel);
			debugPanel.add(lblCommand);
			debugPanel.add(tfGameCommandLine);
			
			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton btnOkay = new JButton("Accept");
			btnOkay.addActionListener(new ALOkay());
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ALCancel());
			buttonsPanel.add(btnOkay);
			buttonsPanel.add(btnCancel);
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			containerPanel.add(debugPanel, BorderLayout.NORTH);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab("Debugging settings", containerPanel);
		}
		
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
//	public UIProjectSettings(Project project, boolean newProject) {
//		this(project, newProject, MainApp.getUserInterface());
//	}
	
	private class ALGamePathButton implements ActionListener {
		private JRadioButton rbCustom;
		private Component[] components;
		private ALGamePathButton(JRadioButton rbCustom, Component ... components) {
			this.rbCustom = rbCustom;
			this.components = components;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (rbCustom != null && components != null) {
				boolean isSelected = rbCustom.isSelected();
				for (Component c : components) {
					c.setEnabled(isSelected);
				}
			}
		}
	}
	
	private class DLTextFieldName implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			update();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			update();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			update();
		}
		
		private void update() {
			if (MainApp.checkProjectName(tfName.getText())) {
				lblNameError.setText("This project name is already in use!");
			} else {
				lblNameError.setText(" ");
			}
			if (defaultPackageName) {
				tfPackageName.setText(Project.getDefaultPackageName(tfName.getText()));
			}
		}
	}
	
	private class DLTextFieldPackageName implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent arg0) {
			updateUseDefault();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			updateUseDefault();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			updateUseDefault();
		}
		
		private void updateUseDefault() {
			if (MainApp.checkProjectPackageName(tfPackageName.getText())) {
				lblPackageNameError.setText("This package name is already in use by another project!");
			} else {
				lblPackageNameError.setText(" ");
			}
			defaultPackageName = tfPackageName.getText().equals(Project.getDefaultPackageName(tfName.getText()));
		}
	}
	
	private class ALOkay implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (!tfName.getText().equals(project.getProjectName()) && MainApp.checkProjectName(tfName.getText())) {
//				if (mode == Mode.UNPACK) {
//					
//					int result = JOptionPane.showConfirmDialog(UIProjectSettings.this, "A project with this name already exists. Do you want to overwrite it?", 
//							"Project name conflict", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//					
//					if (result != JOptionPane.YES_OPTION) {
//						return;
//					}
//					
//				} else {
					JOptionPane.showMessageDialog(UIProjectSettings.this, "This project name is already in use!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
//				}
			} 
			else if (!tfPackageName.getText().equals(project.getPackageName()) && MainApp.checkProjectPackageName(tfPackageName.getText())) {
				JOptionPane.showMessageDialog(UIProjectSettings.this, "This project package name is already in use!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (!tfName.getText().equals(project.getProjectName())) {
				if (mode == Mode.NEW) {
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
			
			project.setPackingConverters(cbConvertPROP.isSelected(), cbConvertRW4.isSelected(), cbConvertTLSA.isSelected(), cbConvertPCTP.isSelected(), cbConvertSPUI.isSelected(),
					cbConvertRAST.isSelected(), cbConvertEffects.isSelected());
			project.setCompressingLimit(cbCompression.isSelected() ? (int)spinnerCompression.getValue() : -1);
			
			if (cbDefaultGamePath.isSelected()) {
				project.setDefaultGamePath(true);
			} else {
				project.setDefaultGamePath(false);
				GamePathType gamePathType = GamePathType.CUSTOM;
				if (rbGamePathSpore.isSelected()) gamePathType = GamePathType.SPORE;
				else if (rbGamePathGA.isSelected()) gamePathType = GamePathType.GALACTIC_ADVENTURES;
				project.setGamePathType(gamePathType);
				if (rbGamePathCustom.isSelected()) project.setGamePath(tfGamePath.getText());
				project.setGameCommandLine(tfGameCommandLine.getText());
			}
			
			// Unpacked projects must only be created when they are unpacked
			if (mode != Mode.UNPACK) {
				if (!MainApp.projectExists(project) || mode == Mode.NEW) {
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

	private class SourcesElementOptions implements ElementOptions {
		@Override
		public String[] getOptions(boolean isAdded) {
			List<Project> projects = MainApp.getProjects();
			List<String> sources = listSources.getElements();
			String selectedSource = listSources.getSelectedElement();
			
			// All the projects except the ones already put in the list and the current project; the current value is displayed, however
//			String[] options = new String[projects.size() - sources.size() - (isAdded ? 1 : 0)];
			List<String> options = new ArrayList<String>();
			
//			if (options.length > 0) {
//				int i = 0;
//				for (Project p : projects) {
//					String name = p.getProjectName();
//					boolean cond = false;
//					if (!isAdded) {
//						cond = name.equals(selectedSource);
//					}
//					if ((sources.contains(name) && !cond) || name.equals(project.getProjectName())) {
//						continue;
//					}
//					options[i++] = name;
//				}
//			}
			
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
	
	private class ALSourceProperties implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Project source = MainApp.getProjectByName(listSources.getSelectedElement()); 
			new UIProjectSettings(source, Mode.NONE);
		}
	}
	
	private void updateDebuggingEnabled() {
		boolean isSelected = !cbDefaultGamePath.isSelected();
		lblGamePath.setEnabled(isSelected);
		rbGamePathSpore.setEnabled(isSelected);
		rbGamePathGA.setEnabled(isSelected);
		rbGamePathCustom.setEnabled(isSelected);
		tfGamePath.setEnabled(isSelected && project.getGamePathType() == GamePathType.CUSTOM);
		btnGamePath.setEnabled(isSelected && project.getGamePathType() == GamePathType.CUSTOM);
		tfGameCommandLine.setEnabled(isSelected);
		lblCommand.setEnabled(isSelected);
	}
}
