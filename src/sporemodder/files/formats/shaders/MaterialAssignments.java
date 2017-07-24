package sporemodder.files.formats.shaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.renderWare4.RW4Main;
import sporemodder.files.formats.renderWare4.RW4TexMetadata;
import sporemodder.utilities.Hasher;

public class MaterialAssignments {
	
	public static class MaterialAssignment {
		public int materialID;
		public int compiledStateIndex;
		public int numCompiledStates;
		public int[] textureNames;
		public int[] textureGroups;
		
		// Only on Darkspore (version == 1)
		public int[] textureUnk1;
		public int[] textureUnk2;
		
		@Override
		public String toString() {
			return Integer.toString(compiledStateIndex) + " - " + Hasher.getFileName(materialID);
		}
	}
	
	public final List<MaterialAssignment> list = new ArrayList<MaterialAssignment>();
	public int version;
	
	public List<MaterialAssignment> read(InputStreamAccessor in) throws IOException {
		
		List<MaterialAssignment> list = new ArrayList<MaterialAssignment>();
		
		version = in.readInt();
		if (version > 1) {
			return null;
		}
		
		int nameID = -1;
		int index = 0;
		
		while ((nameID = in.readInt()) != -1) {
			
			MaterialAssignment material = new MaterialAssignment();
			list.add(material);
			
			material.materialID = nameID;
			material.compiledStateIndex = index;
			
			if (version == 0) 
			{
				material.numCompiledStates = in.readByte();  // num of rw4 compiledStates
				int textureCount = in.readUByte();
				
				material.textureNames = new int[textureCount];
				material.textureGroups = new int[textureCount];
				
				for (int i = 0; i < textureCount; i++) {
					material.textureNames[i] = in.readInt();
					material.textureGroups[i] =  in.readInt();
				}
			}
			else if (version == 1) 
			{
				int textureCount = in.readShort();
				
				material.textureNames = new int[textureCount];
				material.textureGroups = new int[textureCount];
				material.textureUnk1 = new int[textureCount];
				material.textureUnk2 = new int[textureCount];
				
				for (int i = 0; i < textureCount; i++) {
					material.textureNames[i] = in.readInt();
					material.textureGroups[i] =  in.readInt();
					
					material.textureUnk1[i] = in.readShort();
					material.textureUnk2[i] = in.readShort();
				}
				
				for (int i = 0; i < 16; i++) {
					in.readByte();
					in.readByte();
				}
				
				material.numCompiledStates = 0;
				
				for (int i = 0; i < 16; i++) {
					byte b = in.readByte();
					if (b != 0)
					{
						material.numCompiledStates++;
					}
				}
			}
			
			
			index += material.numCompiledStates;
		}
				
		return list;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		MainApp.init();
		
		boolean printRW4 = false;
		
		if (!printRW4) {
			String inputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212000\\#00000003.rw4";
			
			try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
				
				int version = in.readInt();
				if (version > 0) {
					throw new Exception("Unsupported version " + version);
				}
				
				int nameID = -1;
				int index = 0;
				
				while ((nameID = in.readInt()) != -1) {
					
					int numCompiledStates = in.readByte();  // num of rw4 compiledStates
					int count = in.readUByte();
					
					System.out.println("// -- " + index);
					System.out.println("nameID: " + Hasher.getFileName(nameID));
					System.out.println("field_0: " + numCompiledStates);
					
					for (int i = 0; i < count; i++) {
						System.out.println("\t" + Hasher.getFileName(in.readInt()) + "  " + Hasher.getFileName(in.readInt()));
					}
					
					System.out.println();
					
					index += numCompiledStates;
				}
			}
		}
		
		else {
			String inputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212001\\#00000003.rw4\\raw.rw4";
			
			RW4TexMetadata.READ_COMPILED_STATE = false;
			
			try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
				RW4Main main = new RW4Main();
				main.read(in);
				main.print(true, false);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
