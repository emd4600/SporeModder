package sporemodder.files.formats.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;

public class ShaderManager extends ShaderEntry {
	
	public static class ManagerEntryShader {
		public int field_0;
		public int field_1;
		public int field_2;
		public int field_4;
		public int field_6;
		public int field_8;
		public int field_C;
		public int field_10;
		public int field_14;
		public int field_18;
	}
	
	public class ManagerEntry {
		public ManagerEntryShader[] vertexShaders;
		public ManagerEntryShader[] pixelShaders;
		public int index;
		
		public ShaderManager getParent() {
			return ShaderManager.this;
		}
		
		@Override
		public String toString() {
			return "_managerEntry_" + index;
		}
	}
	
	public final List<ManagerEntry> entries = new ArrayList<ManagerEntry>();

	@Override
	public void read(InputStreamAccessor in, int version) throws IOException {
		super.read(in, version);
		
		int index = 0;
		
		while ((index = in.readUByte()) != 0xFF)
		{
			ManagerEntry entry = new ManagerEntry();
			entry.index = index;
			entries.add(entry);
			
			entry.vertexShaders = new ManagerEntryShader[in.readInt()];
			
			for (int i = 0; i < entry.vertexShaders.length; i++) {
				entry.vertexShaders[i] = new ManagerEntryShader();
				readUnk(entry.vertexShaders[i], in, version);
				
				entry.vertexShaders[i].field_0 = in.readByte();  // this is used to get the shader!!
			}
			
			entry.pixelShaders = new ManagerEntryShader[in.readInt()];
			
			for (int i = 0; i < entry.pixelShaders.length; i++) {
				entry.pixelShaders[i] = new ManagerEntryShader();
				readUnk(entry.pixelShaders[i], in, version);
				
				entry.pixelShaders[i].field_0 = in.readByte();  // this is used to get the shader!!
			}
		}
	}
	
	private void readUnk(ManagerEntryShader entry, InputStreamAccessor in, int version) throws IOException {
		entry.field_1 = in.readByte();
		
		if (version <= 6) {
			in.readByte();
		}
		
		entry.field_2 = in.readShort();
		entry.field_4 = in.readShort();
		entry.field_6 = in.readShort();
		
		if (version <= 6) {
			in.readShort();
			in.readShort();
			in.readShort();
		}
		
		entry.field_8 = in.readInt();
		entry.field_C = in.readInt();
		entry.field_10 = in.readInt();
		entry.field_14 = in.readInt();
		entry.field_18 = in.readByte();
	}
}
