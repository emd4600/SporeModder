package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.tlsa.TlsaToTxt;
import sporemodder.files.formats.tlsa.TxtToTlsa;

public class UIDialogTlsa extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	protected static final FileNameExtensionFilter FILEFILTER_TLSA = new FileNameExtensionFilter("Spore Animation List (*.tlsa)", "tlsa");
	protected static final FileNameExtensionFilter FILEFILTER_TLSA_T = new FileNameExtensionFilter("Text Spore Animation List (*.tlsa_t)", "tlsa_t");
	
	private JTabbedPane tabbedPane;

	public UIDialogTlsa() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore Animation List (TLSA)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(".tlsa to .tlsa_t", new UIConvertDialog(this, FILEFILTER_TLSA, FILEFILTER_TLSA_T, "tlsa", "tlsa_t", false, new TlsaToTxt()));
		tabbedPane.addTab(".tlsa_t to .tlsa", new UIConvertDialog(this, FILEFILTER_TLSA_T, FILEFILTER_TLSA, "tlsa_t", "tlsa", true, new TxtToTlsa()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
