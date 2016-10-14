package sporemodder.userinterface.dialogs;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.AdvancedFileChooser.ChooserType;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UIChooseProjectPath extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5000658637081042506L;
	
	private JLabel lblText;
	private JTextField tfPath;
	private JPanel contentPanel;
	private JPanel buttonsPanel;
	private JButton btnAccept;

	public UIChooseProjectPath(String title, String text) {
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setTitle(title);
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		lblText = new JLabel(text);
		contentPanel.add(lblText);
		
		JPanel pathPanel = new JPanel();
		contentPanel.add(pathPanel);
		pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		pathPanel.setLayout(new BorderLayout(0, 0));
		
		tfPath = new JTextField();
		tfPath.getDocument().addDocumentListener(new DocumentListener() {
			private void update() {
				File folder = new File(tfPath.getText());
				btnAccept.setEnabled(folder.isDirectory());
			}
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
		});
		pathPanel.add(tfPath, BorderLayout.CENTER);
		tfPath.setColumns(10);
		
		JButton btnFindFolder = new JButton("Find folder");
		btnFindFolder.setMnemonic('F');
		btnFindFolder.addActionListener(new AdvancedFileChooser(tfPath, this, JFileChooser.DIRECTORIES_ONLY, false, ChooserType.OPEN));
		pathPanel.add(btnFindFolder, BorderLayout.EAST);
		
		buttonsPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonsPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		btnAccept = new JButton("Accept");
		btnAccept.setEnabled(false);
		btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				File folder = new File(tfPath.getText());
				if (!folder.isDirectory()) {
					JOptionPane.showMessageDialog(UIChooseProjectPath.this, "Error", "Projects path must be a folder.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				MainApp.setProjectsPath(folder.getAbsolutePath());
				MainApp.writeSettings();
				dispose();
			}
			
		});
		buttonsPanel.add(btnAccept);
		
		pack();
		setVisible(true);
	}
}
