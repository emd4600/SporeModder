package sporemodder.files.formats.shaders;

import java.io.File;

import javax.swing.JPanel;

import sporemodder.files.ActionCommand;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.ConvertAction;
import sporemodder.files.formats.ResourceKey;

public class UncompiledShadersConverter implements ConvertAction {

	@Override
	public UncompiledShaders convert(File input, File output)
			throws Exception {
		
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			UncompiledShaders shaders = new UncompiledShaders();
			shaders.read(in);
			shaders.writeHLSL(output);
			
			return shaders;
		}
	}

	@Override
	public UncompiledShaders convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {
		throw new UnsupportedOperationException("convert(InputStreamAccessor input, OutputStreamAccessor output) is not supported by UncompiledShadersConverter.class");
	}

	@Override
	public UncompiledShaders convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		UncompiledShaders shaders = new UncompiledShaders();
		shaders.read(input);
		shaders.writeHLSL(new File(outputPath));
		
		return shaders;
	}
	
	@Override
	public UncompiledShaders convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		throw new UnsupportedOperationException("convert(String inputPath, OutputStreamAccessor output) is not supported by UncompiledShadersConverter.class");
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return false;  // We don't want automatic conversion, not all files using the extension are structured like this
	}

	@Override
	public boolean isValid(String extension) {
		return false;  // We don't want automatic conversion, not all files using the extension are structured like this
	}

	@Override
	public String getOutputExtension(String extension) {
		return null;
	}

	@Override
	public boolean isValid(File file) {
		return false;  // We don't want automatic conversion, not all files using the extension are structured like this
	}

	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".materials.unpacked");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x0469A3F7;
	}

	@Override
	public UncompiledShaders process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			UncompiledShaders shaders = new UncompiledShaders();
			shaders.read(in);
			return shaders;
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
	
	public static boolean processCommand(String[] args) {
//		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "tsla", "tlsa_t", false);
//		
//		if (pairs == null) {
//			return false;
//		}
//		
//		HashMap<File, Exception> exceptions = new HashMap<File, Exception>();
//		for (InputOutputPair pair : pairs) {
//			try {
//				TLSAMain.tlsaToTxt(pair.input, pair.output);
//			} catch (Exception e) {
//				exceptions.put(pair.input,  e);
//			}
//			
//		}
//		
//		if (exceptions.size() > 0) {
//			new UIErrorsDialog(exceptions);
//			return false;
//		}
//
//
		return true;
	}
}
