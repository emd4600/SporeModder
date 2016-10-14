package sporemodder.files.formats.prop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class XmlToProp implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		
		return PROPMain.xmlToProp(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		try (ByteArrayInputStream in = new ByteArrayInputStream(input.toByteArray())) {
			return PROPMain.xmlToProp(in, output);
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {

		try (ByteArrayInputStream in = new ByteArrayInputStream(input.toByteArray());
				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			return PROPMain.xmlToProp(in, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (InputStream in = new FileInputStream(inputPath)) {
			return PROPMain.xmlToProp(in, output);
		}
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0x00B1B104 || extension == 0x02B9F662;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("xml");
	}

	@Override
	public String getOutputExtension(String extension) {
		//TODO differenciate between prop and soundProp
		return extension.endsWith("soundProp.xml") ? "soundProp" : "prop";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".prop.xml");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		if (name.endsWith(".soundProp.xml")) {
			result += ".soundProp";
		} else {
			result += ".prop";
		}
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return extension.equals("soundProp.xml") ? 0x02B9F662 : 0x00B1B104;
	}
	
	@Override
	public PROPMain process(File input) throws Exception {
		try (InputStream in = new FileInputStream(input)) {
			PROPMain main = new PROPMain();
			main.readXML(in);
			return main;
		}
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "xml", "prop", true);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				SPUIMain.spuiToTxt(pair.input, pair.output);
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
