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
import sporemodder.files.formats.spui.SpuiToTxt;
import sporemodder.files.formats.spui.TxtToSpui;

public class UIDialogSpui extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1160612132870691425L;
	
	private static final FileNameExtensionFilter FILEFILTER_SPUI = new FileNameExtensionFilter("Spore User Interface (*.spui)", "spui");
	private static final FileNameExtensionFilter FILEFILTER_SPUI_T = new FileNameExtensionFilter("Text Spore User Interface (*.spui_t)", "spui_t");
	
	private JTabbedPane tabbedPane;

	public UIDialogSpui() {
		super(MainApp.getUserInterface());
		setModalityType(ModalityType.TOOLKIT_MODAL);
		setTitle("Convert Spore User Interface (SPUI)");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		
		tabbedPane = new JTabbedPane();
		
//		{
//			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//			panel.setBorder(BorderFactory.createTitledBorder("Advanced options"));
//			JCheckBox cbFlipBlocks = new JCheckBox("Reverse block order");
//			cbFlipBlocks.setAlignmentX(Component.LEFT_ALIGNMENT);
//			panel.add(cbFlipBlocks);
//			tabbedPane.addTab(".spui to .spui_t", 
//					new UIConvertDialog(this, FILEFILTER_SPUI, FILEFILTER_SPUI_T, "spui", "spui_t", false, new SpuiToTxt(), panel));
//		}
		
		tabbedPane.addTab(".spui to .spui_t", 
				new UIConvertDialog(this, FILEFILTER_SPUI, FILEFILTER_SPUI_T, "spui", "spui_t", false, new SpuiToTxt()));
		
		tabbedPane.addTab(".spui_t to .spui", 
				new UIConvertDialog(this, FILEFILTER_SPUI_T, FILEFILTER_SPUI, "spui_t", "spui", true, new TxtToSpui()));
		
//		{
//			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//			panel.setBorder(BorderFactory.createTitledBorder("Advanced options"));
//			JCheckBox cbFlipBlocks = new JCheckBox("Reverse block order");
//			cbFlipBlocks.setAlignmentX(Component.LEFT_ALIGNMENT);
//			panel.add(cbFlipBlocks);
//			tabbedPane.addTab(".spui_t to .spui", 
//					new UIConvertDialog(this, FILEFILTER_SPUI_T, FILEFILTER_SPUI, "spui_t", "spui", true, new TxtToSpui(cbFlipBlocks), panel));
//		}
		
		getContentPane().add(tabbedPane);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
