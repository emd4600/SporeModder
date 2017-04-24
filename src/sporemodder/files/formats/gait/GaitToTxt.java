package sporemodder.files.formats.gait;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.ResourceKey;

public class GaitToTxt implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		
		return GAITMain.gaitToTxt(input, output);
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {
		
		ByteArrayOutputStream arrayStream = null;
		BufferedWriter out = null;
		try
		{
			arrayStream = new ByteArrayOutputStream();
			out = new BufferedWriter(new OutputStreamWriter(arrayStream));
			
			FileFormatStructure errors = GAITMain.gaitToTxt(input, out);
			
			out.flush();
			output.write(arrayStream.toByteArray());
			
			return errors;
		}
		finally {
			if (arrayStream != null) arrayStream.close();
			if (out != null) out.close();
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {

		try (BufferedWriter out = new BufferedWriter(new FileWriter(outputPath))) {
			
			return GAITMain.gaitToTxt(input, out);
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		ByteArrayOutputStream arrayStream = null;
		BufferedWriter out = null;
		FileStreamAccessor in = null;
		
		try
		{
			in = new FileStreamAccessor(inputPath, "r");
			arrayStream = new ByteArrayOutputStream();
			out = new BufferedWriter(new OutputStreamWriter(arrayStream));
			
			FileFormatStructure errors = GAITMain.gaitToTxt(in, out);
			
			out.flush();
			output.write(arrayStream.toByteArray());
			
			return errors;
		}
		finally {
			if (in != null) in.close();
			if (arrayStream != null) arrayStream.close();
			if (out != null) out.close();
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return key.getTypeID() == 0x25DF0112;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("gait");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "gait_t";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".gait");
	}
	
	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".gait.gait_t");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return -1;
	}
	
	@Override
	public GAITMain process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			GAITMain main = new GAITMain();
			main.read(in);
			return main;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
}
