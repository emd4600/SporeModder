package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class RibbonEffect extends EffectComponent {
	
	public static final int TYPE = 0x0E;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "ribbon";
	
	// WARNING: These are based in particle effects. They might not be the same/exist in ribbon effects!
	// also used in 'material'
	private static final int FLAG_TEXTURE = ~0x200000;
	private static final int FLAG_ACCEPTCOMPOSITE = 0x400000;
	private static final int FLAG_FORCEMAP = 0x2000;
	private static final int FLAG_MAP_ADVECT = 0x8000;
	private static final int FLAG_MAP_FORCE = 0x10000;
	private static final int FLAG_KILLOUTSIDEMAP = 0x20000;
	
	private int flags;
	
	private final float[] lifeTime = new float[2];
	
	private float[] offset = new float[0];
	private float[] width = new float[0];
	private float taper;
	private float fade;
	private float alphaDecay;
	
	private EffectColor[] color = new EffectColor[] { EffectColor.WHITE };
	private float[] alpha = new float[] { 1.0f };
	private EffectColor[] lengthColor = new EffectColor[] { EffectColor.WHITE };
	private float[] lengthAlpha = new float[] { 1.0f };
	private EffectColor[] edgeColor = new EffectColor[] { EffectColor.WHITE };
	private float[] edgeAlpha = new float[] { 1.0f };
	private float[] startEdgeAlpha = new float[] { 1.0f };
	private float[] endEdgeAlpha = new float[] { 1.0f };
	
	private int segmentCount;
	private float segmentLength;
	
	private final TextureSlot texture = new TextureSlot();
	
	private int tileUV = -1; //0xFFFFFFFF
	private float slipCurveSpeed = -999f; //?
	private float slipUVSpeed = -1f; // ?
	
	private final float[] directionForcesSum = new float[3];
	private float windStrength;
	private float gravityStrength;
	
	//TODO it seems Spore used mapAdvect too?
	private final ResourceID mapEmitColor = new ResourceID();
	private final ResourceID mapForce = new ResourceID();
	private float mapRepulseStrength;

	public RibbonEffect(RibbonEffect effect) {
		super(effect);
		
		flags = effect.flags;
		lifeTime[0] = effect.lifeTime[0];
		lifeTime[1] = effect.lifeTime[1];
		
		offset = EffectComponent.copyArray(effect.offset);
		width = EffectComponent.copyArray(effect.width);
		taper = effect.taper;
		fade = effect.fade;
		alphaDecay = effect.alphaDecay;
		
		color = EffectComponent.copyArray(effect.color);
		alpha = EffectComponent.copyArray(effect.alpha);
		lengthColor = EffectComponent.copyArray(effect.lengthColor);
		lengthAlpha = EffectComponent.copyArray(effect.lengthAlpha);
		edgeColor = EffectComponent.copyArray(effect.edgeColor);
		edgeAlpha = EffectComponent.copyArray(effect.edgeAlpha);
		startEdgeAlpha = EffectComponent.copyArray(effect.startEdgeAlpha);
		endEdgeAlpha = EffectComponent.copyArray(effect.endEdgeAlpha);
		
		segmentCount = effect.segmentCount;
		segmentLength = effect.segmentLength;
		
		texture.copy(effect.texture);
		
		tileUV = effect.tileUV;
		slipCurveSpeed = effect.slipCurveSpeed;
		slipUVSpeed = effect.slipUVSpeed;
		
		directionForcesSum[0] = effect.directionForcesSum[0];
		directionForcesSum[1] = effect.directionForcesSum[1];
		directionForcesSum[2] = effect.directionForcesSum[2];
		windStrength = effect.windStrength;
		gravityStrength = effect.gravityStrength;
		
		mapEmitColor.copy(effect.mapEmitColor);
		mapForce.copy(effect.mapForce);
		mapRepulseStrength = effect.mapRepulseStrength;
	}

	public RibbonEffect(int type, int version) {
		super(type, version);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		
		lifeTime[0] = in.readLEFloat();
		lifeTime[1] = in.readLEFloat();
		
		offset = new float[in.readInt()];
		for (int i = 0; i < offset.length; i++) offset[i] = in.readFloat();
		
		width = new float[in.readInt()];
		for (int i = 0; i < width.length; i++) width[i] = in.readFloat();
		
		taper = in.readFloat();
		fade = in.readFloat();
		alphaDecay = in.readFloat();
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		alpha = new float[in.readInt()];
		for (int i = 0; i < alpha.length; i++) alpha[i] = in.readFloat();
		
		lengthColor = new EffectColor[in.readInt()];
		for (int i = 0; i < lengthColor.length; i++) {
			lengthColor[i] = new EffectColor();
			lengthColor[i].readLE(in);
		}
		
		lengthAlpha = new float[in.readInt()];
		for (int i = 0; i < lengthAlpha.length; i++) lengthAlpha[i] = in.readFloat();
		
		edgeColor = new EffectColor[in.readInt()];
		for (int i = 0; i < edgeColor.length; i++) {
			edgeColor[i] = new EffectColor();
			edgeColor[i].readLE(in);
		}
		
		edgeAlpha = new float[in.readInt()];
		for (int i = 0; i < edgeAlpha.length; i++) edgeAlpha[i] = in.readFloat();
		
		startEdgeAlpha = new float[in.readInt()];
		for (int i = 0; i < startEdgeAlpha.length; i++) startEdgeAlpha[i] = in.readFloat();
		
		endEdgeAlpha = new float[in.readInt()];
		for (int i = 0; i < endEdgeAlpha.length; i++) endEdgeAlpha[i] = in.readFloat();
		
		segmentCount = in.readInt();
		segmentLength = in.readFloat();
		
		texture.read(in);
		
		tileUV = in.readInt(); //0xFFFFFFFF
		slipCurveSpeed = in.readFloat();
		slipUVSpeed = in.readFloat();
		
		directionForcesSum[0] = in.readLEFloat();
		directionForcesSum[1] = in.readLEFloat();
		directionForcesSum[2] = in.readLEFloat();
		windStrength = in.readFloat();
		gravityStrength = in.readFloat();
		
		
		mapEmitColor.read(in);
		mapForce.read(in);
		mapRepulseStrength = in.readFloat();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		
		out.writeLEFloat(lifeTime[0]);
		out.writeLEFloat(lifeTime[1]);
		
		out.writeInt(offset.length);
		for (float f : offset) out.writeFloat(f);
		
		out.writeInt(width.length);
		for (float f : width) out.writeFloat(f);
		
		out.writeFloat(taper);
		out.writeFloat(fade);
		out.writeFloat(alphaDecay);
		
		out.writeInt(color.length);
		for (EffectColor f : color) f.writeLE(out);
		
		out.writeInt(alpha.length);
		for (float f : alpha) out.writeFloat(f);
		
		out.writeInt(lengthColor.length);
		for (EffectColor f : lengthColor) f.writeLE(out);
		
		out.writeInt(lengthAlpha.length);
		for (float f : lengthAlpha) out.writeFloat(f);
		
		out.writeInt(edgeColor.length);
		for (EffectColor f : edgeColor) f.writeLE(out);
		
		out.writeInt(edgeAlpha.length);
		for (float f : edgeAlpha) out.writeFloat(f);
		out.writeInt(startEdgeAlpha.length);
		for (float f : startEdgeAlpha) out.writeFloat(f);
		out.writeInt(endEdgeAlpha.length);
		for (float f : endEdgeAlpha) out.writeFloat(f);
		
		out.writeInt(segmentCount);
		out.writeFloat(segmentLength);
		
		texture.write(out);
		
		out.writeInt(tileUV);
		out.writeFloat(slipCurveSpeed);
		out.writeFloat(slipUVSpeed);
		
		out.writeLEFloat(directionForcesSum[0]);
		out.writeLEFloat(directionForcesSum[1]);
		out.writeLEFloat(directionForcesSum[2]);
		out.writeFloat(windStrength);
		out.writeFloat(gravityStrength);
		
		mapEmitColor.write(out);
		mapForce.write(out);
		out.writeFloat(mapRepulseStrength);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptCommand c;
		
		if ((c = block.getCommand("color")) != null) color = ArgScript.stringsToColors(c.getArguments());
		if ((c = block.getCommand("alpha")) != null) alpha = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("width")) != null) width = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("offset")) != null) offset = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("taper")) != null) taper = Float.parseFloat(c.getSingleArgument());
		if ((c = block.getCommand("fade")) != null) fade = Float.parseFloat(c.getSingleArgument());
		
		if ((c = block.getCommand("sustain")) != null) lifeTime[1] = Float.parseFloat(c.getSingleArgument()) + lifeTime[0];
		if ((c = block.getCommand("life")) != null) {
			List<String> args = c.getArguments(1, 2);
			lifeTime[0] = Float.parseFloat(args.get(0));
			lifeTime[1] = args.size() > 1 ? Float.parseFloat(args.get(1)) : lifeTime[0]; 
		}
		
		if ((c = block.getCommand("lengthColor")) != null) lengthColor = ArgScript.stringsToColors(c.getArguments());
		if ((c = block.getCommand("edgeColor")) != null) edgeColor = ArgScript.stringsToColors(c.getArguments());
		if ((c = block.getCommand("lengthAlpha")) != null) lengthAlpha = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("edgeAlpha")) != null) edgeAlpha = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("alphaDecay")) != null) alphaDecay = Float.parseFloat(c.getSingleArgument());
		if ((c = block.getCommand("startAlpha")) != null) startEdgeAlpha = ArgScript.stringsToFloats(c.getArguments());
		if ((c = block.getCommand("endAlpha")) != null) endEdgeAlpha = ArgScript.stringsToFloats(c.getArguments());
		
		if ((c = block.getCommand("segments")) != null) segmentCount = Integer.parseInt(c.getSingleArgument());
		if ((c = block.getCommand("segmentLength")) != null) segmentLength = Float.parseFloat(c.getSingleArgument());
		//TODO static ?
		
		if ((c = block.getCommand("tileUV")) != null) tileUV = Integer.parseInt(c.getSingleArgument());
		if ((c = block.getCommand("slipCurve")) != null) slipCurveSpeed = Float.parseFloat(c.getSingleArgument());
		if ((c = block.getCommand("slipUV")) != null) slipUVSpeed = Float.parseFloat(c.getSingleArgument());
		
		if ((c = block.getCommand("force")) != null) {
			if (c.hasFlag("reset")) {
				directionForcesSum[0] = 0;
				directionForcesSum[1] = 0;
				directionForcesSum[2] = 0;
			}
			String arg;
			List<String> args;
			
			if ((arg = c.getOptionArg("gravity")) != null) {
				directionForcesSum[2] -= Float.parseFloat(arg);
			}
			if ((args = c.getOptionArgs("wind", 1, 2)) != null) {
				// make it a unit vector
				float[] vec = ArgScript.parseFloatList(args.get(0), 3);
				float invMod = (float) (1 / Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]));
				if (args.size() > 1) {
					invMod *= Float.parseFloat(args.get(1));
				}
				directionForcesSum[0] = vec[0] * invMod;
				directionForcesSum[1] = vec[1] * invMod;
				directionForcesSum[2] = vec[2] * invMod;
			}
			if ((arg = c.getOptionArg("worldWind")) != null) {
				windStrength = Float.parseFloat(arg);
			}
			if ((arg = c.getOptionArg("worldGravity")) != null) {
				gravityStrength -= Float.parseFloat(arg);
			}
			// particles also use 'bomb' (radialForce), 'drag' and 'attractors', but ribbons don't
		} 
		if ((c = block.getCommand("material")) != null) {
			texture.parse(c);
			texture.setDrawMode(TextureSlot.DRAWMODE_NONE);
			flags &= FLAG_TEXTURE;
		}
		if ((c = block.getCommand("texture")) != null) {
			texture.parse(c);
			flags &= FLAG_TEXTURE;
			
			if (c.hasFlag("acceptComposite")) flags |= FLAG_ACCEPTCOMPOSITE;
			else flags &= ~FLAG_ACCEPTCOMPOSITE;
		}
		if ((c = block.getCommand("mapEmitColor")) != null) {
			String arg = c.getSingleArgument();
			if (arg.equals("terrain")) {
				mapEmitColor.setGroupID(0);
				mapEmitColor.setNameID(0);
			}
			else if (arg.equals("water")) {
				mapEmitColor.setGroupID(1);
				mapEmitColor.setNameID(0);
			}
			else {
				mapEmitColor.parse(arg);
			}
		}
		if ((c = block.getCommand("mapAdvect")) != null) {
			String arg = c.getSingleArgument();
			if (arg.equals("terrain")) {
				mapEmitColor.setGroupID(0);
				mapEmitColor.setNameID(0);
			}
			else if (arg.equals("water")) {
				mapEmitColor.setGroupID(1);
				mapEmitColor.setNameID(0);
			}
			else {
				mapForce.parse(arg);
			}
			
			flags |= FLAG_FORCEMAP;
			flags |= FLAG_MAP_ADVECT;
			
			if ((arg = c.getOptionArg("strength")) != null) {
				mapRepulseStrength = Float.parseFloat(arg);
			} else {
				mapRepulseStrength = 1;
			}
			
			if (c.hasFlag("killOutsideMap")) {
				flags |= FLAG_KILLOUTSIDEMAP;
			}
		}
		if ((c = block.getCommand("mapForce")) != null) {
			String arg = c.getSingleArgument();
			if (arg.equals("terrain")) {
				mapEmitColor.setGroupID(0);
				mapEmitColor.setNameID(0);
			}
			else if (arg.equals("water")) {
				mapEmitColor.setGroupID(1);
				mapEmitColor.setNameID(0);
			}
			else {
				mapForce.parse(arg);
			}
			
			flags |= FLAG_FORCEMAP;
			flags |= FLAG_MAP_FORCE;
			
			if ((arg = c.getOptionArg("strength")) != null) {
				mapRepulseStrength = Float.parseFloat(arg);
			} else {
				mapRepulseStrength = 1;
			}
			
			if (c.hasFlag("killOutsideMap")) {
				flags |= FLAG_KILLOUTSIDEMAP;
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		int bigflag = FLAG_FORCEMAP | FLAG_MAP_ADVECT | FLAG_MAP_FORCE | FLAG_ACCEPTCOMPOSITE | FLAG_KILLOUTSIDEMAP;
		if ((bigflag & flags) != bigflag) block.putOption(new ArgScriptOption("flags", Hasher.hashToHex(flags, "0x")));
		
		if (ArgScript.isWorth(color)) block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		if (ArgScript.isWorth(alpha)) block.putCommand(new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha)));
		if (ArgScript.isWorth(width)) block.putCommand(new ArgScriptCommand("width", ArgScript.floatsToStrings(width)));
		if (ArgScript.isWorth(offset)) block.putCommand(new ArgScriptCommand("offset", ArgScript.floatsToStrings(offset)));
		if (taper != 0) block.putCommand(new ArgScriptCommand("taper", Float.toString(taper)));
		if (fade != 0) block.putCommand(new ArgScriptCommand("fade", Float.toString(fade)));
		//TODO rigid ?
		block.putCommand(new ArgScriptCommand("life", Float.toString(lifeTime[0]), Float.toString(lifeTime[1])));
		if (ArgScript.isWorth(lengthColor)) block.putCommand(new ArgScriptCommand("lengthColor", ArgScript.colorsToStrings(lengthColor)));
		if (ArgScript.isWorth(edgeColor)) block.putCommand(new ArgScriptCommand("edgeColor", ArgScript.colorsToStrings(edgeColor)));
		if (ArgScript.isWorth(lengthAlpha)) block.putCommand(new ArgScriptCommand("lengthAlpha", ArgScript.floatsToStrings(lengthAlpha)));
		if (ArgScript.isWorth(edgeAlpha)) block.putCommand(new ArgScriptCommand("edgeAlpha", ArgScript.floatsToStrings(edgeAlpha)));
		if (alphaDecay != 0) block.putCommand(new ArgScriptCommand("alphaDecay", Float.toString(alphaDecay)));
		if (ArgScript.isWorth(startEdgeAlpha)) block.putCommand(new ArgScriptCommand("startAlpha", ArgScript.floatsToStrings(startEdgeAlpha)));
		if (ArgScript.isWorth(endEdgeAlpha)) block.putCommand(new ArgScriptCommand("endAlpha", ArgScript.floatsToStrings(endEdgeAlpha)));
		
		if (segmentCount != 0) block.putCommand(new ArgScriptCommand("segments", Integer.toString(segmentCount)));
		if (segmentLength != 0) block.putCommand(new ArgScriptCommand("segmentLength", Float.toString(segmentLength)));
		//TODO static ?
		if (texture != null && !texture.isDefault()) {
			if (texture.getDrawMode() == TextureSlot.DRAWMODE_NONE) {
				block.putCommand(texture.toCommand("material"));
			}
			else {
				block.putCommand(texture.toCommand("texture"));
			}
		}
		//TODO face?
		if (tileUV != -1) block.putCommand(new ArgScriptCommand("tileUV", Integer.toString(tileUV)));
		if (slipCurveSpeed != -999f) block.putCommand(new ArgScriptCommand("slipCurve", Float.toString(slipCurveSpeed)));
		if (slipUVSpeed != 0) block.putCommand(new ArgScriptCommand("slipUV", Float.toString(slipUVSpeed)));
		//TODO animUV ?
		
		if (mapEmitColor != null && !mapEmitColor.isDefault()) block.putCommand(new ArgScriptCommand("mapEmitColor", mapEmitColor.toString()));
		
		if (directionForcesSum[0] != 0 || directionForcesSum[1] != 0 || directionForcesSum[0] != 0 || windStrength != 0 || gravityStrength != 0) {
			ArgScriptCommand c = new ArgScriptCommand("force");
			
			if (directionForcesSum[0] == 0 && directionForcesSum[1] == 0 && directionForcesSum[2] != 0) {
				c.putOption(new ArgScriptOption("gravity", Float.toString(-directionForcesSum[2])));
			}
			else {
				c.putOption(new ArgScriptOption("wind", ArgScript.createFloatList(directionForcesSum)));
			}
			
			if (windStrength != 0) c.putOption(new ArgScriptOption("worldWind", Float.toString(windStrength)));
			if (gravityStrength != 0) c.putOption(new ArgScriptOption("worldGravity", Float.toString(gravityStrength)));
		}
		if (mapForce != null && !mapForce.isDefault()) {
			ArgScriptCommand c;
			if ((flags & FLAG_MAP_ADVECT) == FLAG_MAP_ADVECT) {
				c = new ArgScriptCommand("mapAdvect", mapForce.toString());
			}
			else {
				c = new ArgScriptCommand("mapForce", mapForce.toString());
			}
			
			if (mapRepulseStrength != 1) {
				c.putOption(new ArgScriptOption("strength", Float.toString(mapRepulseStrength)));
			}
			
			if ((flags & FLAG_KILLOUTSIDEMAP) == FLAG_KILLOUTSIDEMAP) {
				c.putFlag("killOutsideMap");
			}
			
			block.putCommand(c);
		}
		
		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline ribbon effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		if (texture.getDrawMode() == TextureSlot.DRAWMODE_NONE) {
			return new Effect[] {parent.getResource(MapResource.MASKED_TYPE, mapEmitColor), parent.getResource(MapResource.MASKED_TYPE, mapForce), parent.getResource(MaterialResource.MASKED_TYPE, texture.getResource())};
		}
		else {
			return new Effect[] {parent.getResource(MapResource.MASKED_TYPE, mapEmitColor), parent.getResource(MapResource.MASKED_TYPE, mapForce)};
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
		return new String[] {
			"strength", "killOutsideMap", "acceptComposite",
			"gravity", "wind", "worldWind", "worldGravity"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"color", "alpha", "width", "offset", "taper", "fade",
			"sustain", "life", "lengthColor", "edgeColor", "lengthAlpha", "edgeAlpha",
			"alphaDecay", "startAlpha", "endAlpha", "segments", "segmentLength", 
			"tileUV", "slipCurve", "slipUV", "force", "texture", "material",
			"mapEmitColor", "mapAdvect", "mapForce"
		};
	}
}
