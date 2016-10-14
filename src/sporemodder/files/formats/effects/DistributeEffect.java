package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

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

public class DistributeEffect extends EffectComponent {
	
	/*
	 * FLAGS:
	 *  -> 0x0156 -> model ??
	 */
	
	public static final int TYPE = 0x0D;
	public static final int MIN_VERSION = 3;
	public static final int MAX_VERSION = 4;
	public static final String KEYWORD = "distribute";
	
	private static final ArgScriptEnum ENUM_SOURCE = new ArgScriptEnum(new String[] {
		"square", "circle", "ring", "sphereSurface", "cube", "sphere", "sphereCubeSurface"
	}, new int[] {
		0, 1, 2, 3, 4, 5, 6
	});
	
	private static final int FLAG_SCALE = 1;
	private static final int FLAG_ROTATE = 2;
	private static final int FLAG_OFFSET = 4;
	
	// 0x40 (1000000b) -> heading, pitch, roll ?
	// 0x100 (00000001 00000000b) -> model ?
	
	private int flags;
	private int density = 1;
	private int effectIndex = -1;
	private int sourceType;  // byte
	private int sourceSize;
	private int field_1C;  // byte
	private float start = 1.0f;
	
	private int preTransformFlags;  // short
	private float preTransformScale;
	private final Matrix preTransformRot = Matrix.getIdentity();
	private final float[] preTransformPos = new float[3];
	
	private float[] size = { 1.0f };
	private float sizeVary;
	private float[] pitch = new float[0];
	private float[] roll = new float[0];
	private float[] yaw = new float[0];
	private float pitchVary;
	private float rollVary;
	private float yawVary;
	private float pitchOffset;
	private float rollOffset;
	private float yawOffset;
	private EffectColor[] color = { EffectColor.WHITE };
	private final EffectColor colorVary = EffectColor.WHITE;
	private float[] alpha = { 1.0f };
	private float alphaVary;
	
	private final List<Surface> surfaces = new ArrayList<Surface>();
	
	private final ResourceID emitMap = new ResourceID();
	private final ResourceID colorMap = new ResourceID();
	private final ResourceID pinMap = new ResourceID();
	
	private final float[] altitudeRange = { -10000.0f, 10000.0f };
	
	private final TextureSlot resource = new TextureSlot();
	private int overrideSet;  // byte
	private int messageID;
	
	private int field_160;
	private int field_164;
	private float rotateVary;
	private float[] rotate = new float[0];

	public DistributeEffect(int type, int version) {
		super(type, version);
	}

	public DistributeEffect(DistributeEffect effect) {
		super(effect);
		
		flags = effect.flags;
		density = effect.density;
		effectIndex = effect.effectIndex;
		sourceType = effect.sourceType;
		sourceSize = effect.sourceSize;
		field_1C = effect.field_1C;
		start = effect.start;
		
		preTransformFlags = effect.preTransformFlags;
		preTransformScale = effect.preTransformScale;
		preTransformRot.copy(effect.preTransformRot);
		EffectComponent.copyArray(preTransformPos, effect.preTransformPos);
		
		size = EffectComponent.copyArray(effect.size);
		sizeVary = effect.sizeVary;
		pitch = EffectComponent.copyArray(effect.pitch);
		roll = EffectComponent.copyArray(effect.roll);
		yaw = EffectComponent.copyArray(effect.yaw);
		pitchVary = effect.pitchVary;
		pitchOffset = effect.pitchOffset;
		rollVary = effect.rollVary;
		rollOffset = effect.rollOffset;
		yawVary = effect.yawVary;
		yawOffset = effect.yawOffset;
		color = EffectComponent.copyArray(effect.color);
		colorVary.copy(effect.colorVary);
		alpha = EffectComponent.copyArray(alpha);
		alphaVary = effect.alphaVary;
		
		for (int i = 0; i < effect.surfaces.size(); i++) {
			surfaces.add(new Surface(effect.surfaces.get(i)));		
		}
		emitMap.copy(effect.emitMap);
		colorMap.copy(effect.colorMap);
		pinMap.copy(effect.pinMap);
		EffectComponent.copyArray(altitudeRange, effect.altitudeRange);
		
		resource.copy(effect.resource);
		overrideSet = effect.overrideSet;
		messageID = effect.messageID;
		
		field_160 = effect.field_160;
		field_164 = effect.field_164;
		rotateVary = effect.rotateVary;
		rotate = EffectComponent.copyArray(effect.rotate);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		density = in.readInt();
		effectIndex = in.readInt();
		sourceType = in.readByte();
		sourceSize = in.readInt();
		field_1C = in.readByte();
		start = in.readFloat();
		
		preTransformFlags = in.readShort();
		preTransformScale = in.readFloat();
		in.readLEFloats(preTransformPos);
		preTransformRot.readLE(in);
		
		size = new float[in.readInt()];
		in.readFloats(size);
		sizeVary = in.readFloat();
		
		pitch = new float[in.readInt()];
		in.readFloats(pitch);
		roll = new float[in.readInt()];
		in.readFloats(roll);
		yaw = new float[in.readInt()];
		in.readFloats(yaw);
		
		pitchVary = in.readFloat();
		rollVary = in.readFloat();
		yawVary = in.readFloat();
		pitchOffset = in.readFloat();
		rollOffset = in.readFloat();
		yawOffset = in.readFloat();
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		colorVary.readLE(in);
		
		alpha = new float[in.readInt()];
		in.readFloats(alpha);
		alphaVary = in.readFloat();
		
		int surfaceCount = in.readInt();
		for (int i = 0; i < surfaceCount; i++) {
			Surface surface = new Surface();
			surface.read(in);
			surfaces.add(surface);
		}
		
		emitMap.read(in);
		colorMap.read(in);
		pinMap.read(in);
		
		in.readLEFloats(altitudeRange);
		resource.read(in);
		overrideSet = in.readByte();
		messageID = in.readInt();
		
		if (version > 3) {
			field_160 = in.readInt();
			field_164 = in.readInt();
			rotateVary = in.readFloat();
			rotate = new float[in.readInt()];
			in.readFloats(rotate);
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeInt(density);
		out.writeInt(effectIndex);
		out.writeByte(sourceType);
		out.writeInt(sourceSize);
		out.writeByte(field_1C);
		out.writeFloat(start);
		
		out.writeShort(preTransformFlags);
		out.writeFloat(preTransformScale);
		out.writeLEFloats(preTransformPos);
		preTransformRot.writeLE(out);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		out.writeFloat(sizeVary);
		
		out.writeInt(pitch.length);
		out.writeFloats(pitch);
		out.writeInt(roll.length);
		out.writeFloats(roll);
		out.writeInt(yaw.length);
		out.writeFloats(yaw);
		
		out.writeFloat(pitchVary);
		out.writeFloat(rollVary);
		out.writeFloat(yawVary);
		out.writeFloat(pitchOffset);
		out.writeFloat(rollOffset);
		out.writeFloat(yawOffset);
		
		out.writeInt(color.length);
		for (EffectColor c : color) {
			c.writeLE(out);
		}
		colorVary.writeLE(out);
		
		out.writeInt(alpha.length);
		out.writeFloats(alpha);
		out.writeFloat(alphaVary);
		
		out.writeInt(surfaces.size());
		for (Surface s : surfaces) {
			s.write(out);
		}
		
		emitMap.write(out);
		colorMap.write(out);
		pinMap.write(out);
		
		out.writeLEFloats(altitudeRange);
		resource.write(out);
		out.writeByte(overrideSet);
		out.writeInt(messageID);
		
		if (version > 3) {
			out.writeInt(field_160);
			out.writeInt(field_164);
			out.writeFloat(rotateVary);
			out.writeInt(rotate.length);
			out.writeFloats(rotate);
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptCommand c = null;
		String arg = null;
		if ((c = block.getCommand("color")) != null) {
			color = ArgScript.stringsToColors(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				colorVary.parse(arg);
			}
		}
		if ((c = block.getCommand("alpha")) != null) {
			alpha = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				alphaVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("size")) != null) {
			size = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				sizeVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("pitch")) != null) {
			pitch = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				pitchVary = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				pitchOffset = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("roll")) != null) {
			roll = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				rollVary = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				rollOffset = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("yaw")) != null) {
			yaw = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				yawVary = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("offset")) != null) {
				yawOffset = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("density")) != null) {
			density = Integer.parseInt(c.getSingleArgument());
			if ((arg = c.getOptionArg("start")) != null) {
				start = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("source")) != null) {
			sourceType = ENUM_SOURCE.getValue(c.getSingleArgument());
			if ((arg = c.getOptionArg("start")) != null) {
				sourceSize = Integer.parseInt(arg);
			}
		}
		if ((c = block.getCommand("effect")) != null) {
			effectIndex = parent.getEffectIndex(VisualEffect.TYPE, c.getSingleArgument());
			
			if ((arg = c.getOptionArg("offset")) != null) {
				ArgScript.parseFloatList(arg, preTransformPos);
				preTransformFlags |= FLAG_OFFSET;
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
			
			List<String> args = null;
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
				preTransformRot.rotate(Math.toRadians(euler[0]), Math.toRadians(euler[1]), Math.toRadians(euler[2]));
				preTransformRot.transpose();  // ?
				preTransformFlags |=FLAG_ROTATE;
			}
			if ((arg = c.getOptionArg("scale")) != null) {
				preTransformScale = Float.parseFloat(arg);
				preTransformFlags |= FLAG_SCALE;
			}
		}
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand command : commands) {
			if (command.getKeyword().equals("surface")) {
				if (command.hasFlag("reset")) {
					surfaces.clear();
				}
				Surface s = new Surface();
				s.parse(command, parent);
				surfaces.add(s);
			}
		}
		if ((c = block.getCommand("mapEmit")) != null) {
			emitMap.parseSpecial(c.getSingleArgument());
			List<String> args = null;
			if ((arg = c.getOptionArg("belowHeight")) != null) {
				altitudeRange[1] = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("aboveHeight")) != null) {
				altitudeRange[0] = Float.parseFloat(arg);
			}
			if ((args = c.getOptionArgs("heightRange", 2)) != null) {
				altitudeRange[0] = Float.parseFloat(args.get(0));
				altitudeRange[1] = Float.parseFloat(args.get(1));
			}
		}
		if ((c = block.getCommand("mapEmitColor")) != null) {
			colorMap.parseSpecial(c.getSingleArgument());
		}
		if ((c = block.getCommand("mapPin")) != null) {
			pinMap.parseSpecial(c.getSingleArgument());
		}
		if ((c = block.getCommand("resource")) != null) {
			resource.parse(c);
			if ((arg = c.getOptionArg("overrideSet")) != null) {
				overrideSet = Integer.parseInt(arg);
			}
		}
		if ((c = block.getCommand("message")) != null) {
			messageID = Hasher.getFileHash(c.getSingleArgument());
		}
		if ((c = block.getCommand("rotate")) != null) {
			rotate = ArgScript.stringsToFloats(c.getArguments());
			if ((arg = c.getOptionArg("vary")) != null) {
				rotateVary = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("field_160")) != null) {
			field_160 = Integer.parseInt(c.getSingleArgument());
		}
		if ((c = block.getCommand("field_164")) != null) {
			field_164 = Integer.parseInt(c.getSingleArgument());
		}
		if ((c = block.getCommand("field_1C")) != null) {
			field_1C = Hasher.decodeByte(c.getSingleArgument());
		}
		if ((c = block.getCommand("field_20")) != null) {
			start = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("flags")) != null) {
			flags = Hasher.decodeInt(c.getSingleArgument());
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		if (color.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("color", ArgScript.colorsToStrings(color));
			if (!colorVary.equals(EffectColor.WHITE)) c.putOption(new ArgScriptOption("vary", colorVary.toString()));
			block.putCommand(c);
		}
		if (alpha.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha));
			if (alphaVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(alphaVary)));
			block.putCommand(c);
		}
		if (size.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("size", ArgScript.floatsToStrings(size));
			if (sizeVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(sizeVary)));
			block.putCommand(c);
		}
		if (pitch.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("pitch", ArgScript.floatsToStrings(pitch));
			if (pitchVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(pitchVary)));
			if (pitchOffset != 0) c.putOption(new ArgScriptOption("offset", Float.toString(pitchOffset)));
			block.putCommand(c);
		}
		if (roll.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("roll", ArgScript.floatsToStrings(roll));
			if (rollVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(rollVary)));
			if (rollOffset != 0) c.putOption(new ArgScriptOption("offset", Float.toString(rollOffset)));
			block.putCommand(c);
		}
		if (yaw.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("yaw", ArgScript.floatsToStrings(yaw));
			if (yawVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(yawVary)));
			if (yawOffset != 0) c.putOption(new ArgScriptOption("offset", Float.toString(yawOffset)));
			block.putCommand(c);
		}
		if (density != 1 || start != 1.0f) {
			ArgScriptCommand c = new ArgScriptCommand("density", Integer.toString(density));
			if (start != 1.0f) {
				c.putOption(new ArgScriptOption("start", Float.toString(start)));
			}
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("source", ENUM_SOURCE.getKey(sourceType));
			if (sourceSize != 0) {
				c.putOption(new ArgScriptOption("scale", Integer.toString(sourceSize)));
			}
			block.putCommand(c);
		}
		if (effectIndex != -1) {
			Effect effect = parent.getEffect(VisualEffect.TYPE, effectIndex);
			ArgScriptCommand c = new ArgScriptCommand("effect", 
					effect == null ? VisualEffect.KEYWORD + "-" + effectIndex : effect.getName());
			if ((preTransformFlags & FLAG_SCALE) == FLAG_SCALE) {
				c.putOption(new ArgScriptOption("scale", Float.toString(preTransformScale)));
			}
			if ((preTransformFlags & FLAG_ROTATE) == FLAG_ROTATE) {
				float[] euler = preTransformRot.toEulerDegrees();
				if (euler[0] != 0 && euler[1] != 0 && euler[2] != 0) {
					c.putOption(new ArgScriptOption("rotateXYZ", Float.toString(euler[0]), Float.toString(euler[1]), Float.toString(euler[2])));
				}
				else {
					if (euler[0] != 0) c.putOption(new ArgScriptOption("rotateX", Float.toString(euler[0])));
					if (euler[1] != 0) c.putOption(new ArgScriptOption("rotateY", Float.toString(euler[1])));
					if (euler[2] != 0) c.putOption(new ArgScriptOption("rotateZ", Float.toString(euler[2])));
				}
			}
			if ((preTransformFlags & FLAG_OFFSET) == FLAG_OFFSET) {
				c.putOption(new ArgScriptOption("pos", Float.toString(preTransformPos[0]), Float.toString(preTransformPos[1]), Float.toString(preTransformPos[2])));
			}
			block.putCommand(c);
		}
		//TODO subdivide
		for (Surface s : surfaces) {
			block.putCommand(s.toCommand(new ArgScriptCommand("surface"), parent));
		}
		if (!emitMap.isDefault()) {
			ArgScriptCommand c = new ArgScriptCommand("mapEmit", emitMap.toString());
			if (altitudeRange[0] != -10000.0f || altitudeRange[1] != 10000.0f) {
				boolean hasOption = false;
				ArgScriptOption o = null;
				if (altitudeRange[1] != 10000) {
					o = new ArgScriptOption("belowHeight", Float.toString(altitudeRange[1]));
					hasOption = true;
				}
				if (altitudeRange[0] != -10000) {
					o = new ArgScriptOption("aboveHeight", Float.toString(altitudeRange[0]));
					if (hasOption) {
						hasOption = false;
					} else {
						hasOption = true;
					}
				}
				if (!hasOption) {
					c.putOption(new ArgScriptOption("heightRange", Float.toString(altitudeRange[0]), Float.toString(altitudeRange[1])));
				} else {
					c.putOption(o);
				}
			}
			block.putCommand(c);
		}
		if (!colorMap.isDefault()) {
			block.putCommand(new ArgScriptCommand("mapEmitColor", colorMap.toString()));
		}
		if (!pinMap.isDefault()) {
			block.putCommand(new ArgScriptCommand("mapPin", pinMap.toString()));
		}
		{
			ArgScriptCommand c = resource.toCommand("resource");
			if (overrideSet != 0) {
				c.putOption(new ArgScriptOption("overrideSet", Integer.toString(overrideSet)));
			}
			block.putCommand(c);
		}
		if (messageID != 0) {
			block.putCommand(new ArgScriptCommand("message", Hasher.getFileName(messageID)));
		}
		if (rotate.length > 0) {
			ArgScriptCommand c = new ArgScriptCommand("rotate", ArgScript.floatsToStrings(rotate));
			if (rotateVary != 0) {
				c.putOption(new ArgScriptOption("vary", Float.toString(rotateVary)));
			}
			block.putCommand(c);
		}
		if (field_160 != 0) {
			block.putCommand(new ArgScriptCommand("field_160", Integer.toString(field_160)));
		}
		if (field_164 != 0) {
			block.putCommand(new ArgScriptCommand("field_164", Integer.toString(field_164)));
		}
		if (field_1C != 0) {
			block.putCommand(new ArgScriptCommand("field_1C", "0x" + Integer.toHexString(field_1C)));
		}
		if (flags != 0) {
			block.putCommand(new ArgScriptCommand("flags", Integer.toBinaryString(flags) + "b"));
		}
		
		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline distribute effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		List<Effect> effects = new ArrayList<Effect>();
		for (Surface s : surfaces) {
			int collisionEffectIndex = s.getCollisionEffectIndex();
			int deathEffectIndex = s.getDeathEffectIndex();
			
			if (collisionEffectIndex != -1) {
				if ((collisionEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					effects.add(new ImportedEffect(parent.getImports().get(collisionEffectIndex & ~EffectMain.IMPORT_MASK)));
				}
				else {
					effects.add(EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, collisionEffectIndex));
				}
			}
			if (deathEffectIndex != -1) {
				if ((deathEffectIndex & EffectMain.IMPORT_MASK) == EffectMain.IMPORT_MASK) {
					effects.add(new ImportedEffect(parent.getImports().get(deathEffectIndex & ~EffectMain.IMPORT_MASK)));
				}
				else {
					effects.add(EffectMain.getEffect(parent.getEffectMap(), VisualEffect.TYPE, deathEffectIndex));
				}
			}
		}
		
		if (effectIndex != -1) {
			effects.add(parent.getEffect(VisualEffect.TYPE, effectIndex));
		}
		
		effects.add(parent.getResource(MapResource.MASKED_TYPE, emitMap));
		effects.add(parent.getResource(MapResource.MASKED_TYPE, colorMap));
		effects.add(parent.getResource(MapResource.MASKED_TYPE, pinMap));
		
		return (Effect[]) effects.toArray(new Effect[effects.size()]);
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		Integer index = baseIndices.get(VisualEffect.TYPE);
		if (index != null && index != -1) {
			for (Surface s : surfaces) {
				s.fixEffectIndices(index);
			}
			if (effectIndex != -1) {
				effectIndex += index;
			}
		}
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
				"square", "circle", "ring", "sphereSurface", "cube", "sphere", "sphereCubeSurface"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"vary", "offset", "start", "scale", "rotateX", "rotateY", "rotateZ", "rotateXYZ", "rotateZYX", "offset",
			"belowHeight", "aboveHeight", "heightRange", "overrideSet"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"color", "alpha", "size", "pitch", "roll", "yaw", "density", "effect", "source", "surface",
			"mapEmit", "mapEmitColor", "mapPin", "resource", "flags", "rotate", "field_160", "field_164", 
			"field_1C", "message"
		};
	}

}
