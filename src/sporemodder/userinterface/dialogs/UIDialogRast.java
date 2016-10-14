package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.rast.DDStoRast;
import sporemodder.files.formats.rast.RastToDDS;

public class UIDialogRast extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	protected static final FileNameExtensionFilter FILEFILTER_RASTER = new FileNameExtensionFilter("Spore RAST Texture (*.raster)", "raster");
	protected static final FileNameExtensionFilter FILEFILTER_DDS = new FileNameExtensionFilter("Direct Draw Surface Texture (*.dds)", "dds");
	
	private JTabbedPane tabbedPane;

	public UIDialogRast() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore RAST Texture (RAST)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(".raster to .raster.dds", new UIConvertDialog(this, FILEFILTER_RASTER, FILEFILTER_DDS, "raster", "dds", false, new RastToDDS()));
		tabbedPane.addTab(".raster.dds to .raster", new UIConvertDialog(this, FILEFILTER_DDS, FILEFILTER_RASTER, "dds", "raster", true, new DDStoRast()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
