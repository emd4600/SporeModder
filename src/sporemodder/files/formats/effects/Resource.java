package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public abstract class Resource implements Effect {
	
	public static final int TYPE_MASK = 0x7F0000;
	
	private EffectMain parent;
	protected int version;
	protected final ResourceID resourceID = new ResourceID();

	public Resource() {
	}
	
	@Override
	public abstract boolean read(InputStreamAccessor in) throws IOException;

	@Override
	public abstract boolean write(OutputStreamAccessor out) throws IOException;

	@Override
	public abstract boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException;
	
	@Override
	public abstract boolean toBlock(ArgScriptBlock block);

	@Override
	public abstract ArgScriptBlock toBlock();

	public abstract boolean parseCommand(ArgScriptCommand c) throws ArgScriptException, IOException;
	
	@Override
	public abstract ArgScriptCommand toCommand();

	@Override
	public boolean supportsBlock() {
		return true;
	}

	@Override
	public String getName() {
		return resourceID.toString();
	}

	@Override
	public void setName(String name) {
		resourceID.parse(name);
	}

	@Override
	public void setParent(EffectMain parent) {
		this.parent = parent;
	}

	@Override
	public Effect[] getEffects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public abstract int getType();

	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(BufferedWriter out, int level) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
