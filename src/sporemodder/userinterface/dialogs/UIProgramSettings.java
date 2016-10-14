package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.MainApp.SaveMode;
import sporemodder.userinterface.JGradientButton;
import sporemodder.userinterface.ProjectTreeCellRenderer;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.utilities.SporeGame;
import sporemodder.utilities.Project.GamePathType;

public class UIProgramSettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1066097952221592687L;
	
	private static final String TEXT_AUTOSAVE = "Automatic";
	private static final String TEXT_MANUAL = "Manual";
	
	private static final FileNameExtensionFilter FILEFILTER_EXE = new FileNameExtensionFilter("Executable (*.exe)", "exe");
	
	private JTabbedPane tabbedPane;
	
	private JPanel contentPanel;
	private JLabel lblSaveMode;
	private JComboBox<String> cbSaveMode;
	private JLabel lblSearchableExt;
	private JTextField tfSearchableExt;
	private JLabel lblProjectsPath;
	private JTextField tfProjectsPath;
	private JButton btnProjectsPath;
	
	private JGradientButton btnColorSource;
	private JGradientButton btnColorSourceMod;
	private JGradientButton btnColorMod;
	
	private JPanel debugPanel;
	private JLabel lblGamePath;
	private ButtonGroup bgGamePath;
	private JRadioButton rbGamePathSpore;
	private JRadioButton rbGamePathGA;
	private JRadioButton rbGamePathCustom;
	private JTextField tfGamePath;
	private JButton btnGamePath;
	private JLabel lblCommand;
	private JTextField tfGameCommandLine;
	
	private JPanel buttonsPanel;
	private JButton btnAccept;
	private JButton btnCancel;

	public UIProgramSettings(Frame parent) {
		super(parent);
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("SporeModder Settings");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		////////BUTTONS PANEL ////////
		
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ALAccept());
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ALCancel());
		
		buttonsPanel.add(btnAccept);
		buttonsPanel.add(btnCancel);
		
		///////////////////////////////
		
		//////// CONTENT PANEL ////////
		{
			contentPanel = new JPanel();
			contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			
			{
				JPanel panel = new JPanel();
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				lblSaveMode = new JLabel("Save mode: ");
				lblSaveMode.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				cbSaveMode = new JComboBox<String>(new String[] {TEXT_AUTOSAVE, TEXT_MANUAL});
				cbSaveMode.setAlignmentX(Component.LEFT_ALIGNMENT);
				cbSaveMode.setSelectedItem(MainApp.getSaveMode() == SaveMode.AUTOSAVE ? TEXT_AUTOSAVE : TEXT_MANUAL);
				
				panel.add(lblSaveMode);
				panel.add(cbSaveMode);
				
				contentPanel.add(panel);
			}
			contentPanel.add(Box.createVerticalStrut(10));
			{
				lblSearchableExt = new JLabel("Extensions to search: ");
				lblSearchableExt.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				List<String> extensions = MainApp.getSearchableExtensions();
				StringBuilder sb = new StringBuilder();
				for (String s : extensions) sb.append(s + " ");
				
				tfSearchableExt = new JTextField(sb.toString());
				tfSearchableExt.setAlignmentX(Component.LEFT_ALIGNMENT);
				tfSearchableExt.setColumns(24);
				
				contentPanel.add(lblSearchableExt);
				contentPanel.add(tfSearchableExt);
			}
			contentPanel.add(Box.createVerticalStrut(10));
			{
				lblProjectsPath = new JLabel("Projects path: ");
				
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				
				tfProjectsPath = new JTextField();
				tfProjectsPath.setDropTarget(new DropTargetTextField(tfProjectsPath, true));
				tfProjectsPath.setText(MainApp.getProjectsPath().replace(MainApp.getProgramPath(), "//"));
				tfProjectsPath.setColumns(50);
				
				btnProjectsPath = new JButton("Find file");
				btnProjectsPath = new JButton("Find file");
				btnProjectsPath.addActionListener(new AdvancedFileChooser(tfProjectsPath, this, 
						JFileChooser.DIRECTORIES_ONLY, false, ChooserType.SAVE));
				
				panel.add(tfProjectsPath);
				panel.add(btnProjectsPath);
				
				contentPanel.add(lblProjectsPath);
				contentPanel.add(panel);
			}
			contentPanel.add(Box.createVerticalStrut(10));
			{
				btnColorSource = new JGradientButton("...");
				btnColorSource.setColor(ProjectTreeCellRenderer.COLOR_SOURCE);
				btnColorSource.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						Color color = JColorChooser.showDialog(UIProgramSettings.this, "Choose Source Files Color", btnColorSource.getColor());
						if (color != null) {
							btnColorSource.setColor(color);
						}
					}
				});
				btnColorSourceMod = new JGradientButton("...");
				btnColorSourceMod.setColor(ProjectTreeCellRenderer.COLOR_SOURCE_MOD);
				btnColorSourceMod.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						Color color = JColorChooser.showDialog(UIProgramSettings.this, "Choose Source & Mod Files Color", btnColorSourceMod.getColor());
						if (color != null) {
							btnColorSourceMod.setColor(color);
						}
					}
				});
				btnColorMod = new JGradientButton("...");
				btnColorMod.setColor(ProjectTreeCellRenderer.COLOR_MOD);
				btnColorMod.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						Color color = JColorChooser.showDialog(UIProgramSettings.this, "Choose Mod Files Color", btnColorMod.getColor());
						if (color != null) {
							btnColorMod.setColor(color);
						}
					}
				});
				
				JPanel colorsPanel = new JPanel();
				colorsPanel.setLayout(new GridLayout(3, 2));
				colorsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				colorsPanel.setBorder(BorderFactory.createTitledBorder("File Tree"));
				
				colorsPanel.add(new JLabel("Color of Source files"));
				colorsPanel.add(btnColorSource);
				colorsPanel.add(new JLabel("Color of Source & Mod files"));
				colorsPanel.add(btnColorSourceMod);
				colorsPanel.add(new JLabel("Color of Mod files"));
				colorsPanel.add(btnColorMod);
				
				contentPanel.add(colorsPanel);
			}
			
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			JButton btnAccept = new JButton("Accept");
			btnAccept.addActionListener(new ALAccept());
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ALCancel());
			
			buttonsPanel.add(btnAccept);
			buttonsPanel.add(btnCancel);
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			containerPanel.add(contentPanel, BorderLayout.CENTER);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab("General settings", containerPanel);
		}
		
		///////////////////////////////
		
		/* -- DEBUGGING SETTINGS -- */
		{
			debugPanel = new JPanel();
			debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
			debugPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
			
			final JPanel customGamePathPanel = new JPanel(new BorderLayout());
			customGamePathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			tfGamePath = new JTextField(MainApp.getCustomGamePath());
			tfGamePath.setEnabled(MainApp.getGamePathType() == GamePathType.CUSTOM);
			btnGamePath = new JButton("Find");
			btnGamePath.setEnabled(MainApp.getGamePathType() == GamePathType.CUSTOM);
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
			
			switch(MainApp.getGamePathType()) {
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
			
			tfGameCommandLine = new JTextField(MainApp.getGameCommandLine());
			tfGameCommandLine.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			debugPanel.add(lblGamePath);
			debugPanel.add(gamePathPanel);
			debugPanel.add(customGamePathPanel);
			debugPanel.add(lblCommand);
			debugPanel.add(tfGameCommandLine);
			
			JPanel containerPanel = new JPanel();
			containerPanel.setLayout(new BorderLayout());
			containerPanel.add(debugPanel, BorderLayout.NORTH);
			containerPanel.add(buttonsPanel, BorderLayout.SOUTH);
			
			tabbedPane.addTab("Debugging settings", containerPanel);
		}
		
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
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
	
	private class ALAccept implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainApp.setSaveMode(cbSaveMode.getSelectedItem().equals(TEXT_AUTOSAVE) ? SaveMode.AUTOSAVE : SaveMode.MANUAL);
			MainApp.setSearchableExtensions(Arrays.asList(tfSearchableExt.getText().split(" ")));
			if (tfProjectsPath.getText().length() != 0) MainApp.setProjectsPath(tfProjectsPath.getText());
			
			GamePathType gamePathType = GamePathType.CUSTOM;
			if (rbGamePathSpore.isSelected()) gamePathType = GamePathType.SPORE;
			else if (rbGamePathGA.isSelected()) gamePathType = GamePathType.GALACTIC_ADVENTURES;
			
			MainApp.setGameCommandLine(tfGameCommandLine.getText());
			MainApp.setGamePath(tfGamePath.getText());
			MainApp.setGamePathType(gamePathType);
			
			ProjectTreeCellRenderer.COLOR_SOURCE = btnColorSource.getColor();
			ProjectTreeCellRenderer.COLOR_SOURCE_MOD = btnColorSourceMod.getColor();
			ProjectTreeCellRenderer.COLOR_MOD = btnColorMod.getColor();
			MainApp.getUserInterface().getProjectPanel().repaintTree();
			
			MainApp.writeSettings();
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
