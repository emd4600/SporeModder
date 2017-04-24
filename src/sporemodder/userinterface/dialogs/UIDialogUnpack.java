package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import sporemodder.MainApp;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.dbpf.DBPFUnpacker;
import sporemodder.files.formats.dbpf.DBPFUnpackingTask;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.rast.RastToDDS;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.FieldFileDrop;
import sporemodder.userinterface.settings.project.UIProjectSettings;
import sporemodder.utilities.Project;

public class UIDialogUnpack extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -402964385516973867L;
	
	private static final FileNameExtensionFilter FILTER_DBPF = new FileNameExtensionFilter("Database Packed File (*.package, *.db, *.dat, *.pkp, *.pkt, *.pld)",
			"package", "db", "dat", "pkp", "pkt", "pld");

	private JPanel mainPanel;
	
	private JPanel inputPanel;
	private JLabel lblInput;
	private JTextField tfInput;
	private JButton btnFindInput;
	
	private JLabel lblProject;
	private JComboBox<String> comboBoxProject;
	private JButton btnProjectSettings;
	
	private JPanel optionsPanel;
	private JCheckBox cbConvertPROP;
	private JCheckBox cbConvertRW4;
	private JCheckBox cbConvertTLSA;
	private JCheckBox cbConvertSPUI;
	private JCheckBox cbConvertPCTP;
	private JCheckBox cbConvertRAST;
	private JCheckBox cbConvertEffects;
	
//	private JPanel panelSPUI;
//	private JCheckBox cbSPUIFlipBlocks;
	
	private JPanel panelPROP;
	private JCheckBox cbPROPDebugMode;
	
	private JPanel buttonsPanel;
	private JButton btnUnpack;
	private JButton btnCancel;
	
	private Project project = new Project();
	
	private static final ConvertAction[] availableConverters = new ConvertAction[] {
			new PropToXml(),
			new Rw4ToDDS(),
			new TlsaToTxt(),
			new PctpToTxt(),
			new SpuiToTxt(),
			new RastToDDS(),
			new EffectUnpacker()
	};
	
	public UIDialogUnpack() {
		super(MainApp.getUserInterface());
		
		MainApp.processAutoSave();
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Unpack DBPF");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		getContentPane().setLayout(new BorderLayout());
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		///////// INPUT PANEL /////////
		
		inputPanel = new JPanel();
		inputPanel.setLayout(new FlowLayout());
		inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		lblInput = new JLabel("Input file: ");
		
		tfInput = new JTextField();
		tfInput.setColumns(50);
		tfInput.getDocument().addDocumentListener(new DLTextFieldInput());
		tfInput.setDropTarget(new FieldFileDrop(tfInput));
		
		btnFindInput = new JButton("Find file");
		btnFindInput.addActionListener(new AdvancedFileChooser(tfInput, this, JFileChooser.FILES_ONLY, false, "untitled.package", ChooserType.OPEN, FILTER_DBPF));
		
		inputPanel.add(lblInput);
		inputPanel.add(tfInput);
		inputPanel.add(btnFindInput);
		
		///////////////////////////////
		
		JPanel projectPanel = new JPanel();
		projectPanel.setLayout(new BorderLayout());
		projectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		projectPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
		
		lblProject = new JLabel("Project: ");
		comboBoxProject = new JComboBox<String>(getProjectNames());
		comboBoxProject.setEditable(true);
		comboBoxProject.setSelectedItem("");
		comboBoxProject.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (((String) comboBoxProject.getSelectedItem()).length() > 0) {
					btnProjectSettings.setEnabled(true);
					btnUnpack.setEnabled(tfInput.getText().length() > 0);
				} else {
					btnProjectSettings.setEnabled(false);
					btnUnpack.setEnabled(false);
				}
			}
		});
		final JTextComponent tc = (JTextComponent) comboBoxProject.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(new DocumentListener() {
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
				if (tc.getText().length() > 0) {
					btnProjectSettings.setEnabled(true);
					btnUnpack.setEnabled(tfInput.getText().length() > 0);
				} else {
					btnProjectSettings.setEnabled(false);
					btnUnpack.setEnabled(false);
				}
			}
		});
		
		ImageIcon iconConfig = createImageIcon("/sporemodder/userinterface/images/config_20.png", "Save");
		btnProjectSettings = new JButton(iconConfig);
		// btnProjectSettings.setText("Project Settings");
		btnProjectSettings.setAlignmentX(Component.LEFT_ALIGNMENT);
		btnProjectSettings.setEnabled(false);
		btnProjectSettings.addActionListener(new ALProjectSettings());
		
		projectPanel.add(lblProject, BorderLayout.WEST);
		projectPanel.add(comboBoxProject, BorderLayout.CENTER);
		projectPanel.add(btnProjectSettings, BorderLayout.EAST);
		
		///////////////////////////////
		
		///////// ADVANCED OPTIONS /////////
		
		optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridLayout(3, 3));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Advanced Options"));
		optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cbConvertPROP = new JCheckBox("Convert PROP files");
		cbConvertPROP.setSelected(DBPFUnpacker.CONVERT_PROP);
		
		cbConvertRW4 = new JCheckBox("Convert RW4 textures");
		cbConvertRW4.setSelected(DBPFUnpacker.CONVERT_RW4);
		
		cbConvertTLSA = new JCheckBox("Convert TLSA files");
		cbConvertTLSA.setSelected(DBPFUnpacker.CONVERT_TLSA);
		
		cbConvertPCTP = new JCheckBox("Convert PCTP files");
		cbConvertPCTP.setSelected(DBPFUnpacker.CONVERT_PCTP);
		
		cbConvertSPUI = new JCheckBox("Convert SPUI files");
		cbConvertSPUI.setSelected(DBPFUnpacker.CONVERT_SPUI);
		
		cbConvertRAST = new JCheckBox("Convert RAST files");
		cbConvertRAST.setSelected(DBPFUnpacker.CONVERT_RAST);
		
		cbConvertEffects = new JCheckBox("Convert Effects");
		cbConvertEffects.setSelected(DBPFUnpacker.CONVERT_EFFECTS);
		
		optionsPanel.add(cbConvertPROP);
		optionsPanel.add(cbConvertRW4);
		optionsPanel.add(cbConvertTLSA);
		optionsPanel.add(cbConvertPCTP);
		optionsPanel.add(cbConvertSPUI);
		optionsPanel.add(cbConvertRAST);
		optionsPanel.add(cbConvertEffects);
		
		panelPROP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelPROP.setBorder(BorderFactory.createTitledBorder("Advanced options - PROP"));
		panelPROP.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbPROPDebugMode = new JCheckBox("Debug mode");
		cbPROPDebugMode.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelPROP.add(cbPROPDebugMode);
		panelPROP.setVisible(cbConvertPROP.isSelected());
		cbConvertPROP.addItemListener(new ILConverterCheckBox(panelPROP));
		
		for (int i = 0; i < availableConverters.length; i++) {
			JPanel panel = availableConverters[i].createOptionsPanel();
			if (panel != null) {
				
			}
		}

		
		////////////////////////////////////
		
		mainPanel.add(inputPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(projectPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(optionsPanel);
		
		addOptionsPanel(mainPanel, cbConvertPROP, getConverterByClass(PropToXml.class), "Advanced Options - PROP");
		addOptionsPanel(mainPanel, cbConvertRW4, getConverterByClass(Rw4ToDDS.class), "Advanced Options - RW4");
		addOptionsPanel(mainPanel, cbConvertTLSA, getConverterByClass(TlsaToTxt.class), "Advanced Options - TLSA");
		addOptionsPanel(mainPanel, cbConvertPCTP, getConverterByClass(PctpToTxt.class), "Advanced Options - PCTP");
		addOptionsPanel(mainPanel, cbConvertSPUI, getConverterByClass(SpuiToTxt.class), "Advanced Options - SPUI");
		addOptionsPanel(mainPanel, cbConvertRAST, getConverterByClass(RastToDDS.class), "Advanced Options - RAST");
		addOptionsPanel(mainPanel, cbConvertEffects, getConverterByClass(EffectUnpacker.class), "Advanced Options - Effects");
		
		///////// BUTTONS PANEL /////////
		
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		btnUnpack = new JButton("Unpack");
		btnUnpack.setEnabled(false);
		btnUnpack.addActionListener(new ALUnpack());
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ALCancel());
		
		buttonsPanel.add(btnUnpack);
		buttonsPanel.add(btnCancel);
		
		/////////////////////////////////
		
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void addOptionsPanel(JPanel parentPanel, JCheckBox checkBox, ConvertAction action, String title) {
		JPanel panel = action.createOptionsPanel();
		
		if (panel != null) {
			panel.setBorder(BorderFactory.createTitledBorder(title));
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);

			panel.setVisible(checkBox.isSelected());
			checkBox.addItemListener(new ILConverterCheckBox(panel));
			
			parentPanel.add(panel);
		}
	}
	
	private class DLTextFieldInput implements DocumentListener {

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
			File file = new File(tfInput.getText());
			
			if (file.exists()) {
				// remove extension
				String name = file.getName();
				name = name.substring(0, name.indexOf("."));
				
				comboBoxProject.setSelectedItem(name);
				
//				btnProjectSettings.setEnabled(true);
//				btnUnpack.setEnabled(true);
			}
		}
	}
	
	private class ALProjectSettings implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
//			int result = JOptionPane.YES_OPTION;
//			if (MainApp.checkProjectName(project.getProjectName())) {
//				result = JOptionPane.showOptionDialog(UIDialogUnpack.this, "A project with this name already exists. Do you want to overwrite it or to create a new project?", 
//						"Project name conflict", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, 
//						new String[] {"Overwrite", "Create new"}, "Overwrite");
//			}
//			
//			if (result == JOptionPane.NO_OPTION) {
//				// 'Create new', get the first available name
//				project.setProjectName(MainApp.getNewProjectName());
//			}
			
			String name = (String) comboBoxProject.getSelectedItem();
			Project tempProject = MainApp.getProjectByName(name);
			if (tempProject != null) {
				project = tempProject;
				new UIProjectSettings(project, UIProjectSettings.SettingsMode.NONE);
			}
			else {
				if (!MainApp.projectExists(project)) {
					project.setProjectName(name);
				} else {
					project = new Project(name);
				}
				new UIProjectSettings(project, UIProjectSettings.SettingsMode.UNPACK);
			}
			
			comboBoxProject.setSelectedItem(project.getProjectName());
		}
	}
	
	private class ALUnpack implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			int result = JOptionPane.OK_OPTION;
			String name = (String) comboBoxProject.getSelectedItem();
			Project tempProject = MainApp.getProjectByName(name);
			if (tempProject != null) {
				project = tempProject;
			}
			else {
				if (!MainApp.projectExists(project)) {
					project.setProjectName(name);
				} else {
					project = new Project(name);
				}
			}
			
			//TODO check package name too?
			if (MainApp.checkProjectName(name)) {
				result = JOptionPane.showConfirmDialog(UIDialogUnpack.this, "A project with this name already exists. All files associated with "
						+ "the existing project will be erased. Are you sure you want to overwrite the existing project?", 
						"Project name conflict", JOptionPane.OK_CANCEL_OPTION);
			}
			
			if (result == JOptionPane.OK_OPTION) {
				project.createNewProject();
				
				List<ConvertAction> converters = new ArrayList<ConvertAction>();
				
				if (cbConvertPROP.isSelected()) converters.add(getConverterByClass(PropToXml.class));
				if (cbConvertRW4.isSelected()) converters.add(getConverterByClass(Rw4ToDDS.class));
				if (cbConvertTLSA.isSelected()) converters.add(getConverterByClass(TlsaToTxt.class));
				if (cbConvertPCTP.isSelected()) converters.add(getConverterByClass(PctpToTxt.class));
				if (cbConvertSPUI.isSelected()) converters.add(getConverterByClass(SpuiToTxt.class));
				if (cbConvertRAST.isSelected()) converters.add(getConverterByClass(RastToDDS.class));
				if (cbConvertEffects.isSelected()) converters.add(getConverterByClass(EffectUnpacker.class));
				//TODO GAIT
				
				
				new UnpackDialog(converters);
				
				MainApp.setCurrentProject(project);
				
				project.writeProperties();
				
				dispose();
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends ConvertAction> T getConverterByClass(Class<T> clazz) {
		for (ConvertAction converter : availableConverters) {
			if (clazz.isInstance(converter)) {
				return (T) converter;
			}
		}
		return null;
	}
	
	private class ALCancel implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dispose();
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	private class UnpackDialog extends JDialog {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4448695414236984622L;
		
		private JProgressBar progressBar;
		private JLabel lblExtractedFiles;
		private JLabel lblErrorsFound;
		
		private DBPFUnpackingTask task;
		
		private UnpackDialog(List<ConvertAction> converters) {
			super(MainApp.getUserInterface());
			
			setModalityType(ModalityType.TOOLKIT_MODAL);
			setTitle("Unpacking DBPF");
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setResizable(false);
			
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			//progressBar.setStringPainted(true);
			
			lblExtractedFiles = new JLabel();
			lblErrorsFound = new JLabel();
			
			getContentPane().add(progressBar);
			getContentPane().add(lblExtractedFiles);
			
			task = new DBPFUnpackingTask(tfInput.getText(), project, converters, this);
			task.addPropertyChangeListener(new TaskProgressListener());
			task.execute();
			
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
		
		private class TaskProgressListener implements PropertyChangeListener {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String name = event.getPropertyName();
				
				if (name.equals("extractedFiles")) {
					int newValue = (int) event.getNewValue();
					lblExtractedFiles.setText(newValue + "/" + task.getFileCount() + " files extracted");
				}
				else if (name.equals("errorCount")) {
					lblErrorsFound.setText(task.getErrorCount() + " errors found");
				}
				else if (name.equals("progress")) {
					progressBar.setValue(task.getProgress());
				}
			}
		}
		
	}
	
	
	private class ILConverterCheckBox implements ItemListener {
		
		private JPanel panel;
		
		public ILConverterCheckBox(JPanel panel) {
			this.panel = panel;
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			this.panel.setVisible(arg0.getStateChange() == ItemEvent.SELECTED);
			pack();
		}
	}
	
	
	private static String[] getProjectNames() {
		List<Project> list = MainApp.getProjects();
		String[] result = new String[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i).getProjectName();
		}
		
		return result;
	}
}
