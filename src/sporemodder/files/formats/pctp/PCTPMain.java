package sporemodder.files.formats.pctp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import sporemodder.MainApp;
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

public class PCTPMain extends FileStructure implements FileFormatStructure {
	
	private static final int MAGIC = 0x70637470;
	
	private int version;
	private float priority;
	private List<PCTPCapability> capabilities;
	private List<PCTPRemap> remaps;
	private List<PCTPAggregate> aggregates;
	private List<PCTPDeformSpec> deformSpecs;
	
	protected static String getIdentifierString(int identifier) throws UnsupportedEncodingException {
		byte[] bytes = ByteBuffer.allocate(4).putInt(identifier).array();
		// convert 0s to spaces
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) bytes[i] = 0x20;
		}
		return new String(bytes, "US-ASCII");
	}
	
	protected static void writeIdentifier(OutputStreamAccessor out, String identifier) throws IOException {
		byte[] array = identifier.getBytes("US-ASCII");
		
		if (array.length > 4) throw new IOException("PCTP-001; Unexpected identifier length.");
		
		for (int i = 3; i >= 0; i--) {
			if (i >= array.length) out.writeByte(0);
			else out.writeByte(array[i]);
		}
	}

	public void read(InputStreamAccessor in) throws IOException {
		
		if (in.readInt() != MAGIC) {
			throw new IOException("Input file is not a PCTP file! Position: " + in.getFilePointer());
		}
		
		version = in.readInt();
		if (version != 3 && version != 4) {
			throw new IOException("PCTP-H002; Unsupported version, position: " + in.getFilePointer());
		}
		if (version > 3) {
			priority = in.readFloat();
		}
		
		int count = in.readInt();
		capabilities = new ArrayList<PCTPCapability>(count);
		
		for (int i = 0; i < count; i++) {
			PCTPCapability struct = new PCTPCapability();
			struct.read(in);
			capabilities.add(struct);
		}
		
		int remapCount = in.readInt();
		remaps = new ArrayList<PCTPRemap>(remapCount);
		
		for (int i = 0; i < remapCount; i++) {
			PCTPRemap struct = new PCTPRemap();
			struct.read(in);
			remaps.add(struct);
		}
		
		int aggregateCount = in.readInt();
		aggregates = new ArrayList<PCTPAggregate>(aggregateCount);
		
		for (int i = 0; i < aggregateCount; i++) {
			PCTPAggregate aggregate = new PCTPAggregate();
			aggregate.read(in);
			aggregates.add(aggregate);
		}
		
		int deformSpecCount = in.readInt();
		deformSpecs = new ArrayList<PCTPDeformSpec>(deformSpecCount);
		
		for (int i = 0; i < deformSpecCount; i++) {
			PCTPDeformSpec deformSpec = new PCTPDeformSpec();
			deformSpec.read(in, version);
			deformSpecs.add(deformSpec);
		}
		
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(MAGIC);
		out.writeInt(version);
		if (version > 3) out.writeFloat(priority);
		
		out.writeInt(capabilities.size());
		for (PCTPCapability item : capabilities) item.write(out);
		
		out.writeInt(remaps.size());
		for (PCTPRemap item : remaps) item.write(out);
		
		out.writeInt(aggregates.size());
		for (PCTPAggregate item : aggregates) item.write(out);
		
		out.writeInt(deformSpecs.size());
		for (PCTPDeformSpec item : deformSpecs) item.write(out, version);
	}
	
	public ArgScript toArgScript() throws IOException {
		ArgScript out = new ArgScript();
		
		if (version > 3) {
			out.putCommand(new ArgScriptCommand("version", Integer.toString(version)));
			out.putCommand(new ArgScriptCommand("priority", Float.toString(priority)));
		}
		
		{
			ArgScriptBlock capabilitiesBlock = new ArgScriptBlock("cap");
			
			for (PCTPCapability capabiltiy : capabilities) 
			{
				capabilitiesBlock.putCommand(capabiltiy.toCommand());
			}
			out.putBlock(capabilitiesBlock);
		}
		
		{
			ArgScriptBlock remapsBlock = new ArgScriptBlock("remap");
			
			for (PCTPRemap remap : remaps) 
			{
				remapsBlock.putCommand(remap.toCommand());
			}
			out.putBlock(remapsBlock);
		}
		
		{
			ArgScriptBlock aggregatesBlock = new ArgScriptBlock("aggregate");
			
			for (PCTPAggregate aggregate : aggregates) 
			{
				aggregatesBlock.putCommand(aggregate.toCommand());
			}
			out.putBlock(aggregatesBlock);
		}
		
		{
			ArgScriptBlock deformSpecsBlock = new ArgScriptBlock("deformSpec");
			
			for (PCTPDeformSpec deformSpec : deformSpecs) 
			{
				deformSpecsBlock.putCommand(deformSpec.toCommand(version));
			}
			out.putBlock(deformSpecsBlock);
		}
		
		return out;
	}
	
	public void parse(ArgScript parser) throws ArgScriptException, IOException {
		parser.setParser(new PCTPParser());
		parser.parse();
		
		ArgScriptCommand cVersion = parser.getCommand("version");
		version = cVersion == null ? 3 : Integer.parseInt(cVersion.getSingleArgument());
		
		if (version > 3) {
			ArgScriptCommand cPriority = parser.getCommand("priority");
			priority = cPriority == null ? 2.0f : Float.parseFloat(cVersion.getSingleArgument());  // 2.0f is the GA one, but should it be this by default?
		}
		
		ArgScriptBlock bCapabilities = parser.getBlock("cap");
		if (bCapabilities != null) {
			List<ArgScriptCommand> commands = new ArrayList<ArgScriptCommand>(bCapabilities.getAllCommands());
			int size = commands.size();
			capabilities = new ArrayList<PCTPCapability>(size);
			
			for (int i = 0; i < size; i++) {
				PCTPCapability item = new PCTPCapability();
				item.fromCommand(commands.get(i));
				capabilities.add(item);
			}
		}
		
		ArgScriptBlock bRemaps = parser.getBlock("remap");
		if (bRemaps != null) {
			List<ArgScriptCommand> commands = new ArrayList<ArgScriptCommand>(bRemaps.getAllCommands());
			int size = commands.size();
			remaps = new ArrayList<PCTPRemap>(size);
			
			for (int i = 0; i < size; i++) {
				PCTPRemap item = new PCTPRemap();
				item.fromCommand(commands.get(i));
				remaps.add(item);
			}
		}
		
		ArgScriptBlock bAggregates = parser.getBlock("aggregate");
		if (bAggregates != null) {
			List<ArgScriptCommand> commands = new ArrayList<ArgScriptCommand>(bAggregates.getAllCommands());
			int size = commands.size();
			aggregates = new ArrayList<PCTPAggregate>(size);
			
			for (int i = 0; i < size; i++) {
				PCTPAggregate item = new PCTPAggregate();
				item.fromCommand(commands.get(i));
				aggregates.add(item);
			}
		}
		
		ArgScriptBlock bDeformSpecs = parser.getBlock("deformSpec");
		if (bDeformSpecs != null) {
			List<ArgScriptCommand> commands = new ArrayList<ArgScriptCommand>(bDeformSpecs.getAllCommands());
			int size = commands.size();
			deformSpecs = new ArrayList<PCTPDeformSpec>(size);
			
			for (int i = 0; i < size; i++) {
				PCTPDeformSpec item = new PCTPDeformSpec();
				item.fromCommand(commands.get(i), version);
				deformSpecs.add(item);
			}
		}
	}
	
	
	public int getVersion() {
		return version;
	}

	public float getPriority() {
		return priority;
	}

	public List<PCTPCapability> getCapabilities() {
		return capabilities;
	}

	public List<PCTPRemap> getRemaps() {
		return remaps;
	}

	public List<PCTPAggregate> getAggregates() {
		return aggregates;
	}

	public List<PCTPDeformSpec> getDeformSpecs() {
		return deformSpecs;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setPriority(float priority) {
		this.priority = priority;
	}

	public void setCapabilities(List<PCTPCapability> capabilities) {
		this.capabilities = capabilities;
	}

	public void setRemaps(List<PCTPRemap> remaps) {
		this.remaps = remaps;
	}

	public void setAggregates(List<PCTPAggregate> aggregates) {
		this.aggregates = aggregates;
	}

	public void setDeformSpecs(List<PCTPDeformSpec> deformSpecs) {
		this.deformSpecs = deformSpecs;
	}

	
	public static PCTPMain pctpToTxt(InputStreamAccessor in, BufferedWriter out) throws IOException {
		PCTPMain main = new PCTPMain();
		
//		long time1 = System.currentTimeMillis();
//		main.read(in);
//		System.out.println((System.currentTimeMillis() - time1) + " ms reading PCTP");
//		
//		long time2 = System.currentTimeMillis();
//		ArgScript argScript = main.toArgScript();
//		System.out.println((System.currentTimeMillis() - time2) + " ms to argscript PCTP");
//		
//		long time3 = System.currentTimeMillis();
//		argScript.write(out);
//		System.out.println((System.currentTimeMillis() - time3) + " ms writing PCTP");
		
		main.read(in);
		main.toArgScript().write(out);
		
		return main;
	}
	
	public static PCTPMain pctpToTxt(File in, File out) throws IOException {
		try (FileStreamAccessor _in = new FileStreamAccessor(in, "r");
				BufferedWriter _out = new BufferedWriter(new FileWriter(out))) {
			return pctpToTxt(_in, _out);
		}
	}
	
	public static PCTPMain pctpToTxt(String in, String out) throws IOException {
		try (FileStreamAccessor _in = new FileStreamAccessor(in, "r");
				BufferedWriter _out = new BufferedWriter(new FileWriter(out))) {
			return pctpToTxt(_in, _out);
		}
	}
	
	
	public static PCTPMain txtToPctp(BufferedReader in, OutputStreamAccessor out) throws ArgScriptException, IOException {
		PCTPMain main = new PCTPMain();
		main.parse(new ArgScript(in));
		main.write(out);
		return main;
	}
	
	public static PCTPMain txtToPctp(File in, File out) throws IOException, ArgScriptException {
		try (BufferedReader _in = new BufferedReader(new FileReader(in));
				FileStreamAccessor _out = new FileStreamAccessor(out, "rw", true)) {
			return txtToPctp(_in, _out);
		}
	}
	
	public static PCTPMain txtToPctp(String in, String out) throws IOException, ArgScriptException {
		try (BufferedReader _in = new BufferedReader(new FileReader(in));
				FileStreamAccessor _out = new FileStreamAccessor(out, "rw", true)) {
			return txtToPctp(_in, _out);
		}
	}
	
	@Override
	public List<FileStructureError> getAllErrors() {
//		List<FileStructureError> errors = new ArrayList<FileStructureError>(this.getErrors());
//		
//		for (PCTPCapability cap : capabilities) errors.addAll(cap.getErrors());
//		for (PCTPRemap remap : remaps) errors.addAll(remap.getErrors());
//		for (PCTPAggregate aggregate : aggregates) errors.addAll(aggregate.getErrors());
//		for (PCTPDeformSpec deformSpec : deformSpecs) errors.addAll(deformSpec.getErrors());
//		
//		return errors;
		
		return null;
	}
	

	private static class PCTPParser implements ArgScriptParser {

		@Override
		public ArgScriptType checkElement(String keyword, String line, int level) {
			
			if (level == 0) {
				if (keyword.equals("version") || keyword.equals("priority")) {
					return ArgScriptType.COMMAND;
				}
				else {
					return ArgScriptType.BLOCK;
				}
			}
			return ArgScriptType.COMMAND;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		MainApp.init();
		
		String path = "E:\\Eric\\SporeMaster 2.0 beta\\TestMod.package.unpacked\\animations~\\animations~.pctp";
//		String path = "E:\\Eric\\SporeMaster AG\\spore.unpacked\\#00007C8C\\#1A9BD570.pctp";
		String outputPath = "C:\\Users\\Eric\\Desktop\\test.pctp_t";
		
		try (FileStreamAccessor in = new FileStreamAccessor(path, "r");
				BufferedWriter out = new BufferedWriter(new FileWriter(outputPath))) 
		{
//			PCTPMain pctp = new PCTPMain();
//			pctp.read(in);
//			System.out.println(pctp.toArgScript().toString());
//			
//			System.out.println(pctp.getAllErrors().toString());
			pctpToTxt(in, out);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

