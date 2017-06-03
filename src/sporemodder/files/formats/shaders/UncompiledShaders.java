package sporemodder.files.formats.shaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;

public class UncompiledShaders {
	
	private static final int SHADER_COUNT = 255;  // 255 ?
	
	public static UncompiledShader[] read(InputStreamAccessor in) throws IOException {
		int version = in.readInt();
		
		System.out.println("version: " + version);
		
		UncompiledShader[] shaders = new UncompiledShader[SHADER_COUNT];
		
		for (int i = 0; i < SHADER_COUNT; i++) {
			
			shaders[i] = new UncompiledShader();
			shaders[i].read(in);
		}
		
		return shaders;
	}

	public static void main(String[] args) throws IOException {
		
//		String inputPath = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\#40212002\\#00000003.cpp";
//		String outputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\#40212002\\";
		
		String inputPath = "E:\\Downloads\\CompiledMaterials.package.unpacked\\#40212002\\#00000003.#0469A3F7";
		String outputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\Darkspore #40212002\\";
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			
			UncompiledShader[] shaders = read(in);
			
			for (int i = 0; i < shaders.length; i++) {
				String shaderName = shaders[i].getShaderName();
				
				if (shaderName == null || shaderName.length() == 0) {
					shaderName = Integer.toString(i);
				}
				
				try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputPath, shaderName + ".hlsl")))) {
					shaders[i].writeHLSL(out);
				}
			}
		}
	}
}
