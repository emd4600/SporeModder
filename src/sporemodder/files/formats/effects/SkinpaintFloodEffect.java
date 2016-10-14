package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class SkinpaintFloodEffect extends EffectComponent {

	public static final int TYPE = 0x27;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "SPSkinPaintFlood";
	
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
	
	private List<VarModifier> varModifiers = new ArrayList<VarModifier>();
	private List<Variable> variables = new ArrayList<Variable>();
	private List<Float> variableValues = new ArrayList<Float>();
	private boolean hairFaceCamera;
	private final float[] diffuseColor = {0, 0, 1};  // BE
	private int diffuseColorIndex;
	private int diffuseBlend;  // byte
	private int specularBlend;  // byte
	private int bumpBlend;  // byte
	
	public SkinpaintFloodEffect(int type, int version) {
		super(type, version);
	}

	public SkinpaintFloodEffect(SkinpaintFloodEffect effect) {
		super(effect);

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
		hairFaceCamera = effect.hairFaceCamera;
		EffectComponent.copyArray(diffuseColor, effect.diffuseColor);
		diffuseColorIndex = effect.diffuseColorIndex;
		diffuseBlend = effect.diffuseBlend;
		specularBlend = effect.specularBlend;
		bumpBlend = effect.bumpBlend;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		int varModifiersCount = in.readInt();
		for (int i = 0; i < varModifiersCount; i++) varModifiers.add(new VarModifier(in));
		
		int variableCount = in.readInt();
		for (int i = 0; i < variableCount; i++) variables.add(new Variable(in));
		
		int valueCount = in.readInt();
		for (int i = 0; i < valueCount; i++) variableValues.add(in.readFloat());
		
		hairFaceCamera = in.readBoolean();
		in.readFloats(diffuseColor);  // this one is BE!
		diffuseColorIndex = in.readInt();
		diffuseBlend = in.readByte();
		specularBlend = in.readByte();
		bumpBlend = in.readByte();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(varModifiers.size());
		for (VarModifier v : varModifiers) v.write(out);
		
		out.writeInt(variables.size());
		for (Variable m : variables) m.write(out);
		
		out.writeInt(variableValues.size());
		for (float f : variableValues) out.writeFloat(f);
		
		out.writeBoolean(hairFaceCamera);
		out.writeFloats(diffuseColor);
		out.writeInt(diffuseColorIndex);
		out.writeByte(diffuseBlend);
		out.writeByte(specularBlend);
		out.writeByte(bumpBlend);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		{
			ArgScriptCommand c = null;
			if ((c = block.getCommand("hairFaceCamera")) != null)
			{
				hairFaceCamera = Boolean.parseBoolean(c.getSingleArgument());
			}
			if ((c = block.getCommand("diffuseColor")) != null) 
			{
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
			}
			
			for (int i = 0; i < 2; i++) {
				VarModifier varMod = new VarModifier();
				varMod.modifierType = -1;
				varModifiers.add(varMod);
			}
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
						throw new ArgScriptException(KEYWORD + ": " + LIST_MODIFIER_CHANNELS.get(i) + " must have arguments or 'set', 'add', 'mult', 'div' options.");
					}
				}
				else if (c.getOptionCount() == 0) {
					Variable var = new Variable();
					var.channel = (byte) i;
					var.varModifierIndex = -1;
					var.operationType = 0;
					var.range[0] = 0;
					var.range[1] = 1.0f;
					var.values[0] = Float.parseFloat(c.getSingleArgument());
					var.values[1] = 1.0f; 
					variables.add(var);
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
				for (ArgScriptCommand command : commands) 
				{
					Variable var = new Variable();
					var.channel = LIST_MODIFIER_CHANNELS.indexOf(command.getKeyword());
					var.varModifierIndex = modifierIndex;
					variables.add(var);
					
					for (int i = 0; i < OPERATION_TYPES.size(); i++) {
						ArgScriptOption o = command.getOption(OPERATION_TYPES.get(i));
						if (o != null) {
							List<String> optionArgs = o.getArguments(2);
							var.values[0] = Float.parseFloat(optionArgs.get(0));
							var.values[1] = Float.parseFloat(optionArgs.get(1));
							var.operationType = i;
							break;
						}
					}
					
					ArgScriptOption o = command.getOption("range");
					if (o != null) {
						List<String> optionArgs = o.getArguments(2);
						var.range[0] = Float.parseFloat(optionArgs.get(0));
						var.range[1] = Float.parseFloat(optionArgs.get(1));
					}
					
					for (int i = 0; i < MODIFIER_FLAG.size(); i++) {
						if (command.hasFlag(MODIFIER_FLAG.get(i))) {
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
		HashMap<Integer, ArgScriptBlock> modifierBlocks = new HashMap<Integer, ArgScriptBlock>();
		
		block.putCommand(new ArgScriptCommand("hairFaceCamera", Boolean.toString(hairFaceCamera)));
		
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
		
		return true;
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
			else if (varModifier.valueCount < variableValues.size()) {
				for (int i = 0; i < varModifier.valueCount; i++) {
					if (varModifier.valueIndex + i >= variableValues.size()) {
						System.out.println("BREAKPOINT");
					}
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
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		throw new UnsupportedOperationException("Inline SPSkinPaintFlood effect is not supported.");
	}

	
	// For Syntax Highlighting
	// most of the words are in SkinpaintParticleEffect
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"hairFaceCamera"
		};
	}
}
