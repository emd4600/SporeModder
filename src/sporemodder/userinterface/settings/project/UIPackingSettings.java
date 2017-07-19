package sporemodder.userinterface.settings.project;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import sporemodder.userinterface.settings.project.UIProjectSettings.SettingsMode;
import sporemodder.utilities.Project;
import sporemodder.utilities.Project.EditorsPackages;

public class UIPackingSettings extends SettingsCategory {
	
	private JPanel packingPanel;
	private JCheckBox cbConvertPROP;
	private JCheckBox cbConvertRW4;
	private JCheckBox cbConvertSPUI;
	private JCheckBox cbConvertTLSA;
	private JCheckBox cbConvertPCTP;
	private JCheckBox cbConvertRAST;
	//TODO GAIT
	private JCheckBox cbConvertEffects;
	private JCheckBox cbCompression;
	private JSpinner spinnerCompression;
	
	private JComboBox<EditorsPackages> cboxEditorsPackages;
	
	public UIPackingSettings(Project project, SettingsMode mode) {
		super(project, mode);
	}

	@Override
	public String getName() {
		return "Packing Settings";
	}

	@Override
	public JPanel createPanel(JDialog parent) {
		packingPanel = new JPanel();
		packingPanel.setLayout(new BoxLayout(packingPanel, BoxLayout.Y_AXIS));
		packingPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 3, 10));
		
		JLabel label = new JLabel("Convert files when packing: ");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		packingPanel.add(label);
		
		{
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(4, 2));
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			cbConvertPROP = new JCheckBox("Convert PROP files", project.isConvertPROP());
			cbConvertRW4 = new JCheckBox("Convert RW4 files", project.isConvertRW4());
			cbConvertSPUI = new JCheckBox("Convert SPUI files", project.isConvertSPUI());
			cbConvertTLSA = new JCheckBox("Convert TLSA files", project.isConvertTLSA());
			cbConvertPCTP = new JCheckBox("Convert PCTP files", project.isConvertPCTP());
			cbConvertRAST = new JCheckBox("Convert RAST files", project.isConvertRAST());
			//TODO GAIT
			cbConvertEffects = new JCheckBox("Convert Effects", project.isConvertEffects());
			
			panel.add(cbConvertPROP);
			panel.add(cbConvertRW4);
			panel.add(cbConvertSPUI);
			panel.add(cbConvertTLSA);
			panel.add(cbConvertPCTP);
			panel.add(cbConvertRAST);
			//TODO GAIT
			panel.add(cbConvertEffects);
			
			packingPanel.add(panel);
		}
		
		{
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			int limit = project.getCompressingLimit();
			cbCompression = new JCheckBox("Compress files larger than: ", limit != -1);
			cbCompression.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					spinnerCompression.setEnabled(arg0.getStateChange() == ItemEvent.SELECTED);
				}
			});
			
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
			spinnerModel.setMinimum(0);
			spinnerModel.setMaximum(Integer.MAX_VALUE);
			spinnerModel.setStepSize(1024);
			
			spinnerCompression = new JSpinner();
			spinnerCompression.setModel(spinnerModel);
			spinnerCompression.setEnabled(limit != -1);
			if (limit != -1) spinnerCompression.setValue(limit);
			
			panel.add(cbCompression);
			panel.add(spinnerCompression);
			
			packingPanel.add(panel);
		}
		
		{
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			cboxEditorsPackages = new JComboBox<EditorsPackages>(new EditorsPackages[] {
					EditorsPackages.NONE, EditorsPackages.PATCH51, EditorsPackages.BOT_PARTS
			});
			cboxEditorsPackages.setSelectedItem(project.getEmbeddedEditorPackages());
			
			panel.add(new JLabel("Embed editorsPackages~ file:"));
			panel.add(cboxEditorsPackages);
			
			packingPanel.add(panel);
		}
		
		return packingPanel;
	}

	@Override
	public void saveSettings() {
		project.setPackingConverters(cbConvertPROP.isSelected(), cbConvertRW4.isSelected(), cbConvertTLSA.isSelected(), cbConvertPCTP.isSelected(), cbConvertSPUI.isSelected(),
				cbConvertRAST.isSelected(), cbConvertEffects.isSelected());
		project.setCompressingLimit(cbCompression.isSelected() ? (int)spinnerCompression.getValue() : -1);
		project.setEmbeddedEditorPackages(cboxEditorsPackages.getItemAt(cboxEditorsPackages.getSelectedIndex()));
	}

}
