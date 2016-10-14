package sporemodder.files.formats.tlsa;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
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

public class TlsaToTxt implements ConvertAction {

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {
		return TLSAMain.tlsaToTxt(input, output);
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
			
			TLSAMain tlsa = TLSAMain.tlsaToTxt(input, out);
			
			out.flush();
			output.write(arrayStream.toByteArray());
			
			return tlsa;
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
			
			return TLSAMain.tlsaToTxt(input, out);
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
			
			TLSAMain tlsa = TLSAMain.tlsaToTxt(in, out);
			
			out.flush();
			output.write(arrayStream.toByteArray());
			
			return tlsa;
		}
		finally {
			if (in != null) in.close();
			if (arrayStream != null) arrayStream.close();
			if (out != null) out.close();
		}
	}

	@Override
	public boolean isValid(int extension) {
		return extension == 0x4AEB6BC6;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("tlsa");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "tlsa_t";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".tlsa");
	}

	@Override
	public String getOutputName(String name) {
		String result = name;
		int index = name.indexOf(".");
		if (index != -1) {
			result = name.substring(0, index);
		}
		result += ".tlsa.tlsa_t";
		return result;
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return -1;
	}

	@Override
	public TLSAMain process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			TLSAMain main = new TLSAMain();
			main.read(in);
			return main;
		}
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "tsla", "tlsa_t", false);
		
		if (pairs == null) {
			return false;
		}
		
		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
		for (InputOutputPair pair : pairs) {
			try {
				TLSAMain.tlsaToTxt(pair.input, pair.output);
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
