package sporemodder.userinterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import sporemodder.MainApp;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.names.NameRegistry;

import java.awt.Component;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

public class HashNames extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6080348961756949088L;
	
	private JLabel lblName;
	private JTextField tfName;
	private JPanel nameResultPanel;
	private JLabelLink lblNameResult;
	private JLabel lblNameInfo;
	
	private JLabel lblHash;
	private JTextField tfHash;
	private JPanel hashResultPanel;
	private JLabel lblHashResult;
	private JLabel lblHashInfo;
	
	private GridBagConstraints gbc_1;
	
	private Action nameResultAction = new AbstractAction() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4412677816642140127L;

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println(lblNameResult.getText());
		}
	};
	
	private MouseListener labelMouseListener = new MouseAdapter() {
		
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu menu = new JPopupMenu();
				
				JMenuItem mntmCopy = new JMenuItem("Copy");
				mntmCopy.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						JLabel label = (JLabel) e.getSource();
						StringSelection sel = new StringSelection(label.getText());
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
					}
					
				});
				menu.add(mntmCopy);
				
				menu.show((Component) e.getSource(), 0, 0);
			}
		}
	};
	
	private final HashMap<String, String> hashResults = new HashMap<String, String>();
	private String hashResultReg = null;
	private boolean isNamesReg;
	
	private final HashMap<String, Integer> nameResults = new HashMap<String, Integer>();
	private String nameResultReg = null;
	
	public HashNames() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Hash-Name converter"));
		
		lblName = new JLabel("Name:");
		lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblName.setHorizontalAlignment(SwingConstants.LEFT);
		tfName = new JTextField();
		tfName.setColumns(16);
		tfName.addKeyListener(new KLName());
		
		
		nameResultPanel = new JPanel();
	    nameResultPanel.setLayout(new GridBagLayout());
		
		lblHash = new JLabel("Hash:");
		lblHash.setAlignmentX(Component.CENTER_ALIGNMENT);
		tfHash = new JTextField();
		tfHash.setColumns(16);
		tfHash.addKeyListener(new KLHash());
		
		add(Box.createRigidArea(new Dimension(0, 6)));
		add(lblName);
		add(tfName);
		add(Box.createRigidArea(new Dimension(0, 6)));
		//add(lblNameResult);
		add(nameResultPanel);
		
		lblNameResult = new JLabelLink("", null);
		FontMetrics metrics = lblNameResult.getFontMetrics(lblNameResult.getFont());
		Dimension size = new Dimension(metrics.stringWidth("#FFFFFFFF0"), metrics.getHeight());
		lblNameResult.setMinimumSize(size);
		lblNameResult.setPreferredSize(size);
		lblNameResult.addMouseListener(labelMouseListener);
		
		GridBagConstraints  gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		//gbc.anchor = GridBagConstraints.WEST;
		//nameResultPanel.add(btnNameResult, gbc);
		nameResultPanel.add(lblNameResult, gbc);
		//btnNameResult.setVisible(false);
		
		ImageIcon infoIcon = (ImageIcon) UIManager.getIcon("OptionPane.informationIcon");
		infoIcon = new ImageIcon(infoIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		lblNameInfo = new JLabel(infoIcon);
		lblNameInfo.setEnabled(false);
		lblNameInfo.addMouseListener(new NameInfoMouseListener());
		//lblNameInfo.setPreferredSize(new Dimension(16, 16));
		
		gbc_1 = new GridBagConstraints();
		gbc_1.anchor = GridBagConstraints.LINE_END;
		gbc_1.insets = new Insets(0, 0, 0, 5);
		gbc_1.gridy = 0;
		gbc_1.gridx = 1;
		//gbc.anchor = GridBagConstraints.EAST;
		nameResultPanel.add(lblNameInfo, gbc_1);
		add(Box.createRigidArea(new Dimension(0, 6)));
		add(new JSeparator());
		add(Box.createRigidArea(new Dimension(0, 6)));
		add(lblHash);
		add(tfHash);
		add(Box.createRigidArea(new Dimension(0, 6)));
		
		hashResultPanel = new JPanel();
		add(hashResultPanel);
		hashResultPanel.setLayout(new GridBagLayout());
		
		lblHashResult = new JLabel("");
		size = new Dimension(metrics.stringWidth("F") * 25, metrics.getHeight());
		lblHashResult.setMinimumSize(size);
		lblHashResult.setPreferredSize(size);
		lblHashResult.addMouseListener(labelMouseListener);
		
		GridBagConstraints gbc_lblHashResult = new GridBagConstraints();
		gbc_lblHashResult.anchor = GridBagConstraints.LINE_START;
		gbc_lblHashResult.weightx = 1.0;
		gbc_lblHashResult.insets = new Insets(0, 0, 0, 5);
		gbc_lblHashResult.gridx = 0;
		gbc_lblHashResult.gridy = 0;
		hashResultPanel.add(lblHashResult, gbc_lblHashResult);
		
		lblHashInfo = new JLabel(infoIcon);
		lblHashInfo.setEnabled(false);
		lblHashInfo.addMouseListener(new HashInfoMouseListener());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.LINE_END;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		hashResultPanel.add(lblHashInfo, gbc_lblNewLabel);
	}
	
	private class KLHash implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			if (tfHash.getText().length() == 0) {
				tfHash.setBackground(Color.white);
				lblHashResult.setText("");
				lblHashInfo.setToolTipText("");
				lblHashInfo.setEnabled(false);
				return;
			}
			if (tfHash.getText().length() > 8 || !tfHash.getText().matches("^[0-9A-Fa-f]+$")) {
				tfHash.setBackground(Color.red);
				return;
			}
			else {
				tfHash.setBackground(Color.white);
			}
			int hash = -1;
			try {
				hash = Integer.parseUnsignedInt(tfHash.getText(), 16);
			} catch (NumberFormatException e) {
				System.out.println("Error parsing");
				tfHash.setBackground(Color.red);
				return;
			}
			
			String name = null;
			hashResultReg = null;
			hashResults.clear();
			isNamesReg = false;
			
			name = MainApp.getRegistry(NameRegistry.NAME_SPUI).getName(hash);
			if (name != null) {
				hashResults.put(NameRegistry.NAME_SPUI, name);
				hashResultReg = NameRegistry.NAME_SPUI;
			}
			
			name = MainApp.getRegistry(NameRegistry.NAME_TYPE).getName(hash);
			if (name != null) {
				hashResults.put(NameRegistry.NAME_TYPE, name);
				hashResultReg = NameRegistry.NAME_TYPE;
			}
			
			name = MainApp.getRegistry(NameRegistry.NAME_PROP).getName(hash);
			if (name != null) {
				hashResults.put(NameRegistry.NAME_PROP, name);
				hashResultReg = NameRegistry.NAME_PROP;
			}
			
			name = MainApp.getRegistry(NameRegistry.NAME_FILE).getName(hash);
			if (name != null) {
				hashResults.put(NameRegistry.NAME_FILE, name);
				hashResultReg = NameRegistry.NAME_FILE;
			}
			
			if (Hasher.UsedNames != null) {
				name = Hasher.UsedNames.getName(hash);
				if (name != null) {
					hashResults.put("names.txt", name);
					hashResultReg = "names.txt";
					isNamesReg = true;
				}
			}
			
			if (hashResultReg != null) {
				lblHashResult.setText(hashResults.get(hashResultReg));
				lblHashInfo.setToolTipText(isNamesReg ? ("Found in " + hashResultReg) : ("Found in reg_" + hashResultReg.toLowerCase()));
				lblHashInfo.setEnabled(true);
			}
			else {
				lblHashResult.setText("");
				lblHashInfo.setToolTipText("");
				lblHashInfo.setEnabled(false);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			
			if ((tfHash.getSelectionEnd() == tfHash.getSelectionStart() && tfHash.getText().length() >= 8) || (!Character.isDigit(c) && c != 'a' && c != 'A' && c != 'b' && c != 'B' && c != 'c' && c != 'C' && c != 'd'
					&& c != 'D' && c != 'e' && c != 'E' && c != 'f' && c != 'F')) {
				e.consume();
			}
		}
		
	}
	
	private class KLName implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			String name = tfName.getText();
			
			if (name == null || name.length() == 0) {
				lblNameResult.setText("");
				//TODO check if hash is present in Spore
				lblNameResult.setLinkAction(null);
				lblNameInfo.setToolTipText("");
				lblNameInfo.setEnabled(false);
				return;
			}
			
			int hash = -1;
			nameResults.clear();
			nameResultReg = null;
			
			if (!name.endsWith("~")) {
				nameResults.put(null, Hasher.stringToFNVHash(name));
				nameResultReg = null;
			}
			
			hash = MainApp.getRegistry(NameRegistry.NAME_FILE).getHash(name);
			if (hash != -1) {
				nameResults.put(NameRegistry.NAME_FILE, hash);
				nameResultReg = NameRegistry.NAME_FILE;
			}
			
			hash = MainApp.getRegistry(NameRegistry.NAME_SPUI).getHash(name);
			if (hash != -1) {
				nameResults.put(NameRegistry.NAME_SPUI, hash);
				nameResultReg = NameRegistry.NAME_SPUI;
			}
			
			hash = MainApp.getRegistry(NameRegistry.NAME_TYPE).getHash(name);
			if (hash != -1) {
				nameResults.put(NameRegistry.NAME_TYPE, hash);
				nameResultReg = NameRegistry.NAME_TYPE;
			}
			
			hash = MainApp.getRegistry(NameRegistry.NAME_PROP).getHash(name);
			if (hash != -1) {
				nameResults.put(NameRegistry.NAME_PROP, hash);
				nameResultReg = NameRegistry.NAME_PROP;
			}
			
			lblNameResult.setText(Hasher.hashToHex(nameResults.get(nameResultReg)));
			//TODO check if hash is present in Spore
			lblNameResult.setLinkAction(null);
			lblNameInfo.setToolTipText(nameResultReg == null ? "Real hash" : "Found in reg_" + nameResultReg.toLowerCase());
			lblNameInfo.setEnabled(true);
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}
		
	}
	
	private class HashInfoMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			JPopupMenu menu = new JPopupMenu();
			
			boolean show = false;
			for (Map.Entry<String, String> entry : hashResults.entrySet()) {
				
				final String reg = entry.getKey();
				
				String text = "Show reg_" + reg.toLowerCase() + " result.";
				if (reg.equals(hashResultReg)) {
					text = "-> " + text;
				}
				
				JMenuItem item = new JMenuItem(text);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						hashResultReg = reg;
						lblHashResult.setText(hashResults.get(reg));
						lblHashInfo.setToolTipText(nameResultReg == null ? "Real hash" : "Found in reg_" + nameResultReg.toLowerCase());
					}
				});
				
				menu.add(item);
				
				show = true;
			}
			
			if (show) {
				menu.show(lblHashInfo, 0, 0);
			}
		}
	}
	
	private class NameInfoMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			JPopupMenu menu = new JPopupMenu();
			
			boolean show = false;
			for (Map.Entry<String, Integer> entry : nameResults.entrySet()) {
				
				final String reg = entry.getKey();
				
				String text = reg == null ? 
						"Show real hash." : 
							"Show reg_" + reg.toLowerCase() + " result.";
				
				if ((reg == null && nameResultReg == null) || (reg != null && reg.equals(nameResultReg))) {
					text = "-> " + text;
				}
				
				JMenuItem item = new JMenuItem(text);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						nameResultReg = reg;
						//TODO Found in Spore?
						lblNameResult.setText(Hasher.hashToHex(nameResults.get(reg)));
						lblNameInfo.setToolTipText(reg == null ? "Real hash" : "Found in reg_" + reg.toLowerCase());
					}
				});
				
				menu.add(item);
				
				show = true;
			}
			
			if (show) {
				menu.show(lblNameInfo, 0, 0);
			}
		}
	}
}
