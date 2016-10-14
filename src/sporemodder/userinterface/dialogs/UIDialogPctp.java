package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.pctp.PctpToTxt;
import sporemodder.files.formats.pctp.TxtToPctp;

public class UIDialogPctp extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	protected static final FileNameExtensionFilter FILEFILTER_PCTP = new FileNameExtensionFilter("Spore Capability List (*.pctp)", "pctp");
	protected static final FileNameExtensionFilter FILEFILTER_PCTP_T = new FileNameExtensionFilter("Text Spore Capability List (*.pctp_t)", "pctp_t");
	
	private JTabbedPane tabbedPane;

	public UIDialogPctp() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore Capability List (PCTP)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(".pctp to .pctp_t", new UIConvertDialog(this, FILEFILTER_PCTP, FILEFILTER_PCTP_T, "pctp", "pctp_t", false, new PctpToTxt()));
		tabbedPane.addTab(".pctp_t to .pctp", new UIConvertDialog(this, FILEFILTER_PCTP_T, FILEFILTER_PCTP, "pctp_t", "pctp", true, new TxtToPctp()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
