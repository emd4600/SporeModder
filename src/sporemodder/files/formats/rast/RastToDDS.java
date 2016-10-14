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
import sporemodder.files.formats.dds.DDSTexture;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class RastToDDS implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {

		return RASTMain.rastToDDS(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		return RASTMain.rastToDDS(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		try (FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			return RASTMain.rastToDDS(input, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "rw", true)) {
			return RASTMain.rastToDDS(in, output);
		}
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0x2F4E681C;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("raster");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "dds";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".raster");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		result += ".raster.dds";
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x2F4E681C;
	}

	@Override
	public DDSTexture process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			RASTMain main = new RASTMain();
			return main.read(in);
		}
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "raster", "dds", false);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				RASTMain.rastToDDS(pair.input, pair.output);
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
