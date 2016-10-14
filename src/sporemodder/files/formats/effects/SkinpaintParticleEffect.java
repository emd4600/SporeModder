package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class SkinpaintParticleEffect extends EffectComponent {
	
	private static final List<String> LIST_MODIFIER_TYPES = Arrays.asList("age", "random", "worldAngle", "worldPos", "worldDist",
			"boneAngle", "bonePos", "boneOffset", "torsoPos", "limbPos", "limbType", "region", "paintMask");
	
	private static final List<String> LIST_MODIFIER_CHANNELS = Arrays.asList("size", "aspect", "rotation", "spacing", "killswitch",
			"hairRadius", "alpha", "diffuseHue", "diffuseSat", "diffuseVal", "diffuseAlpha", "bumpScale", "bumpAlpha",
			"specularScale", "specularAlpha", "userVar1", "userVar2", "userVar3", "userVar4");
	
	private static final List<String> OPERATION_TYPES = Arrays.asList("set", "add", "mult", "div");
	private static final List<String> MODIFIER_FLAG = Arrays.asList(null, "open", "clamp", "wrap", "mirror", "clamp2", "wrap2", "mirror2");
	
	private static final ArgScriptEnum ENUM_BLEND = new ArgScriptEnum(new String[] {
			"alpha", "add", "substract", "multiply", "screen", "inherit", "default"
		}, new int[] {
			1, 2, 3, 4, 5, 0, 1
		});
	private static final ArgScriptEnum ENUM_FLAGS = new ArgScriptEnum(new String[] {
			"inherit", "move", "dir", "attract", "aroundbone", "alongbone", "aroundspine", "alongspine", "bone"
		}, new int[] {
			0, 1, 1, 2, 5, 4, 7, 6, 4
		}, 3, null);
	
	private static final int BRUSH_RANDOM = 1;
	private static final int BRUSH_RANDOMSTART = 2;
	private static final int CURVE_RANDOM = 4;
	
	private static final int ALIGN_INHERIT = 0;
	private static final int ALIGN_DIR = 1;  // also for move
	private static final int ALIGN_ATTRACT = 2;
	private static final int ALIGN_AROUNDBONE = 5;
	private static final int ALIGN_ALONGBONE = 4;
	private static final int ALIGN_AROUNDSPINE = 7;
	private static final int ALIGN_ALONGSPINE = 6;
	// these are determined by the 'bone' flag
	private static final int ALIGN_BONE = 4;
	private static final int ALIGN_NOBONE = 3;
	
	private static final int FLAG_ATTRACT_REVERSE = 0x10;
	private static final int FLAG_INITDIR_REVERSE = 0x20;
	private static final int FLAG_ALIGN_REVERSE = 0x40;
	private static final int FLAG_IDENTITY = 0x80;
	
	private static final int INHERIT_DIFFUSECOLOR_1 = 0x80;
	private static final int INHERIT_DIFFUSECOLOR_2 = 0x100;
	private static final int INHERIT_DIFFUSECOLOR_3 = 0x200;

	private static class VarModifier {
		private float value_0;
		private float value_1;
		private float value_2;
		private int modifierType;  // byte
		private int valueCount;  // byte
		private int valueIndex;  // short
		
		protected VarModifier(InputStreamAccessor in) throws IOException {
			read(in);
		}
		VarModifier() {};
		VarModifier(VarModifier other) {
			value_0 = other.value_0;
			value_1 = other.value_1;
			value_2 = other.value_2;
			modifierType = other.modifierType;
			valueCount = other.valueCount;
			valueIndex = other.valueIndex;
		}
		
		protected void read(InputStreamAccessor in) throws IOException {
			value_0 = in.readFloat();
			value_1 = in.readFloat();
			value_2 = in.readFloat();
			modifierType = in.readByte();
			valueCount = in.readByte();
			valueIndex = in.readShort();
		}
		
		protected void write(OutputStreamAccessor out) throws IOException {
			out.writeFloat(value_0);
			out.writeFloat(value_1);
			out.writeFloat(value_2);
			out.writeByte(modifierType);
			out.writeByte(valueCount);
			out.writeShort(valueIndex);
		}
		
		protected void setModifierCommand(ArgScriptBlock b) {
			b.addArgument(LIST_MODIFIER_TYPES.get(modifierType));
			
			if (modifierType == 0 || modifierType == 13) {
				
				if (value_1 != 1.0f) {
					b.putOption(new ArgScriptOption("scale", Float.toString(value_1)));
				}
			}
			else if (modifierType >= 2 && modifierType <= 5) {
				if (modifierType == 4) {
					// To parse it, squareSum = 1 / (x*x, y*y, z*z)
					float squaredSum = value_0*value_0 + value_1*value_1 + value_2*value_2  - 0.00000001f;
					b.addArgument(ArgScript.createFloatList(value_0 / squaredSum, value_1 / squaredSum, value_2 / squaredSum));
				}
				else if (modifierType == 3) {
					// 1 / (Vector(1, 1, 1) · vector)
//					float dotProduct = 1 * value_0 + 1 * value_1 + 1 * value_2  + 0.00000001; 
					// we can't calculate this, but since it's already "normalized" it will work anyways
					b.addArgument(ArgScript.createFloatList(value_0, value_1, value_2));
				}
				else {
					b.addArgument(ArgScript.createFloatList(value_0, value_1, value_2));
				}
			}
		}
		
		protected ArgScriptCommand toCommand() {
			ArgScriptCommand c = new ArgScriptCommand("varModifier", Float.toString(value_0), Float.toString(value_1), Float.toString(value_2),
					Integer.toString(modifierType), Integer.toString(valueCount), Integer.toString(valueIndex));
			
			return c;
		}
		
		protected ArgScriptCommand toCommand(ArgScriptCommand c) {
			c = new ArgScriptCommand(c.getKeyword(), Float.toString(value_0), Float.toString(value_1), Float.toString(value_2),
					Integer.toString(modifierType), Integer.toString(valueCount), Integer.toString(valueIndex));
			
			return c;
		}
	}
	
	private static class Variable {
		private float[] range = new float[2];  // if varModifierIndex == -1 -> 0, 1
		private float[] values = new float[2];  // if varModifierIndex == -1 -> value, 1.0f
		private int channel;  // byte
		private int varModifierIndex;  // byte  // index to Variable
		private int operationType;  // byte  // 2 by default for normal variables; 1 for 'add' ? // not really?
		private int flag;  // byte
		
		protected Variable(InputStreamAccessor in) throws IOException {
			read(in);
		}
		Variable() {};
		Variable(Variable other) {
			range[0] = other.range[0];
			range[1] = other.range[1];
			values[0] = other.values[0];
			values[1] = other.values[1];
			channel = other.channel;
			varModifierIndex = other.varModifierIndex;
			operationType = other.operationType;
			flag = other.flag;
		}
		
		protected void read(InputStreamAccessor in) throws IOException {
			range[0] = in.readFloat();
			range[1] = in.readFloat();
			values[0] = in.readFloat();
			values[1] = in.readFloat();
			channel = in.readByte();
			varModifierIndex = in.readByte();
			operationType = in.readByte();
			flag = in.readByte();
		}
		
		protected void write(OutputStreamAccessor out) throws IOException {
			out.writeFloat(range[0]);
			out.writeFloat(range[1]);
			out.writeFloat(values[0]);
			out.writeFloat(values[1]);
			out.writeByte(channel);
			out.writeByte(varModifierIndex);
			out.writeByte(operationType);
			out.writeByte(flag);
		}
		
		protected ArgScriptCommand toCommand() {
			ArgScriptCommand c = new ArgScriptCommand("variable", LIST_MODIFIER_CHANNELS.get(channel), Float.toString(range[0]), Float.toString(range[1]), Float.toString(values[0]),
					Float.toString(values[1]), Integer.toString(varModifierIndex), Integer.toString(operationType), Integer.toString(flag));
			
			return c;
		}
	}
	
	public static final int TYPE = 0x0026;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "SPSkinPaintParticle";
	
	/*
	 * 0x00 vftable
	 * 0x04 field_4
	 * 0x08 SkinpaintParticleEffect
	 * 
	 * SkinpaintParticleEffect
	 * 0x00 vftable
	 * 0x04 EffectCurve brushes
	 * ....
	 * 
	 */
	
	private int[] brushes = new int[0];  // 0x04
	private int chainIndex = -1;  // 0x18  // index to another SkinpaintParticle effect
	private int flags;
	private final float[] diffuseColor = new float[3];
	private byte diffuseColorIndex;  // 0x2C
	private byte alignFlag;  // 0x2D
	private byte initDirFlag;  // 0x2E
	private byte attractFlag;  // 0x2F
	private final float[] align = new float[3];  // 0x30
	private final float[] initDir = new float[3];  // 0x3C
	private final float[] attract = new float[3];  // 0x48
	private float attractForce;  // 0x54
	private float delay_0;  // 0x58
	private float delay_1;  // 0x5C
	private float life_0;  // 0x60
	private float life_1;  // 0x64
	private int inheritFlags;
	//TODO The first two are always 0 0 0 -1 0 0 
	private List<VarModifier> varModifiers = new ArrayList<VarModifier>();
	private List<Variable> variables = new ArrayList<Variable>();
	private List<Float> variableValues = new ArrayList<Float>();
	private byte diffuseBlend;  // 0xA8
	private byte specularBlend;  // 0xA9
	private byte bumpBlend;  // 0xAA
	
	public SkinpaintParticleEffect(int type, int version) {
		super(type, version);
	}
	
	public SkinpaintParticleEffect(SkinpaintParticleEffect effect) {
		super(effect);
		
		brushes = new int[effect.brushes.length];
		for (int i = 0; i < brushes.length; i++) brushes[i] = effect.brushes[i];
		chainIndex = effect.chainIndex;
		flags = effect.flags;
		diffuseColor[0] = effect.diffuseColor[0];
		diffuseColor[1] = effect.diffuseColor[1];
		diffuseColor[2] = effect.diffuseColor[2];
		diffuseColorIndex = effect.diffuseColorIndex;
		alignFlag = effect.alignFlag;
		initDirFlag = effect.initDirFlag;
		attractFlag = effect.attractFlag;
		align[0] = effect.align[0];
		align[1] = effect.align[1];
		align[2] = effect.align[2];
		initDir[0] = effect.initDir[0];
		initDir[1] = effect.initDir[1];
		initDir[2] = effect.initDir[2];
		attract[0] = effect.attract[0];
		attract[1] = effect.attract[1];
		attract[2] = effect.attract[2];
		attractForce = effect.attractForce;
		delay_0 = effect.delay_0;
		delay_1 = effect.delay_1;
		life_0 = effect.life_0;
		life_1 = effect.life_1;
		inheritFlags = effect.inheritFlags;
		
		varModifiers = new ArrayList<VarModifier>();
		for (VarModifier varMod : effect.varModifiers) {
			varModifiers.add(new VarModifier(varMod));
		}
		variables = new ArrayList<Variable>();
		for (Variable var : effect.variables) {
			variables.add(new Variable(var));
		}
		variableValues = new ArrayList<Float>();
		for (float value : effect.variableValues) {
			variableValues.add(value);
		}
		diffuseBlend = effect.diffuseBlend;
		specularBlend = effect.specularBlend;
		bumpBlend = effect.bumpBlend;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException 
	{
		brushes = new int[in.readInt()];
		in.readInts(brushes);
		chainIndex = in.readInt();
		flags = in.readInt();
		in.readLEFloats(diffuseColor);
		diffuseColorIndex = in.readByte();
		alignFlag = in.readByte();
		initDirFlag = in.readByte();
		attractFlag = in.readByte();
		
		in.readLEFloats(align);
		in.readLEFloats(initDir);
		in.readLEFloats(attract);
		
		attractForce = in.readFloat();
		delay_0 = in.readFloat();
		delay_1 = in.readFloat();
		life_0 = in.readFloat();
		life_1 = in.readFloat();
		inheritFlags = in.readInt();
		
		int varModifiersCount = in.readInt();
		for (int i = 0; i < varModifiersCount; i++) varModifiers.add(new VarModifier(in));
		
		int variableCount = in.readInt();
		for (int i = 0; i < variableCount; i++) variables.add(new Variable(in));
		
		int valueCount = in.readInt();
		for (int i = 0; i < valueCount; i++) variableValues.add(in.readFloat());
		
		diffuseBlend = in.readByte();
		specularBlend = in.readByte();
		bumpBlend = in.readByte();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(brushes.length);
		out.writeInts(brushes);
		out.writeInt(chainIndex);
		out.writeInt(flags);
		out.writeLEFloats(diffuseColor);
		out.writeByte(diffuseColorIndex);
		out.writeByte(alignFlag);
		out.writeByte(initDirFlag);
		out.writeByte(attractFlag);
		
		out.writeLEFloats(align);
		out.writeLEFloats(initDir);
		out.writeLEFloats(attract);
		
		out.writeFloat(attractForce);
		out.writeFloat(delay_0);
		out.writeFloat(delay_1);
		out.writeFloat(life_0);
		out.writeFloat(life_1);
		out.writeInt(inheritFlags);
		
		out.writeInt(varModifiers.size());
		for (VarModifier v : varModifiers) v.write(out);
		
		out.writeInt(variables.size());
		for (Variable m : variables) m.write(out);
		
		out.writeInt(variableValues.size());
		for (float f : variableValues) out.writeFloat(f);
		
		out.writeByte(diffuseBlend);
		out.writeByte(specularBlend);
		out.writeByte(bumpBlend);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		// TODO Auto-generated method stub
		
		{
			ArgScriptCommand c = block.getCommand("brush");
			if (c != null) {
				List<String> args = c.getArguments(0, 100);
				brushes = new int[args.size()];
				for (int i = 0; i < brushes.length; i++) {
					brushes[i] = Hasher.getFileHash(args.get(i));
				}
				if (c.hasFlag("random")) flags |= BRUSH_RANDOM;
				if (c.hasFlag("randomStart")) flags |= BRUSH_RANDOMSTART;
			}
		}
		{
			ArgScriptCommand c = block.getCommand("curve");
			// Spore accepts up to 500 arguments, but it doesn't use them
			if (c != null) if (c.hasFlag("random")) flags |= CURVE_RANDOM;
		}
		{
			ArgScriptCommand c = block.getCommand("chain"); 
			if (c != null) chainIndex = EffectMain.getEffectIndex(parent.getEffectMap(), SkinpaintParticleEffect.TYPE, c.getSingleArgument());
		}
		{
			ArgScriptCommand c = block.getCommand("align");
			if (c != null) {
				String arg = c.getSingleArgument();
				if (arg.equals("inherit")) alignFlag = ALIGN_INHERIT;
				else if (arg.equals("move") || arg.equals("dir")) alignFlag = ALIGN_DIR;
				else if (arg.equals("attract")) alignFlag = ALIGN_ATTRACT;
				else if (arg.equals("aroundBone")) alignFlag = ALIGN_AROUNDBONE;
				else if (arg.equals("alongBone")) {
					alignFlag = ALIGN_ALONGBONE;
					align[0] = 0;
					align[1] = 1;
					align[2] = 0;
				}
				else if (arg.equals("aroundSpine")) alignFlag = ALIGN_AROUNDSPINE;
				else if (arg.equals("alongSpine")) {
					alignFlag = ALIGN_ALONGSPINE;
					align[0] = 0;
					align[1] = 1;
					align[2] = 0;
				}
				else {
					float[] list = ArgScript.parseFloatList(arg, 3);
					align[0] = list[0];
					align[1] = list[1];
					align[2] = list[2];
					if (c.hasFlag("bone")) alignFlag = ALIGN_BONE;
					else alignFlag = ALIGN_NOBONE;
				}
				if (c.hasFlag("reverse")) flags |= FLAG_ALIGN_REVERSE;
			}
		}
		{
			ArgScriptCommand c = block.getCommand("initDir");
			if (c != null) {
				String arg = c.getSingleArgument();
				if (arg.equals("inherit")) initDirFlag = ALIGN_INHERIT;
				// initDir doesn't have 'dir' or 'move'
				else if (arg.equals("attract")) initDirFlag = ALIGN_ATTRACT;
				else if (arg.equals("aroundBone")) initDirFlag = ALIGN_AROUNDBONE;
				else if (arg.equals("alongBone")) {
					initDirFlag = ALIGN_ALONGBONE;
					initDir[0] = 0;
					initDir[1] = 1;
					initDir[2] = 0;
				}
				else if (arg.equals("aroundSpine")) initDirFlag = ALIGN_AROUNDSPINE;
				else if (arg.equals("alongSpine")) {
					initDirFlag = ALIGN_ALONGSPINE;
					initDir[0] = 0;
					initDir[1] = 1;
					initDir[2] = 0;
				}
				else {
					float[] list = ArgScript.parseFloatList(arg, 3);
					initDir[0] = list[0];
					initDir[1] = list[1];
					initDir[2] = list[2];
					if (c.hasFlag("bone")) initDirFlag = ALIGN_BONE;
					else initDirFlag = ALIGN_NOBONE;
				}
				if (c.hasFlag("reverse")) flags |= FLAG_INITDIR_REVERSE;
			}
		}
		{
			ArgScriptCommand c = block.getCommand("attract");
			if (c == null) c = block.getCommand("restrict");
			if (c != null) {
				List<String> args = c.getArguments(1, 2);
				String arg = args.get(0);
				if (arg.equals("inherit")) attractFlag = ALIGN_INHERIT;
				// attract doesn't have 'dir', 'move' and 'attract'
				else if (arg.equals("aroundBone")) attractFlag = ALIGN_AROUNDBONE;
				else if (arg.equals("alongBone")) {
					attractFlag = ALIGN_ALONGBONE;
					attract[0] = 0;
					attract[1] = 1;
					attract[2] = 0;
				}
				else if (arg.equals("aroundSpine")) attractFlag = ALIGN_AROUNDSPINE;
				else if (arg.equals("alongSpine")) {
					attractFlag = ALIGN_ALONGSPINE;
					attract[0] = 0;
					attract[1] = 1;
					attract[2] = 0;
				}
				else {
					// normalize vector
					float[] list = ArgScript.parseFloatList(arg, 3);
					double sqrt = Math.sqrt(list[0] * list[0] + list[1] * list[1] + list[2] * list[2]);
					attract[0] = (float) (list[0] / sqrt);
					attract[1] = (float) (list[1] / sqrt);
					attract[2] = (float) (list[2] / sqrt);
					if (c.hasFlag("bone")) attractFlag = ALIGN_BONE;
					else attractFlag = ALIGN_NOBONE;
				}
				
				if (args.size() > 1) {
					attractForce = ArgScript.parseRangedFloat(args.get(1), 0, 1);
				}
				else {
					attractForce = 1.0f;
				}
				
				if (c.hasFlag("reverse")) flags |= FLAG_ATTRACT_REVERSE;
			}
		}
		{
			ArgScriptCommand c = block.getCommand("delay");
			if (c != null) {
				List<String> args = c.getArguments(1, 2);
				if (args.size() == 2) {
					delay_0 = Float.parseFloat(args.get(0));
					delay_1 = Float.parseFloat(args.get(1));
				}
				else {
					float value = Float.parseFloat(args.get(0));
					float vary = 0;
					ArgScriptOption o = c.getOption("vary");
					if (o != null) vary = Float.parseFloat(o.getSingleArgument());
					
					delay_0 = value * (1 - vary);
					delay_1 = value * (1 + vary);
				}
			}
		}
		{
			ArgScriptCommand c = block.getCommand("life");
			if (c != null) {
				List<String> args = c.getArguments(1, 2);
				if (args.size() == 2) {
					life_0 = Float.parseFloat(args.get(0));
					life_1 = Float.parseFloat(args.get(1));
				}
				else {
					float value = Float.parseFloat(args.get(0));
					float vary = 0;
					ArgScriptOption o = c.getOption("vary");
					if (o != null) vary = Float.parseFloat(o.getSingleArgument());
					
					life_0 = value * (1 - vary);
					life_1 = value * (1 + vary);
				}
			}
		}
		{
			ArgScriptCommand c = block.getCommand("diffuseColor");
			if (c != null) {
				String arg = c.getSingleArgument();
				if (arg.startsWith("color")) {
					if (Character.isDigit(arg.charAt(5))) {
						diffuseColorIndex = (byte) (Byte.parseByte(arg.substring(5)) - 1);
						diffuseColor[0] = 0.0f;
						diffuseColor[1] = 0.0f;
						diffuseColor[2] = 1.0f;
					}
				}
				else if (arg.equals("identity")) {
					diffuseColorIndex = -2;  // 0xFE
					diffuseColor[0] = 0.0f;
					diffuseColor[1] = 0.0f;
					diffuseColor[2] = 1.0f;
				}
				else {
					float[] list = ArgScript.parseFloatList(arg, 3);
					//TODO Spore does something with that vector!
					diffuseColor[0] = list[0];
					diffuseColor[1] = list[1];
					diffuseColor[2] = list[2];
				}
				
				if (c.hasFlag("identity")) flags |= FLAG_IDENTITY;
			}
		}
		{
			ArgScriptCommand c = block.getCommand("inherit");
			if (c != null) {
				List<String> args = c.getArguments(1, 50);
				
				for (String arg : args) {
					
					if (arg.equals("diffuseColor")) {
						inheritFlags |= INHERIT_DIFFUSECOLOR_1;
						inheritFlags |= INHERIT_DIFFUSECOLOR_2;
						inheritFlags |= INHERIT_DIFFUSECOLOR_3;
					}
					else {
						for (int i = 0; i < LIST_MODIFIER_CHANNELS.size(); i++) {
							if (arg.equals(LIST_MODIFIER_CHANNELS.get(i))) {
								inheritFlags |= 1 << i;
							}
						}
					}
				}
			}
		}
		
		{ ArgScriptCommand c = block.getCommand("diffuseBlend"); if (c != null) diffuseBlend = (byte) ENUM_BLEND.getValue(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("specularBlend"); if (c != null) specularBlend = (byte) ENUM_BLEND.getValue(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("bumpBlend"); if (c != null) bumpBlend = (byte) ENUM_BLEND.getValue(c.getSingleArgument()); }
		
		//TODO add two default VarModifiers
		
		for (int i = 0; i < 2; i++) {
			VarModifier varMod = new VarModifier();
			varMod.modifierType = -1;
			varModifiers.add(varMod);
		}
		
		for (int i = 0; i < LIST_MODIFIER_CHANNELS.size(); i++) {
			ArgScriptCommand c = block.getCommand(LIST_MODIFIER_CHANNELS.get(i));
			if (c != null) {
				if (c.getArgumentCount() == 0) {
					Variable var = new Variable();
					ArgScriptOption o = null;
					if ((o = c.getOption("set")) != null) var.operationType = 0;
					else if ((o = c.getOption("add")) != null) var.operationType = 1;
					else if ((o = c.getOption("mult")) != null) var.operationType = 2;
					else if ((o = c.getOption("div")) != null) var.operationType = 3;

					if (o != null) {
						var.channel = (byte) i;
						var.varModifierIndex = -1;
						var.range[0] = 0;
						var.range[1] = 1.0f;
						var.values[0] = Float.parseFloat(o.getSingleArgument());
						var.values[1] = 1.0f; 
						variables.add(var);
					}
					else {
						//throw new ArgScriptException(KEYWORD + ": " + LIST_MODIFIER_CHANNELS.get(i) + " must have arguments or 'set', 'add', 'mult', 'div' options.");
						// killswitches don't use any argument...?
						var.channel = (byte) i;
						var.varModifierIndex = varModifiers.size();
						
						VarModifier varMod = new VarModifier();
						varMod.modifierType = -1;
						varMod.valueCount = 0;
						varMod.value_0 = 0;
						varMod.value_1 = 0;
						varMod.value_2 = 0;
						var.range[0] = 0.0f;
						var.range[1] = 0.0f;
						
						variables.add(var);
						varModifiers.add(varMod);
					}
				}
				else if (c.getOptionCount() == 0) {
					if (c.getArgumentCount() > 2)
					{
						Variable var = new Variable();
						VarModifier varMod = new VarModifier();
						variables.add(var);
						varModifiers.add(varMod);
						
						List<String> args = c.getArguments();
						
						varMod.valueCount = args.size();
						varMod.valueIndex = variableValues.size();
						varMod.modifierType = -1;
						var.channel = (byte) i;
						var.varModifierIndex = varModifiers.size() - 1;
						
						for (String arg : args) {
							variableValues.add(Float.parseFloat(arg));
						}
					}
					else
					{
						Variable var = new Variable();
						var.channel = (byte) i;
						var.varModifierIndex = -1;
						var.operationType = 0;
						var.range[0] = 0;
						var.range[1] = 1.0f;
						List<String> args = c.getArguments(1, 2);
						var.values[0] = Float.parseFloat(args.get(0));
						var.values[1] = args.size() == 1 ? 1.0f : Float.parseFloat(args.get(1)); 
						variables.add(var);
					}
				} 
				else {
					Variable var = new Variable();
					VarModifier varMod = new VarModifier();
					variables.add(var);
					varModifiers.add(varMod);
					
					List<String> args = c.getArguments();
					
					varMod.valueCount = args.size();
					varMod.valueIndex = variableValues.size();
					varMod.modifierType = -1;
					var.channel = (byte) i;
					var.varModifierIndex = varModifiers.size() - 1;
					
					for (String arg : args) {
						variableValues.add(Float.parseFloat(arg));
					}
					
					ArgScriptOption o = c.getOption("vary");
					if (o != null) {
						varMod.value_0 = Float.parseFloat(o.getSingleArgument());
					}
					
					o = c.getOption("offset");
					if (o != null) {
						List<String> optionArgs = o.getArguments(1, 2);
						if (optionArgs.size() == 1) {
							varMod.value_1 = varMod.value_2 = Float.parseFloat(optionArgs.get(0));
						}
						else {
							varMod.value_1 = Float.parseFloat(optionArgs.get(0));
							varMod.value_2 = Float.parseFloat(optionArgs.get(1));
						}
					}
					
					if (c.hasFlag("scaleNorm")) {
						var.range[0] = 1.0f;
					}
					if (c.hasFlag("scalePos")) {
						var.range[1] = 1.0f;
					}
				}
			}
		}
		
		Collection<ArgScriptBlock> blocks = block.getAllBlocks();
		for (ArgScriptBlock b : blocks) {
			if (b.getKeyword().equals("modifier")) {
				List<String> args = b.getArguments(1, 2);
				VarModifier modifier = new VarModifier();
				modifier.modifierType = LIST_MODIFIER_TYPES.indexOf(args.get(0));
				modifier.valueCount = 19;
				modifier.valueIndex = 0;
				int modifierIndex = varModifiers.size();
				varModifiers.add(modifier);
				
				if (modifier.modifierType == 0 || modifier.modifierType == 13) {
					ArgScriptOption o = b.getOption("scale");
					if (o != null) modifier.value_1 = Float.parseFloat(o.getSingleArgument());
				}
				else if (modifier.modifierType >= 2 && modifier.modifierType <= 5) {
					if (modifier.modifierType == 4) {
						float[] list = ArgScript.parseFloatList(args.get(1), 3);
						
						float scalar = 1 / (list[0] * list[0] + list[1] * list[1] + list[2] * list[2] + 0.00000001f);
						modifier.value_0 = list[0] * scalar;
						modifier.value_1 = list[1] * scalar;
						modifier.value_2 = list[2] * scalar;
					}
					else if (modifier.modifierType == 3) {
						float[] list = ArgScript.parseFloatList(args.get(1), 3);
						
						float scalar = 1 / (list[0] + list[1] + list[2] + 0.00000001f);
						modifier.value_0 = list[0] * scalar;
						modifier.value_1 = list[1] * scalar;
						modifier.value_2 = list[2] * scalar;
					}
					else {
						float[] list = ArgScript.parseFloatList(args.get(1), 3);
						modifier.value_0 = list[0];
						modifier.value_1 = list[1];
						modifier.value_2 = list[2];
					}
				}
				
				Collection<ArgScriptCommand> commands = b.getAllCommands();
				for (ArgScriptCommand c : commands) 
				{
					Variable var = new Variable();
					var.channel = LIST_MODIFIER_CHANNELS.indexOf(c.getKeyword());
					var.varModifierIndex = modifierIndex;
					variables.add(var);
					
					for (int i = 0; i < OPERATION_TYPES.size(); i++) {
						ArgScriptOption o = c.getOption(OPERATION_TYPES.get(i));
						if (o != null) {
							List<String> optionArgs = o.getArguments(2);
							var.values[0] = Float.parseFloat(optionArgs.get(0));
							var.values[1] = Float.parseFloat(optionArgs.get(1));
							var.operationType = i;
							break;
						}
					}
					
					ArgScriptOption o = c.getOption("range");
					if (o != null) {
						List<String> optionArgs = o.getArguments(2);
						var.range[0] = Float.parseFloat(optionArgs.get(0));
						var.range[1] = Float.parseFloat(optionArgs.get(1));
					}
					
					for (int i = 0; i < MODIFIER_FLAG.size(); i++) {
						if (c.hasFlag(MODIFIER_FLAG.get(i))) {
							var.flag = i;
							break;
						}
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		// TODO Auto-generated method stub
		
		HashMap<Integer, ArgScriptBlock> modifierBlocks = new HashMap<Integer, ArgScriptBlock>();
		
		{
			ArgScriptCommand c = new ArgScriptCommand("brush");
			for (int brush : brushes) {
				c.addArgument(Hasher.getFileName(brush));
			}
			if ((flags & BRUSH_RANDOM) == BRUSH_RANDOM) c.putFlag("random");
			if ((flags & BRUSH_RANDOMSTART) == BRUSH_RANDOMSTART) c.putFlag("randomStart");
			block.putCommand(c);
		}
		block.addBlankLine();
		if ((flags & CURVE_RANDOM) == CURVE_RANDOM) {
			// curve arguments are not used, apparently
			ArgScriptCommand c = new ArgScriptCommand("curve");
			c.putFlag("random");
			block.putCommand(c);
		}
		// by default it's 'inherit'
		if (alignFlag != ALIGN_INHERIT || (flags & FLAG_ALIGN_REVERSE) == FLAG_ALIGN_REVERSE) {
			ArgScriptCommand c = new ArgScriptCommand("align");
			if (alignFlag == ALIGN_INHERIT) c.addArgument("inherit");
			else if (alignFlag == ALIGN_DIR) c.addArgument("dir");
			else if (alignFlag == ALIGN_ATTRACT) c.addArgument("attract");
			else if (alignFlag == ALIGN_AROUNDBONE) c.addArgument("aroundBone");
			else if (alignFlag == ALIGN_ALONGBONE) {
				if (align[0] != 0.0f || align[1] != 1.0f || align[2] != 0.0f) {
					c.addArgument(ArgScript.createFloatList(align));
					c.putFlag("bone");
				}
				else {
					c.addArgument("alongBone");
				}
			}
			else if (alignFlag == ALIGN_AROUNDSPINE) c.addArgument("aroundSpine");
			else if (alignFlag == ALIGN_ALONGSPINE) c.addArgument("alongSpine");
			else if (alignFlag == ALIGN_NOBONE) c.addArgument(ArgScript.createFloatList(align));
			
			if ((flags & FLAG_ALIGN_REVERSE) == FLAG_ALIGN_REVERSE) c.putFlag("reverse");
			
			block.putCommand(c);
		}
		
		// by default it's 'inherit'
		if (initDirFlag != ALIGN_INHERIT || (flags & FLAG_INITDIR_REVERSE) == FLAG_INITDIR_REVERSE) {
			ArgScriptCommand c = new ArgScriptCommand("initDir");
			if (initDirFlag == ALIGN_INHERIT) c.addArgument("inherit");
			// initDir doesn't use 'dir'
			else if (initDirFlag == ALIGN_ATTRACT) c.addArgument("attract");
			else if (initDirFlag == ALIGN_AROUNDBONE) c.addArgument("aroundBone");
			else if (initDirFlag == ALIGN_ALONGBONE) {
				if (initDir[0] != 0.0f || initDir[1] != 1.0f || initDir[2] != 0.0f) {
					c.addArgument(ArgScript.createFloatList(initDir));
					c.putFlag("bone");
				}
				else {
					c.addArgument("alongBone");
				}
			}
			else if (initDirFlag == ALIGN_AROUNDSPINE) c.addArgument("aroundSpine");
			else if (initDirFlag == ALIGN_ALONGSPINE) c.addArgument("alongSpine");
			else if (initDirFlag == ALIGN_NOBONE) c.addArgument(ArgScript.createFloatList(initDir));
			
			if ((flags & FLAG_INITDIR_REVERSE) == FLAG_INITDIR_REVERSE) c.putFlag("reverse");
			
			block.putCommand(c);
		}
		// by default it's 'inherit'
		if (attractFlag != ALIGN_INHERIT || (flags & FLAG_ATTRACT_REVERSE) == FLAG_ATTRACT_REVERSE || attractForce != 0.0f) {
			ArgScriptCommand c = new ArgScriptCommand("attract");
			if (attractFlag == ALIGN_INHERIT) c.addArgument("inherit");
			// initDir doesn't use 'dir' nor 'attract'
			else if (attractFlag == ALIGN_AROUNDBONE) c.addArgument("aroundBone");
			else if (attractFlag == ALIGN_ALONGBONE) {
				if (attract[0] != 0.0f || attract[1] != 1.0f || attract[2] != 0.0f) {
					c.addArgument(ArgScript.createFloatList(attract));
					c.putFlag("bone");
				}
				else {
					c.addArgument("alongBone");
				}
			}
			else if (attractFlag == ALIGN_AROUNDSPINE) c.addArgument("aroundSpine");
			else if (attractFlag == ALIGN_ALONGSPINE) c.addArgument("alongSpine");
			else if (attractFlag == ALIGN_NOBONE) c.addArgument(ArgScript.createFloatList(attract));
			
			if (attractForce != 1.0f) c.addArgument(Float.toString(attractForce));
			
			if ((flags & FLAG_ATTRACT_REVERSE) == FLAG_ATTRACT_REVERSE) c.putFlag("reverse");
			
			block.putCommand(c);
		}
		if (delay_0 != 0 || delay_1 != 0) {
			ArgScriptCommand c = new ArgScriptCommand("delay");
			if (delay_0 == delay_1) c.addArgument(Float.toString(delay_0));
			else {
				c.addArgument(Float.toString(delay_0));
				c.addArgument(Float.toString(delay_1));
			}
			block.putCommand(c);
		}
		if (life_0 != 0 || life_1 != 0) {
			ArgScriptCommand c = new ArgScriptCommand("life");
			if (life_0 == life_1) c.addArgument(Float.toString(life_0));
			else {
				c.addArgument(Float.toString(life_0));
				c.addArgument(Float.toString(life_1));
			}
			block.putCommand(c);
		}
		if (inheritFlags != 0) {
			ArgScriptCommand c = new ArgScriptCommand("inherit");
			
			if ((inheritFlags & INHERIT_DIFFUSECOLOR_1) == INHERIT_DIFFUSECOLOR_1 ||
					(inheritFlags & INHERIT_DIFFUSECOLOR_2) == INHERIT_DIFFUSECOLOR_2 ||
					(inheritFlags & INHERIT_DIFFUSECOLOR_3) == INHERIT_DIFFUSECOLOR_3) {
				c.addArgument("diffuseColor");
			}
			for (int i = 0; i < LIST_MODIFIER_CHANNELS.size(); i++) {
				int flag = 1 << i;
				if ((inheritFlags & flag) == flag) {
					c.addArgument(LIST_MODIFIER_CHANNELS.get(i));
				}
			}
			
			block.putCommand(c);
		}
		
		// size, aspect
		for (int i = 0; i < 2; i++) {
			getNormalVariableAS(block, i);
		}
		
		// "spacing", "killswitch", "hairRadius", "alpha"
		for (int i = 3; i < 7; i++) {
			getNormalVariableAS(block, i);
		}
		
		
		
		/* --- DIFFUSE --- */
		block.addBlankLine();
		{
			ArgScriptCommand c = new ArgScriptCommand("diffuseColor");
			
			if (diffuseColorIndex == -2) {
				c.addArgument("identity");
			}
			else if (diffuseColorIndex > -1) {
				c.addArgument("color" + Integer.toString(diffuseColorIndex + 1));
			}
			else {
				//TODO Spore does something with that list
				c.addArgument(ArgScript.createFloatList(diffuseColor));
			}
			
			if ((flags & FLAG_IDENTITY) == FLAG_IDENTITY) c.putFlag("identity");
			
			block.putCommand(c);
		}
		// diffuseAlpha
		getNormalVariableAS(block, 10);
		if (diffuseBlend != 0) block.putCommand(new ArgScriptCommand("diffuseBlend", ENUM_BLEND.getKey(diffuseBlend)));
		/* --------------- */
		
		
		/* --- SPECULAR --- */
		block.addBlankLine();
		// specularAlpha
		getNormalVariableAS(block, 14);
		// specularScale
		getNormalVariableAS(block, 13);
		if (specularBlend != 0) block.putCommand(new ArgScriptCommand("specularBlend", ENUM_BLEND.getKey(specularBlend)));
		/* ---------------- */
		
		
		/* --- BUMP --- */
		block.addBlankLine();
		// bumpAlpha
		getNormalVariableAS(block, 12);
		// bumpScale
		getNormalVariableAS(block, 11);
		if (bumpBlend != 0) block.putCommand(new ArgScriptCommand("bumpBlend", ENUM_BLEND.getKey(bumpBlend)));
		/* ------------ */
		
		boolean blankLine = true;
		// "diffuseHue", "diffuseSat", "diffuseVal"
		for (int i = 7; i < 10; i++) {
			if (getNormalVariableAS(block, i, blankLine)) {
				blankLine = false;
			}
		}
		// rotate
		if (getNormalVariableAS(block, 2, blankLine)) {
			blankLine = false;
		}
		// "userVar1", "userVar2", "userVar3", "userVar4"
		for (int i = 15; i < 19; i++) {
			if (getNormalVariableAS(block, i, blankLine)) {
				blankLine = false;
			}
		}
		
		// process the modifiers
		for (Variable var : variables) {
			if (var.varModifierIndex != -1 && varModifiers.get(var.varModifierIndex).valueCount == 19) {
				generateVariableArgScript(var, modifierBlocks);
			}
		}
		
		
//		/* --- VARIABLES --- */
//		block.addBlankLine();
//		for (VarModifier var : varModifiers) {
//			block.putCommand(var.toCommand());
//		}
//		/* ----------------- */
//		
//		
//		/* --- MODIFIERS --- */
//		block.addBlankLine();
//		for (Variable var : this.variables) {
//			block.putCommand(var.toCommand());
//		}
//		/* ----------------- */
//		
//		//TODO
//		block.addBlankLine();
//		block.putCommand(new ArgScriptCommand("field_94", ArgScript.floatsToStrings(variableValues)));
//		
//		//TODO
//		block.addBlankLine();
//		for (Variable var : variables) {
//			ArgScriptCommand c = new ArgScriptCommand(LIST_MODIFIER_CHANNELS.get(var.channel));
//			
//			if (var.varModifierIndex == -1) {
//				if (var.operationType == 0) {
//					c.addArgument(Float.toString(var.values[0]));
//				}
//				else {
//					c.putOption(new ArgScriptOption(OPERATION_TYPES.get(var.operationType), Float.toString(var.values[0])));
//				}
//			}
//			else {
//				VarModifier varModifier = varModifiers[var.varModifierIndex];
//				if (varModifier.valueCount == 19) {
//					//TODO group 'modifier's
//					
//					c.putOption(new ArgScriptOption(OPERATION_TYPES.get(var.operationType), Float.toString(var.values[0]), Float.toString(var.values[1])));
//					c.putOption(new ArgScriptOption("range", Float.toString(var.range[0]), Float.toString(var.range[1])));
//					
//					if (var.flag != 0) c.putFlag(MODIFIER_FLAG.get(var.flag));
//					
//					if (modifierBlocks.get(var.varModifierIndex) == null) {
//						ArgScriptBlock b = new ArgScriptBlock("modifier", new String[0]);
//						varModifier.setModifierCommand(b);
//						b.putCommand(c);
//						
//						modifierBlocks.put(var.varModifierIndex, b);
//					}
//					else {
//						modifierBlocks.get(var.varModifierIndex).putCommand(c);
//					}
//					
//					// This kind of variables aren't added to the block but to a 'modifier' block instead
//					continue;
//				}
//				else {
//					for (int i = 0; i < varModifier.valueCount; i++) {
//						c.addArgument(Float.toString(variableValues[varModifier.valueIndex + i]));
//					}
//					
//					if (varModifier.value_0 != 0) {
//						c.putOption(new ArgScriptOption("vary", Float.toString(varModifier.value_0)));
//					}
//					if (varModifier.value_1 != 0 || varModifier.value_2 != 0) {
//						if (varModifier.value_1 == varModifier.value_2) {
//							c.putOption(new ArgScriptOption("offset", Float.toString(varModifier.value_1)));
//						}
//						else {
//							c.putOption(new ArgScriptOption("offset", Float.toString(varModifier.value_1), Float.toString(varModifier.value_2)));
//						}
//					}
//					if (var.range[0] == 1.0f) {
//						c.putFlag("scaleNorm");
//					}
//					if (var.range[1] == 1.0f) {
//						c.putFlag("scalePos");
//					}
//				}
//			}
//			
//			block.putCommand(c);
//		}
		block.addBlankLine();
		for (ArgScriptBlock b : modifierBlocks.values()) {
			block.putBlock(b);
			block.addBlankLine();
		}
		
		
		// 'chain' usually goes at the end
		
		if (chainIndex != -1) {
			// blocks already have a blank line, so we don't need another
			if (variables.size() == 0) block.addBlankLine();
			block.putCommand(new ArgScriptCommand("chain", EffectMain.getEffect(parent.getEffectMap(), SkinpaintParticleEffect.TYPE, chainIndex).getName()));
		}
		
		//TODO what's this?
		if (varModifiers.size() >= 2) {
			VarModifier varMod = varModifiers.get(0);
			
			if (varMod.value_0 != 0 || varMod.value_1 != 0 || varMod.value_2 != 0 || varMod.modifierType != -1 || varMod.valueCount != 0 || varMod.valueIndex != 0) {
				ArgScriptCommand c = new ArgScriptCommand("compareNormal");
				block.putCommand(varMod.toCommand(c));
			}
			
			varMod = varModifiers.get(1);
			if (varMod.value_0 != 0 || varMod.value_1 != 0 || varMod.value_2 != 0 || varMod.modifierType != -1 || varMod.valueCount != 0 || varMod.valueIndex != 0) {
				ArgScriptCommand c = new ArgScriptCommand("comparePosition");
				block.putCommand(varMod.toCommand(c));
			}
		}
		
		return false;
	}
	
	private ArgScriptCommand generateVariableArgScript(Variable var, HashMap<Integer, ArgScriptBlock> modifierBlocks) {
		ArgScriptCommand c = new ArgScriptCommand(LIST_MODIFIER_CHANNELS.get(var.channel));
		
		if (var.varModifierIndex == -1) {
			if (var.operationType == 0) {
				c.addArgument(Float.toString(var.values[0]));
			}
			else {
				c.putOption(new ArgScriptOption(OPERATION_TYPES.get(var.operationType), Float.toString(var.values[0])));
			}
		}
		else {
			VarModifier varModifier = varModifiers.get(var.varModifierIndex);
			if (varModifier.valueCount == 19) {
				//TODO group 'modifier's
				
				c.putOption(new ArgScriptOption(OPERATION_TYPES.get(var.operationType), Float.toString(var.values[0]), Float.toString(var.values[1])));
				c.putOption(new ArgScriptOption("range", Float.toString(var.range[0]), Float.toString(var.range[1])));
				
				if (var.flag != 0) c.putFlag(MODIFIER_FLAG.get(var.flag));
				
				if (modifierBlocks.get(var.varModifierIndex) == null) {
					ArgScriptBlock b = new ArgScriptBlock("modifier", new String[0]);
					varModifier.setModifierCommand(b);
					b.putCommand(c);
					
					modifierBlocks.put(var.varModifierIndex, b);
				}
				else {
					modifierBlocks.get(var.varModifierIndex).putCommand(c);
				}
				
				// This kind of variables aren't added to the block but to a 'modifier' block instead
				return null;
			}
			else if (varModifier.valueIndex + varModifier.valueCount <= variableValues.size()) {
				for (int i = 0; i < varModifier.valueCount; i++) {
//					if (varModifier.valueIndex + i >= variableValues.size()) {
//						System.out.println("BREAKPOINT");
//					}
					c.addArgument(Float.toString(variableValues.get(varModifier.valueIndex + i)));
				}
				
				if (varModifier.value_0 != 0) {
					c.putOption(new ArgScriptOption("vary", Float.toString(varModifier.value_0)));
				}
				if (varModifier.value_1 != 0 || varModifier.value_2 != 0) {
					if (varModifier.value_1 == varModifier.value_2) {
						c.putOption(new ArgScriptOption("offset", Float.toString(varModifier.value_1)));
					}
					else {
						c.putOption(new ArgScriptOption("offset", Float.toString(varModifier.value_1), Float.toString(varModifier.value_2)));
					}
				}
				if (var.range[0] == 1.0f) {
					c.putFlag("scaleNorm");
				}
				if (var.range[1] == 1.0f) {
					c.putFlag("scalePos");
				}
			}
		}
		
		return c;
	}
	
	private boolean getNormalVariableAS(ArgScriptBlock block, int channel) {
		for (Variable var : variables) {
			if (var.channel == channel) {
				if (var.varModifierIndex == -1 || varModifiers.get(var.varModifierIndex).valueCount < 19) {
					ArgScriptCommand c = generateVariableArgScript(var, null);
					if (c != null) block.putCommand(c);
					
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean getNormalVariableAS(ArgScriptBlock block, int channel, boolean addBlankLine) {
		for (Variable var : variables) {
			if (var.channel == channel) {
				if (var.varModifierIndex == -1 || varModifiers.get(var.varModifierIndex).valueCount < 19) {
					ArgScriptCommand c = generateVariableArgScript(var, null);
					if (c != null) {
						if (addBlankLine) block.addBlankLine();
						block.putCommand(c);
					}
					
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline " + KEYWORD + " effect is not supported.");
	}


	@Override
	public Effect[] getEffects() {
		if (chainIndex == -1) {
			return new Effect[0];
		}
		else {
			return new Effect[] {EffectMain.getEffect(parent.getEffectMap(), SkinpaintParticleEffect.TYPE, chainIndex)};
		}
	}

	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		if (chainIndex != -1) {
			Integer index = baseIndices.get(SkinpaintParticleEffect.TYPE);
			if (index != null && index != -1) chainIndex += index;
		}
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"move", "dir", "attract", "aroundBone", "alongBone", "aroundSpine", "alongSpine", "bone",
			"alpha", "add", "substract", "multiply", "screen", "inherit", "default", "identity",
			"age", "random", "worldAngle", "worldPos", "worldDist",
			"boneAngle", "bonePos", "boneOffset", "torsoPos", "limbPos", "limbType", "region", "paintMask"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD, "modifier"
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"identity", "reverse", "random", "randomStart", "bone",
			"set", "add", "mult", "div", "range", "vary",
			"open", "clamp", "wrap", "mirror", "clamp2", "wrap2", "mirror2"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"brush", "curve", "chain", "align", "initDir", "attract", "delay", "life", "diffuseColor",
			"diffuseBlend", "specularBlend", "bumpBlend", "inherit", 
			"size", "aspect", "rotation", "spacing", "killswitch",
			"hairRadius", "alpha", "diffuseHue", "diffuseSat", "diffuseVal", "diffuseAlpha", "bumpScale", "bumpAlpha",
			"specularScale", "specularAlpha", "userVar1", "userVar2", "userVar3", "userVar4"
		};
	}
}
