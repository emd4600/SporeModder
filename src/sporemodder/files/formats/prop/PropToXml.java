package sporemodder.files.formats.prop;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class PropToXml implements ConvertAction {
	
	private JCheckBox cbDebugMode;
	
	public PropToXml(JCheckBox cbDebugMode) {
		this.cbDebugMode = cbDebugMode;
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
	public boolean isValid(int extension) {
		return extension == 0x00B1B104 || extension == 0x02B9F662;
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
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		if (name.endsWith(".soundProp")) {
			result += ".soundProp.xml";
		} else {
			result += ".prop.xml";
		}
		return result;
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
