package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.effects.EffectUnpacker;
import sporemodder.files.formats.shaders.UncompiledShadersConverter;

public class UIDialogUncompiledShaders extends JDialog {

	private static final FileNameExtensionFilter FILEFILTER_EFFDIR = new FileNameExtensionFilter("Spore Effect Directory (*.effdir)", "effdir");
	
	public UIDialogUncompiledShaders() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Unpack Spore Unconverted Shaders");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		UIConvertDialog unpackDialog = new UIConvertDialog(this, null, null, null, null, false, new UncompiledShadersConverter(), null,
				JFileChooser.FILES_ONLY, false, JFileChooser.DIRECTORIES_ONLY, false);
		
		getContentPane().add(unpackDialog);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
