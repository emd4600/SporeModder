package sporemodder.userinterface.contextmenu;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.tree.TreePath;

import sporemodder.MainApp;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.pctp.TxtToPctp;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.prop.XmlToProp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.rast.RastToDDS;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.spui.TxtToSpui;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.files.formats.tlsa.TxtToTlsa;
import sporemodder.userinterface.ErrorManager;
import sporemodder.userinterface.UIProjectPanel;
import sporemodder.utilities.Project;

public class UIFileContextMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 697450268599231173L;

	private JMenuItem mntmOpenFolder;
	private JMenuItem mntmCopyName;
	private JMenuItem mntmCopyPath;
	
	private JMenuItem mntmConvert;
	
	private File file;
	
	public UIFileContextMenu(TreePath path) {
		//file = new File(Project.getCompletePath(path));
		
		file = MainApp.getCurrentProject().getFile(Project.getRelativePath(path));
		
		mntmOpenFolder = new JMenuItem("Open folder");
		mntmOpenFolder.addActionListener(new ALOpenFolder());
		mntmCopyName = new JMenuItem("Copy Name");
		mntmCopyName.addActionListener(new ALCopyName());
		mntmCopyPath = new JMenuItem("Copy Path");
		mntmCopyPath.addActionListener(new ALCopyPath());
		
		add(mntmOpenFolder);
		add(mntmCopyName);
		add(mntmCopyPath);
		
		String fileName = file.getName();
		String text = null;
		ConvertAction action = null;
		if (fileName.endsWith(".prop")) action = new PropToXml();
		else if (fileName.endsWith(".xml")) action = new XmlToProp();
		else if (fileName.endsWith(".rw4")) action = new Rw4ToDDS();
		else if (fileName.endsWith(".dds")) action = new DDSToRw4();
		else if (fileName.endsWith(".rast") || fileName.endsWith(".raster")) action = new RastToDDS();
		else if (fileName.endsWith(".rast.dds") || fileName.endsWith(".raster.dds")) action = new DDStoRast();
		else if (fileName.endsWith(".spui")) action = new SpuiToTxt();
		else if (fileName.endsWith(".spui_t")) action = new TxtToSpui();
		else if (fileName.endsWith(".tlsa")) action = new TlsaToTxt();
		else if (fileName.endsWith(".tlsa_t")) action = new TxtToTlsa();
		else if (fileName.endsWith(".pctp")) action = new PctpToTxt();
		else if (fileName.endsWith(".pctp_t")) action = new TxtToPctp();
		else if (fileName.endsWith(".effdir") && file.isFile()) {
			action = new EffectUnpacker();
			text = "Unpack effects";
		}
		else if (fileName.endsWith(".effdir") && file.isDirectory()) {
			action = new EffectPacker();
			text = "Pack effects";
		}
		
		int indexOf = fileName.lastIndexOf('.');
		String extension = indexOf == -1 ? null : fileName.substring(indexOf + 1);
		if (action != null) {
			if (text == null) {
				text = "Convert to " + action.getOutputExtension(extension);
			}
			mntmConvert = new JMenuItem(text);
			mntmConvert.addActionListener(new ALConvert(action));
			add(new JSeparator());
			add(mntmConvert);
		}
	}
	
	private class ALOpenFolder implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Desktop.isDesktopSupported()) {
				try {
					if (file.isDirectory()) {
						Desktop.getDesktop().open(file);
					} else {
						Desktop.getDesktop().open(file.getParentFile());
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
 		}
	}
	
	private class ALCopyName implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			StringSelection string = new StringSelection(file.getName());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string, string);
 		}
	}
	
	private class ALCopyPath implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			StringSelection string = new StringSelection(file.getAbsolutePath());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(string, string);
		}
	}
	
	private class ALConvert implements ActionListener {
		private ConvertAction converter;
		private ALConvert(ConvertAction converter) {
			this.converter = converter;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				converter.convert(file, converter.getOutputFile(file));
				UIProjectPanel.refreshActiveNode();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(UIFileContextMenu.this, "There were errors converting the file:\n" + ErrorManager.getStackTraceString(e), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
