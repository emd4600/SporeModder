package sporemodder.files.formats.gait;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.argscript.ArgScript;

public class TxtToGait implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		
		return GAITMain.txtToGait(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())))) {
			
			return GAITMain.txtToGait(in, output);
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {

		try (BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.toByteArray())));
				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			
			return GAITMain.txtToGait(in, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (BufferedReader in = new BufferedReader(new FileReader(inputPath))) {
			return GAITMain.txtToGait(in, output);
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return key.getTypeID() == 0x25DF0112;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("gait_t");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "gait";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".gait.gait_t");
	}
	
	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".gait");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x25DF0112;
	}
	
	@Override
	public GAITMain process(File input) throws Exception {
		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			GAITMain main = new GAITMain();
			main.parse(new ArgScript(input));
			return main;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
}
