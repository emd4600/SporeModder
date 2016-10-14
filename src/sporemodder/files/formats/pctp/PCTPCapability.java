package sporemodder.files.formats.pctp;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class PCTPCapability extends FileStructure {
	
	private String name;
	private String identifier;  // four bytes
	
	public void read(InputStreamAccessor in) throws IOException {
		name = in.readString8(in.readInt());
		identifier = PCTPMain.getIdentifierString(in.readLEInt());
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(name.length());
		out.writeString8(name);
		PCTPMain.writeIdentifier(out, identifier);
	}
	
	public ArgScriptCommand toCommand() {
		return new ArgScriptCommand(identifier, name);
	}
	
	public void fromCommand(ArgScriptCommand command) throws ArgScriptException {
		List<String> args = command.getArguments(1);
		
		identifier = command.getKeyword();
		name = args.get(0);
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
}
