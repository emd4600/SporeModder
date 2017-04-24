package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileStructureError;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.InputOutputPaths;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class UIConvertDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4192713466752761123L;
	
	private String defaultInputExtension;
	private String defaultOutputExtension;
	private boolean removeExtension;
	private ConvertAction convertAction;
	
	private JDialog parent;
	
	private JPanel infoPanel;
	
	private JLabel lblInputFile;
	private JLabel lblOutputFile;
	
	private JTextField tfInputFile;
	private JTextField tfOutputFile;
	
	//private JButton btnFindInFile;
	//private JButton btnFindOutFile;
	
	private JPanel buttonsPanel;
	private JButton btnCancel;
	private JButton btnConvert;
	
//	private AdvancedFileChooser inputFileChooser;
//	private AdvancedFileChooser outputFileChooser;
	
	private int inputSelectionMode;
	private int outputSelectionMode;
	private boolean inputMultiSelectEnabled;
	private boolean outputMultiSelectEnabled;
	
	public UIConvertDialog(
			JDialog parent, FileNameExtensionFilter inputFilter, FileNameExtensionFilter outputFilter, String defaultInputExtension, String defaultOutputExtension, boolean removeExtension, 
			ConvertAction convertAction) {
		this(parent, inputFilter, outputFilter, defaultInputExtension, defaultOutputExtension, removeExtension, convertAction, null, JFileChooser.FILES_AND_DIRECTORIES, true);
	}
	
	public UIConvertDialog(
			JDialog parent, FileNameExtensionFilter inputFilter, FileNameExtensionFilter outputFilter, String defaultInputExtension, 
			String defaultOutputExtension, boolean removeExtension, ConvertAction convertAction, JPanel advancedOptionsPanel) {
		this(parent, inputFilter, outputFilter, defaultInputExtension, defaultOutputExtension, removeExtension, convertAction, advancedOptionsPanel, JFileChooser.FILES_AND_DIRECTORIES, true);
	}
	
	public UIConvertDialog(
			JDialog parent, FileNameExtensionFilter inputFilter, FileNameExtensionFilter outputFilter, String defaultInputExtension, 
			String defaultOutputExtension, boolean removeExtension, ConvertAction convertAction, JPanel advancedOptionsPanel, int selectionMode, boolean multiSelectEnabled) {
		this(parent, inputFilter, outputFilter, defaultInputExtension, defaultOutputExtension, removeExtension, convertAction, advancedOptionsPanel,
				selectionMode, multiSelectEnabled, selectionMode, multiSelectEnabled);
	}
	
	public UIConvertDialog(
			JDialog parent, FileNameExtensionFilter inputFilter, FileNameExtensionFilter outputFilter, String defaultInputExtension, 
			String defaultOutputExtension, boolean removeExtension, ConvertAction convertAction, JPanel advancedOptionsPanel, 
			int inputSelectionMode, boolean inputMultiSelectEnabled, int outputSelectionMode, boolean outputMultiSelectEnabled) {
		this.parent = parent;
		
		this.defaultInputExtension = defaultInputExtension;
		this.defaultOutputExtension = defaultOutputExtension;
		this.removeExtension = removeExtension;
		this.convertAction = convertAction;
		this.inputSelectionMode = inputSelectionMode;
		this.outputSelectionMode = outputSelectionMode;
		this.inputMultiSelectEnabled = inputMultiSelectEnabled;
		this.outputMultiSelectEnabled = outputMultiSelectEnabled;
		
		if (advancedOptionsPanel == null) {
			advancedOptionsPanel = convertAction.createOptionsPanel();
			if (advancedOptionsPanel != null) {
				advancedOptionsPanel.setBorder(BorderFactory.createTitledBorder("Advanced options"));
			}
		}
		
		setLayout(new BorderLayout());
		
		infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints c = new GridBagConstraints();
		
		///////// LABELS /////////
		
		lblInputFile = new JLabel("Input file: ");
		lblOutputFile = new JLabel("Output file: ");
		
		c.gridx = 0;
		c.gridy = 0;
		infoPanel.add(lblInputFile, c);
		c.gridy = 1;
		infoPanel.add(lblOutputFile, c);
		
		///////// TEXT FIELDS /////////
		
		tfInputFile = new JTextField();
		tfInputFile.setDropTarget(new DropTargetTextField(tfInputFile, true));
		tfInputFile.setColumns(50);
		tfOutputFile = new JTextField();
		tfOutputFile.setDropTarget(new DropTargetTextField(tfOutputFile, false));
		tfOutputFile.setColumns(50);
		
		c.gridx = 1;
		c.gridy = 0;
		infoPanel.add(tfInputFile, c);
		c.gridy = 1;
		infoPanel.add(tfOutputFile, c);
		
		///////// FIND FILE BUTTONS /////////
		
		c.gridx = 1;
		c.gridy = 0;
		if (inputSelectionMode == JFileChooser.FILES_AND_DIRECTORIES || inputSelectionMode == JFileChooser.FILES_ONLY) {
			JButton btnFindInFile = new JButton("Find file");
			btnFindInFile.addActionListener(new AdvancedFileChooser(tfInputFile, parent, 
					inputSelectionMode, inputMultiSelectEnabled, "untitled." + (removeExtension ? (defaultOutputExtension + "." + defaultInputExtension) : defaultInputExtension), 
					ChooserType.OPEN, inputFilter));
			
			c.gridx += 1;
			infoPanel.add(btnFindInFile, c);
		}
		if (inputSelectionMode == JFileChooser.FILES_AND_DIRECTORIES || inputSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
			JButton btnFindInFolder = new JButton("Find folder");
			// force it to find folders
			btnFindInFolder.addActionListener(new AdvancedFileChooser(tfInputFile, parent, 
					JFileChooser.DIRECTORIES_ONLY, inputMultiSelectEnabled, "untitled." + (removeExtension ? (defaultOutputExtension + "." + defaultInputExtension) : defaultInputExtension), 
					ChooserType.OPEN, inputFilter));
			
			c.gridx += 1;
			infoPanel.add(btnFindInFolder, c);
		}
		c.gridx = 1;
		c.gridy = 1;
		if (outputSelectionMode == JFileChooser.FILES_AND_DIRECTORIES || outputSelectionMode == JFileChooser.FILES_ONLY) {
			JButton btnFindOutFile = new JButton("Find file");
			btnFindOutFile.addActionListener(new AdvancedFileChooser(tfOutputFile, parent, 
					outputSelectionMode, outputMultiSelectEnabled, "untitled." + (removeExtension ? defaultOutputExtension : (defaultInputExtension + "." + defaultOutputExtension)), 
					ChooserType.SAVE, outputFilter));
			
			c.gridx += 1;
			infoPanel.add(btnFindOutFile, c);
		}
		if (outputSelectionMode == JFileChooser.FILES_AND_DIRECTORIES || outputSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
			JButton btnFindOutFolder = new JButton("Find folder");
			// force it to find folders
			btnFindOutFolder.addActionListener(new AdvancedFileChooser(tfOutputFile, parent, 
					JFileChooser.DIRECTORIES_ONLY, outputMultiSelectEnabled, "untitled." + (removeExtension ? defaultOutputExtension : (defaultInputExtension + "." + defaultOutputExtension)), 
					ChooserType.SAVE, outputFilter));
			
			c.gridx += 1;
			infoPanel.add(btnFindOutFolder, c);
		}
		
		///////// BUTTONS PANEL /////////
		
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ALCancel());
		btnConvert = new JButton("Convert");
		btnConvert.addActionListener(new ALConvert());
		
		buttonsPanel.add(btnConvert);
		buttonsPanel.add(btnCancel);
		
		parent.getRootPane().setDefaultButton(btnConvert);
		
		
		add(infoPanel, BorderLayout.NORTH);
		if (advancedOptionsPanel != null) {
			add(advancedOptionsPanel, BorderLayout.CENTER);
		}
		add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	private class ALConvert implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			String inputFile = tfInputFile.getText();
			String outputFile = tfOutputFile.getText();
			List<InputOutputPair> pairs = null;
			
			if (inputFile.length() == 0 || outputFile.length() == 0) {
				JOptionPane.showMessageDialog(UIConvertDialog.this, "No input/output file specified", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// These special cases (like effects) require a directory as input
			if (!inputMultiSelectEnabled || !outputMultiSelectEnabled ||
					inputSelectionMode == JFileChooser.DIRECTORIES_ONLY || outputSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
				pairs = new ArrayList<InputOutputPair>();
				pairs.add(new InputOutputPair(inputFile, outputFile));
			}
			else {
			
				try {
					pairs = InputOutputPaths.parsePairs(inputFile, outputFile, defaultInputExtension, defaultOutputExtension, removeExtension);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(UIConvertDialog.this, "Error parsing input/outputs strings: \n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			if (pairs.size() > 1) {
				new ProgressDialog(pairs);
			}
			else if (pairs.size() > 0) {
				InputOutputPair pair = pairs.get(0);
				try {
					
					List<FileStructureError> errors = convertAction.convert(pair.input, pair.output).getAllErrors();
					if (errors != null && errors.size() > 0) {
						JOptionPane.showMessageDialog(UIConvertDialog.this, "Errors converting file " + pair.input + ":\n" + FileStructureError.getErrorsString(errors), 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} 
				catch (Exception e) {
					JOptionPane.showMessageDialog(UIConvertDialog.this, "Error converting file " + pair.input + ":\n" + ErrorManager.getStackTraceString(e), 
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			else {
				JOptionPane.showMessageDialog(UIConvertDialog.this, "No files were converted", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			parent.dispose();
		}
	}
	
	private class ALCancel implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			parent.dispose();
		}
	}
	
	private class ProgressDialog extends JDialog {
		/**
		 * 
		 */
		private static final long serialVersionUID = 669845576013891050L;
		private List<InputOutputPair> pairs;
		private JProgressBar progressBar;
		private Task task;
		
		private ProgressDialog(List<InputOutputPair> pairs) {
			super(MainApp.getUserInterface());
			setModalityType(ModalityType.TOOLKIT_MODAL);
			this.pairs = pairs;
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			//progressBar.setStringPainted(true);
			
			add(progressBar);
			
			task = new Task();
			task.addPropertyChangeListener(new TaskProgressListener());
			task.execute();
			
			pack();
			setVisible(true);
		}
		
		private class TaskProgressListener implements PropertyChangeListener {

			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				progressBar.setValue(task.getProgress());
			}
		}
		
		private class Task extends SwingWorker<Void, Void> {
			
			@Override
			protected Void doInBackground() throws Exception {
				float inc = 100.0f / pairs.size();
				float progress = 0;
				
				List<List<FileStructureError>> errors = new ArrayList<List<FileStructureError>>();
				List<Exception> exceptions = new ArrayList<Exception>();
				List<InputOutputPair> failedPairs = new ArrayList<InputOutputPair>();
				
				
				for (InputOutputPair pair : pairs) {
					
					try {
						List<FileStructureError> error = convertAction.convert(pair.input, pair.output).getAllErrors();
						if (error != null && error.size() > 0) {
							errors.add(error);
							failedPairs.add(pair);
						}
					} catch (Exception e) {
						failedPairs.add(pair);
						exceptions.add(e);
					}
					
					progress += inc;
					setProgress((int) progress);
				}
				
				if (failedPairs.size() > 0) {
					StringBuilder sb = new StringBuilder("Couldn't convert the following files:\n");
					for (int i = 0; i < failedPairs.size(); i++) 
					{
						sb.append(failedPairs.get(i).input + "\n");
					}
					JOptionPane.showMessageDialog(ProgressDialog.this, sb.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			@Override
			public void done() {
				dispose();
			}
		}
	}

	
	public static void addConvertTab(JDialog parent, JTabbedPane tabbedPane, ConvertAction action, String title, 
			FileNameExtensionFilter inFilter, FileNameExtensionFilter outFilter, String inExtension, String outExtension, boolean removeExtension) {
		
		JPanel panel = action.createOptionsPanel();
		if (panel != null) {
			panel.setBorder(BorderFactory.createTitledBorder("Advanced options"));
		}
		
		tabbedPane.addTab(title, new UIConvertDialog(parent, inFilter, outFilter, inExtension, outExtension, removeExtension, action, panel));
	}
}
