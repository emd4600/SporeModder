package sporemodder.userinterface;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.UIDialogPack;
import sporemodder.userinterface.dialogs.UIDialogUnpack;
import sporemodder.utilities.SporeGame;

public class UIToolBar extends JToolBar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4544196540066183659L;
	
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
	
	private JButton btnSave;
	//private JButton btnRun;
	
	private JButton btnUnpack;
	private JButton btnPack;
	private JButton btnPackAndRun;
	private JButton btnRunWOPack;

	public UIToolBar()
	{
		super("Package Management");
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		ImageIcon iconSave = createImageIcon("/sporemodder/userinterface/images/save_24.png", "Save");
		
		btnSave = new JButton(iconSave);
		btnSave.setPreferredSize(new Dimension(32, 32));
//		btnSave.setContentAreaFilled(false);
//		btnSave.setFocusPainted(false);
		
		ImageIcon iconUnpack = createImageIcon("/sporemodder/userinterface/images/Unpack_32x32.png", "Save");
		btnUnpack = new JButton(iconUnpack);
		btnUnpack.setToolTipText("Unpack mod (Ctrl + U)");
		btnUnpack.setPreferredSize(new Dimension(32, 32));
		btnUnpack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogUnpack();
			}
		});
		
		ImageIcon iconPack = createImageIcon("/sporemodder/userinterface/images/Pack_32x32.png", "Save");
		btnPack = new JButton(iconPack);
		btnPack.setToolTipText("Pack mod (Ctrl + P)");
		btnPack.setPreferredSize(new Dimension(32, 32));
		btnPack.setEnabled(false);
		btnPack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogPack();
			}
		});
		
		Action packAndRunAction = new AbstractAction("PackAndRun") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 6758659551157488394L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
//				new UIDialogPack();
				if (UIDialogPack.createPackDialog()) {
					MainApp.runGame();
				}
			}
		};
		ImageIcon iconPackAndRun = createImageIcon("/sporemodder/userinterface/images/PackAndRun_48x32.png", "Save");
		btnPackAndRun = new JButton(iconPackAndRun);
		btnPackAndRun.setToolTipText("Pack mod and run game (F9)");
		btnPackAndRun.setEnabled(false);
		btnPackAndRun.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "PackAndRun");
		btnPackAndRun.getActionMap().put("PackAndRun", packAndRunAction);
		btnPackAndRun.setPreferredSize(new Dimension(48, 32));
		btnPackAndRun.addActionListener(packAndRunAction);
		
		
		Action runAction = new AbstractAction("Run") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 930418025391158170L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				MainApp.runGame();
			}
		};
		
		ImageIcon iconRunWOPack = createImageIcon("/sporemodder/userinterface/images/RunWOPack_32x32.png", "Save");
		btnRunWOPack = new JButton(iconRunWOPack);
		btnRunWOPack.setToolTipText("Run game (F7)");
		// VK_F8 already does something related with split panes...
		btnRunWOPack.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "Run");
		btnRunWOPack.getActionMap().put("Run", runAction);
		btnRunWOPack.setPreferredSize(new Dimension(32, 32));
		btnRunWOPack.addActionListener(runAction);
		
		add(btnPackAndRun);
		add(btnPack);
		add(btnRunWOPack);
		add(btnUnpack);
	}
	
	public void update() {
		boolean enabled = MainApp.getCurrentProject() != null;
		btnPack.setEnabled(enabled);
		btnPackAndRun.setEnabled(enabled);
	}
	
	private class ALRun implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			SporeGame game = SporeGame.getGalacticAdventures();
			if (game == null) game = SporeGame.getSpore();
			if (game == null) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Spore/Galactic Adventures couldn't be found on your computer.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			//TODO args?
			try {
				game.execute();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "An error occurred while starting Spore/Galactic Adventures:\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
