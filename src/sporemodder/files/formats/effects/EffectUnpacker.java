package sporemodder.files.formats.effects;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import sporemodder.MainApp;
import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.userinterface.dialogs.UIErrorsDialog;

public class EffectUnpacker implements ConvertAction {
	
	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {

		return EffectMain.unpackEffdir(input, output.getAbsolutePath());
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		throw new UnsupportedOperationException("convert(InputStreamAccessor input, OutputStreamAccessor output) is not supported by EffectUnpack.class");
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		return EffectMain.unpackEffdir(input, outputPath);
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		throw new UnsupportedOperationException("convert(String inputPath, OutputStreamAccessor output) is not supported by EffectUnpack.class");
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0xEA5118B0;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("effdir");
	}

	@Override
	public String getOutputExtension(String extension) {
		return null;
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith("effdir");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		result += ".effdir.unpacked";
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0xEA5118B0;
	}

	@Override
	public FileFormatStructure process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			EffectMain effdir = new EffectMain();
			effdir.read(in);
			return effdir;
		}
	}
	
	public static boolean processCommand(String[] args) {
		List<String> inputs = new ArrayList<String>();
		List<String> outputs = new ArrayList<String>();
		ActionCommand.parseDefaultArguments(args, inputs, outputs);
		
		if (inputs.size() != 1) {
			JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Effect unpacker can only convert 1 file at the same time.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		File input = new File(inputs.get(0));
		
		String outputName = outputs.size() > 0 ? outputs.get(0) : null;
		if (outputName == null) {
			outputName = inputs.get(0);
			int indexOf = outputName.indexOf(".");
			if (indexOf != -1) {
				outputName = outputName.substring(0, indexOf) + ".effdir.unpacked";
			}
		}
		
		File output = new File(outputName);
		if (!output.exists()) {
			output.mkdir();
		} else {
			if (!output.isDirectory()) {
				JOptionPane.showMessageDialog(MainApp.getUserInterface(), "Effect unpacker output must be a folder.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		EffectUnpacker converter = new EffectUnpacker();
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		try {
			converter.convert(input, output);
		} catch (Exception e) {
			exceptions.put(input,  e);
		}
		
		if (exceptions.size() > 0) {
			new UIErrorsDialog(exceptions);
			return false;
		}


		return true;
	}

}
