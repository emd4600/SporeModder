package sporemodder.userinterface.dialogs;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import sporemodder.MainApp;
import sporemodder.files.formats.prop.PropToXml;
import sporemodder.files.formats.prop.XmlToProp;

public class UIDialogProp extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8834838963815833923L;
	
	private static final FileNameExtensionFilter FILEFILTER_PROP = new FileNameExtensionFilter("Spore Property List (*.prop)", "prop");
	private static final FileNameExtensionFilter FILEFILTER_XML = new FileNameExtensionFilter("XML Spore Property List (*.xml)", "xml");
	
	private JTabbedPane tabbedPane;

	public UIDialogProp() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore Property List (PROP)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		tabbedPane = new JTabbedPane();
		
		UIConvertDialog.addConvertTab(this, tabbedPane, new PropToXml(), ".prop to .xml", FILEFILTER_PROP, FILEFILTER_XML, "prop", "xml", false);
		UIConvertDialog.addConvertTab(this, tabbedPane, new XmlToProp(), ".xml to .prop", FILEFILTER_XML, FILEFILTER_PROP, "xml", "prop", true);
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
