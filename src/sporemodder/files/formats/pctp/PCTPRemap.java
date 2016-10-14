package sporemodder.files.formats.pctp;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class PCTPRemap extends FileStructure {
	
	private int verbIconCategory;
	private String identifier;  // four bytes
	private int unk;
	
	public void read(InputStreamAccessor in) throws IOException {
		verbIconCategory = in.readInt();
		identifier = PCTPMain.getIdentifierString(in.readLEInt());
		unk = in.readInt();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(verbIconCategory);
		PCTPMain.writeIdentifier(out, identifier);
		out.writeInt(unk);
	}
	
	public ArgScriptCommand toCommand() {
		return new ArgScriptCommand(Hasher.getFileName(verbIconCategory), identifier, Integer.toString(unk));
	}
	
	public void fromCommand(ArgScriptCommand command) throws ArgScriptException, IOException {
		List<String> args = command.getArguments(2);
		
		verbIconCategory = Hasher.getFileHash(command.getKeyword());
		identifier = args.get(0);
		unk = Hasher.decodeInt(args.get(1));
	}

	public int getVerbIconCategory() {
		return verbIconCategory;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getUnk() {
		return unk;
	}

	public void setVerbIconCategory(int verbIconCategory) {
		this.verbIconCategory = verbIconCategory;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setUnk(int unk) {
		this.unk = unk;
	}
	
	
}
