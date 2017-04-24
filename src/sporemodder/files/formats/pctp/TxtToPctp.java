package sporemodder.files.formats.pctp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class TxtToPctp implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		return PCTPMain.txtToPctp(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())))) {
			
			return PCTPMain.txtToPctp(in, output);
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())));
				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			
			return PCTPMain.txtToPctp(in, out);
		}
	}

	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {

		try (BufferedReader in = new BufferedReader(new FileReader(inputPath))) {
			
			return PCTPMain.txtToPctp(in, output);
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return key.getTypeID() == 0x7C19AA7A;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("pctp_t");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "pctp";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".pctp.pctp_t");
	}

	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".pctp");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x7C19AA7A;
	}
	
	@Override
	public PCTPMain process(File input) throws Exception {
		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			PCTPMain main = new PCTPMain();
			main.parse(new ArgScript(input));
			return main;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "pctp_t", "pctp", true);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				PCTPMain.txtToPctp(pair.input, pair.output);
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
