package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EmptyBorder;

public class UIUtilitiesPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3849916312990529894L;
	
	private static final ImageIcon ICON_ENABLED = new ImageIcon(UIUtilitiesPanel.class.getResource("/sporemodder/userinterface/images/UtilitiesPanel_Enabled.png"));
	private static final ImageIcon ICON_DISABLED = new ImageIcon(UIUtilitiesPanel.class.getResource("/sporemodder/userinterface/images/UtilitiesPanel_Disabled.png"));
	
	private HashNames hashNames;
	private NumberConversor numberConversor;
	private JScrollPane spPasteBin;
	private JTextArea taPasteBin;
	private JPanel panel_1;
	private JLabel lblUtilitiesPanel;
	private JButton btnMinimize;
	
	private boolean isMinimized;
	
	public UIUtilitiesPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		
		taPasteBin = new JTextArea();
		spPasteBin = new JScrollPane(taPasteBin);
		
		panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		lblUtilitiesPanel = new JLabel("Utilities Panel");
		lblUtilitiesPanel.setBorder(new EmptyBorder(0, 6, 0, 0));
		panel_1.add(lblUtilitiesPanel, BorderLayout.WEST);
		
		btnMinimize = new JButton("");
		btnMinimize.setContentAreaFilled(false);
		btnMinimize.setPreferredSize(new Dimension(24, 24));
		btnMinimize.setMinimumSize(new Dimension(24, 24));
		btnMinimize.setMaximumSize(new Dimension(24, 24));
		btnMinimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				isMinimized = !isMinimized;
				
				lblUtilitiesPanel.setVisible(!isMinimized);
				hashNames.setVisible(!isMinimized);
				numberConversor.setVisible(!isMinimized);
				spPasteBin.setVisible(!isMinimized);
				
				if (isMinimized) {
					btnMinimize.setIcon(ICON_DISABLED);
				} else {
					btnMinimize.setIcon(ICON_ENABLED);
				}
			}
		});
		btnMinimize.setIcon(ICON_ENABLED);
		panel_1.add(btnMinimize, BorderLayout.EAST);
//		taPasteBin.setRows(10);
//		taPasteBin.setLineWrap(true);
		
		hashNames = new HashNames();
		panel.add(hashNames);
		
		numberConversor = new NumberConversor();
		panel.add(numberConversor);
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(spPasteBin, BorderLayout.CENTER);
		
//		add(Box.createVerticalGlue());
	}
	
	
}
