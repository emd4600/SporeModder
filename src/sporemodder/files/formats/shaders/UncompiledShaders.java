package sporemodder.files.formats.shaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure.DefaulFormatStructure;

public class UncompiledShaders extends DefaulFormatStructure {
	
	private static final int SHADER_COUNT = 255;  // 255 ?
	
	private final List<UncompiledShader> shaders = new ArrayList<UncompiledShader>();
	
	public List<UncompiledShader> read(InputStreamAccessor in) throws IOException {
		int version = in.readInt();
		
		for (int i = 0; i < SHADER_COUNT; i++) {
			
			UncompiledShader shader = new UncompiledShader();
			shader.read(in);
			shaders.add(shader);
		}
		
		return shaders;
	}
	
	public void writeHLSL(File outputFolder) throws IOException {
		for (int i = 0; i < shaders.size(); i++) {
			String shaderName = shaders.get(i).getShaderName();
			
			if (shaderName == null || shaderName.length() == 0) {
				shaderName = Integer.toString(i);
			}
			
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFolder, shaderName + ".hlsl")))) {
				shaders.get(i).writeHLSL(out);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		
//		String inputPath = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\#40212002\\#00000003.cpp";
//		String outputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\#40212002\\";
		
		String inputPath = "E:\\Downloads\\CompiledMaterials.package.unpacked\\#40212002\\#00000003.#0469A3F7";
		String outputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\Darkspore #40212002\\";
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			
			UncompiledShaders shaders = new UncompiledShaders();
			shaders.read(in);
			shaders.writeHLSL(new File(outputPath));
		}
	}
}
