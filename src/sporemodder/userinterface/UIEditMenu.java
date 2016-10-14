package sporemodder.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import sporemodder.userinterface.dialogs.UIFindAndReplace;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.userinterface.fileview.TextFileView;

public class UIEditMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973112294281315243L;

	private JMenuItem mntmCopy;
	private JMenuItem mntmCut;
	private JMenuItem mntmPaste;
	private JMenuItem mntmFind;
//	private JMenuItem mntmFindAndReplace;
	
	public UIEditMenu(String text) {
		super(text);
		
		mntmCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
		mntmCopy.setText("Copy");
		mntmCopy.setMnemonic(KeyEvent.VK_C);
		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		
		mntmCut = new JMenuItem(new DefaultEditorKit.CutAction());
		mntmCut.setText("Cut");
		mntmCut.setMnemonic(KeyEvent.VK_U);
		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
		
		mntmPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
		mntmPaste.setText("Paste");
		mntmPaste.setMnemonic(KeyEvent.VK_P);
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
		
		mntmFind = new JMenuItem("Find");
		mntmFind.setEnabled(false);
		mntmFind.setMnemonic(KeyEvent.VK_F);
		mntmFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
		mntmFind.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new UIFindAndReplace(UIFindAndReplace.FIND);
			}
		});
		
//		mntmFindAndReplace = new JMenuItem("Replace");
//		mntmFindAndReplace.setEnabled(false);
//		mntmFindAndReplace.setMnemonic(KeyEvent.VK_R);
//		mntmFindAndReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
//		mntmFindAndReplace.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				new UIFindAndReplace(UIFindAndReplace.REPLACE);
//			}
//		});
		
		add(mntmCopy);
		add(mntmCut);
		add(mntmPaste);
		add(mntmFind);
//		add(mntmFindAndReplace);
	}
	
	protected void update(FileView fileView) {
		if (fileView != null && fileView instanceof TextFileView && ((TextFileView) fileView).isEditable()) {
			mntmFind.setEnabled(true);
//			mntmFindAndReplace.setEnabled(true);
		} else {
			mntmFind.setEnabled(false);
//			mntmFindAndReplace.setEnabled(false);
		}
	}
}
