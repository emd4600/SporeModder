package sporemodder.files.formats.gait;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptParser;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class GAITMain extends FileStructure implements FileFormatStructure {
	
	private static final int VERSION = 11;
	private static final int MAGIC = 0x54494147; //'GAIT' in little endian
	private List<GAITStepLengthKey> stepLengthKeys = new ArrayList<GAITStepLengthKey>();
	private List<GAITGroupKey> gaitGroupKeys = new ArrayList<GAITGroupKey>();
	
	private static class GAITParser implements ArgScriptParser {
		@Override
		public ArgScriptType checkElement(String keyword, String line, int level) {
			if (level == 0) {
				if (keyword.equals("version") || keyword.equals("minharmo")) {
					return ArgScriptType.COMMAND;
				}
				else {
					return ArgScriptType.BLOCK;
				}
			}
			else if (level == 1) {
				if (keyword.equals("taxon")) {
					return ArgScriptType.BLOCK;
				}
				else {
					return ArgScriptType.COMMAND;
				}
			}
			else if (level == 2) {
				return ArgScriptType.COMMAND;
			}
			
			// should we just return null? There's nothing else above level 2
			return ArgScriptType.COMMAND;
		}
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		//minharmo -> field_4
		expect(in.readLEInt(), MAGIC, "GAIT-001", in.getFilePointer());
		expect(in.readLEInt(), in.length(), "GAIT-002", in.getFilePointer());
		expect(in.readLEInt(), VERSION, "GAIT-003", in.getFilePointer());
		expect(in.readLEInt(), 1, "GAIT-004", in.getFilePointer());
		
		int stepLengthKeysCount = in.readLEInt();
		int gaitGroupKeysCount = in.readLEInt();
		
		for (int i = 0; i < stepLengthKeysCount; i++) {
			GAITStepLengthKey stepLengthKey = new GAITStepLengthKey();
			stepLengthKey.read(in);
			stepLengthKeys.add(stepLengthKey);
		}
		
		for (int i = 0; i < gaitGroupKeysCount; i++) {
			GAITGroupKey gaitGroupKey = new GAITGroupKey();
			gaitGroupKey.read(in);
			gaitGroupKeys.add(gaitGroupKey);
			
			//TODO there's something else here!!
		}
		
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEInt(MAGIC);
		out.writeLEInt(0); // file size
		out.writeLEInt(VERSION);
		out.writeLEInt(1); //?
		
		out.writeLEInt(stepLengthKeys.size());
		out.writeLEInt(gaitGroupKeys.size());
		
		for (GAITStepLengthKey f : stepLengthKeys) {
			f.write(out);
		}
		
		for (GAITGroupKey f : gaitGroupKeys) {
			f.write(out);
		}
		
		out.seek(4);
		out.writeLEInt(out.length());
	}
	
	public ArgScript toArgScript() {
		ArgScript parser = new ArgScript();
		
		parser.putCommand(new ArgScriptCommand("version", Integer.toString(VERSION)));
		//TODO minharmo ?
		parser.addBlankLine();
		
		for (GAITStepLengthKey key : stepLengthKeys) {
			parser.putBlock(key.toBlock());
			parser.addBlankLine();
		}
		
		for (GAITGroupKey key : gaitGroupKeys) {
			parser.putBlock(key.toBlock());
			parser.addBlankLine();
		}
		
		return parser;
	}
	
	public void parse(ArgScript parser) throws NumberFormatException, ArgScriptException, IOException {
		parser.setParser(new GAITParser());
		
		ArgScriptCommand cVersion = parser.getCommand("version");
		if (cVersion != null) {
			if (Integer.parseInt(cVersion.getSingleArgument()) != VERSION) {
				throw new ArgScriptException("Unsupported GAIT version.");
			}
		}
		
		Collection<ArgScriptBlock> blocks = parser.getAllBlocks();
		for (ArgScriptBlock block : blocks) 
		{
			if (block.getKeyword().equals("steplengthkey"))
			{
				GAITStepLengthKey key = new GAITStepLengthKey();
				key.parse(block);
				stepLengthKeys.add(key);
			}
			else if (block.getKeyword().equals("gaitgroupkey"))
			{
				GAITGroupKey key = new GAITGroupKey();
				key.parse(block);
				gaitGroupKeys.add(key);
			}
		}
	}
	
	@Override
	public List<FileStructureError> getAllErrors() {
		List<FileStructureError> errors = new ArrayList<FileStructureError>(getErrors());
		
//		for (GAITStepLengthKey key : stepLengthKeys) {
//			errors.addAll(key.getErrors());
//		}
//		
//		for (GAITGroupKey key : gaitGroupKeys) {
//			errors.addAll(key.getErrors());
//		}
		
		return errors;
	}
	
	////////////////////////////////////////////////////////
	
	public static GAITMain gaitToTxt(InputStreamAccessor in, BufferedWriter out) throws IOException {
		GAITMain gait = new GAITMain();
		gait.read(in);
		gait.toArgScript().write(out);
		return gait;
	}
	
	public static GAITMain gaitToTxt(File inputFile, File outputFile) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(inputFile, "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(outputFile))) {
			return gaitToTxt(in, out);
		}
	}
	
	public static GAITMain gaitToTxt(String inputPath, String outputPath) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(outputPath))) {
			return gaitToTxt(in, out);
		}
	}
	
	////////////////////////////////////////////////////////
	
	public static GAITMain txtToGait(BufferedReader in, OutputStreamAccessor out) throws IOException, ArgScriptException {
		GAITMain gait = new GAITMain();
		gait.parse(new ArgScript(in));
		gait.write(out);
		return gait;
	}
	
	public static GAITMain txtToGait(File inputFile, File outputFile) throws IOException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(inputFile));
				FileStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)) {
			return txtToGait(in, out);
		}
	}
	
	public static GAITMain txtToGait(String inputPath, String outputPath) throws IOException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(inputPath));
				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw", true)) {
			return txtToGait(in, out);
		}
	}
	
}
