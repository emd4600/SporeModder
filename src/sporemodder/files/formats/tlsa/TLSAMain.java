package sporemodder.files.formats.tlsa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptParser;
import sporemodder.files.formats.argscript.ArgScript.ArgScriptType;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScript;

public class TLSAMain extends FileStructure implements FileFormatStructure {
	private int numEntries;
	private int version = 7;
	private List<TLSAAnimationGroup> groups = new ArrayList<TLSAAnimationGroup>();
	private ArgScript parser;
	
	public TLSAMain() {
		
	}
	
	public TLSAMain(BufferedReader in) throws ArgScriptException, IOException {
		parse(new ArgScript(in));
	}
	
	public TLSAMain(File file, boolean parse) throws ArgScriptException, IOException, FileStructureException {
		if (parse) {
			parse(new ArgScript(file));
		}
		else {
			try (FileStreamAccessor in = new FileStreamAccessor(file, "r")) {
				read(in);
			}
		}
	}
	
	public TLSAMain(String path, boolean parse) throws ArgScriptException, IOException, FileStructureException {
		this(new File(path), parse);
	}
	
	public TLSAMain(InputStreamAccessor in) throws IOException, FileStructureException {
		read(in);
	}
	
	
	private void readHeader(InputStreamAccessor in) throws IOException {
		expect(in.readInt(), 0x74736C61, "TLSA-H001", in.getFilePointer()); //tlsa
		version = in.readInt();
		numEntries = in.readInt();
	}
	
	public void parse() throws ArgScriptException, IOException {
		parser.setParser(new TLSAParser());
		parser.parse();
		
		for (ArgScriptBlock block : parser.getAllBlocks()) {
			TLSAAnimationGroup group = null;
			
			if (version == 7) {
				group = new TLSAAnimationGroupV7(); 
				group.parse(block);
			}
			else if (version == 10) {
				group = new TLSAAnimationGroupV10(); 
				group.parse(block);
			}
			
			groups.add(group);
		}
	}
	
	public void parse(ArgScript parser) throws ArgScriptException, IOException {
		this.parser = parser;
		parse();
	}
	
	// This method is used to generate another ArgScript from the TLSA
	public ArgScript toArgScript() throws IOException {
		ArgScript parser = new ArgScript();
		
		if (version != 7) {
			parser.putCommand(new ArgScriptCommand("version", Integer.toString(version)));
			parser.addBlankLine();
		}
		
		for (TLSAAnimationGroup group : groups) {
			parser.putBlock(group.toBlock());
		}
		
		return parser;
	}
	
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(0x74736C61);//tlsa
		out.writeInt(version);
		numEntries = groups.size();
		out.writeInt(numEntries);
		for (int i = 0; i < numEntries; i++) {
			groups.get(i).write(out);
		}
	}
	
	public void read(InputStreamAccessor in) throws IOException, FileStructureException {
		readHeader(in);
		
		if (version == 7) {
			for (int i = 0; i < numEntries; i++) {
				TLSAAnimationGroup group = new TLSAAnimationGroupV7();
				group.read(in);
				groups.add(group);
			}
		} else if (version == 10){
			for (int i = 0; i < numEntries; i++) {
				TLSAAnimationGroup group = new TLSAAnimationGroupV10();
				group.read(in);
				groups.add(group);
			}
		} else {
			throw new IOException("Unsupported version " + version);
		}
	}
	
	public static TLSAMain tlsaToTxt(InputStreamAccessor in, BufferedWriter out) throws IOException, FileStructureException {
		TLSAMain tlsa = new TLSAMain();
		
		long time1 = System.currentTimeMillis();
		tlsa.read(in);
		System.out.println((System.currentTimeMillis() - time1) + " ms reading TLSA");
		
		long time2 = System.currentTimeMillis();
		ArgScript argScript = tlsa.toArgScript();
		System.out.println((System.currentTimeMillis() - time2) + " ms to argscript TLSA");
		
		long time3 = System.currentTimeMillis();
		argScript.write(out);
		System.out.println((System.currentTimeMillis() - time3) + " ms writing TLSA");
		
		return tlsa;
	}
	
	public static TLSAMain tlsaToTxt(File inFile, File outFile) throws IOException, FileStructureException {
		try (FileStreamAccessor in = new FileStreamAccessor(inFile, "r"); 
				BufferedWriter out = new BufferedWriter(new FileWriter(outFile))){
			return tlsaToTxt(in, out);
		}
	}
	
	public static TLSAMain tlsaToTxt(String inPath, String outPath) throws IOException, FileStructureException {
		return tlsaToTxt(new File(inPath), new File(outPath));
	}
	
	
	public static TLSAMain txtToTlsa(BufferedReader in, OutputStreamAccessor out) throws IOException, FileStructureException, ArgScriptException {
		TLSAMain tlsa = new TLSAMain(in);
		tlsa.write(out);
		return tlsa;
	}
	
	public static TLSAMain txtToTlsa(File inputFile, File outputFile) throws IOException, FileStructureException, ArgScriptException {
		try (BufferedReader in = new BufferedReader(new FileReader(inputFile)); 
				FileStreamAccessor out = new FileStreamAccessor(outputFile, "rw", true)){
			return txtToTlsa(in, out);
		}
	}
	
	public static TLSAMain txtToTlsa(String inPath, String outPath) throws IOException, FileStructureException, ArgScriptException {
		return txtToTlsa(new File(inPath), new File(outPath));
	}
	
	@Override
	public List<FileStructureError> getAllErrors() {
		List<FileStructureError> errors = new ArrayList<FileStructureError>(getErrors());
		
		for (TLSAAnimationGroup group : groups) {
			if (group instanceof TLSAAnimationGroupV7) {
				TLSAAnimationGroupV7 g = (TLSAAnimationGroupV7) group;
				errors.addAll(g.getErrors());
			}
			else if (group instanceof TLSAAnimationGroupV10) {
				TLSAAnimationGroupV10 g = (TLSAAnimationGroupV10) group;
				errors.addAll(g.getErrors());
			}
		}
		
		return errors;
	}
	
	public static void main(String[] args) throws IOException, FileStructureException, ArgScriptException {
		MainApp.init();
		
		tlsaToTxt("E:\\Eric\\SporeMaster 2.0 beta\\TestMod.package.unpacked\\animations~\\animations~.tlsa", 
				"C:\\Users\\Eric\\Desktop\\animations~.tlsa.tlsa_t");
		
//		txtToTlsa("C:\\Users\\Eric\\Desktop\\CC TLSA.txt", 
//				"C:\\Users\\Eric\\Desktop\\CC TLSA.tlsa");
	}
	
	public static void parseArgs(String[] args) throws IOException {
		String inPath = null;
		String outPath = null;
		boolean decode = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-tlsa")) {
				decode = args[++i].equals("-d") ? true : false;
				inPath = args[++i];
				if (i < args.length) {
					outPath = args[++i];
				}
			}
		}
		
		if (outPath == null) {
			if (decode) {
				outPath = inPath + ".tlsa_t";
			}
			else {
				outPath = inPath.substring(inPath.indexOf(".")) + ".tlsa";
			}
		}
		
		if (decode) {
			try {
				tlsaToTxt(inPath, outPath);
			} catch (FileStructureException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.in.read();
			}
		} else {
			try {
				txtToTlsa(inPath, outPath);
			} catch (FileStructureException | ArgScriptException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.in.read();
			}
		}
	}
	
	private class TLSAParser implements ArgScriptParser {

		@Override
		public ArgScriptType checkElement(String keyword, String line, int level) {
			
			if (level == 0 && keyword.equals("version")) {
				version = Integer.parseInt(line.split("\\s", 2)[1]);
				return ArgScriptType.COMMAND;
			}
			else if (level == 0 || version == 10) {
				// version 10 uses blocks
				return ArgScriptType.BLOCK;
			} 
			else {
				return ArgScriptType.COMMAND;
			}
		}
		
	}

}
