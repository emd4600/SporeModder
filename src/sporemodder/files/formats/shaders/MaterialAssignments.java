package sporemodder.files.formats.shaders;

import java.io.IOException;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.utilities.Hasher;

public class MaterialAssignments {

	public static void main(String[] args) throws Exception {
		
		MainApp.init();
		
		String inputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\GA Shaders\\#40212000\\#00000003.rw4";
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			
			int version = in.readInt();
			if (version > 0) {
				throw new Exception("Unsupported version " + version);
			}
			
			int nameID = -1;
			
			while ((nameID = in.readInt()) != -1) {
				
				int field_0 = in.readByte();  // num of rw4 compiledStates
				int count = in.readUByte();
				
				System.out.println("nameID: " + Hasher.getFileName(nameID));
				System.out.println("field_0: " + field_0);
				
				for (int i = 0; i < count; i++) {
					System.out.println("\t" + Hasher.getFileName(in.readInt()) + "  " + Hasher.getFileName(in.readInt()));
				}
				
				System.out.println();
			}
		}
	}
}
