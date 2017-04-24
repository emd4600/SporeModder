package sporemodder.files.formats.shaders;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;

public class ShaderConstant {
	public int constantIndex;  // short;
	public int field_2;  // short;
	public int registerSize;  // short; register size?
	public int register;  // short;
	public int field_8;
	
	public void read(InputStreamAccessor in) throws IOException {
		constantIndex = in.readShort();
		field_2 = in.readShort();
		registerSize = in.readShort();
		register = in.readShort();
		field_8 = in.readInt();
	}
}