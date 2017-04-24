package sporemodder.files.formats.renderWare4;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
import sporemodder.files.formats.dds.DDSTexture;
import sporemodder.userinterface.dialogs.UIErrorsDialog;
import sporemodder.utilities.InputOutputPaths.InputOutputPair;

public class Rw4ToDDS implements ConvertAction {
	
	private static RW4Main getRW4(InputStreamAccessor in) throws InstantiationException, IllegalAccessException, IOException {
		RW4Main rw4 = new RW4Main();
		rw4.readHeader(in);
		if (!rw4.isTexture()) {
			return null;
		}
		// only read necessary sections
		rw4.readSections(in, RW4Texture.class, RW4Buffer.class);
		
		return rw4;
	}
	
	private void writeRW4(InputStreamAccessor in, OutputStreamAccessor out) throws IOException {
		out.seek(0);
		out.write(in.toByteArray());
	}
	
	private void writeRW4(InputStreamAccessor in, String outPath) throws IOException {
		// remove .dds extension if present
		String path = outPath;
		int indexOf = path.indexOf(".dds");
		if (indexOf != -1) {
			path = path.substring(0, indexOf);
		}
		Files.write(new File(path).toPath(), in.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	@Override
	public FileFormatStructure convert(File input, File output)
			throws Exception {

		FileStreamAccessor in = null;
		FileStreamAccessor out = null;
		try {
			in = new FileStreamAccessor(input, "r");
			RW4Main main = getRW4(in);
			DDSTexture texture;
			if (main == null || (texture = main.toTexture()) == null) {
//				writeRW4(in, output.getAbsolutePath());
//				return new FileFormatStructure.DefaulFormatStructure();
				// the unpacker will write the original file
				return null;
			}
			else {
				out = new FileStreamAccessor(output, "rw", true);
				texture.write(out);
				return main;
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			OutputStreamAccessor output) throws Exception {

		RW4Main main = getRW4(input);
		DDSTexture texture;
		if (main == null || (texture = main.toTexture()) == null) {
//			writeRW4(in, output.getAbsolutePath());
//			return new FileFormatStructure.DefaulFormatStructure();
			// the unpacker will write the original file
			return null;
		}
		else {
			texture.write(output);
			return main;
		}
	}

	@Override
	public FileFormatStructure convert(InputStreamAccessor input,
			String outputPath) throws Exception {
		
		FileStreamAccessor out = null;
		try {
			RW4Main main = getRW4(input);
			DDSTexture texture;
			if (main == null || (texture = main.toTexture()) == null) {
//				writeRW4(in, output.getAbsolutePath());
//				return new FileFormatStructure.DefaulFormatStructure();
				// the unpacker will write the original file
				return null;
			}
			else {
				out = new FileStreamAccessor(outputPath, "rw", true);
				texture.write(out);
				return main;
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	@Override
	public FileFormatStructure convert(String inputPath,
			OutputStreamAccessor output) throws Exception {
		
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			RW4Main main = getRW4(in);
			DDSTexture texture;
			if (main == null || (texture = main.toTexture()) == null) {
//				writeRW4(in, output.getAbsolutePath());
//				return new FileFormatStructure.DefaulFormatStructure();
				// the unpacker will write the original file
				return null;
			}
			else {
				texture.write(output);
				return main;
			}
		}
	}

	@Override
	public boolean isValid(ResourceKey key) {
		return key.getTypeID() == 0x2F4E681B;
	}

	@Override
	public boolean isValid(String extension) {
		return extension.equals("rw4");
	}

	@Override
	public String getOutputExtension(String extension) {
		return "dds";
	}

	@Override
	public boolean isValid(File file) {
		return file.isFile() && file.getName().endsWith(".rw4");
	}

	@Override
	public File getOutputFile(File file) {
		return ActionCommand.replaceFileExtension(file, ".rw4.dds");
	}

	@Override
	public int getOutputExtensionID(String extension) {
		return 0x2F4E681B;
	}

	@Override
	public DDSTexture process(File input) throws Exception {
		try (InputStreamAccessor in = new FileStreamAccessor(input, "r")) {
			RW4Main main = getRW4(in);
			DDSTexture texture;
			if (main == null || (texture = main.toTexture()) == null) {
				return null;
			} else {
				return texture;
			}
		}
	}
	
	@Override
	public JPanel createOptionsPanel() {
		return null;
	}
	
	public static boolean processCommand(String[] args) {
		List<InputOutputPair> pairs = ActionCommand.parseDefaultArguments(args, "rw4", "dds", false);
		
		if (pairs == null) {
			return false;
		}
		
		Rw4ToDDS converter = new Rw4ToDDS();
		
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
