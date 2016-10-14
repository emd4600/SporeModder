package sporemodder.files.formats.rast;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class DDStoRast implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {

		try (FileStreamAccessor out = new FileStreamAccessor(output, "rw")) {
			return RASTMain.ddsToRast(input, out);
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		return RASTMain.ddsToRast(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		try (FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			return RASTMain.ddsToRast(input, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		return RASTMain.ddsToRast(new File(inputPath), output);
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0x2F4E681C;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("dds");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "raster";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".dds");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		result += ".raster";
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x2F4E681C;
	}

	@Override
	public RASTMain process(File input) throws Exception {
		RASTMain main = new RASTMain();
		main.setTexture(input);
		return main;
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "dds", "raster", true);
		
		if (pairs == null) {
			return false;
		}
		
		DDStoRast converter = new DDStoRast();
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				converter.convert(pair.input, pair.output);
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
