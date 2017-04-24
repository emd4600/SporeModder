package sporemodder.files.formats.shaders;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.utilities.Hasher;

// TODO need better name
public abstract class ShaderEntry {

	private int entryID;
	
	private int var_28;
	private int var_24;
	private int var_2C;
	private int var_30;
	private int var_34;
	private int field_44;
	
	private String entryName;
	
	public void read(InputStreamAccessor in, int version) throws IOException {
		
		entryID = in.readInt();
		
		var_28 = in.readInt();
		var_24 = in.readInt();
		var_2C = in.readInt();
		var_30 = in.readInt();
		var_34 = in.readInt();
		field_44 = in.readInt();
		
		if ((field_44 & 0x10) != 0) {
			entryName = in.readString8(in.readInt());
		}
	}
	
	public void print() {
		System.out.println("entryID: " + Hasher.getFileName(entryID));
		System.out.println("var_28: " + var_28);
		System.out.println("var_24: " + var_24);
		System.out.println("var_2C: " + var_2C);
		System.out.println("var_30: 0x" + Integer.toHexString(var_30));
		System.out.println("var_34: 0x" + Integer.toHexString(var_34));
		System.out.println("field_44: " + field_44);
		if (entryName != null) {
			System.out.println("shaderName: " + entryName);
		}
		
		System.out.println();
	}

	public int getEntryID() {
		return entryID;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryID(int entryID) {
		this.entryID = entryID;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	
	@Override
	public String toString() {
		return entryName;
	}

	public int getVar_28() {
		return var_28;
	}

	public int getVar_24() {
		return var_24;
	}

	public int getVar_2C() {
		return var_2C;
	}

	public int getVar_30() {
		return var_30;
	}

	public int getVar_34() {
		return var_34;
	}

	public int getField_44() {
		return field_44;
	}
	
}
