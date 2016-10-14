package sporemodder.userinterface.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import sporemodder.MainApp;
import sporemodder.userinterface.UIList;

public class UISourcePreset extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8274279084165135989L;

	private JPanel panelMain;
	private JLabel lblName;
	private JTextField tfName;
	private UIList sourceList;
	private JButton btnAddPreset;
	
	private JPanel panelButtons;
	private JButton btnAccept;
	private JButton btnCancel;
	
	private final String presetName;
	
	public UISourcePreset(String presetName) {
		super(MainApp.getUserInterface());
		
		this.presetName = presetName;
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(presetName == null ? "Create Source Preset" : "Edit Source Preset '" + presetName + "'");
		setResizable(false);
		setLocationRelativeTo(null);
		
		panelMain = new JPanel();
		panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		lblName = new JLabel("Name: ");
		tfName = new JTextField();
		if (presetName != null) {
			tfName.setText(presetName);
		}
		panel.add(lblName, BorderLayout.WEST);
		panel.add(tfName, BorderLayout.CENTER);
		
		btnAddPreset = new JButton("Add Preset");
		btnAddPreset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = getPresetsMenu(UISourcePreset.this, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						List<String> list = MainApp.getSourcePresets().get(arg0.getActionCommand());
						if (list != null && list.size() > 0) {
							for (String s : list) {
								sourceList.addElement(s);
							}
							sourceList.setSelectedElement(list.get(list.size() - 1));
						}
					}
				});
				menu.show(btnAddPreset, 0, 0);
			}
			
		});
		
		sourceList = new UIList();
		sourceList.setDefaultText("MyProject");
		sourceList.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		if (presetName != null) {
			List<String> list = MainApp.getSourcePresets().get(presetName);
			if (list != null && list.size() > 0) {
				for (String s : list) {
					sourceList.addElement(s);
				}
				sourceList.setSelectedElement(list.get(list.size() - 1));
			}
		}
		
		panelMain.add(panel);
		panelMain.add(sourceList);
		panelMain.add(btnAddPreset);
		
		panelButtons = new JPanel();
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String text = tfName.getText();
				if (text.length() == 0) {
					JOptionPane.showMessageDialog(UISourcePreset.this, "Need a name for the source preset to save it.");
					return;
				}
				
				HashMap<String, List<String>> presets = MainApp.getSourcePresets();
				if (UISourcePreset.this.presetName != null) {
					presets.remove(UISourcePreset.this.presetName);
				}
				presets.put(text, sourceList.getElements());
				
				// update settings
				MainApp.writeSettings();
				
				dispose();
			}
		});
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		panelButtons.add(btnAccept);
		panelButtons.add(btnCancel);
		
		getContentPane().add(panelMain, BorderLayout.CENTER);
		getContentPane().add(panelButtons, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}
	
	public UISourcePreset() {
		this(null);
	}
	
	public static JPopupMenu getPresetsMenu(final Component parent, ActionListener action) {
		JPopupMenu menu = new JPopupMenu();
		
		final HashMap<String, List<String>> sourcePresets = MainApp.getSourcePresets();
		for (String s : sourcePresets.keySet()) {
			JMenuItem item = new JMenuItem(s);
			item.addActionListener(action);
			menu.add(item);
		}
		
		menu.add(new JSeparator());
		
		JMenuItem mntmCreatePreset = new JMenuItem("Create Preset");
		mntmCreatePreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UISourcePreset();
			}
		});
		menu.add(mntmCreatePreset);
		
		JMenuItem mntmEditPreset = new JMenuItem("Edit Preset");
		mntmEditPreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] options = (String[]) sourcePresets.keySet().toArray(new String[sourcePresets.size()]);
				String value = (String) JOptionPane.showInputDialog(parent, "Choose which preset to edit:", "Edit Source Preset", JOptionPane.PLAIN_MESSAGE, null, options, null);
				if (value != null) {
					new UISourcePreset(value);
				}
			}
		});
		menu.add(mntmEditPreset);
		
		JMenuItem mntmDeletePreset = new JMenuItem("Delete Preset");
		mntmDeletePreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String[] options = (String[]) sourcePresets.keySet().toArray(new String[sourcePresets.size()]);
				String value = (String) JOptionPane.showInputDialog(parent, "Choose which preset to delete:", "Delete Source Preset", JOptionPane.PLAIN_MESSAGE, null, options, null);
				if (value != null) {
					int result = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete the '" + value + "' source preset?", "Delete Source Preset", JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						sourcePresets.remove(value);
						// update settings
						MainApp.writeSettings();
					}
				}
			}
		});
		menu.add(mntmDeletePreset);
		
		return menu;
	}
	
	public static void main(String[] args) {
		MainApp.init();
		new UISourcePreset();
	}
}
