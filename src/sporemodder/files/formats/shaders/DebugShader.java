package sporemodder.files.formats.shaders;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.shaders.SourceManager.ShaderSource;

public class DebugShader extends ShaderSource {
	
	public static enum ShaderType {VERTEX, PIXEL};

	private byte[] infoData;  // ?
	private byte[] data;
	private ShaderConstant[] constants;
	private int[] startRegisters;
	private int field_12C;
	
	// Only used for toString
	public int index;
	public ShaderType shaderType;
	
	public DebugShader(ShaderType shaderType, int index) {
		this.index = index;
		this.shaderType = shaderType;
	}
	
	public String getShaderSource(SourceManager manager) throws IOException, InterruptedException {
		// this class either represents a vertex or pixel shader, but we'll always store the source as a vertex shader for convenience
		if (vertexShaderSource == null) {
			vertexShaderSource = manager.loadSource(this, data);
		}
		return vertexShaderSource;
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		infoData = new byte[32];
		in.read(infoData);
		
		data = new byte[in.readInt()];
		in.read(data);
		
		constants = new ShaderConstant[in.readInt()];
		for (int i = 0; i < constants.length; i++) {
			constants[i] = new ShaderConstant();
			constants[i].read(in);
		}
		
		startRegisters = new int[constants.length];
		in.readInts(startRegisters);
		
		field_12C = in.readInt();
	}
	
	@Override
	public String toString() {
		return (shaderType == ShaderType.VERTEX ? "_vertex_" : "_pixel_") + index;
	}

	public ShaderConstant[] getConstants() {
		return constants;
	}

	public int getField_12C() {
		return field_12C;
	}

	public int[] getStartRegisters() {
		return startRegisters;
	}
	
}
