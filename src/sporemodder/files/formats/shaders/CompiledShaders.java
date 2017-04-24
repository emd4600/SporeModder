package sporemodder.files.formats.shaders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.shaders.CompiledShader.StandardShaderEntry;
import sporemodder.files.formats.shaders.DebugShader.ShaderType;

public class CompiledShaders {
	
	private CompiledShader[] standardShaders;
	private ShaderManager[] shaderManagers;
	private DebugShader[] debugVertexShaders;
	private DebugShader[] debugPixelShaders;
	private String name;
	
	public void read(InputStreamAccessor in) throws IOException {
		int version = in.readInt();
		
		System.out.println("version: " + version);
		
		standardShaders = new CompiledShader[in.readInt()];
		
		for (int i = 0; i < standardShaders.length; i++) {
			
			standardShaders[i] = new CompiledShader();
			standardShaders[i].read(in, version);
			
			System.out.println();
			standardShaders[i].print();
		}
		
		System.out.println();
		System.out.println(" ## -- MANAGERS -- ## ");
		System.out.println();
		
		shaderManagers = new ShaderManager[in.readInt()];
		
		for (int i = 0; i < shaderManagers.length; i++) {
			
			shaderManagers[i] = new ShaderManager();
			shaderManagers[i].read(in, version);
			
			System.out.println();
			shaderManagers[i].print();
		}
		
		System.out.println(in.getFilePointer());
		
		in.readInt();  // ?
		debugVertexShaders = new DebugShader[in.readInt()];
		for (int i = 0; i < debugVertexShaders.length; i++) {
			debugVertexShaders[i] = new DebugShader(ShaderType.VERTEX, i);
			debugVertexShaders[i].read(in);
		}
		
		in.readInt();  // ?
		debugPixelShaders = new DebugShader[in.readInt()];
		for (int i = 0; i < debugPixelShaders.length; i++) {
			debugPixelShaders[i] = new DebugShader(ShaderType.PIXEL, i);
			debugPixelShaders[i].read(in);
		}
		
		System.out.println(in.getFilePointer());
		
		name = in.readString8(in.readInt());
	}
	

	public CompiledShader[] getStandardShaders() {
		return standardShaders;
	}

	public ShaderManager[] getShaderManagers() {
		return shaderManagers;
	}

	public DebugShader[] getDebugVertexShaders() {
		return debugVertexShaders;
	}

	public DebugShader[] getDebugPixelShaders() {
		return debugPixelShaders;
	}

	public String getName() {
		return name;
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		
		MainApp.init();
		
		String inputPath = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\#40212004\\#00000003.cpp";
		String outputPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\#40212004\\";
		
		String fxcPath = "E:\\Eric\\Spore DLL Injection\\Shaders\\#40212004\\Decompiler\\fxc.exe";
		boolean decompile = false;
		boolean onlyConstants = false;
		boolean writeResult = false;
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			CompiledShaders cShaders = new CompiledShaders();
			cShaders.read(in);
			CompiledShader[] shaders = cShaders.standardShaders;
			
			if (writeResult) {
				for (int i = 0; i < shaders.length; i++) {
					
					String name = shaders[i].getEntryName();
					if (name == null) {
						name = "_shader_" + i;
					}
					
					List<StandardShaderEntry> entries = shaders[i].getEntries();
					
					for (StandardShaderEntry entry : entries) 
					{
						String entryName = name;
						if (entry.getIndex() != 0) {
							entryName += "_" + entry.getIndex();
						}
						
						if (!decompile) {
							try (FileOutputStream out = new FileOutputStream(new File(outputPath, entryName + ".vsh.raw"))) {
								out.write(entry.getVertexShader());
							}
							
							try (FileOutputStream out = new FileOutputStream(new File(outputPath, entryName + ".psh.raw"))) {
								out.write(entry.getPixelShader());
							}
						}
						else {
							if (!onlyConstants)
							{
								File vertexFile = File.createTempFile(entryName, ".vsh.raw.tmp");
								vertexFile.deleteOnExit();
								
								try (FileOutputStream out = new FileOutputStream(vertexFile)) {
									
									out.write(entry.getVertexShader());
									out.close();
									
									Process p = Runtime.getRuntime().exec(
											"\"" + fxcPath + "\" /dumpbin /Fc \"" + outputPath + entryName + ".vsh\" \"" + vertexFile.getAbsolutePath() + "\"");
									p.waitFor();
									
								}
							}
							// write constants
							try (BufferedWriter out = new BufferedWriter(new FileWriter(outputPath + entryName + ".vsh.txt")))
							{
								int index = 0;
								int startRegister = 0;
								for (ShaderConstant constant : entry.getVertexShaderConstants())
								{
									out.write("extern uniform const" + index++ + " : register(c" + startRegister + ");");
									out.write("  // " + constant.constantIndex + " " + constant.field_2 + " " + constant.register + " " + constant.field_8);
									out.newLine();
									
									startRegister += constant.registerSize;
								}
							}
							
							if (!onlyConstants)
							{
								File pixelFile = File.createTempFile(entryName, ".psh.raw.tmp");
								pixelFile.deleteOnExit();
								
								try (FileOutputStream out = new FileOutputStream(pixelFile)) {
									
									out.write(entry.getPixelShader());
									out.close();
									
									Process p = Runtime.getRuntime().exec(
											"\"" + fxcPath + "\" /dumpbin /Fc \"" + outputPath + entryName + ".psh\" \"" + pixelFile.getAbsolutePath() + "\"");
									p.waitFor();
									
								}
							}
							// write constants
							try (BufferedWriter out = new BufferedWriter(new FileWriter(outputPath + entryName + ".psh.txt")))
							{
								int index = 0;
								int startRegister = 0;
								for (ShaderConstant constant : entry.getPixelShaderConstants())
								{
									out.write("extern uniform const" + index++ + " : register(c" + startRegister + ");");
									out.write("  // " + constant.constantIndex + " " + constant.field_2 + " " + constant.register + " " + constant.field_8);
									out.newLine();
									
									startRegister += constant.registerSize;
								}
							}
						}
					}
					
				}
			}
		}
	}
}
