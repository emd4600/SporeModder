package sporemodder.files.formats.tlsa;

import java.io.IOException;
import java.util.List;

import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class TLSAAnimationV7 extends FileStructure {
	protected int id;
	protected String description = "";
	
	public void parse(ArgScriptCommand command) throws IOException, ArgScriptException {
		
		id = Hasher.getFileHash(command.getKeyword());
		List<String> args = command.getArguments(0, 1);
		if (args.size() == 1) {
			description = args.get(0);
		}
	}
	
	public ArgScriptCommand toCommand() throws IOException {
		return new ArgScriptCommand(Hasher.getFileName(id), description);
	}
}
