package sporemodder.files.formats.shaders;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;

public class UncompiledShader {
	
	private static class ShaderVariable {
		private String text;
		/* 10h */	private short field_10;
		/* 12h */	private short field_12;
		/* 14h */	private short registerSize;
		/* 16h */	private short field_16;
		/* 18h */	private int field_18;
		
		public void read(InputStreamAccessor in) throws IOException {
			text = in.readString8(in.readInt());
			field_10 = in.readShort();
			field_12 = in.readShort();
			registerSize = in.readShort();
			field_16 = in.readShort();
			field_18 = in.readInt();
		}
		
		public void writeHLSL(BufferedWriter out, int startRegister) throws IOException {
			out.write("extern uniform " + text + " : register(c" + startRegister + ");");
			/* Only for testing */ 
			out.write("  // " + field_10 + " " + field_12 + " " + field_16 + " " + field_18);
		}
	}

	private String mainCode;
	private String unknownText;
	private final List<ShaderVariable> variables = new ArrayList<ShaderVariable>();
	
	private int field_20;
	private int field_24;
	private byte field_28;
	private byte field_29;
	private byte field_2A;
	private int field_minus18;
	
	private String shaderName;
	
	public void read(InputStreamAccessor in) throws IOException {
		
		field_20 = in.readInt();
		field_24 = in.readInt();
		field_29 = in.readByte();
		field_2A = in.readByte();
		field_28 = in.readByte();
		field_minus18 = in.readInt();
		
		System.out.println("field_20: " + field_20);
		System.out.println("field_24: " + field_24);
		System.out.println("field_29: " + field_29);
		System.out.println("field_2A: " + field_2A);
		System.out.println("field_28: " + field_28);
		System.out.println("field_minus18: " + field_minus18);
		
		mainCode = in.readString8(in.readInt());
		unknownText = in.readString8(in.readInt());
		
		int variableCount = in.readInt();
		
		for (int i = 0; i < variableCount; i++) {
			ShaderVariable variable = new ShaderVariable();
			variable.read(in);
			variables.add(variable);
		}
		
		if ((field_minus18 & 0x2) != 0) {
			shaderName = in.readString8(in.readInt());
		}
	}
	
	public void writeHLSL(BufferedWriter out) throws IOException {
		int startRegister = 0;
		for (ShaderVariable var : variables) {
			var.writeHLSL(out, startRegister);
			out.newLine();
			
			startRegister += var.registerSize;
		}
		
		out.newLine();
		
		if (unknownText != null && unknownText.length() > 0) {
			out.write(unknownText);
			out.newLine();
		}
		
		out.write("cVertOut main( cVertIn In )");
		out.newLine();
		out.write("{");
		out.newLine();
		out.write("cVertCurrent Current;");
		out.newLine();
		out.write("cVertOut Out;");
		out.newLine();
		out.newLine();
		
		out.write(mainCode);
		
		out.newLine();
		out.write("return Out;");
		out.newLine();
		out.write("}");
		out.newLine();
	}
	
	public String getShaderName() {
		return shaderName;
	}
}
