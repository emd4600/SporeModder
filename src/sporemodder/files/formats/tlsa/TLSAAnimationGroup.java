package sporemodder.files.formats.tlsa;

import java.io.IOException;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptException;

public interface TLSAAnimationGroup {
	public void read(InputStreamAccessor in) throws IOException, FileStructureException;
	public void parse(ArgScriptBlock block) throws IOException, ArgScriptException;
	public void write(OutputStreamAccessor out) throws IOException;
	public ArgScriptBlock toBlock() throws IOException;
}