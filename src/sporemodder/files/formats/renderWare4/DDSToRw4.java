package sporemodder.files.formats.renderWare4;

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

public class DDSToRw4 implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		return RW4Main.ddsToRw4(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {
		return RW4Main.ddsToRw4(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		try (FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw")) {
			return RW4Main.ddsToRw4(input, out);
		}
	}

	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			return RW4Main.ddsToRw4(in, output);
		}
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0x2F4E681B;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("dds");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "rw4";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".rw4.dds");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		result += ".rw4";
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x2F4E681B;
	}
	
	

	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "dds", "rw4", true);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				RW4Main.ddsToRw4(pair.input, pair.output);
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

	@Override
	public RW4Main process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			return RW4Main.fromTexture(in);
		}
	}

}
