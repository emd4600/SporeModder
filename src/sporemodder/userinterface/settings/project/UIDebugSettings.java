package sporemodder.userinterface.settings.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.userinterface.dialogs.AdvancedFileChooser;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import sporemodder.userinterface.settings.project.UIProjectSettings.SettingsMode;
import sporemodder.utilities.Project;
import sporemodder.utilities.SporeGame;
import sporemodder.utilities.Project.GamePathType;

public class UIDebugSettings extends SettingsCategory {
	
	private static final FileNameExtensionFilter FILEFILTER_EXE = new FileNameExtensionFilter("Executable (*.exe)", "exe");
	
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

	public UIDebugSettings(Project project, SettingsMode mode) {
		super(project, mode);
	}

	@Override
	public String getName() {
		return "Debugging Settings";
	}

	@Override
	public JPanel createPanel() {
		debugPanel = new JPanel();
		debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
		debugPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
		
		final JPanel customGamePathPanel = new JPanel(new BorderLayout());
		customGamePathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		tfGamePath = new JTextField(project.getCustomGamePath());
		tfGamePath.setEnabled(project.getGamePathType() == GamePathType.CUSTOM);
		btnGamePath = new JButton("Find");
		btnGamePath.setEnabled(project.getGamePathType() == GamePathType.CUSTOM);
		btnGamePath.addActionListener(new AdvancedFileChooser(tfGamePath, debugPanel, JFileChooser.FILES_ONLY, false, ChooserType.OPEN, FILEFILTER_EXE));
		
		customGamePathPanel.add(tfGamePath, BorderLayout.CENTER);
		customGamePathPanel.add(btnGamePath, BorderLayout.EAST);
		
		lblGamePath = new JLabel("Game path: ");
		lblGamePath.setAlignmentX(Component.LEFT_ALIGNMENT);
		final JPanel gamePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		rbGamePathCustom = new JRadioButton("Custom");
		rbGamePathCustom.setActionCommand("custom");
		rbGamePathCustom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UIProjectSettings.enableComponents(rbGamePathCustom, tfGamePath, btnGamePath);
			}
		});
		
		rbGamePathSpore = new JRadioButton("Spore");
		rbGamePathSpore.setEnabled(SporeGame.hasSpore());
		rbGamePathSpore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UIProjectSettings.enableComponents(rbGamePathCustom, tfGamePath, btnGamePath);
			}
		});
		rbGamePathSpore.setActionCommand("spore");
		
		rbGamePathGA = new JRadioButton("Galactic Adventures");
		rbGamePathGA.setEnabled(SporeGame.hasGalacticAdventures());
		rbGamePathGA.setActionCommand("ga");
		rbGamePathGA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				UIProjectSettings.enableComponents(rbGamePathCustom, tfGamePath, btnGamePath);
			}
		});
		
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
		
		return debugPanel;
	}

	@Override
	public void saveSettings() {
		if (cbDefaultGamePath.isSelected()) {
			project.setDefaultGamePath(true);
		} else {
			project.setDefaultGamePath(false);
			
			GamePathType gamePathType = GamePathType.CUSTOM;
			if (rbGamePathSpore.isSelected()) {
				gamePathType = GamePathType.SPORE;
			}
			else if (rbGamePathGA.isSelected()) {
				gamePathType = GamePathType.GALACTIC_ADVENTURES;
			}
			
			project.setGamePathType(gamePathType);
			
			
			if (rbGamePathCustom.isSelected()) {
				project.setGamePath(tfGamePath.getText());
			}
			
			project.setGameCommandLine(tfGameCommandLine.getText());
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
