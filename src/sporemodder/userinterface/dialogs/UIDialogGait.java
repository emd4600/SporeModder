package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.gait.GaitToTxt;
import sporemodder.files.formats.gait.TxtToGait;

public class UIDialogGait extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	protected static final FileNameExtensionFilter FILEFILTER_GAIT = new FileNameExtensionFilter("Spore Gait (*.gait)", "gait");
	protected static final FileNameExtensionFilter FILEFILTER_GAIT_T = new FileNameExtensionFilter("Text Spore Gait (*.gait_t)", "gait_t");
	
	private JTabbedPane tabbedPane;

	public UIDialogGait() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore Gait (GAIT)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(".gait to .gait_t", new UIConvertDialog(this, FILEFILTER_GAIT, FILEFILTER_GAIT_T, "gait", "gait_t", false, new GaitToTxt()));
		tabbedPane.addTab(".gait_t to .gait", new UIConvertDialog(this, FILEFILTER_GAIT_T, FILEFILTER_GAIT, "gait_t", "gait", true, new TxtToGait()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
