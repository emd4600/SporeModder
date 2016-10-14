package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.files.formats.argscript.ArgScript;

public class BrushEffect extends EffectComponent {
	
	private static final ArgScriptEnum ENUM_DRAWMODE = new ArgScriptEnum(new String[] {
			"add", "biased", "substract", "level", "levelRange", "absoluteMax", "absoluteMin"
		}, new int[] {
			0, 1, 2, 3, 4, 5, 6
		});;
	private static final ArgScriptEnum ENUM_FILTER_TYPE = new ArgScriptEnum(new String[] {
			"global", "local"
		}, new int[] {
			2, 1
		});
	private static final ArgScriptEnum ENUM_FILTER = new ArgScriptEnum(new String[] {
			"smooth", "median", "lowpass", "lowpass1", "lowpass2", "lowpass3", "lowpass4", "lowpass5",
			"highpass", "highpass1", "highpass2", "highpass3", "highpass4",
			"edge", "edge1", "edge2", "edge3",
			"add", "level", "susbtract", "fractal", "winderosion", "watererosion"
		}, new int[] {
			3, 3, 3, 3, 3, 3, 3, 3,
			8, 8, 8, 8, 8,
			0xC, 0xC, 0xC, 0xC,
			3, 3, 3, 3, 0x12, 3
		});
	private static final ArgScriptEnum ENUM_FILTER_MODE = new ArgScriptEnum(new String[] {
			"replace", "add", "average"
		}, new int[] {
			1, 2, 3
		});
	private static final ArgScriptEnum ENUM_COND = new ArgScriptEnum(new String[] {
			"less", "greater", "between"
		}, new int[] {
			1, 2, 3
		});
	
	public static final int TYPE = 0x20;
	public static final int MIN_VERSION = 2;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "brush";
	
	private final int[] field_8 = new int[3];
	private final ResourceID texture = new ResourceID();
	private byte drawMode;
	private boolean texGaussian;
	private float texGaussianValue;
	private float life = 5;
	private float[] size = new float[] { 1 };
	private float[] intensity = new float[] { 1 };
	private boolean waterLevelIsZero;
	private float intensityVary;
	private float sizeVary;
	private int field_60;
	private final float[] texOffset = new float[2];
	private float drawLevel;
	private float texVary;
	private float field_74;
	private float field_78;
	private float rate = 1;
	private boolean useFilter;
	private boolean sizeGlobal;
	private byte filter;
	private byte filterMode;
	private float filterValue;
	private float[] spacing = new float[0];
	private float spacingVary;
	private byte cond;
	private float cond_0;
	private float cond_1;
	private byte falloff;
	private float falloff_0;
	private float falloff_1;
	private byte gradientCond;
	private float gradientCond_0;
	private float gradientCond_1;
	private byte gradientCondFalloff;
	private float gradientCondFalloff_0;
	private float gradientCondFalloff_1;
	private boolean texCube;
	private final float[] rotate = new float[3];
	private final float[] rotateVary = new float[3];
	private boolean useRibbon;
	private int ribbonNumTiles = 1;
	private int ribbonNumSteps = 1;
	private int ribbonNumTexStrips = 1;
	private int ribbonNumSkip = 20;
	private boolean ribbonCap = true;

	public BrushEffect(int type, int version) {
		super(type, version);
	}
	
	public BrushEffect(BrushEffect effect) {
		super(effect);
		field_8[0] = effect.field_8[0];
		field_8[1] = effect.field_8[1];
		field_8[2] = effect.field_8[2];
		texture.copy(effect.texture);
		drawMode = effect.drawMode;
		texGaussian = effect.texGaussian;
		texGaussianValue = effect.texGaussianValue;
		life = effect.life;
		
		size = new float[effect.size.length];
		for (int i = 0; i < size.length; i++) size[i] = effect.size[i];
		intensity = new float[effect.size.length];
		for (int i = 0; i < intensity.length; i++) intensity[i] = effect.intensity[i];
		waterLevelIsZero = effect.waterLevelIsZero;
		intensityVary = effect.intensityVary;
		sizeVary = effect.sizeVary;
		field_60 = effect.field_60;
		
		texOffset[0] = effect.texOffset[0];
		texOffset[1] = effect.texOffset[1];
		drawLevel = effect.drawLevel;
		texVary = effect.texVary;
		field_74 = effect.field_74;
		field_78 = effect.field_78;
		rate = effect.rate;
		
		useFilter = effect.useFilter;
		sizeGlobal = effect.sizeGlobal;
		filter = effect.filter;
		filterMode = effect.filterMode;
		filterValue = effect.filterValue;
		
		spacing = new float[effect.spacing.length];
		for (int i = 0; i < spacing.length; i++) spacing[i] = effect.spacing[i];
		spacingVary = effect.spacingVary;
		
		cond = effect.cond;
		cond_0 = effect.cond_0;
		cond_1 = effect.cond_1;
		
		falloff = effect.falloff;
		falloff_0 = effect.falloff_0;
		falloff_1 = effect.falloff_1;
		
		gradientCond = effect.gradientCond;
		gradientCond_0 = effect.gradientCond_0;
		gradientCond_1 = effect.gradientCond_1;
		
		gradientCondFalloff = effect.gradientCondFalloff;
		gradientCondFalloff_0 = effect.gradientCondFalloff_0;
		gradientCondFalloff_1 = effect.gradientCondFalloff_1;
		
		texCube = effect.texCube;
		rotate[0] = effect.rotate[0];
		rotate[1] = effect.rotate[1];
		rotate[2] = effect.rotate[2];
		rotateVary[0] = effect.rotateVary[0];
		rotateVary[1] = effect.rotateVary[1];
		rotateVary[2] = effect.rotateVary[2];
		
		useRibbon = effect.useRibbon;
		ribbonNumTiles = effect.ribbonNumTiles;
		ribbonNumSteps = effect.ribbonNumSteps;
		ribbonNumTexStrips = effect.ribbonNumTexStrips;
		ribbonNumSkip = effect.ribbonNumSkip;
		ribbonCap = effect.ribbonCap;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException 
	{
		in.readLEInts(field_8);  // floats?
		texture.read(in);
		drawMode = in.readByte();
		texGaussian = in.readBoolean();
		texGaussianValue = in.readFloat();
		life = in.readFloat();
		
		size = new float[in.readInt()];
		in.readFloats(size);
		intensity = new float[in.readInt()];
		in.readFloats(intensity);
		waterLevelIsZero = in.readBoolean();
		intensityVary = in.readFloat();
		sizeVary = in.readFloat();
		field_60 = in.readInt();
		if (field_60 != 0) {
			throw new IOException("Error in pos " + in.getFilePointer());
		}
		
		in.readLEFloats(texOffset);
		drawLevel = in.readFloat();
		texVary = in.readFloat();
		field_74 = in.readFloat();
		field_78 = in.readFloat();
		rate = in.readFloat();
		
		useFilter = in.readBoolean();
		sizeGlobal = in.readBoolean();
		filter = in.readByte();
		filterMode = in.readByte();
		
		filterValue = in.readFloat();
		spacing = new float[in.readInt()];
		in.readFloats(spacing);
		spacingVary = in.readFloat();
		
		cond = in.readByte();
		cond_0 = in.readFloat();
		cond_1 = in.readFloat();
		
		falloff = in.readByte();
		falloff_0 = in.readFloat();
		falloff_1 = in.readFloat();
		
		gradientCond = in.readByte();
		gradientCond_0 = in.readFloat();
		gradientCond_1 = in.readFloat();
		
		gradientCondFalloff = in.readByte();
		gradientCondFalloff_0 = in.readFloat();
		gradientCondFalloff_1 = in.readFloat();
		
		texCube = in.readBoolean();
		in.readLEFloats(rotate);
		in.readLEFloats(rotateVary);
		
		useRibbon = in.readBoolean();
		ribbonNumTiles = in.readInt();
		ribbonNumSteps = in.readInt();
		ribbonNumTexStrips = in.readInt();
		ribbonNumSkip = in.readInt();
		ribbonCap = in.readBoolean();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeLEInts(field_8);
		texture.write(out);
		out.writeByte(drawMode);
		out.writeBoolean(texGaussian);
		out.writeFloat(texGaussianValue);
		out.writeFloat(life);
		
		out.writeInt(size.length);
		out.writeFloats(size);
		out.writeInt(intensity.length);
		out.writeFloats(intensity);
		out.writeBoolean(waterLevelIsZero);
		out.writeFloat(intensityVary);
		out.writeFloat(sizeVary);
		out.writeInt(field_60);
		
		out.writeLEFloats(texOffset);
		out.writeFloat(drawLevel);
		out.writeFloat(texVary);
		out.writeFloat(field_74);
		out.writeFloat(field_78);
		out.writeFloat(rate);
		
		out.writeBoolean(useFilter);
		out.writeBoolean(sizeGlobal);
		out.writeByte(filter);
		out.writeByte(filterMode);
		
		out.writeFloat(filterValue);
		out.writeInt(spacing.length);
		out.writeFloats(spacing);
		out.writeFloat(spacingVary);
		
		out.writeByte(cond);
		out.writeFloat(cond_0);
		out.writeFloat(cond_1);
		
		out.writeByte(falloff);
		out.writeFloat(falloff_0);
		out.writeFloat(falloff_1);
		
		out.writeByte(gradientCond);
		out.writeFloat(gradientCond_0);
		out.writeFloat(gradientCond_1);
		
		out.writeByte(gradientCondFalloff);
		out.writeFloat(gradientCondFalloff_0);
		out.writeFloat(gradientCondFalloff_1);
		
		out.writeBoolean(texCube);
		out.writeLEFloats(rotate);
		out.writeLEFloats(rotateVary);
		
		out.writeBoolean(useRibbon);
		out.writeInt(ribbonNumTiles);
		out.writeInt(ribbonNumSteps);
		out.writeInt(ribbonNumTexStrips);
		out.writeInt(ribbonNumSkip);
		out.writeBoolean(ribbonCap);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException, ArgScriptException {
		{
			ArgScriptCommand cRibbon = block.getCommand("ribbon");
			if (cRibbon != null) {
				if (cRibbon.hasFlag("cap")) ribbonCap = true;
				
				ArgScriptOption oNumTiles = cRibbon.getOption("numTiles");
				if (oNumTiles != null) ribbonNumTiles = Integer.parseInt(oNumTiles.getSingleArgument());
				
				ArgScriptOption oNumSteps = cRibbon.getOption("numSteps");
				if (oNumSteps != null) ribbonNumSteps = Integer.parseInt(oNumSteps.getSingleArgument());
				
				ArgScriptOption oNumTextureStrips = cRibbon.getOption("numTextureStrips");
				if (oNumTextureStrips != null) ribbonNumTexStrips = Integer.parseInt(oNumTextureStrips.getSingleArgument());
				
				ArgScriptOption oNumSkip = cRibbon.getOption("numSkip");
				if (oNumSkip != null) ribbonNumSkip = Integer.parseInt(oNumSkip.getSingleArgument());
			}
		}
		{
			ArgScriptCommand cIntensity = block.getCommand("intensity");
			if (cIntensity != null) intensity = ArgScript.stringsToFloats(cIntensity.getArguments());
			else intensity = new float[0];
			
			ArgScriptOption oVary = cIntensity.getOption("vary");
			if (oVary != null) intensityVary = Float.parseFloat(oVary.getSingleArgument());
		}
		{
			ArgScriptCommand cSize = block.getCommand("size");
			if (cSize != null) size = ArgScript.stringsToFloats(cSize.getArguments());
			else size = new float[0];
			
			ArgScriptOption oVary = cSize.getOption("vary");
			if (oVary != null) sizeVary = Float.parseFloat(oVary.getSingleArgument());
			if (cSize != null) {
				if (cSize.hasFlag("global")) sizeGlobal = true;
			}
		}
		{
			ArgScriptCommand cSpacing = block.getCommand("spacing");
			if (cSpacing != null) spacing = ArgScript.stringsToFloats(cSpacing.getArguments());
			else spacing = new float[0];
			
			ArgScriptOption oVary = cSpacing.getOption("vary");
			if (oVary != null) spacingVary = Float.parseFloat(oVary.getSingleArgument());
		}

		ArgScriptCommand cLife = block.getCommand("life");
		if (cLife != null) life = Float.parseFloat(cLife.getSingleArgument());
		
		ArgScriptCommand cRate = block.getCommand("rate");
		if (cRate != null) rate = Float.parseFloat(cRate.getSingleArgument());
		
		{
			ArgScriptCommand cTexture = new ArgScriptCommand("texture");
			if (cTexture != null) 
			{
				texture.parse(cTexture.getSingleArgument());
				ArgScriptOption oDrawMode = cTexture.getOption("draw");
				if (oDrawMode != null) {
					List<String> args = oDrawMode.getArguments(0, 3);
					if (args.size() > 0) {
						ENUM_DRAWMODE.getValue(args.get(0));
						
						if (args.size() > 1) {
							field_74 = Float.parseFloat(args.get(1));
							if (args.size() > 2) {
								field_78 = Float.parseFloat(args.get(2));
							}
						}
					}
				}
				
				ArgScriptOption oVary = cTexture.getOption("vary");
				if (oVary != null) texVary = Float.parseFloat(oVary.getSingleArgument());
				
				ArgScriptOption oOffset = cTexture.getOption("offset");
				if (oOffset != null) ArgScript.parseFloatList(oOffset.getSingleArgument(), texOffset);
				
				ArgScriptOption oGaussian = cTexture.getOption("gaussian");
				if (oGaussian != null) {
					texGaussian = true;
					List<String> args = oGaussian.getArguments(0, 1);
					if (args.size() == 1) texGaussianValue = Float.parseFloat(args.get(0));
				}
				
				if (cTexture.hasFlag("cube")) texCube = true;
			}
		}
		{
			ArgScriptCommand cFilter = block.getCommand("filter");
			if (cFilter != null) {
				useFilter = true;
				List<String> args = cFilter.getArguments(1, 2);
				filter = (byte) ENUM_FILTER.getValue(args.get(0));

				if (filter == 0xF || filter == 0x10 || filter == 0x14) {
					if (args.size() != 2) throw new ArgScriptException("Missing argument for filter.");
					filterValue = Float.parseFloat(args.get(1));
				}
				else if (args.size() == 2) {
					filterMode = (byte) ENUM_FILTER_MODE.getValue(args.get(1));
				}
			}
		}
		{
			ArgScriptCommand cCond = block.getCommand("cond");
			List<String> args = cCond.getArguments(2, 3);
			cond = (byte)ENUM_COND.getValue(args.get(0));
			cond_0 = Float.parseFloat(args.get(1));
			
			if (cond == 3) {
				if (args.size() != 3) throw new ArgScriptException("Missing argument for cond 'between'.");
				cond_1 = Float.parseFloat(args.get(2));
			}
			
			ArgScriptOption oFalloff = cCond.getOption("falloff");
			if (oFalloff != null) {
				List<String> falloffArgs = oFalloff.getArguments(2);
				falloff = 1;
				falloff_0 = Float.parseFloat(falloffArgs.get(0));
				falloff_1 = Float.parseFloat(falloffArgs.get(1));
			}
		}
		{
			ArgScriptCommand cCond = block.getCommand("gradientCond");
			List<String> args = cCond.getArguments(2, 3);
			gradientCond = (byte)ENUM_COND.getValue(args.get(0));
			gradientCond_0 = Float.parseFloat(args.get(1));
			
			if (gradientCond == 3) {
				if (args.size() != 3) throw new ArgScriptException("Missing argument for gradientCond 'between'.");
				gradientCond_1 = Float.parseFloat(args.get(2));
			}
			
			ArgScriptOption oFalloff = cCond.getOption("falloff");
			if (oFalloff != null) {
				List<String> falloffArgs = oFalloff.getArguments(2);
				gradientCondFalloff = 1;
				gradientCondFalloff_0 = Float.parseFloat(falloffArgs.get(0));
				gradientCondFalloff_1 = Float.parseFloat(falloffArgs.get(1));
			}
		}
		{
			ArgScriptCommand cRotate = block.getCommand("rotate");
			if (cRotate != null) {
				List<String> args = cRotate.getArguments(3);
				rotate[0] = Float.parseFloat(args.get(0));
				rotate[1] = Float.parseFloat(args.get(1));
				rotate[2] = Float.parseFloat(args.get(2));
				
				ArgScriptOption oRotateVary = cRotate.getOption("vary");
				if (oRotateVary != null) {
					List<String> varyArgs = oRotateVary.getArguments(3);
					rotateVary[0] = Float.parseFloat(varyArgs.get(0));
					rotateVary[1] = Float.parseFloat(varyArgs.get(1));
					rotateVary[2] = Float.parseFloat(varyArgs.get(2));
				}
			}
		}
		
		waterLevelIsZero = block.hasFlag("waterLevelIsZero");
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		
		// this is set to true when "ribbon" command is present
		if (useRibbon) {
			ArgScriptCommand cRibbon = new ArgScriptCommand("ribbon");
			
			if (ribbonCap) cRibbon.putFlag("cap");
			if (ribbonNumTiles != 1) cRibbon.putOption(new ArgScriptOption("numTiles", Integer.toString(ribbonNumTiles)));
			if (ribbonNumSteps != 1) cRibbon.putOption(new ArgScriptOption("numSteps", Integer.toString(ribbonNumSteps)));
			if (ribbonNumTexStrips != 1) cRibbon.putOption(new ArgScriptOption("numTextureStrips", Integer.toString(ribbonNumTexStrips)));
			if (ribbonNumSkip != 20) cRibbon.putOption(new ArgScriptOption("numSkip", Integer.toString(ribbonNumSkip)));
			
			block.putCommand(cRibbon);
		}
		
		if (intensity.length > 0) {
			ArgScriptCommand cIntensity = new ArgScriptCommand("intensity", ArgScript.floatsToStrings(intensity));
			if (intensityVary != 0) cIntensity.putOption(new ArgScriptOption("vary", Float.toString(intensityVary)));
			block.putCommand(cIntensity);
		}
		
		if (size.length > 0) {
			ArgScriptCommand cSize = new ArgScriptCommand("size", ArgScript.floatsToStrings(size));
			if (sizeVary != 0) cSize.putOption(new ArgScriptOption("vary", Float.toString(sizeVary)));
			if (sizeGlobal) cSize.putFlag("global");  // according to Spore, this can have up to 1 argument, but it just ignores it
			block.putCommand(cSize);
		}
		
		if (spacing.length > 0) {
			ArgScriptCommand cSpacing = new ArgScriptCommand("spacing", ArgScript.floatsToStrings(spacing));
			if (spacingVary != 0) cSpacing.putOption(new ArgScriptOption("vary", Float.toString(spacingVary)));  // value in range [0, 1]
			block.putCommand(cSpacing);
		}
		
		if (life != 5) block.putCommand(new ArgScriptCommand("life", Float.toString(life)));
		
		if (rate != 1) block.putCommand(new ArgScriptCommand("rate", Float.toString(rate)));
		
		if (!texture.isDefault()) {
			ArgScriptCommand cTexture = new ArgScriptCommand("texture", texture.toString());
			ArgScriptOption oDrawMode = new ArgScriptOption("draw", ENUM_DRAWMODE.getKey(drawMode));
			if (drawMode == 3) {
				oDrawMode.addArgument(Float.toString(drawLevel));
			}
			else if (field_74 != 0 && field_78 != 0) {
				oDrawMode.addArgument(Float.toString(field_74));
				oDrawMode.addArgument(Float.toString(field_78));
			}
			cTexture.putOption(oDrawMode);
			
			if (texVary != 0) cTexture.putOption(new ArgScriptOption("vary", Float.toString(texVary)));
			if (texOffset[0] != 0 || texOffset[1] != 0) cTexture.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(texOffset)));
			if (texGaussian) {
				ArgScriptOption oGaussian = new ArgScriptOption("gaussian");
				if (texGaussianValue != 1) {
					oGaussian.addArgument(Float.toString(texGaussianValue));
				}
				cTexture.putOption(oGaussian);
			}
			if (texCube) cTexture.putFlag("cube");
			
			block.putCommand(cTexture);
		}
		
		if (useFilter) {
			// here should go -global or -local too, but it seems Spore didn't use it at all
			ArgScriptCommand cFilter = new ArgScriptCommand("filter", ENUM_FILTER.getKey(filter));
			if (filter == 0xF || filter == 0x10 || filter == 0x14) {
				cFilter.addArgument(Float.toString(filterValue));
			} else if (filterMode != 0) {
				cFilter.addArgument(ENUM_FILTER_MODE.getKey(filterMode));
			}
			block.putCommand(cFilter);
		}
		
		if (cond != 0) {
			ArgScriptCommand cCond = new ArgScriptCommand("cond", ENUM_COND.getKey(cond));
			
			cCond.addArgument(Float.toString(cond_0));
			if (cond == 3) {
				cCond.addArgument(Float.toString(cond_1));
			}
			
			if (falloff == 1) {
				ArgScriptOption oFalloff = new ArgScriptOption("falloff");
				oFalloff.addArgument(Float.toString(falloff_0));
				oFalloff.addArgument(Float.toString(falloff_1));
				
				//TODO lots of checks here
				
				cCond.putOption(oFalloff);
			}
			
			block.putCommand(cCond);
		}
		
		if (gradientCond != 0) {
			ArgScriptCommand cCond = new ArgScriptCommand("gradientCond", ENUM_COND.getKey(gradientCond));
			
			cCond.addArgument(Float.toString(gradientCond_0));
			if (gradientCond == 3) {
				cCond.addArgument(Float.toString(gradientCond_1));
			}
			
			if (gradientCondFalloff == 1) {
				ArgScriptOption oFalloff = new ArgScriptOption("falloff");
				oFalloff.addArgument(Float.toString(gradientCondFalloff_0));
				oFalloff.addArgument(Float.toString(gradientCondFalloff_1));
				
				//TODO lots of checks here
				
				cCond.putOption(oFalloff);
			}
			
			block.putCommand(cCond);
		}
		
		
		if (rotate[0] != 0 || rotate[1] != 0 || rotate[2] != 0 ||
				rotateVary[0] != 0 || rotateVary[1] != 0 || rotateVary[2] != 0) {
			ArgScriptCommand cRotate = new ArgScriptCommand("rotate", Float.toString(rotate[0]), Float.toString(rotate[1]), Float.toString(rotate[2]));
			
			if (rotateVary[0] != 0 || rotateVary[1] != 0 || rotateVary[2] != 0) {
				cRotate.putOption(new ArgScriptOption("vary", Float.toString(rotateVary[0]), Float.toString(rotateVary[1]), Float.toString(rotateVary[2])));
			}
			
			block.putCommand(cRotate);
		}
		
		if (waterLevelIsZero) block.putFlag("waterLevelIsZero");
		
		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline light effect is not supported.");
	}

	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			// ENUM_DRAWMODE
			"add", "biased", "substract", "level", "levelRange", "absoluteMax", "absoluteMin",
			// ENUM_FILTER_TYPE
			"global", "local",
			// ENUM_FILTER
			"smooth", "median", "lowpass", "lowpass1", "lowpass2", "lowpass3", "lowpass4", "lowpass5",
			"highpass", "highpass1", "highpass2", "highpass3", "highpass4",
			"edge", "edge1", "edge2", "edge3",
			"add", "level", "susbtract", "fractal", "winderosion", "watererosion",
			// ENUM_FILTER_MODE
			"replace", "add", "average",
			// ENUM_COND
			"less", "greater", "between"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"cap", "numTiles", "numSteps", "numTextureStrips", "numSkip",
			"vary", "draw", "offset", "gaussian", "falloff", "global", "cube", "waterLevelIsZero"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"ribbon", "intensity", "size", "spacing", "life", "rate", "texture",
			"filter", "cond", "gradientCond", "rotate"
		};
	}
}
