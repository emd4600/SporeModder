package sporemodder.files.formats;

import java.io.File;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public interface ConvertAction {
	public static final String KEYWORD_INPUTS = "-i";
	public static final String KEYWORD_OUTPUTS = "-o";
	
	public FileFormatStructure convert(File input, File output) throws Exception;
	public FileFormatStructure convert(InputStreamAccessor input, OutputStreamAccessor output) throws Exception;
	public FileFormatStructure convert(InputStreamAccessor input, String outputPath) throws Exception;
	public FileFormatStructure convert(String inputPath, OutputStreamAccessor output) throws Exception;
	public FileFormatStructure process(File input) throws Exception;
	public boolean isValid(int extension);
	public boolean isValid(String extension);
	public boolean isValid(File file);
	public String getOutputName(String name);
	public String getOutputExtension(String extension);
	public int getOutputExtensionID(String extension);
	
}
