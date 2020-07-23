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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.Font;
import sporemodder.MainApp;
import sporemodder.userinterface.UIList;

public class UIDialogSporeModderFX extends JDialog {

	private JPanel panelMain;
	private JTextArea textArea;
	
	private JPanel panelButtons;
	private JButton btnAccept;
	private JButton btnCancel;
	
	private final String presetName;
	
	public UIDialogSporeModderFX(String presetName) {
		super(MainApp.getUserInterface());
		
		this.presetName = presetName;
		
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle(presetName == null ? "Create Source Preset" : "Edit Source Preset '" + presetName + "'");
		setResizable(false);
		setLocationRelativeTo(null);
		
		panelMain = new JPanel();
		panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
		
		textArea = new JTextArea(
				"This program is obsolete and no longer development. Spore modding has moved to a new program: SporeModder FX. It has many more features and a better UI."
				);
		textArea.setFont(new Font("Regular", Font.PLAIN, 18));
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.set
		textArea.setSize(600, 400);
		
		panelMain.add(textArea);
		
		panelButtons = new JPanel();
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				
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
	
	public UIDialogSporeModderFX() {
		this(null);
	}
	
	public static void main(String[] args) {
		MainApp.init();
		new UIDialogSporeModderFX();
	}
}
