package sporemodder.files.formats.effects;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Matrix;

public class VisualEffectBlock {
	private static final int FLAG_SCALE = 1;
	private static final int FLAG_ROTATE = 2;
	private static final int FLAG_OFFSET = 4;
	private static final int FLAG_IGNORELENGTH = 1;
	private static final int FLAG_RESPECTLENGTH = ~1;  // &, 0xFFFFFFFE
	private static final int FLAG_IGNOREPARAMS = 0x20;
	private static final int FLAG_RIGID = 0x40;
	
	private static final List<String> APP_FLAGS = Arrays.asList("kAppFlagDeepWater", "kAppFlagShallowWater", "kAppFlagLowerAtmosphere",
			"kAppFlagUpperAtmosphere", "kAppFlagEnglish", "kAppFlagPlanetHasWater", "kAppFlagCinematics", "kAppFlagCellGameMode", "kAppFlagCreatureGameMode",
			"kAppFlagTribeGameMode", "kAppFlagGGEMode", "kAppFlagCivGameMode", "kAppFlagSpaceGameMode", "kAppFlagAtmoLow", "kAppFlagAtmoMed",
			"kAppFlagAtmoHigh", "kAppFlagEditorMode", "kAppFlagSpaceGameOutOfUFO", "kAppFlagSpaceGameGalaxyMode", "kAppFlagSpaceGameSolarMode",
			"kAppFlagSpaceGamePlanetMode", "kAppFlagIsNight", "kAppFlagIsRaining", "kAppFlagWeatherIce", "kAppFlagWeatherCold",
			"kAppFlagWeatherWarm", "kAppFlagWeatherHot", "kAppFlagWeatherLava");
	
	private int blockType; //byte  // 0x00 ?
	private int flags1 = 0;  // 0x04
	private int flags2 = 0;  //short, 6 default?  0x8
	// 0x0A might be a counter or something like that?
	
	private float scale = 1;  // 0x18
	//TODO transpoed ??
	private Matrix rotation = Matrix.getIdentity();  // 0x1C
	private final float[] position = new float[3];  // 0x0C
	
	private int lodBegin = 1;  // 0x40
	private int lodEnd = 255;  //byte  // 0x41
	
	private float[] emitScale = new float[0];  // 0x44
	private float[] sizeScale = new float[0];  // 0x48
	private float[] alphaScale = new float[0];  // 0x4C
	
	private float emitScaleBegin = 1;  // 0x58
	private float emitScaleEnd = 1;  // 0x5C
	private float sizeScaleBegin = 1;  // 0x60
	private float sizeScaleEnd = 1;  // 0x64
	private float alphaScaleBegin = 1;  // 0x68
	private float alphaScaleEnd = 1;  // 0x6C
	
	private int appFlags;  // 0x70
	private int appFlagsMask;  // 0x74
	private int selectionGroup;  //short // 0x78
	private int selectionChance;  //short  // 0x7A
	
	private float timeScale = 1;  // 0x7C
	
	private int blockIndex = -1;  // 0x80
	private Effect effect;
//	private TreeMap<Integer, List<Effect>> effectMap;
	private EffectMain parent;
	
	public void read(InputStreamAccessor in) throws IOException {
		blockType = in.readUByte();
		if (blockType == 0) {
			
		}
		
		flags1 = in.readInt();
		flags2 = in.readUShort();
		
		scale = in.readFloat();
		rotation.readLE(in);
		rotation.transpose();  // ?
		for (int i = 0; i < 3; i++) {
			position[i] = in.readLEFloat();
		}
		
		lodBegin = in.readUByte();
		lodEnd = in.readUByte();
		
		int count = in.readInt();
		emitScale = new float[count];
		sizeScale = new float[count];
		alphaScale = new float[count];
		for (int i = 0; i < count; i++) {
			emitScale[i] = in.readFloat();
			sizeScale[i] = in.readFloat();
			alphaScale[i] = in.readFloat();
		}
		
		emitScaleBegin = in.readFloat();
		emitScaleEnd = in.readFloat();
		sizeScaleBegin = in.readFloat();
		sizeScaleEnd = in.readFloat();
		alphaScaleBegin = in.readFloat();
		alphaScaleEnd = in.readFloat();
		
		appFlags = in.readInt();
		appFlagsMask = in.readInt();
		selectionGroup = in.readUShort();
		selectionChance = in.readUShort();
		
		timeScale = in.readFloat();
		
		blockIndex = in.readInt();
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeUByte(blockType);
		
		out.writeInt(flags1);
		out.writeUShort(flags2);
		
		out.writeFloat(scale);
		rotation.writeLE(out);
		for (int i = 0; i < 3; i++) {
			out.writeLEFloat(position[i]);
		}
		
		out.writeUByte(lodBegin);
		out.writeUByte(lodEnd);
		
		out.writeInt(emitScale.length);
		for (int i = 0; i < emitScale.length; i++) {
			out.writeFloat(emitScale[i]);
			out.writeFloat(sizeScale[i]);
			out.writeFloat(alphaScale[i]);
		}
		
		out.writeFloat(emitScaleBegin);
		out.writeFloat(emitScaleEnd);
		out.writeFloat(sizeScaleBegin);
		out.writeFloat(sizeScaleEnd);
		out.writeFloat(alphaScaleBegin);
		out.writeFloat(alphaScaleEnd);
		
		out.writeInt(appFlags);
		out.writeInt(appFlagsMask);
		out.writeUShort(selectionGroup);
		out.writeUShort(selectionChance);
		
		out.writeFloat(timeScale);
		
		out.writeInt(blockIndex);
	}
	
	public void parse(ArgScriptCommand command) throws ArgScriptException {
		List<String> args = null;
		String arg = null;
		{
			TreeMap<Integer, List<Effect>> effectMap = parent.getEffectMap();
			args = command.getArguments();
			
			
			blockType = EffectMain.getType(command.getKeyword());
			if (blockType == -1) return;
			if (blockType == VisualEffect.TYPE && args.size() > 0) {
				int indexOf = parent.getImports().indexOf(args.get(0));
				if (indexOf != -1) {
					blockIndex = indexOf | EffectMain.IMPORT_MASK;
				}
				else {
					effect = EffectMain.getEffect(effectMap, blockType, args.get(0));
					blockIndex = EffectMain.getEffectIndex(effectMap, blockType, effect);
				}
			}
			
			// effect isn't imported
			if (blockIndex == -1) {
				//check if it's inline
				Class<? extends EffectComponent> clazz = EffectComponent.SUPPORTED_COMPONENTS.get(blockType);
				
				if (clazz == null) {
					System.out.print("nulL");
				}
				EffectComponent effComp = null;
				try {
					Constructor<? extends EffectComponent> ctor = clazz.getConstructor(int.class, int.class);
					int version = 1;
					Field field;
					field = clazz.getField("MAX_VERSION");
					if (field != null) {
						version = field.getInt(null);
					}
					effComp = ctor.newInstance(blockType, version);
					effect = effComp;
				} catch (NoSuchFieldException | SecurityException | InstantiationException | 
						IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
						NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				if (effComp.isInline(command)) {
					// inline
					
					EffectMain.addEffect(effectMap, effect, blockType);
					((EffectComponent) effect).parseInline(command);
					blockIndex = EffectMain.getEffectIndex(effectMap, blockType, effect);
				}
				else {
					effect = EffectMain.getEffect(effectMap, blockType, args.get(0));
					blockIndex = EffectMain.getEffectIndex(effectMap, blockType, effect);
					if (blockIndex == -1) {
						throw new ArgScriptException("Effect component '" + args.get(0) + "' of type " + command.getKeyword() + " doesn't exist.");
					}
				}
			}
		}
		
		if ((args = command.getOptionArgs("emitScale", 1, 255)) != null) {
			
			emitScale = new float[args.size()];
			sizeScale = new float[emitScale.length];
			alphaScale = new float[emitScale.length];
			
			for (int i = 0; i < emitScale.length; i++) {
				emitScale[i] = Float.parseFloat(args.get(i));
				sizeScale[i] = 1.0f;
				alphaScale[i] = 1.0f;
			}
		}
		if ((args = command.getOptionArgs("sizeScale", 1, 255)) != null) {
			if (args.size() != sizeScale.length && sizeScale.length != 0) {
				throw new ArgScriptException("emitScale, sizeScale and alphaScale argument count doesn't match.");
			}
			
			sizeScale = new float[args.size()];
			
			for (int i = 0; i < sizeScale.length; i++) {
				sizeScale[i] = Float.parseFloat(args.get(i));
			}
			
			if (emitScale.length == 0) {
				emitScale = new float[sizeScale.length];
				
				for (int i = 0; i < emitScale.length; i++) {
					emitScale[i] = 1.0f;
				}
			}
			if (alphaScale.length == 0) {
				alphaScale = new float[sizeScale.length];
				
				for (int i = 0; i < emitScale.length; i++) {
					alphaScale[i] = 1.0f;
				}
			}
		}
		if ((args = command.getOptionArgs("alphaScale", 1, 255)) != null) {
			if (args.size() != alphaScale.length && alphaScale.length != 0) {
				throw new ArgScriptException("emitScale, sizeScale and alphaScale argument count doesn't match.");
			}
			
			alphaScale = new float[args.size()];
			
			for (int i = 0; i < alphaScale.length; i++) {
				alphaScale[i] = Float.parseFloat(args.get(i));
			}
			
			if (emitScale.length == 0) {
				emitScale = new float[alphaScale.length];
				
				for (int i = 0; i < emitScale.length; i++) {
					emitScale[i] = 1.0f;
				}
			}
			if (sizeScale.length == 0) {
				sizeScale = new float[alphaScale.length];
				
				for (int i = 0; i < sizeScale.length; i++) {
					sizeScale[i] = 1.0f;
				}
			}
		}
		
		// this can be overrided with the 'lodRange' option
//		lodBegin = 1;
//		lodEnd = emitScale.length == 0 ? 255 : emitScale.length;
		// Spore didn't do that automatically, we won't neither (basically because it causes effects not showing and such)
		lodBegin = 1;
		lodEnd = 255;
		
		if ((args = command.getOptionArgs("lodRange", 2)) != null) {
			lodBegin = Integer.parseInt(args.get(0));
			lodEnd = Integer.parseInt(args.get(1));
		}
		else if ((arg = command.getOptionArg("lod")) != null) {
			lodBegin = Integer.parseInt(arg);
			lodEnd = lodBegin + 1;
		}
		
		if ((arg = command.getOptionArg("emitScaleBegin")) != null) emitScaleBegin = Float.parseFloat(arg);
		if ((arg = command.getOptionArg("emitScaleEnd")) != null) emitScaleEnd = Float.parseFloat(arg);
		if ((arg = command.getOptionArg("sizeScaleBegin")) != null) sizeScaleBegin = Float.parseFloat(arg);
		if ((arg = command.getOptionArg("sizeScaleEnd")) != null) sizeScaleEnd = Float.parseFloat(arg);
		if ((arg = command.getOptionArg("alphaScaleBegin")) != null) alphaScaleBegin = Float.parseFloat(arg);
		if ((arg = command.getOptionArg("alphaScaleEnd")) != null) alphaScaleEnd = Float.parseFloat(arg);
		
		if ((arg = command.getOptionArg("timeScale")) != null) timeScale = Float.parseFloat(arg);
		
		if ((arg = command.getOptionArg("offset")) != null) {
			ArgScript.parseFloatList(arg, position);
			flags2 |= FLAG_OFFSET;
		}
		{
			float[] euler = new float[3];
			
			if ((arg = command.getOptionArg("rotateZ")) != null) euler[2] = Float.parseFloat(arg);
			if ((arg = command.getOptionArg("rotateY")) != null) euler[1] = Float.parseFloat(arg);
			if ((arg = command.getOptionArg("rotateX")) != null) euler[0] = Float.parseFloat(arg);
			
			if ((args = command.getOptionArgs("rotateZYX", 3)) != null) {
				euler[2] = Float.parseFloat(args.get(0));
				euler[1] = Float.parseFloat(args.get(1));
				euler[0] = Float.parseFloat(args.get(2));
			}
			
			if ((args = command.getOptionArgs("rotateXYZ", 3)) != null) {
				euler[0] = Float.parseFloat(args.get(0));
				euler[1] = Float.parseFloat(args.get(1));
				euler[2] = Float.parseFloat(args.get(2));
			}
			
			if (euler[0] != 0 || euler[1] != 0 || euler[2] != 0) {
				rotation.rotate(Math.toRadians(euler[0]), Math.toRadians(euler[1]), Math.toRadians(euler[2]));
				rotation.transpose();  // ?
				flags2 |=FLAG_ROTATE;
			}
		}
		if ((arg = command.getOptionArg("scale")) != null) {
			scale = Float.parseFloat(arg);
			flags2 |= FLAG_SCALE;
		}
		
		// Flags
		if (command.hasFlag("ignoreLength")) flags1 |= FLAG_IGNORELENGTH;
		if (command.hasFlag("respectLength")) flags1 &= FLAG_RESPECTLENGTH;
		if (command.hasFlag("ignoreParams")) flags1 |= FLAG_IGNOREPARAMS;
		if (command.hasFlag("rigid")) flags1 |= FLAG_RIGID;
		
		// Spore uses multiple 'flag' options, it's not supported here.
		if ((args = command.getOptionArgs("flag", 2, 64)) != null) {
			// there can be up to 32 flags
			if (args.size() % 2 != 0) {
				throw new ArgScriptException("'flag' option must have an even number of argument. Example: '-flag kAppFlagAtmoMed true kAppFlagEnglish false'.");
			}
			
			for (int i = 0; i < args.size()/2; i++) {
				arg = args.get(i * 2 + 0);
				int number = APP_FLAGS.indexOf(arg);
				if (number == -1) {
					try {
						number = Integer.parseInt(arg);
					} catch (NumberFormatException e) {
						throw new ArgScriptException("'flag' option must either be a valid flag or a number from 0 to 31.");
					}
				}
				boolean value = Boolean.parseBoolean(args.get(i * 2 + 1));
				int mask = 1 << number;
				
				appFlagsMask |= mask;
				if (value == true) {
					appFlags |= mask;
				}
			}
		}
		
		if ((arg = command.getOptionArg("prob")) != null) {
			selectionChance = (int)(ArgScript.parseRangedFloat(arg, 0, 1) * 65535.0f);
		}
		
	}
	
	
	private static boolean isWorth(float[] array, float notWorthValue) {
		for (int i = 0; i < array.length; i++) if (array[i] != notWorthValue) return true;
		return false;
	}
	 
	public ArgScriptCommand toCommand() {
		String keyword = EffectMain.getKeyword(blockType);
		//TODO maybe the name should be handled elsewhere
		ArgScriptCommand c;
		
		if ((blockIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
			c = new ArgScriptCommand(keyword, parent.getImports().get(blockIndex & ~EffectMain.IMPORT_MASK));
		}
		else {
			if (effect == null) {
				c = new ArgScriptCommand(keyword, keyword + "-" + blockIndex);
				if (keyword.equals(EffectMain.UNKNOWN_KEYWORD)) {
					c.addArgument("0x" + Integer.toHexString(blockType));
				}
			}
			else {
				if (effect.supportsBlock()) {
					c = new ArgScriptCommand(keyword, effect.getName());
					if (keyword.equals(EffectMain.UNKNOWN_KEYWORD)) {
						c.addArgument("0x" + Integer.toHexString(blockType));
					}
				}
				else {
					// component doesn't support block format, so it must be inlined here
					c = effect.toCommand();
				}
			}
		}
		
		//if (position[0] != 0 || position[1] != 0 || position[2] != 0) {
		if ((flags2 & FLAG_OFFSET) == FLAG_OFFSET) {
			c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(position)));
		}
		if ((flags2 & FLAG_ROTATE) != 0) {
			float[] euler = rotation.toEulerDegrees();
			if (euler[0] != 0 && euler[1] != 0 && euler[2] != 0) {
				c.putOption(new ArgScriptOption("rotateXYZ", Float.toString(euler[0]), Float.toString(euler[1]), Float.toString(euler[2])));
			}
			else {
				if (euler[0] != 0) c.putOption(new ArgScriptOption("rotateX", Float.toString(euler[0])));
				if (euler[1] != 0) c.putOption(new ArgScriptOption("rotateY", Float.toString(euler[1])));
				if (euler[2] != 0) c.putOption(new ArgScriptOption("rotateZ", Float.toString(euler[2])));
			}
		}
		//if (scale != 1) {
		if ((flags2 & FLAG_SCALE) == FLAG_SCALE) {
			c.putOption(new ArgScriptOption("scale", Float.toString(scale)));
		}
		
		if (lodBegin != 1 || (lodEnd != emitScale.length && lodEnd != 255)) {
			c.putOption(new ArgScriptOption("lodRange", Integer.toString(lodBegin), Integer.toString(lodEnd)));
		}
		
		if (isWorth(emitScale, 1.0f)) c.putOption(new ArgScriptOption("emitScale", ArgScript.floatsToStrings(emitScale)));
		if (isWorth(sizeScale, 1.0f)) c.putOption(new ArgScriptOption("sizeScale", ArgScript.floatsToStrings(sizeScale)));
		if (isWorth(alphaScale, 1.0f)) c.putOption(new ArgScriptOption("alphaScale", ArgScript.floatsToStrings(alphaScale)));
		
		if (emitScaleBegin != 1.0f) c.putOption(new ArgScriptOption("emitScaleBegin", Float.toString(emitScaleBegin)));
		if (emitScaleEnd != 1.0f) c.putOption(new ArgScriptOption("emitScaleEnd", Float.toString(emitScaleEnd)));
		if (sizeScaleBegin != 1.0f) c.putOption(new ArgScriptOption("sizeScaleBegin", Float.toString(sizeScaleBegin)));
		if (sizeScaleEnd != 1.0f) c.putOption(new ArgScriptOption("sizeScaleEnd", Float.toString(sizeScaleEnd)));
		if (alphaScaleBegin != 1.0f) c.putOption(new ArgScriptOption("alphaScaleBegin", Float.toString(alphaScaleBegin)));
		if (alphaScaleEnd != 1.0f) c.putOption(new ArgScriptOption("alphaScaleEnd", Float.toString(alphaScaleEnd)));
		
		if (timeScale != 1.0f) c.putOption(new ArgScriptOption("timeScale", Float.toString(timeScale)));
		
		// Flags
		if ((flags1 & FLAG_IGNORELENGTH) != 0) c.putFlag("ignoreLength");
		if ((flags1 & FLAG_IGNOREPARAMS) != 0) c.putFlag("ignoreParams");
		if ((flags1 & FLAG_RIGID) != 0) c.putFlag("rigid");
		
		// are there any remaining unknown flags?
		if ((flags1 & ~(FLAG_IGNORELENGTH | FLAG_IGNOREPARAMS | FLAG_RIGID)) != 0) {
			c.putOption(new ArgScriptOption("flags1", Hasher.hashToHex(flags1, "0x")));
		}
		// are there any remaining unknown flags?
		if ((flags2 & ~(FLAG_SCALE | FLAG_ROTATE | FLAG_OFFSET)) != 0) {
			c.putOption(new ArgScriptOption("flags2", Hasher.hashToHex(flags2, "0x")));
		}
		if (appFlagsMask != 0) {
			ArgScriptOption o = new ArgScriptOption("flag");
			for (int i = 0; i < 32; i++) {
				if ((appFlagsMask & (1 << i)) != 0) {
					String value = i >= APP_FLAGS.size() ? Integer.toString(i) : APP_FLAGS.get(i);
					o.addArgument(value);
					o.addArgument(Boolean.toString((appFlags & (1 << i)) == 1));
				}
			}
			c.putOption(o);
		}
		
//		
//		if (appFlags != 0) c.putOption(new ArgScriptOption("appFlags", Hasher.hashToHex(appFlags, "0x")));
//		if (appFlagsMask != 0) c.putOption(new ArgScriptOption("appFlagsMask", Hasher.hashToHex(appFlagsMask, "0x")));
//		
		//TODO selectionGroup and selectionChance
		
		if (selectionGroup != 0) {
			c.putOption(new ArgScriptOption("prob", Float.toString(selectionChance / 65535.0f)));
//			c.putOption(new ArgScriptOption("selectionGroup", Integer.toString(selectionGroup)));
		}
		
		return c;
	}
	
//	public void setEffectMap(TreeMap<Integer, List<Effect>> effectMap) {
//		this.effectMap = effectMap;
//	}
	
	public void setParent(EffectMain parent) {
		this.parent = parent;
	}
	
	public void setEffect(TreeMap<Integer, List<Effect>> components) {
		List<Effect> list = components.get(blockType);
		if (list != null && list.size() > 0 && blockIndex != -1) {
			if ((blockIndex & EffectMain.IMPORT_MASK) != EffectMain.IMPORT_MASK) {
				effect = list.get((blockIndex));
			}
		}
	}
	
	public Effect getEffect() {
		if (effect == null && (blockIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
			effect = new ImportedEffect(parent.getImports().get(blockIndex & ~EffectMain.IMPORT_MASK));
		}
		return effect;
	}
	
	
	public void setSelectionGroup(int selectionGroup) {
		this.selectionGroup = selectionGroup;
	}
	
	public int getSelectionGroup() {
		return selectionGroup;
	}

	public float getSelectionChance() {
		return selectionChance / 65535.0f;
	}
	
	public int getSelectionChanceAbs() {
		return selectionChance;
	}
	
	public void setSelectionChance(float selectionChance) {
		this.selectionChance = Math.round(selectionChance * 65535.0f);
	}
	
	public void setSelectionChanceAbs(int selectionChance) {
		this.selectionChance = selectionChance;
	}

	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		if (blockIndex != -1 && (blockIndex & EffectMain.IMPORT_MASK) != EffectMain.IMPORT_MASK) {
			Integer index = baseIndices.get(blockType);
			if (index != null && index != -1) blockIndex += index;
		}
	}
	
	public int getAppFlagsMask() {
		return appFlagsMask;
	}
	

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {"kAppFlagDeepWater", "kAppFlagShallowWater", "kAppFlagLowerAtmosphere",
				"kAppFlagUpperAtmosphere", "kAppFlagEnglish", "kAppFlagPlanetHasWater", "kAppFlagCinematics", "kAppFlagCellGameMode", "kAppFlagCreatureGameMode",
				"kAppFlagTribeGameMode", "kAppFlagGGEMode", "kAppFlagCivGameMode", "kAppFlagSpaceGameMode", "kAppFlagAtmoLow", "kAppFlagAtmoMed",
				"kAppFlagAtmoHigh", "kAppFlagEditorMode", "kAppFlagSpaceGameOutOfUFO", "kAppFlagSpaceGameGalaxyMode", "kAppFlagSpaceGameSolarMode",
				"kAppFlagSpaceGamePlanetMode", "kAppFlagIsNight", "kAppFlagIsRaining", "kAppFlagWeatherIce", "kAppFlagWeatherCold",
				"kAppFlagWeatherWarm", "kAppFlagWeatherHot", "kAppFlagWeatherLava"};
	}
	
	public static String[] getBlockTags() {
		return new String[] {};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"offset", "rotateX", "rotateY", "rotateZ", "rotateXYZ", "rotateZYX",
			"scale", "lodRange", "lod", "emitScaleBegin", "emitScaleEnd",
			"sizeScaleBegin", "sizeScaleEnd", "alphaScaleBegin", "alphaScaleEnd",
			"timeScale", "ignoreLength", "ignoreParams", "rigid",
			"flags1", "flags2", "flag", "appFlags", "appFlagsMask", "prob",
			"emitScale", "sizeScale", "alphaScale"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {};
	}
}
