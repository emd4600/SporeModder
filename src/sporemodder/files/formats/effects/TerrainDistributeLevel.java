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
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class TerrainDistributeLevel {
	
	private static final ArgScriptEnum ENUM_EFFECT_TYPE = new ArgScriptEnum(new String[] {
		"SmallPlant1", "SmallPlant2", "SmallPlant3", "MediumPlant1", "MediumPlant2", "MediumPlant3", "LargePlant1", "LargePlant2", "LargePlant3", "All"
	}, new int[] {
		6, 7, 8, 3, 4, 5, 0, 1, 2, 9
	});
	private static final ArgScriptEnum ENUM_EFFECT_RESOURCE = new ArgScriptEnum(new String[] {
			"lod0", "lod1", "lod2", "lod3", "all", 
		}, new int[] {
			0, 1, 2, 3, 5
		});
	
	public static class TerrainDistEffect {
		private int effectIndex;  // 0x00
		private final float[] heightRange = {0, Float.MAX_VALUE};  // 0x04
		private int type = -1;  // 0x0C
		private int resource = -1;  // 0x0D
		private int overrideSet;  // 0x0E
		
		public TerrainDistEffect() { }
		
		public TerrainDistEffect(TerrainDistEffect other) {
			effectIndex = other.effectIndex;
			heightRange[0] = other.heightRange[0];
			heightRange[1] = other.heightRange[1];
			type = other.type;
			resource = other.resource;
			overrideSet = other.overrideSet;
		}
		
		public void read(InputStreamAccessor in) throws IOException {
			effectIndex = in.readInt();
			heightRange[0] = in.readFloat();
			heightRange[1] = in.readFloat();
			type = in.readByte();
			resource = in.readByte();
			overrideSet = in.readByte();
		}
		
		public void write(OutputStreamAccessor out) throws IOException {
			out.writeInt(effectIndex);
			out.writeFloat(heightRange[0]);
			out.writeFloat(heightRange[1]);
			out.writeByte(type);
			out.writeByte(resource);
			out.writeByte(overrideSet);
		}
		
		public void parse(ArgScriptCommand c, EffectMain parent) throws ArgScriptException {
			List<String> args = null;
			String arg = c.getSingleArgument();
			effectIndex = parent.getEffectIndex(VisualEffect.TYPE, arg);
			
			if ((args = c.getOptionArgs("heightRange", 2)) != null) {
				heightRange[0] = Float.parseFloat(args.get(0));
				heightRange[1] = Float.parseFloat(args.get(1));
			}
			if ((arg = c.getOptionArg("type")) != null) {
				type = ENUM_EFFECT_TYPE.getValue(arg);
			}
			if ((arg = c.getOptionArg("resource")) != null) {
				resource = ENUM_EFFECT_RESOURCE.getValue(arg);
			}
			if ((arg = c.getOptionArg("overrideSet")) != null) {
				overrideSet = Integer.parseInt(arg);
			}
		}
		
		public ArgScriptCommand toCommand(EffectMain parent) {
			ArgScriptCommand c = new ArgScriptCommand("effect", parent.getEffect(VisualEffect.TYPE, effectIndex).getName());
			
			if (heightRange[0] != 0 || Float.floatToRawIntBits(heightRange[1]) != 0x7F7FFFFF) {
				c.putOption(new ArgScriptOption("heightRange", Float.toString(heightRange[0]), Float.toString(heightRange[1])));
			}
			if (type != -1) {
				c.putOption(new ArgScriptOption("type", ENUM_EFFECT_TYPE.getKey(type)));
			}
			if (resource != -1) {
				c.putOption(new ArgScriptOption("resource", ENUM_EFFECT_RESOURCE.getKey(resource)));
			}
			if (overrideSet != 0) {
				c.putOption(new ArgScriptOption("overrideSet", Integer.toString(overrideSet)));
			}
			
			return c;
		}
	}

	// offset in Spore ArgScript structure is 0xC or 0x10
	private float distance;  // 0x00  // possibly related with effects  // distance?
	private float distance_noEffects;  // 0x04
	private float verticalWeight = 1.0f;  // 0x08  // verticalWeight ?
	private float facing;  // 0x0C  // facing ?
	private float facing_noEffects;  // 0x10
	private final List<TerrainDistEffect> effects = new ArrayList<TerrainDistEffect>();  // 0x14
	
	public TerrainDistributeLevel() {
		
	}
	
	public TerrainDistributeLevel(TerrainDistributeLevel other) {
		distance = other.distance;
		distance_noEffects = other.distance_noEffects;
		verticalWeight = other.verticalWeight;
		facing = other.facing;
		facing_noEffects = other.facing_noEffects;
		
		effects.clear();
		for (int i = 0; i < other.effects.size(); i++) {
			effects.add(new TerrainDistEffect(other.effects.get(i)));
		}
	}
	
	public void read(InputStreamAccessor in, int version) throws IOException {
		distance = in.readFloat();
		distance_noEffects = in.readFloat();
		if (version >= 2) {
			verticalWeight = in.readFloat();
		}
		facing = in.readFloat();
		facing_noEffects = in.readFloat();
		
		int effectCount = in.readInt();
		for (int i = 0; i < effectCount; i++) {
			TerrainDistEffect eff = new TerrainDistEffect();
			eff.read(in);
			effects.add(eff);
		}
	}
	
	public void write(OutputStreamAccessor out, int version) throws IOException {
		out.writeFloat(distance);
		out.writeFloat(distance_noEffects);
		if (version >= 2) {
			out.writeFloat(verticalWeight);
		}
		out.writeFloat(facing);
		out.writeFloat(facing_noEffects);
		out.writeInt(effects.size());
		for (TerrainDistEffect eff : effects) {
			eff.write(out);
		}
	}
	
	public void parse(ArgScriptBlock block, EffectMain parent, TerrainDistributeLevel[] levels, int level, List<Integer> customLevels) throws ArgScriptException {
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("effect")) {
				TerrainDistEffect eff = new TerrainDistEffect();
				eff.parse(c, parent);
				effects.add(eff);
			}
		}
		
		boolean hasEffects = effects.size() > 0;
		
		ArgScriptCommand c = null;
		if ((c = block.getCommand("distance")) != null) {
			if (hasEffects) {
				distance = Float.parseFloat(c.getSingleArgument());
			} else {
				distance_noEffects = Float.parseFloat(c.getSingleArgument());
			}
			String arg = c.getOptionArg("verticalWeight");
			if (arg != null) {
				verticalWeight = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("facing")) != null) {
			if (hasEffects) {
				facing = Float.parseFloat(c.getSingleArgument());
			} else {
				facing_noEffects = Float.parseFloat(c.getSingleArgument());
			}
		}
		
		if (hasEffects) {
			// set previous levels values
			for (int i = level-2; i >= 0; i--) {
				// we don't want to automatically set those the user has set manually
				if (!customLevels.contains(i)) {
					if (levels[i] == null) {
						levels[i] = new TerrainDistributeLevel();
					}
					if (levels[i].effects.size() > 0) {
						// it has effects so the previous ones have been set by that one
						break;
					}
					levels[i].distance_noEffects = distance + 100;
					levels[i].facing_noEffects = facing + 0.1f;
				}
			}
		}
	}
	
	public void toBlock(ArgScriptBlock block, EffectMain parent) {
		if (effects.size() > 0) {
			ArgScriptCommand c = new ArgScriptCommand("distance", Float.toString(distance));
			if (verticalWeight != 1.0f) c.putOption(new ArgScriptOption("verticalWeight", Float.toString(verticalWeight)));
			block.putCommand(c);
			
			block.putCommand(new ArgScriptCommand("facing", Float.toString(facing)));
			
			for (TerrainDistEffect eff : effects) {
				block.putCommand(eff.toCommand(parent));
			}
		} else {
			ArgScriptCommand c = new ArgScriptCommand("distance", Float.toString(distance_noEffects));
			if (verticalWeight != 1.0f) c.putOption(new ArgScriptOption("verticalWeight", Float.toString(verticalWeight)));
			block.putCommand(c);
			
			block.putCommand(new ArgScriptCommand("facing", Float.toString(facing_noEffects)));
		}
	}
	
	public boolean isDefault() {
		return distance == 0 && distance_noEffects == 0 && verticalWeight == 1.0f && facing == 0 && facing_noEffects == 0;
	}
	
	public List<Effect> getEffects(EffectMain parent) {
		int count = effects.size();
		List<Effect> result = new ArrayList<Effect>(count);
		for (int i = 0; i < count; i++) {
			result.add(parent.getEffect(VisualEffect.TYPE, effects.get(i).effectIndex));
		}
		return result;
	}
	
	public void fixEffectIndices(int index) {
		for (TerrainDistEffect eff : effects) {
			eff.effectIndex += index;
		}
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"SmallPlant1", "SmallPlant2", "SmallPlant3", "MediumPlant1", "MediumPlant2", "MediumPlant3", "LargePlant1", "LargePlant2", "LargePlant3", "All",
			"lod0", "lod1", "lod2", "lod3", "all"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"verticalWeight", "heightRange", "type", "resource", "overrideSet"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"distance", "facing", "effect"
		};
	}
}
