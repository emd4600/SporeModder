package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

// What the heck is this???
@Deprecated
public class UIErrorReport extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8170811826792127563L;
	
	private final JPanel contentPanel = new JPanel();
	private JScrollPane scrollPane;
	private JTextArea textArea;
	
	private JPanel buttonPane;
	private JButton okButton;
	
	public UIErrorReport() {
		setResizable(false);
		setTitle("Errors report");
		setBounds(100, 100, 450, 229);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 424, 146);
		
		textArea = new JTextArea();
		textArea.setText("testsetsetsetsetse  sd t sdt d st sd t sd t st stststsd ts s ts t");
		textArea.setLineWrap(true);
		textArea.setAutoscrolls(true);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		contentPanel.add(scrollPane);
		
		// OK Button
		buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		buttonPane.add(okButton);
		
		setVisible(true);
	}
	
	public static JPanel getPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 424, 146);
		
		JTextArea textArea = new JTextArea();
		textArea.setText("testsetsetsetsetse  sd t sdt d st sd t sd t st stststsd ts s ts t");
		textArea.setLineWrap(true);
		textArea.setAutoscrolls(true);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		contentPanel.add(scrollPane);
		
		return contentPanel;
	}

	
	public static void main(String[] args) {
//		UIErrorReport dialog = new UIErrorReport();
		JOptionPane.showMessageDialog(null, getPanel(), "Error", JOptionPane.ERROR_MESSAGE);
	}
}
