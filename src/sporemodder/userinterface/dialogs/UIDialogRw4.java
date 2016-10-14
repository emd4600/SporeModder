package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.renderWare4.DDSToRw4;
import sporemodder.files.formats.renderWare4.Rw4ToDDS;

public class UIDialogRw4 extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	protected static final FileNameExtensionFilter FILEFILTER_RW4 = new FileNameExtensionFilter("Spore RenderWare4 (*.rw4)", "rw4");
	protected static final FileNameExtensionFilter FILEFILTER_DDS = new FileNameExtensionFilter("Direct Draw Surface Texture (*.dds)", "dds");
	
	private JTabbedPane tabbedPane;

	public UIDialogRw4() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore RenderWare4 Texture (RW4)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(".rw4 to .rw4.dds", new UIConvertDialog(this, FILEFILTER_RW4, FILEFILTER_DDS, "rw4", "dds", false, new Rw4ToDDS()));
		tabbedPane.addTab(".rw4.dds to .rw4", new UIConvertDialog(this, FILEFILTER_DDS, FILEFILTER_RW4, "dds", "rw4", true, new DDSToRw4()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
