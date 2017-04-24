package sporemodder.files.formats.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.shaders.SourceManager.ShaderSource;

public class CompiledShader extends ShaderEntry {
	
	public class StandardShaderEntry extends ShaderSource {
		private byte[] vertexShader;
		private byte[] pixelShader;
		
		private byte var_12A;  // byte
		
		private ShaderConstant[] vertexShaderConstants;
		private ShaderConstant[] pixelShaderConstants;
		
		public byte[] getVertexShader() {
			return vertexShader;
		}

		public byte[] getPixelShader() {
			return pixelShader;
		}
		
		// ?
		public int getIndex() {
			return var_12A;
		}
		
		public ShaderConstant[] getVertexShaderConstants() {
			return vertexShaderConstants;
		}

		public ShaderConstant[] getPixelShaderConstants() {
			return pixelShaderConstants;
		}
		
		@Override
		public String toString() {
			return "_entry_" + var_12A;
		}
		
		public String getVertexShaderSource(SourceManager manager) throws IOException, InterruptedException {
			if (vertexShaderSource == null) {
				vertexShaderSource = manager.loadSource(this, vertexShader);
			}
			return vertexShaderSource;
		}
		
		public String getPixelShaderSource(SourceManager manager) throws IOException, InterruptedException {
			if (pixelShaderSource == null) {
				pixelShaderSource = manager.loadSource(this, pixelShader);
			}
			return pixelShaderSource;
		}
		
		public CompiledShader getParent() {
			return CompiledShader.this;
		}
	}

	
	private final List<StandardShaderEntry> entries = new ArrayList<StandardShaderEntry>();
	
	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		
		super.read(in, version);
		
		byte var_12A = in.readByte();
		
		while (var_12A != (byte) 0xFF) {
			
			StandardShaderEntry entry = new StandardShaderEntry();
			entries.add(entry);
			
			entry.var_12A = var_12A;
			
			entry.vertexShader = new byte[in.readInt()];
			in.read(entry.vertexShader);
			
			entry.pixelShader = new byte[in.readInt()];
			in.read(entry.pixelShader);
			
			entry.vertexShaderConstants = new ShaderConstant[in.readInt()];
			for (int i = 0; i < entry.vertexShaderConstants.length; i++) {
				entry.vertexShaderConstants[i] = new ShaderConstant();
				entry.vertexShaderConstants[i].read(in);
			}
			
			entry.pixelShaderConstants = new ShaderConstant[in.readInt()];
			for (int i = 0; i < entry.pixelShaderConstants.length; i++) {
				entry.pixelShaderConstants[i] = new ShaderConstant();
				entry.pixelShaderConstants[i].read(in);
			}
			
			var_12A = in.readByte();
		}
	}
	
	
	
	public List<StandardShaderEntry> getEntries() {
		return entries;
	}
}
