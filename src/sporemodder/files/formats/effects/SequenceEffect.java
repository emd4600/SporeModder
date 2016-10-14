package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class SequenceEffect extends EffectComponent {

	public static final int TYPE = 0x0004;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "sequence";
	
	private static class SequenceInstance {
		float[] timeRange = {-1, -1};
		int effectIndex;
		
		SequenceInstance() {};
		SequenceInstance(SequenceInstance other) {
			timeRange[0] = other.timeRange[0];
			timeRange[1] = other.timeRange[1];
			effectIndex = other.effectIndex;
		}
	}
	
	private List<SequenceInstance> instances = new ArrayList<SequenceInstance>();
	private int flags;  // ?
	
	public SequenceEffect(int type, int version) {
		super(type, version);
	}
	
	public SequenceEffect(SequenceEffect effect) {
		super(effect);
		int size = effect.instances.size();
		instances = new ArrayList<SequenceInstance>(size);
		for (int i = 0; i < size; i++) {
			instances.add(new SequenceInstance(effect.instances.get(i)));
		}
		flags = effect.flags;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			SequenceInstance instance = new SequenceInstance();
			instance.timeRange[0] = in.readLEFloat();
			instance.timeRange[1] = in.readLEFloat();
			instance.effectIndex = in.readInt();
			instances.add(instance);
		}
		flags = in.readInt();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(instances.size());
		for (SequenceInstance ins : instances) {
			out.writeLEFloat(ins.timeRange[0]);
			out.writeLEFloat(ins.timeRange[1]);
			out.writeInt(ins.effectIndex);
		}
		out.writeInt(flags);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		Collection<ArgScriptCommand> commands = block.getAllCommands();
		
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("play")) {
				SequenceInstance ins = new SequenceInstance();
				
				ins.effectIndex = EffectMain.getEffectIndex(parent.getEffectMap(), parent.getImports(), VisualEffect.TYPE, c.getSingleArgument());
				
				List<String> args = c.getOptionArgs("range", 1, 2);
				if (args != null) {
					if (args.size() == 1) {
						ins.timeRange[0] = ins.timeRange[1] = Float.parseFloat(args.get(0));
					}
					else if (args.size() == 2) {
						ins.timeRange[0] = Float.parseFloat(args.get(0));
						ins.timeRange[1] = Float.parseFloat(args.get(1));
					}
				}
				instances.add(ins);
			}
		}
		
		{ ArgScriptCommand c = block.getCommand("flags"); if (c != null) flags = Hasher.decodeInt(c.getSingleArgument()); }
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		for (SequenceInstance ins : instances) {
			ArgScriptCommand c;
			if (ins.effectIndex != -1) {
				if ((ins.effectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					c = new ArgScriptCommand("play", parent.getImports().get(ins.effectIndex));
				}
				else {
					Effect effect = EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, ins.effectIndex);
					c = new ArgScriptCommand("play", effect.getName());
				}
				
				if (ins.timeRange[0] != -1 || ins.timeRange[1] != -1) {
					c.putOption(new ArgScriptOption("range", Float.toString(ins.timeRange[0]), Float.toString(ins.timeRange[1])));
				}
			}
			else {
				c = new ArgScriptCommand("play", new String[0]);
			}
			
			if (ins.timeRange[0] != -1 || ins.timeRange[1] != -1) {
				c.putOption(new ArgScriptOption("range", Float.toString(ins.timeRange[0]), Float.toString(ins.timeRange[1])));
			}
			
			block.putCommand(c);
		}
		
		if (flags != 0) {
			block.putCommand(new ArgScriptCommand("flags", "0x" + Integer.toHexString(flags)));
		}
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		throw new UnsupportedOperationException("Inline sequence effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		Effect[] effects = new Effect[instances.size()];
		
		for (int i = 0; i < effects.length; i++) {
			if (instances.get(i).effectIndex != -1) {
				if ((instances.get(i).effectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					effects[i] = new ImportedEffect(parent.getImports().get(instances.get(i).effectIndex));
				}
				else {
					effects[i] = EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, instances.get(i).effectIndex);
				}
			}
		}
		
		return effects;
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		Integer index = baseIndices.get(VisualEffect.TYPE);
		if (index != null && index != -1) {
			for (SequenceInstance ins : instances) {
				ins.effectIndex += index;
			}
		}
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"play", "flags"
		};
	}
}
