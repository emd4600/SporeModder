package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public final class ImportedEffect implements Effect {
	
	private String name;
	
	public ImportedEffect(String name) {
		this.name = name;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		throw new UnsupportedOperationException("Imported effects can't be read.");
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		throw new UnsupportedOperationException("Imported effects can't be written.");
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		throw new UnsupportedOperationException("Imported effects don't support block format.");
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		throw new UnsupportedOperationException("Imported effects don't support block format.");
	}

	@Override
	public ArgScriptBlock toBlock() {
		throw new UnsupportedOperationException("Imported effects don't support block format.");
	}

	@Override
	public ArgScriptCommand toCommand() {
		return new ArgScriptCommand("import", name);
	}

	@Override
	public boolean supportsBlock() {
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setParent(EffectMain parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Effect[] getEffects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(BufferedWriter out, int level) throws IOException {
		// TODO Auto-generated method stub

	}

}
