package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public interface Effect {
	public boolean read(InputStreamAccessor in) throws IOException;
	public boolean write(OutputStreamAccessor out) throws IOException;
	
	public boolean parse(ArgScriptBlock block) throws IOException, ArgScriptException;
	public boolean toBlock(ArgScriptBlock block);
	public ArgScriptBlock toBlock();
	
	// for those EffectComponents that only support inline mode
	public ArgScriptCommand toCommand();
	public boolean supportsBlock();
	
	public String getName();
	public void setName(String name);
	
//	public TreeMap<Integer, List<Effect>> getEffectMap();
//	public void setEffectMap(TreeMap<Integer, List<Effect>> effects);
	public void setParent(EffectMain parent);
	public Effect[] getEffects();
	
	public int getType();
	
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices);
	
	
	public void write(BufferedWriter out, int level) throws IOException;
}
