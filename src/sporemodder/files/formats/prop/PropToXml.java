package sporemodder.files.formats.prop;

import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.ResourceKey;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class PropToXml implements ConvertAction {
	
	private JCheckBox cbDebugMode;
	private JCheckBox cbConvertEditorPackages;
	
	public PropToXml() {
	}

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		
		return PROPMain.propToXml(input, output, cbDebugMode == null ? false : cbDebugMode.isSelected());
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {
		
		return PROPMain.propToXml(input, output, cbDebugMode == null ? false : cbDebugMode.isSelected());
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {

		try (FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			return PROPMain.propToXml(input, out, cbDebugMode == null ? false : cbDebugMode.isSelected());
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			return PROPMain.propToXml(in, output, cbDebugMode == null ? false : cbDebugMode.isSelected());
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		if (cbConvertEditorPackages != null && !cbConvertEditorPackages.isSelected() && key.getGroupID() == 0x40404000) {
			return false;
		}
		return key.getTypeID() == 0x00B1B104 || key.getTypeID() == 0x02B9F662;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("prop") || extension.equals("soundProp");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "xml";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && (file.getName().endsWith(".prop") || file.getName().endsWith(".soundProp"));
	}

	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, file.getName().endsWith(".soundProp") ? ".soundProp.xml" : ".prop.xml");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return -1;
	}

	@Override
	public PROPMain process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			PROPMain main = new PROPMain();
			main.readProp(in);
			return main;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		JPanel panel = new JPanel(new GridLayout(2, 1));
		
		cbDebugMode = new JCheckBox("Debug mode");
		cbDebugMode.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(cbDebugMode);
		
		cbConvertEditorPackages = new JCheckBox("Convert editorPackages~ files");
		cbConvertEditorPackages.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(cbConvertEditorPackages);
		
		return panel;
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "prop", "xml", false);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				PROPMain.propToXml(pair.input, pair.output);
			} catch (Exception e) {
				exceptions.put(pair.input,  e);
			}
			
		}
		
		if (exceptions.size() > 0) {
			new UIErrorsDialog(exceptions);
			return false;
		}


		return true;
	}

}
