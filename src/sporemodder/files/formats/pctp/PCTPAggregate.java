package sporemodder.files.formats.pctp;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class PCTPAggregate extends FileStructure {
	
	private String identifier;  // four bytes
	private String[] aggregateIdentifiers;  // four bytes
	
	public void read(InputStreamAccessor in) throws IOException {
		identifier = PCTPMain.getIdentifierString(in.readLEInt());
		
		aggregateIdentifiers = new String[in.readInt()];
		for (int i = 0; i < aggregateIdentifiers.length; i++) {
			aggregateIdentifiers[i] = PCTPMain.getIdentifierString(in.readLEInt());
		}
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		PCTPMain.writeIdentifier(out, identifier);
		out.writeInt(aggregateIdentifiers.length);
		for (String str : aggregateIdentifiers) {
			PCTPMain.writeIdentifier(out, str);
		}
	}
	
	public ArgScriptCommand toCommand() {
		return new ArgScriptCommand(identifier, aggregateIdentifiers);
	}
	
	public void fromCommand(ArgScriptCommand command) throws ArgScriptException {
		List<String> args = command.getArguments(0, Integer.MAX_VALUE);
		
		identifier = command.getKeyword();
		aggregateIdentifiers = new String[args.size()];
		
		for (int i = 0; i < aggregateIdentifiers.length; i++) {
			aggregateIdentifiers[i] = args.get(i);
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public String[] getAggregateIdentifiers() {
		return aggregateIdentifiers;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setAggregateIdentifiers(String[] aggregateIdentifiers) {
		this.aggregateIdentifiers = aggregateIdentifiers;
	}

}
