package sporemodder.userinterface.dialogs;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.effects.EffectPacker;
import sporemodder.files.formats.effects.EffectUnpacker;

public class UIDialogEffects extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -749325139849689050L;

	private static final FileNameExtensionFilter FILEFILTER_EFFDIR = new FileNameExtensionFilter("Spore Effect Directory (*.effdir)", "effdir");
	
	private JTabbedPane tabbedPane;

	public UIDialogEffects() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore Effects (EFFDIR/PFX)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		UIConvertDialog unpackDialog = new UIConvertDialog(this, FILEFILTER_EFFDIR, FILEFILTER_EFFDIR, "effdir", "effdir", false, new EffectUnpacker(), null,
				JFileChooser.FILES_ONLY, false, JFileChooser.DIRECTORIES_ONLY, false);
//		unpackDialog.setInputChooserSelectionMode(JFileChooser.FILES_ONLY, false);
//		unpackDialog.setOutputChooserSelectionMode(JFileChooser.DIRECTORIES_ONLY, false);
		tabbedPane.addTab("Unpack .effdir", unpackDialog);
		
		UIConvertDialog packDialog = new UIConvertDialog(this, FILEFILTER_EFFDIR, FILEFILTER_EFFDIR, "effdir", "effdir", true, new EffectPacker(), null,
				JFileChooser.DIRECTORIES_ONLY, false, JFileChooser.FILES_ONLY, false);
//		packDialog.setInputChooserSelectionMode(JFileChooser.DIRECTORIES_ONLY, false);
//		packDialog.setOutputChooserSelectionMode(JFileChooser.FILES_ONLY, false);
		tabbedPane.addTab("Pack .effdir", packDialog);
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
