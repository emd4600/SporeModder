package sporemodder.files.formats.shaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;

// This class keeps track of all the vertex/pixel shaders sources converted, so we don't have to convert them every single time.
// When a certain limit of files is exceeded, it removes the first shaders sources so we don't use too much memory
public class SourceManager {

	public static abstract class ShaderSource {
		protected String vertexShaderSource;
		protected String pixelShaderSource;
		
		private void freeResources() {
			vertexShaderSource = null;
			pixelShaderSource = null;
		}
		
	}
	
	private final Deque<ShaderSource> sources;
	private int sourceLimit;
	private String fxcPath;
	
	public SourceManager(int sourceLimit, String fxcPath) {
		this.sourceLimit = sourceLimit;
		// the max number of sources we are going to have is sourceLimit + 1
		this.sources = new ArrayDeque<ShaderSource>(sourceLimit + 1);
		this.fxcPath = fxcPath;
	}
	
	public String loadSource(ShaderSource object, byte[] data) throws IOException, InterruptedException {
		File dataFile = File.createTempFile("SporeModder-shaders-source", ".vsh.raw.tmp");
		dataFile.deleteOnExit();
		
		File sourceFile = File.createTempFile("SporeModder-shaders-source", ".vsh.tmp");
		sourceFile.deleteOnExit();
		
		try (FileOutputStream out = new FileOutputStream(dataFile)) {
			out.write(data);
		}
			
		// Convert the file to shader assembly using FXC.exe
		Process p = Runtime.getRuntime().exec(
				"\"" + fxcPath + "\" /dumpbin /Fc \"" + sourceFile.getAbsolutePath() + "\" \"" + dataFile.getAbsolutePath() + "\"");
		p.waitFor();
		
		// Add the source to the list and free any necessary resources
		sources.addLast(object);
		checkSourceLimit();
		
		return new String(Files.readAllBytes(sourceFile.toPath()), "US-ASCII");
	}
	
	private void checkSourceLimit() {
		if (sources.size() > sourceLimit) {
			ShaderSource source = sources.pollFirst();
			if (source != null) {
				source.freeResources();
			}
		}
	}
}
