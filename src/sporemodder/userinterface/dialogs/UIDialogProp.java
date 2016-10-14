package sporemodder.userinterface.dialogs;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
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
		{
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.setBorder(BorderFactory.createTitledBorder("Advanced options"));
			JCheckBox cbDebugMode = new JCheckBox("Debug mode");
			cbDebugMode.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(cbDebugMode);
			tabbedPane.addTab(".prop to .xml", 
					new UIConvertDialog(this, FILEFILTER_PROP, FILEFILTER_XML, "prop", "xml", false, new PropToXml(cbDebugMode), panel));
		}
		tabbedPane.addTab(".xml to .prop", new UIConvertDialog(this, FILEFILTER_XML, FILEFILTER_PROP, "xml", "prop", true, new XmlToProp()));
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
