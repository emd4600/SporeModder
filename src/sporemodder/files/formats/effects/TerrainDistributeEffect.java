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

public class TerrainDistributeEffect extends EffectComponent {
	
	public static final int TYPE = 0x2A;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "terrainDistribute";
	
	private static final int LEVEL_COUNT = 5;
	
	private static final int FLAG_HEIGHTS = 1;
	private static final int FLAG_TYPES = 2;
	private static final int FLAG_ACTIVE = 4;
	
	private int flags;  // 0x08  // & 8
	private final TerrainDistributeLevel[] levels = new TerrainDistributeLevel[LEVEL_COUNT];

	public TerrainDistributeEffect(int type, int version) {
		super(type, version);
	}

	public TerrainDistributeEffect(TerrainDistributeEffect effect) {
		super(effect);

		flags = effect.flags;
		for (int i = 0; i < LEVEL_COUNT; i++) {
			levels[i] = new TerrainDistributeLevel(effect.levels[i]);
		}
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		
		flags = in.readInt();  // & 8
		for (int i = 0; i < LEVEL_COUNT; i++) {
			levels[i] = new TerrainDistributeLevel();
			levels[i].read(in, version);
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		for (int i = 0; i < LEVEL_COUNT; i++) {
			if (levels[i] == null) {
				levels[i] = new TerrainDistributeLevel();
			}
			levels[i].write(out, version);
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		ArgScriptCommand c = null;
		if ((c = block.getCommand("debug")) != null) 
		{
			// showLevels is not used?
			if (c.hasFlag("heights")) flags |= FLAG_HEIGHTS;
			else flags &= ~FLAG_HEIGHTS;
			
			if (c.hasFlag("types")) flags |= FLAG_TYPES;
			else flags &= ~FLAG_TYPES;
			
			if (c.hasFlag("active")) flags |= FLAG_ACTIVE;
			else flags &= ~FLAG_ACTIVE;
		}
		Collection<ArgScriptBlock> blocks = block.getAllBlocks();
		List<Integer> customLevels = new ArrayList<Integer>();
		for (ArgScriptBlock b : blocks) {
			if (b.getKeyword().equals("level")) {
				int level = Integer.parseInt(b.getSingleArgument());
				customLevels.add(level - 1);
				TerrainDistributeLevel l = new TerrainDistributeLevel();
				l.parse(b, parent, levels, level, customLevels);
				levels[level - 1] = l;
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {

		if (flags != 0) {
			ArgScriptCommand c = new ArgScriptCommand("debug", new String[0]);
			if ((flags & FLAG_HEIGHTS) == FLAG_HEIGHTS) c.putFlag("heights");
			if ((flags & FLAG_TYPES) == FLAG_TYPES) c.putFlag("types");
			if ((flags & FLAG_ACTIVE) == FLAG_ACTIVE) c.putFlag("active");
			block.putCommand(c);
		}
		
		for (int i = 0; i < LEVEL_COUNT; i++) {
			if (!levels[i].isDefault()) {
				ArgScriptBlock b = new ArgScriptBlock("level", Integer.toString(i + 1));
				levels[i].toBlock(b, parent);
				block.putBlock(b);
			}
		}
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline " + KEYWORD + " effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		List<Effect> effects = new ArrayList<Effect>();
		for (TerrainDistributeLevel level : levels) {
			effects.addAll(level.getEffects(parent));
		}
		
		return (Effect[]) effects.toArray(new Effect[effects.size()]);
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		Integer index = baseIndices.get(VisualEffect.TYPE);
		if (index != null && index != -1) {
			for (TerrainDistributeLevel level : levels) {
				level.fixEffectIndices(index);
			}
		}
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"heights", "types", "active"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"debug", "level"
		};
	}

}
