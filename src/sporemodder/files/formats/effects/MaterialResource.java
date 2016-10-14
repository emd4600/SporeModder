package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class MaterialResource extends Resource {
	
	public static final int TYPE = 1;
	public static final int MASKED_TYPE = TYPE | TYPE_MASK;
	public static final String KEYWORD = "material";
	
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	private static final int W = 3;
	
	private static final HashMap<String, Integer> MATERIAL_PARAMS = new HashMap<String, Integer>();
	static {
		MATERIAL_PARAMS.put("lightIndex", Property.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("depthOffset", Property.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("aspectRatio", Property.getMaterialParam(0, W));
		MATERIAL_PARAMS.put("shDiffuseAtten", Property.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("shSpecularAtten", Property.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("shSpecularExponent", Property.getMaterialParam(0, W));
		// register 1
		MATERIAL_PARAMS.put("alphaDistances", Property.getMaterialParam(1, X));
		// register 2
		MATERIAL_PARAMS.put("alphaValues", Property.getMaterialParam(2, X));
		// register 3
		MATERIAL_PARAMS.put("facetAttenSet", Property.getMaterialParam(3, X));
		MATERIAL_PARAMS.put("facingNormalJitter", Property.getMaterialParam(3, W));
		// register 4
		MATERIAL_PARAMS.put("vertexAttenSet", Property.getMaterialParam(4, X));
		MATERIAL_PARAMS.put("vertexNormalJitter", Property.getMaterialParam(4, W));
		// register 5
		MATERIAL_PARAMS.put("scaleDistances", Property.getMaterialParam(5, X));
		// register 6
		MATERIAL_PARAMS.put("scaleValues", Property.getMaterialParam(6, X));
		// register 7
		MATERIAL_PARAMS.put("reflectancePower", Property.getMaterialParam(7, X));
		MATERIAL_PARAMS.put("alphaFacingAttenSet", Property.getMaterialParam(7, Y));
		// register 8
		MATERIAL_PARAMS.put("texTiles", Property.getMaterialParam(8, X));
		MATERIAL_PARAMS.put("texTileSpeed", Property.getMaterialParam(8, Z));
		MATERIAL_PARAMS.put("texTileOffset", Property.getMaterialParam(8, W));
		// register 9
		MATERIAL_PARAMS.put("texTileLifespanFrames", Property.getMaterialParam(9, X));
		MATERIAL_PARAMS.put("texTileLifespanClipPerRow", Property.getMaterialParam(9, Y));
		MATERIAL_PARAMS.put("texTileLifespanOffset", Property.getMaterialParam(9, Z));
		MATERIAL_PARAMS.put("texTileBias", Property.getMaterialParam(9, W));
		// register 10
		MATERIAL_PARAMS.put("lightDesaturation", Property.getMaterialParam(10, X));
		MATERIAL_PARAMS.put("fogRemoval", Property.getMaterialParam(10, Y));
		MATERIAL_PARAMS.put("shadowRemoval", Property.getMaterialParam(10, Z));
		MATERIAL_PARAMS.put("sunColor", Property.getMaterialParam(10, X));
		// register 12
		MATERIAL_PARAMS.put("gasGiant_baseColorTiles", Property.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("gasGiant_baseDistortTiles", Property.getMaterialParam(12, Y));
		MATERIAL_PARAMS.put("gasGiant_cloudColorTiles", Property.getMaterialParam(12, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudDistortTiles", Property.getMaterialParam(12, W));
		
		MATERIAL_PARAMS.put("planetaryRings_shadowDensity", Property.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("planetaryRings_planetRadius", Property.getMaterialParam(12, Y));
		
		MATERIAL_PARAMS.put("model_uvSpeed", Property.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("model_uvScale", Property.getMaterialParam(12, Z));
		
		MATERIAL_PARAMS.put("column_beamVariance", Property.getMaterialParam(12, X));
		MATERIAL_PARAMS.put("column_beamCount", Property.getMaterialParam(12, Y));
		
		MATERIAL_PARAMS.put("stretch_uTimes", Property.getMaterialParam(12, X));
		
		MATERIAL_PARAMS.put("sunBlendDistances", Property.getMaterialParam(12, X));
		// register 13
		MATERIAL_PARAMS.put("sizeDistances", Property.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("sizeValues", Property.getMaterialParam(13, Z));
		
		MATERIAL_PARAMS.put("gasGiant_baseColorSpeed", Property.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("gasGiant_baseDistortSpeed", Property.getMaterialParam(13, Y));
		MATERIAL_PARAMS.put("gasGiant_baseDistortAmt", Property.getMaterialParam(13, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudColorSpeed", Property.getMaterialParam(13, W));
		
		MATERIAL_PARAMS.put("model_scale", Property.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("column_vPositions", Property.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("stretch_uScales", Property.getMaterialParam(13, X));
		
		MATERIAL_PARAMS.put("sunBlendValues", Property.getMaterialParam(13, X));
		MATERIAL_PARAMS.put("thinRing_widthDistances", Property.getMaterialParam(13, X));
		// register 14
		MATERIAL_PARAMS.put("lumDistances", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("lumValues", Property.getMaterialParam(14, Z));
		
		MATERIAL_PARAMS.put("windTimeFreq", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("windSpaceFreq", Property.getMaterialParam(14, Y));
		MATERIAL_PARAMS.put("windAmpVerticalScalar", Property.getMaterialParam(14, Z));
		MATERIAL_PARAMS.put("windWaveOffset", Property.getMaterialParam(14, W));
		
		MATERIAL_PARAMS.put("gasGiant_cloudDistortSpeed", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("gasGiant_cloudDistortAmt", Property.getMaterialParam(14, Y));
		MATERIAL_PARAMS.put("gasGiant_cloudColorAtten", Property.getMaterialParam(14, Z));
		MATERIAL_PARAMS.put("gasGiant_cloudDensity", Property.getMaterialParam(14, W));
		
		MATERIAL_PARAMS.put("model_rgbIn", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("column_vAlphas", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("stretch_vTimes", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("emissiveLerpDistances", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("borderSpriteNudge", Property.getMaterialParam(14, X));
		MATERIAL_PARAMS.put("thinRing_widthValues", Property.getMaterialParam(14, X));
		// register 15
		MATERIAL_PARAMS.put("flickerFreq", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("flickerLegato", Property.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("flickerLumMin", Property.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("ring0spin", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("ring0alpha", Property.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("ring1spin", Property.getMaterialParam(15, Z));
		MATERIAL_PARAMS.put("ring1alpha", Property.getMaterialParam(15, W));
		
		MATERIAL_PARAMS.put("windAmpDistances", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("windAmpValues", Property.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("borderSpriteOffscreenPct", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("borderSpriteAlwaysUp", Property.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("borderSpriteSnapRotation", Property.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("rotationSpeed", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("rotationVariance", Property.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("tiltVariance", Property.getMaterialParam(15, Z));
		
		MATERIAL_PARAMS.put("gasGiant_scatterAtten", Property.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("model_rgbOut", Property.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("column_rgb", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("column_rotateSpeed", Property.getMaterialParam(15, W));
		
		MATERIAL_PARAMS.put("stretch_vValues", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("emissiveLerpValues", Property.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("emissiveLerp", Property.getMaterialParam(15, X));
		
		MATERIAL_PARAMS.put("thinRing_discOpacity", Property.getMaterialParam(15, X));
		MATERIAL_PARAMS.put("thinRing_discRampScale", Property.getMaterialParam(15, Y));
		MATERIAL_PARAMS.put("thinRing_discRampOffset", Property.getMaterialParam(15, Z));
		// cloudVanilla.smt/smokeVanilla.smt/effectVanilla.smt
		MATERIAL_PARAMS.put("animBlendRate", Property.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("stripLength", Property.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("topFade", Property.getMaterialParam(0, Z));
		// grassVanilla.smt
		MATERIAL_PARAMS.put("startFade", Property.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("endFade", Property.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("tilt", Property.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("startFadeIn", Property.getMaterialParam(0, W));
		
		MATERIAL_PARAMS.put("startScale", Property.getMaterialParam(1, X));
		MATERIAL_PARAMS.put("endScale", Property.getMaterialParam(1, Y));
		MATERIAL_PARAMS.put("scaleFactor", Property.getMaterialParam(1, Z));
		MATERIAL_PARAMS.put("endFadeIn", Property.getMaterialParam(1, W));
		// effectVanilla.smt
		MATERIAL_PARAMS.put("minScaleRatio", Property.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("maxScaleRatio", Property.getMaterialParam(0, W));
		// effectVanilla.smt/effectPlanetRingMaterial
		MATERIAL_PARAMS.put("ring0vel", Property.getMaterialParam(0, X));
		MATERIAL_PARAMS.put("ring1vel", Property.getMaterialParam(0, Y));
		MATERIAL_PARAMS.put("ring0fade", Property.getMaterialParam(0, Z));
		MATERIAL_PARAMS.put("ring1fade", Property.getMaterialParam(0, W));
		// effectVanilla.smt/effectStarMaterial
		MATERIAL_PARAMS.put("fadeStart", Property.getMaterialParam(1, X));
		MATERIAL_PARAMS.put("fadeEnd", Property.getMaterialParam(1, Y));
		MATERIAL_PARAMS.put("scaleFactor", Property.getMaterialParam(1, Z));
		MATERIAL_PARAMS.put("depthBias", Property.getMaterialParam(1, W));
	}
	
	private static class Property {
		private int type; //byte
		private final ResourceID name = new ResourceID();
		// registerIndex = (group - 0x10000) >> 4
		private float[] valuesF;
		private int[] valuesI;
		private boolean[] valuesB;
		private float valueF;
		private int valueI;
		private boolean valueB;
		private ResourceID valueRes;
		
		private static String getRegisterOffsetString(int registerOffset, int valueCount) {
			if (registerOffset + valueCount > 4) return null;
			StringBuffer sb =  new StringBuffer(4);
			for (int i = registerOffset; i < registerOffset + valueCount; i++) {
				sb.append(getRegisterOffsetChar(i));
			}
			return sb.toString();
		}
		
		private static char getRegisterOffsetChar(int registerOffset) {
			if (registerOffset == X) return 'x';
			else if (registerOffset == Y) return 'y';
			else if (registerOffset == Z) return 'z';
			else if (registerOffset == W) return 'w';
			else return Character.forDigit(registerOffset, 10);
		}
		
		private String getNameString(int valueCount) {
			int num = (name.getNameID() - 0x10000) >> 4;
			int registerIndex = num / 4;
			int registerOffset = num % 4;
			String offsetString = getRegisterOffsetString(registerOffset, valueCount);
			if (offsetString == null) {
				return name.toString();
			} else {
				return "c" + registerIndex + "." + offsetString;
			}
		}
		
		private static int getMaterialParam(int index, int offset) {
			int num = index * 4 + offset;
			return (num << 4) + 0x10000;
		}
		
		private static int getRegisterOffset(String str) {
			if (str.startsWith("x")) return X;
			else if (str.startsWith("y")) return Y;
			else if (str.startsWith("z")) return Z;
			else if (str.startsWith("w")) return W;
			return -1;
		}
		
		private void setName(String string) {
			if (string.startsWith("#") || string.startsWith("0x")) {
				name.setNameID(Hasher.decodeInt(string));
				name.setGroupID(0);
			} else {
				int id = 0;
				if (MATERIAL_PARAMS.containsKey(string)) {
					id = MATERIAL_PARAMS.get(string);
				}
				else {
					String[] splits  = string.split("\\.", 2);
					if (splits.length != 2) {
						throw new UnsupportedOperationException("Material: Wrong property format: " + string);
					}
					int registerIndex = Integer.parseInt(splits[0].substring(1));
					int registerOffset = getRegisterOffset(splits[1]);
					if (registerOffset == -1) {
						throw new UnsupportedOperationException("Material: Wrong property format: " + string);
					}
					id = getMaterialParam(registerIndex, registerOffset);
				}
				
				name.setNameID(id);
				name.setGroupID(0);
			}
		}
		
		@Override
		public String toString() {
			switch(type) {
			case 0: return "float " + getNameString(1) + " " + Float.toString(valueF);
			case 1: return "int " + getNameString(1) + " " + valueI;
			case 2: return "boolean " + getNameString(1) + " " + valueB;
			case 3: String s = "floats " + getNameString(valuesF.length);
					for (float f : valuesF) s += " " + Float.toString(f);
					return s;
			case 4: String s1 = "ints " + getNameString(valuesI.length);
					for (int f : valuesI) s1 += " " + f;
					return s1;
			case 5: String s2 = "booleans " + getNameString(valuesB.length);
					for (boolean f : valuesB) s2 += " " + f;
					return s2;
			case 6: return "texture sampler" + name.getNameID() + " " + Hasher.getFileName(valuesI[1]);
			default: return null;
			}
		}
		
		public ArgScriptCommand toCommand() {
			ArgScriptCommand c;
			switch(type) {
			case 0: return new ArgScriptCommand("float", getNameString(1), Float.toString(valueF));
			
			case 1: return new ArgScriptCommand("int", getNameString(1), Integer.toString(valueI));
			
			case 2: return new ArgScriptCommand("boolean", getNameString(1), Boolean.toString(valueB));
			
			case 3: c = new ArgScriptCommand("floats", getNameString(valuesF.length));
					for (float f : valuesF) c.addArgument(Float.toString(f));
					return c;
					
			case 4: c = new ArgScriptCommand("ints", getNameString(valuesI.length));
					for (int f : valuesI) c.addArgument(Integer.toString(f));
					return c;
			
			case 5: c = new ArgScriptCommand("booleans", getNameString(valuesB.length));
					for (boolean f : valuesB) c.addArgument(Boolean.toString(f));
					return c;
			
			case 6: return new ArgScriptCommand("texture", "sampler" + name.getNameID(), valueRes.toString());
			default: return null;
			}
		}
		
		public static Property parseProperty(ArgScriptCommand c) throws ArgScriptException {
			Property prop = new Property();
			List<String> args = c.getArguments(2, Integer.MAX_VALUE);
			String keyword = c.getKeyword();
			if (keyword.equals("float")) {
				prop.type = 0;
				prop.setName(args.get(0));
				prop.valueF = Float.parseFloat(args.get(1));
			} else if (keyword.equals("int")) {
				prop.type = 1;
				prop.setName(args.get(0));
				prop.valueI = Integer.parseInt(args.get(1));
			} else if (keyword.equals("boolean")) {
				prop.type = 2;
				prop.setName(args.get(0));
				prop.valueB = Boolean.parseBoolean(args.get(1));
			} else if (keyword.equals("floats")) {
				prop.type = 3;
				prop.setName(args.get(0));
				prop.valuesF = new float[args.size() - 2];
				for (int i = 0; i < prop.valuesF.length; i++) {
					prop.valuesF[i] = Float.parseFloat(args.get(i + 2));
				}
			} else if (keyword.equals("ints")) {
				prop.type = 4;
				prop.setName(args.get(0));
				prop.valuesI = new int[args.size() - 2];
				for (int i = 0; i < prop.valuesI.length; i++) {
					prop.valuesI[i] = Integer.parseInt(args.get(i + 2));
				}
			} else if (keyword.equals("booleans")) {
				prop.type = 5;
				prop.setName(args.get(0));
				prop.valuesB = new boolean[args.size() - 2];
				for (int i = 0; i < prop.valuesI.length; i++) {
					prop.valuesB[i] = Boolean.parseBoolean(args.get(i + 2));
				}
			} else if (keyword.equals("texture")) {
				prop.type = 6;
				prop.valueRes = new ResourceID(0, 0);
				if (args.get(0).startsWith("sampler")) {
					prop.name.setNameID(Integer.parseInt(args.get(0).substring(7)));
				}
				prop.valueRes = new ResourceID(args.get(1));
				
			} else {
				return null;
			}
			
			return prop;
		}
	}
	
	private final ResourceID shaderID = new ResourceID();
	private final List<Property> properties = new ArrayList<Property>();
	
	@Override
	public boolean read(InputStreamAccessor in) throws IOException {

		resourceID.read(in);
		shaderID.read(in);
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			Property prop = new Property();
			prop.name.read(in);
			prop.type = in.readByte();
			
			if (prop.type == 0) {
				prop.valueF = in.readFloat();
				
			} else if (prop.type == 1) {
				prop.valueI = in.readInt();
				
			} else if (prop.type == 2) {
				prop.valueB = in.readByte() == 0 ? false : true;
				
			} else if (prop.type == 3) {
				prop.valuesF = new float[in.readShort()];
				in.readFloats(prop.valuesF);
				
			} else if (prop.type == 4) {
				prop.valuesI = new int[in.readShort()];
				in.readInts(prop.valuesI);
				
			} else if (prop.type == 5) {
				prop.valuesB = new boolean[in.readShort()];
				for (int f = 0; f < prop.valuesB.length; f++) {
					prop.valuesB[f] = in.readByte() == 0 ? false : true;
				}
				
			} else if (prop.type == 6) {
				prop.valueRes = new ResourceID(in);
			}
			properties.add(prop);
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		resourceID.write(out);
		shaderID.write(out);
		
		out.writeInt(properties.size());
		for (Property prop : properties) {
			prop.name.write(out);
			out.writeByte(prop.type);
			
			if (prop.type == 0) {
				out.writeFloat(prop.valueF);
			} else if (prop.type == 1) {
				out.writeInt(prop.valueI);
			} else if (prop.type == 2) {
				out.writeBoolean(prop.valueB);
			} else if (prop.type == 3) {
				out.writeShort(prop.valuesF.length);
				out.writeFloats(prop.valuesF);
			} else if (prop.type == 4) {
				out.writeShort(prop.valuesI.length);
				out.writeInts(prop.valuesI);
			} else if (prop.type == 5) {
				out.writeShort(prop.valuesB.length);
				out.writeBooleans(prop.valuesB);
			} else if (prop.type == 6) {
				prop.valueRes.write(out);
			}
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		resourceID.parse(block.getSingleArgument());
		ArgScriptCommand cShader = block.getCommand("shader");
		if (cShader != null) {
			shaderID.parse(cShader.getSingleArgument());
		}
		
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand c : commands) {
			if (!c.getKeyword().equals("shader")) {
				Property prop = Property.parseProperty(c);
				if (prop != null) {
					properties.add(prop);
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		block.putCommand(new ArgScriptCommand("shader", shaderID.toString()));
		for (Property prop : properties) {
			block.putCommand(prop.toCommand());
		}
		
		return true;
	}

	@Override
	public ArgScriptBlock toBlock() {
		ArgScriptBlock b = new ArgScriptBlock(KEYWORD, resourceID.toString());
		toBlock(b);
		return b;
	}

	@Override
	public boolean parseCommand(ArgScriptCommand c) throws ArgScriptException,
			IOException {
		throw new UnsupportedOperationException(KEYWORD + ": Inline command format is not supported.");
	}

	@Override
	public ArgScriptCommand toCommand() {
		throw new UnsupportedOperationException(KEYWORD + ": Inline command format is not supported.");
	}
	
	@Override
	public int getType() {
		return MASKED_TYPE;
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
				"lightIndex", "depthOffset", "aspectRatio", "shDiffuseAtten", "shSpecularAtten", "shSpecularExponent",
				"alphaDistances", "alphaValues", "facetAttenSet", "facingNormalJitter", "scaleDistances",
				"scaleValues", "reflectancePower", "alphaFacingAttenSet", "texTiles", "texTileSpeed", "texTileOffset",
				"texTileLifespanFrames", "texTileLifespanClipPerRow", "texTileLifespanOffset", "texTileBias",
				"lightDesaturation", "fogRemoval", "shadowRemoval", "sunColor", "gasGiant_baseColorTiles",
				"gasGiant_baseDistortTiles", "gasGiant_cloudColorTiles", "gasGiant_cloudDistortTiles",
				"planetaryRings_shadowDensity", "planetaryRings_planetRadius", "model_uvSpeed", "model_uvScale",
				"column_beamVariance", "column_beamCount", "stretch_uTimes", "sunBlendDistances", "sizeDistances",
				"sizeValues", "gasGiant_baseColorSpeed", "gasGiant_baseDistortSpeed", "gasGiant_baseDistortAmt",
				"gasGiant_cloudColorSpeed", "model_scale", "column_vPositions", "stretch_uScales", "sunBlendValues", 
				"thinRing_widthDistances", "lumDistances", "lumValues", "windTimeFreq", "windSpaceFreq", 
				"windAmpVerticalScalar", "windWaveOffset", "gasGiant_cloudDistortSpeed", "gasGiant_cloudDistortAmt",
				"gasGiant_cloudColorAtten", "gasGiant_cloudDensity", "model_rgbIn", "column_vAlphas", "stretch_vTimes",
				"emissiveLerpDistances", "borderSpriteNudge", "thinRing_widthValues", "flickerFreq", "flickerLegato",
				"flickerLumMin", "ring0spin", "ring0alpha", "ring1spin", "ring1alpha", "windAmpDistances", "windAmpValues",
				"borderSpriteOffscreenPct", "borderSpriteAlwaysUp", "borderSpriteSnapRotation", "rotationSpeed", "rotationVariance",
				"tiltVariance", "gasGiant_scatterAtten", "model_rgbOut", "column_rgb", "column_rotateSpeed", "stretch_vValues",
				"emissiveLerpValues", "emissiveLerp", "thinRing_discOpacity", "thinRing_discRampScale", "thinRing_discRampOffset",
				"animBlendRate", "stripLength", "topFade", "startFade", "endFade", "tilt", "startFadeIn", "startScale", "endScale",
				"scaleFactor", "endFadeIn", "minScaleRatio", "maxScaleRatio", "ring0vel", "ring1vel", "ring0fade",
				"ring1fade", "fadeStart", "fadeEnd", "scaleFactor", "depthBias"
			};
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
			"shader", "int", "float", "boolean", "texture", "ints", "floats", "booleans"
		};
	}
}
