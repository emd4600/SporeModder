package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;
import sporemodder.utilities.Matrix;

public class GameModelEffect extends EffectComponent {
	
	public static final int TYPE = 0x25;
	public static final int MIN_VERSION = 7;
	public static final int MAX_VERSION = 8;
	public static final String KEYWORD = "gameModel";
	
	private static final int FLAG_SCALE = 1;
	private static final int FLAG_ROTATE = 2;
	private static final int FLAG_OFFSET = 4;
	
	private static final ArgScriptEnum ENUM_GROUPS = new ArgScriptEnum(new String[] {
		"deformHandle", "deformHandleOverdraw", "background", "overdraw", "effectsMask",
		"testEnv", "partsPaintEnv", "skin", "rotationRing", "rotationBall",
		"socketConnector", "animatedCreature", "testMode", "vertebra", "paletteSkin",
		"excludeFromPinning", "palette", "ballConnector", "rigblock", "rigblockeffect",
		"gameBackground"
	}, new int[] {
		0x1BA53EA, 0x1BA53EB, 0x223E8E0, 0x22FFF11, 0x23008D4,
		0x26F3933, 0xFE39DE0, 0xFEB8DF2, 0x31390732, 0x31390733,
		0x4FF4AF74, 0x509991E6, 0x509AA7C9, 0x513CDFC1, 0x71257E8B,
		0x900C6ADD, 0x900C6AE5, 0x900C6CDD, 0x9138FD8D, 0x4FE3913,
		0x64AC354
	}, -1, null);
	
	private static final ArgScriptEnum ENUM_OPTIONS = new ArgScriptEnum(new String[] {
		"visible", "applyColor", "applyColorAsIdentity", "highlight", "aboveGround",
		"localZUp", "showDescription", "visualScale", "customPickLevel", "overrideBounds",
		"applyNightLights", "applyMaxIdentity", "alphaSort", "superHighLOD"
	}, new int[] {
		0, 			1, 			2, 						3,				4,
		5,			6,					7, 				8,				9, 
		10,					11, 				12,			13
	});
	
	private static final ArgScriptEnum ENUM_PICKLEVEL = new ArgScriptEnum(new String[] {
		"boundingSphere", "boundingBox", "hullTriangle", "meshCluster", "meshTriangle"
	}, new int[] {
		0, 1, 2, 3, 4	
	});
	
	private static final ArgScriptEnum ENUM_SPLITTER_TYPE = new ArgScriptEnum(new String[] {
		"negativeOnly", "positiveOnly", "binary"
	}, new int[] {
		1, 0, 2	
	});
	private static final ArgScriptEnum ENUM_SPLITTER_PREFERENCE = new ArgScriptEnum(new String[] {
		"default", "preferPositive", "preferNegative", "preferBest", "split", "none"
	}, new int[] {
		4, 0, 1, 2, 3, 4
	});
	
	private static final int SPLIT_FILTER = 0;
	private static final int SPLIT_CONTROL = 1;
	private static final int SPLIT_STATIC = 2;
	
	private static final ArgScriptEnum ENUM_SPLIT_TYPE = new ArgScriptEnum(new String[] {
			"filter", "control", "static"
		}, new int[] {
			SPLIT_FILTER, SPLIT_CONTROL, SPLIT_STATIC	
		});
	
	private static final int FLAG_MESSAGE = 4;
	private static final int FLAG_NOATTACHMENTS = 0x20;
	private static final int FLAG_FIXEDSIZE = 0x10;
	private static final int FLAG_PERSIST = 8;
	
	private static class ModelSplitter {
		private ModelSplitter() {};
		private ModelSplitter(ModelSplitter other) {
			splitterKernelIndex = other.splitterKernelIndex;
			splitType = other.splitType;
			preference = other.preference;
		}
		private int splitterKernelIndex;
		private int splitType;  // byte
		private int preference;  // byte
	}
	
	
	private static class ModelSplit {
		private ModelSplit() {};
		private ModelSplit(ModelSplit other) {
			type = other.type;
			splitterIndex = other.splitterIndex;
			transformFlags = other.transformFlags;
			rotation.copy(other.rotation);
			EffectComponent.copyArray(offset, other.offset);
			EffectComponent.copyArray(origin, other.origin);
			field_4C = EffectComponent.copyArray(other.field_4C);
		}
		private int type;  // byte
		private int splitterIndex;  // can point to a metaParticle too
		private int transformFlags;  // short
		private float scale;
		private final Matrix rotation = Matrix.getIdentity();
		private final float[] offset = new float[3];
		private int[] field_4C = new int[0];  // it does something weird to it
		private final float[] origin = new float[3];
	}
	
	private int flags;
	private float scale = 1.0f;
	private final EffectColor color = EffectColor.WHITE;
	private float alpha = 1.0f;
	private final ResourceID model = new ResourceID();
	private int world;
	
	private final List<GameModelAnim> animations = new ArrayList<GameModelAnim>();
	private final List<ModelSplitter> splitters = new ArrayList<ModelSplitter>();
	private final List<ModelSplit> splits = new ArrayList<ModelSplit>();
	private int[] groups = new int[0];
	
	private int options;
	private int pickLevel;  // byte
	private int overrideSet;  // byte
	private int message;  // only in version > 7

	public GameModelEffect(int type, int version) {
		super(type, version);
	}

	public GameModelEffect(GameModelEffect effect) {
		super(effect);
		
		flags = effect.flags;
		scale = effect.scale;
		color.copy(effect.color);
		alpha = effect.alpha;
		model.copy(effect.model);
		world = effect.world;
		
		// We don't copy the arrays
		groups = EffectComponent.copyArray(effect.groups);
		options = effect.options;
		pickLevel = effect.pickLevel;
		overrideSet = effect.overrideSet;
		message = effect.message;
		
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		scale = in.readFloat();
		color.readLE(in);
		alpha = in.readFloat();
		model.read(in);
		model.flip();
		world = in.readInt();
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			GameModelAnim anim = new GameModelAnim();
			anim.read(in);
			animations.add(anim);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ModelSplitter object = new ModelSplitter();
			object.splitterKernelIndex = in.readInt();
			object.splitType = in.readByte();
			object.preference = in.readByte();
			splitters.add(object);
		}
		
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			ModelSplit object = new ModelSplit();
			object.type = in.readByte();
			object.splitterIndex = in.readInt();
			object.transformFlags = in.readShort();
			object.scale = in.readFloat();
			object.rotation.readLE(in);
			in.readLEFloats(object.offset);
			object.field_4C = new int[in.readInt()];
			in.readInts(object.field_4C);
			in.readLEFloats(object.origin);
			splits.add(object);
		}
		
		groups = new int[in.readInt()];
		in.readInts(groups);
		
		options = in.readInt();
		pickLevel = in.readByte();
		overrideSet = in.readByte();
		if (version > 7) {
			message = in.readInt();
		} else {
			flags &= 0xFFFFFFFB;
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeFloat(scale);
		color.writeLE(out);
		out.writeFloat(alpha);
		model.flip();
		model.write(out);
		out.writeInt(world);
		
		out.writeInt(animations.size());
		for (GameModelAnim anim : animations) {
			anim.write(out);
		}
		
		out.writeInt(splitters.size()) ;
		for (ModelSplitter splitter : splitters) {
			out.writeInt(splitter.splitterKernelIndex);
			out.writeByte(splitter.splitType);
			out.writeByte(splitter.preference);
		}
		
		out.writeInt(splits.size());
		for (ModelSplit split : splits) {
			out.writeByte(split.type);
			out.writeInt(split.splitterIndex);
			out.writeShort(split.transformFlags);
			out.writeFloat(split.scale);
			split.rotation.writeLE(out);
			out.writeLEFloats(split.offset);
			out.writeInt(split.field_4C.length);
			out.writeInts(split.field_4C);
			out.writeLEFloats(split.origin);
		}
	
		out.writeInt(groups.length);
		out.writeInts(groups);
		
		out.writeInt(options);
		out.writeByte(pickLevel);
		out.writeByte(overrideSet);
		if (version > 7) {
			out.writeInt(message);
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptCommand c = null;
		if ((c = block.getCommand("name")) != null) 
		{
			//TODO it does something weird here, related with messages
			model.parse(c.getSingleArgument());
			String arg = c.getOptionArg("message");
			if (arg != null) {
				message = Hasher.getFileHash(arg);
				flags |= FLAG_MESSAGE;
			}
			if ((arg = c.getOptionArg("overrideSet")) != null) {
				overrideSet = Integer.parseInt(arg);
			}
			if (c.hasFlag("noAttachments")) {
				flags |= FLAG_NOATTACHMENTS;
			}
		}
		if ((c = block.getCommand("size")) != null) 
		{
			scale = Float.parseFloat(c.getSingleArgument());
			if (c.hasFlag("fixed")) {
				flags |= FLAG_FIXEDSIZE;
			}
		}
		if ((c = block.getCommand("color")) != null) 
		{
			color.parse(c.getSingleArgument());
		}
		if ((c = block.getCommand("alpha")) != null) 
		{
			alpha = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("world")) != null) 
		{
			//TODO it does something weird here, related with messages
			world = Hasher.getFileHash(c.getSingleArgument());
		}
		if ((c = block.getCommand("persist")) != null) 
		{
			boolean value = Boolean.parseBoolean(c.getSingleArgument());
			if (value) {
				flags |= FLAG_PERSIST;
			} else {
				flags &= ~FLAG_PERSIST;
			}
		}
		if ((c = block.getCommand("groups")) != null) 
		{
			List<String> args = c.getArguments();
			groups = new int[args.size()];
			for (int i = 0; i < groups.length; i++) {
				String str = args.get(0);
				int value = ENUM_GROUPS.getValue(str);
				if (value == -1) {
					value = Hasher.getFileHash(str);
				}
				groups[i] = value;
			}
		}
		if ((c = block.getCommand("options")) != null) 
		{
			List<String> args = c.getArguments();
			for (int i = 0; i < args.size(); i++) {
				int num = ENUM_OPTIONS.getValue(args.get(0));
				options |= (1 << num);
			}
		}
		if ((c = block.getCommand("pickLevel")) != null) 
		{
			pickLevel = ENUM_PICKLEVEL.getValue(c.getSingleArgument());
		}

		
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand command : commands) {
			if (command.getKeyword().equals("animate")) {
				GameModelAnim anim = new GameModelAnim();
				anim.parse(command);
				animations.add(anim);
			}
			else if (command.getKeyword().equals("splitter")) {
				List<String> args = command.getArguments(3);
				ModelSplitter splitter = new ModelSplitter();
				splitter.splitterKernelIndex = parent.getEffectIndex(SplitModelKernelResource.MASKED_TYPE, args.get(0));
				splitter.splitType = ENUM_SPLITTER_TYPE.getValue(args.get(1));
				splitter.preference = ENUM_SPLITTER_PREFERENCE.getValue(args.get(2));
				splitters.add(splitter);
			}
			else if (command.getKeyword().equals("split")) {
				parseSplit(command);
			}
		}
		
		return true;
	}
	
	private void parseSplit(ArgScriptCommand c) throws ArgScriptException {
		List<String> args = c.getArguments(2, Integer.MAX_VALUE);
		int argCount = args.size();
		ModelSplit split = new ModelSplit();
		split.type = ENUM_SPLIT_TYPE.getValue(args.get(0));
		int argIndex = 1;
		if (split.type == SPLIT_CONTROL) {
			if (argCount < 3) {
				throw new ArgScriptException(KEYWORD + ": Either meta particle or at least one split index unspecified ('split').");
			}
			split.splitterIndex = parent.getEffectIndex(MetaparticleEffect.TYPE, args.get(argIndex++));
		}
		else {
			if (split.type == SPLIT_FILTER) {
				split.splitterIndex = splitters.size();
			}
		}
		
		split.field_4C = new int[argCount - argIndex];
		for (int i = argIndex; i < argCount; i++) {
			split.field_4C[i - argIndex] = Integer.parseInt(args.get(i));
		}
		
		//TODO Origin
		
		String arg = null;
		if ((arg = c.getOptionArg("offset")) != null) {
			ArgScript.parseFloatList(arg, split.offset);
			split.transformFlags |= FLAG_OFFSET;
		}
		float[] euler = new float[3];
		
		if ((arg = c.getOptionArg("rotateZ")) != null) {
			euler[2] = Float.parseFloat(arg);
		}
		
		if ((arg = c.getOptionArg("rotateY")) != null) {
			euler[1] = Float.parseFloat(arg);
		}
		
		if ((arg = c.getOptionArg("rotateX")) != null) {
			euler[0] = Float.parseFloat(arg);
		}
		
		if ((args = c.getOptionArgs("rotateZYX", 3)) != null) {
			euler[2] = Float.parseFloat(args.get(0));
			euler[1] = Float.parseFloat(args.get(1));
			euler[0] = Float.parseFloat(args.get(2));
		}
		
		if ((args = c.getOptionArgs("rotateXYZ", 3)) != null) {
			euler[0] = Float.parseFloat(args.get(0));
			euler[1] = Float.parseFloat(args.get(1));
			euler[2] = Float.parseFloat(args.get(2));
		}
		
		if (euler[0] != 0 || euler[1] != 0 || euler[2] != 0) {
			split.rotation.rotate(Math.toRadians(euler[0]), Math.toRadians(euler[1]), Math.toRadians(euler[2]));
			split.rotation.transpose();  // ?
			split.transformFlags |=FLAG_ROTATE;
		}
		if ((arg = c.getOptionArg("scale")) != null) {
			split.scale = Float.parseFloat(arg);
			split.transformFlags |= FLAG_SCALE;
		}
		
		splits.add(split);
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {

		{
			ArgScriptCommand c = new ArgScriptCommand("name", model.toString());
			if ((flags & FLAG_MESSAGE) == FLAG_MESSAGE) {
				c.putOption(new ArgScriptOption("message", Hasher.getFileName(message, "0x")));
			}
			if (overrideSet != 0) {
				c.putOption(new ArgScriptOption("overrideSet", Integer.toString(overrideSet)));
			}
			if ((flags & FLAG_NOATTACHMENTS) == FLAG_NOATTACHMENTS) {
				c.putFlag("noAttachments");
			}
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("size", Float.toString(scale));
			if ((flags & FLAG_FIXEDSIZE) == FLAG_FIXEDSIZE) {
				c.putFlag("fixed");
			}
			block.putCommand(c);
		}
		block.putCommand(new ArgScriptCommand("color", color.toString()));
		block.putCommand(new ArgScriptCommand("alpha", Float.toString(alpha)));
		if (world != 0) {
			block.putCommand(new ArgScriptCommand("world", Hasher.getFileName(world, "0x")));
		}
		if ((flags & FLAG_PERSIST) == FLAG_PERSIST) {
			block.putCommand(new ArgScriptCommand("persist", "true"));
		}
		if (groups.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("groups");
			for (int i : groups) {
				String key = ENUM_GROUPS.getKey(i);
				c.addArgument(key == null || key.equals(ArgScriptEnum.DEFAULT_KEY) ? Hasher.getFileName(i, "0x") : key);
			}
			block.putCommand(c);
		}
		if (options != 0) {
			ArgScriptCommand c = new ArgScriptCommand("options");
			for (int i = 0; i < 32; i++) {
				if ((options & (1 << i)) != 0) {
					c.addArgument(ENUM_OPTIONS.getKey(i));
				}
			}
			block.putCommand(c);
		}
		if (pickLevel != 0) {
			block.putCommand(new ArgScriptCommand("pickLevel", Integer.toString(pickLevel)));
		}
		for (GameModelAnim anim : animations) {
			block.putCommand(anim.toCommand());
		}
		for (ModelSplitter splitter : splitters) {
			block.putCommand(new ArgScriptCommand("splitter", 
					parent.getEffect(SplitModelKernelResource.MASKED_TYPE, splitter.splitterKernelIndex).getName(),
					ENUM_SPLITTER_TYPE.getKey(splitter.splitType),
					ENUM_SPLITTER_PREFERENCE.getKey(splitter.preference)));
		}
		for (ModelSplit split : splits) {
			block.putCommand(splitToCommand(split));
		}
		
		return true;
	}
	
	private ArgScriptCommand splitToCommand(ModelSplit split) {
		ArgScriptCommand c = new ArgScriptCommand("split", ENUM_SPLIT_TYPE.getKey(split.type));
		
		if (split.type == SPLIT_CONTROL) {
			Effect eff = parent.getEffect(MetaparticleEffect.TYPE, split.splitterIndex);
			c.addArgument(eff == null ? 
					MetaparticleEffect.KEYWORD + "-" + split.splitterIndex : eff.getName());
		}
		for (int i : split.field_4C) {
			c.addArgument(Integer.toString(i));
		}
		
		//TODO origin
		
		if ((split.transformFlags & FLAG_OFFSET) == FLAG_OFFSET) {
			c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(split.offset)));
			if (split.origin[0] != 0 || split.origin[1] != 0 || split.origin[2] != 0) {
				c.putOption(new ArgScriptOption("origin", ArgScript.createFloatList(split.origin)));
			}
		}
		if ((split.transformFlags & FLAG_ROTATE) != 0) {
			float[] euler = split.rotation.toEulerDegrees();
			if (euler[0] != 0 && euler[1] != 0 && euler[2] != 0) {
				c.putOption(new ArgScriptOption("rotateXYZ", Float.toString(euler[0]), Float.toString(euler[1]), Float.toString(euler[2])));
			}
			else {
				if (euler[0] != 0) c.putOption(new ArgScriptOption("rotateX", Float.toString(euler[0])));
				if (euler[1] != 0) c.putOption(new ArgScriptOption("rotateY", Float.toString(euler[1])));
				if (euler[2] != 0) c.putOption(new ArgScriptOption("rotateZ", Float.toString(euler[2])));
			}
		}
		if ((split.transformFlags & FLAG_SCALE) == FLAG_SCALE) {
			c.putOption(new ArgScriptOption("scale", Float.toString(split.scale)));
		}
		
		return c;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		String arg = null;
		if ((arg = command.getOptionArg("name")) != null) 
		{
			//TODO it does something weird here, related with messages
			model.parse(arg);
		} else {
			throw new ArgScriptException("Need at least -name for anonymous gameModel effect.");
		}
		if ((arg = command.getOptionArg("message")) != null) 
		{
			message = Hasher.getFileHash(arg);
			flags |= FLAG_MESSAGE;
		}
		if ((arg = command.getOptionArg("size")) != null) 
		{
			scale = Float.parseFloat(arg);
		}
		if ((arg = command.getOptionArg("alpha")) != null) 
		{
			alpha = Float.parseFloat(arg);
		}
		if ((arg = command.getOptionArg("color")) != null) 
		{
			color.parse(arg);
		}
		if ((arg = command.getOptionArg("world")) != null) 
		{
			world = Hasher.getFileHash(arg);
		}
	}

	@Override
	public Effect[] getEffects() {
		List<Effect> effects = new ArrayList<Effect>();
		
		for (ModelSplitter splitter : splitters) {
			effects.add(parent.getEffect(SplitModelKernelResource.MASKED_TYPE, splitter.splitterKernelIndex));
		}
		for (ModelSplit split : splits) {
			if (split.type == SPLIT_CONTROL) {
				effects.add(parent.getEffect(MetaparticleEffect.TYPE, split.splitterIndex));
			}
		}
		
		return (Effect[]) effects.toArray(new Effect[effects.size()]);
	}
	

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"filter", "control", "static", "default", "preferPositive", "preferNegative", "preferBest", "split", "none",
			"negativeOnly", "positiveOnly", "binary", "boundingSphere", "boundingBox", "hullTriangle", "meshCluster", "meshTriangle",
			"visible", "applyColor", "applyColorAsIdentity", "highlight", "aboveGround",
			"localZUp", "showDescription", "visualScale", "customPickLevel", "overrideBounds",
			"applyNightLights", "applyMaxIdentity", "alphaSort", "superHighLOD",
			"deformHandle", "deformHandleOverdraw", "background", "overdraw", "effectsMask",
			"testEnv", "partsPaintEnv", "skin", "rotationRing", "rotationBall",
			"socketConnector", "animatedCreature", "testMode", "vertebra", "paletteSkin",
			"excludeFromPinning", "palette", "ballConnector", "rigblock", "rigblockeffect",
			"gameBackground",
			"true", "false"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"message", "overrideSet", "noAttachments", "fixed"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"name", "size", "color", "alpha", "world", "persist", "groups",
			"options", "pickLevel", "animate", "splitter", "split"
		};
	}
}
