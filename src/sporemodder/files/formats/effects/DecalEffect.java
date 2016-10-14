package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class DecalEffect extends EffectComponent {
	
	public static final int TYPE = 0x03;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "decal";
	
	private int field_8;
	private byte field_C;
	private byte field_D;
	
	private float lifeTime;
	
	private float[] rotation = {0};
	private float[] size = {1};
	private float[] alpha = {1};
	private EffectColor[] color = new EffectColor[] { EffectColor.WHITE };
	private float[] aspectRatio = {1};
	
	private float rotationVary;
	private float sizeVary;
	private float alphaVary;
	
	private float textureRepeat = 1;
	private final float[] textureOffset = new float[2];
	
	private final ResourceID mapEmitColor = new ResourceID();
	
	private final TextureSlot texture = new TextureSlot();

	public DecalEffect(DecalEffect effect) {
		super(effect);

		field_8 = effect.field_8;
		field_C = effect.field_C;
		field_D = effect.field_D;
		
		lifeTime = effect.lifeTime;
		
		rotation = new float[effect.rotation.length];
		for (int i = 0; i < rotation.length; i++) rotation[i] = effect.rotation[i];
		
		size = new float[effect.size.length];
		for (int i = 0; i < size.length; i++) size[i] = effect.size[i];
		
		alpha = new float[effect.alpha.length];
		for (int i = 0; i < alpha.length; i++) alpha[i] = effect.alpha[i];
		
		color = new EffectColor[effect.rotation.length];
		for (int i = 0; i < color.length; i++) color[i] = new EffectColor(effect.color[i]);
		
		aspectRatio = new float[effect.aspectRatio.length];
		for (int i = 0; i < aspectRatio.length; i++) aspectRatio[i] = effect.aspectRatio[i];
		
		rotationVary = effect.rotationVary;
		sizeVary = effect.sizeVary;
		alphaVary = effect.alphaVary;
		
		textureRepeat = effect.textureRepeat;
		textureOffset[0] = effect.textureOffset[0];
		textureOffset[1] = effect.textureOffset[1];
		
		mapEmitColor.copy(effect.mapEmitColor);
		texture.copy(effect.texture);
	}

	public DecalEffect(int type, int version) {
		super(type, version);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		//TODO one of these is type (terrain, water, terrainAndWater, paint, user1 - user8) ??
		field_8 = in.readInt();  // look at planet.effdir!decal-55
		field_C = in.readByte();  // lots of effects have errors here
		field_D = in.readByte();  // look at planet.effdir!decal-204
		// decal-2, decal-3, decal-72, decal-73, decal-74 -> field_D 1 (water ?) 
		
		texture.read(in);
		
		lifeTime = in.readFloat();
		
		int rotateCount = in.readInt();
		rotation = new float[rotateCount];
		for (int i = 0; i < rotateCount; i++) {
			rotation[i] = in.readFloat();
		}
		
		int sizeCount = in.readInt();
		size = new float[sizeCount];
		for (int i = 0; i < sizeCount; i++) {
			size[i] = in.readFloat();
		}
		
		int alphaCount = in.readInt();
		alpha = new float[alphaCount];
		for (int i = 0; i < alphaCount; i++) {
			alpha[i] = in.readFloat();
		}
		
		int colorCount = in.readInt();
		color = new EffectColor[colorCount];
		for (int i = 0; i < colorCount; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		int arCount = in.readInt();
		aspectRatio = new float[arCount];
		for (int i = 0; i < arCount; i++) {
			aspectRatio[i] = in.readFloat();
		}
		
		alphaVary = in.readFloat();
		sizeVary = in.readFloat();
		rotationVary = in.readFloat();
		
		textureRepeat = in.readFloat();
		textureOffset[0] = in.readLEFloat();
		textureOffset[1] = in.readLEFloat();
		
		mapEmitColor.read(in);
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(0);
		out.writeByte(0);
		out.writeByte(0);
		texture.write(out);
		
		out.writeFloat(lifeTime);
		
		out.writeInt(rotation.length);
		for (float f : rotation) {
			out.writeFloat(f);
		}
		
		out.writeInt(size.length);
		for (float f : size) {
			out.writeFloat(f);
		}
		
		out.writeInt(alpha.length);
		for (float f : alpha) {
			out.writeFloat(f);
		}
		
		out.writeInt(color.length);
		for (EffectColor c : color) {
			c.writeLE(out);
		}
		
		out.writeInt(aspectRatio.length);
		for (float f : aspectRatio) {
			out.writeFloat(f);
		}
		
		out.writeFloat(alphaVary);
		out.writeFloat(sizeVary);
		out.writeFloat(rotationVary);
		
		out.writeFloat(textureRepeat);
		out.writeLEFloat(textureOffset[0]);
		out.writeLEFloat(textureOffset[1]);
		
		mapEmitColor.write(out);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		
		{ ArgScriptCommand c = block.getCommand("field_8"); if (c != null) field_8 = Hasher.decodeInt(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("field_C"); if (c != null) field_C = Hasher.decodeByte(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("field_D"); if (c != null) field_D = Hasher.decodeByte(c.getSingleArgument()); }
		
		{ 
			ArgScriptCommand c = block.getCommand("color"); 
			if (c != null) {
				color = ArgScript.stringsToColors(c.getArguments());
			}
		}
		{ 
			ArgScriptCommand c = block.getCommand("alpha"); 
			if (c != null) {
				alpha = ArgScript.stringsToFloats(c.getArguments());
				ArgScriptOption o = c.getOption("vary");
				if (o != null) alphaVary = Float.parseFloat(o.getSingleArgument());
			}
		}
		{ 
			ArgScriptCommand c = block.getCommand("size"); 
			if (c != null) {
				size = ArgScript.stringsToFloats(c.getArguments());
				ArgScriptOption o = c.getOption("vary");
				if (o != null) sizeVary = Float.parseFloat(o.getSingleArgument());
			}
		}
		{ 
			ArgScriptCommand c = block.getCommand("rotation"); 
			if (c != null) {
				rotation = ArgScript.stringsToFloats(c.getArguments());
				ArgScriptOption o = c.getOption("vary");
				if (o != null) rotationVary = Float.parseFloat(o.getSingleArgument());
			}
		}
		{ ArgScriptCommand c = block.getCommand("life"); if (c != null) lifeTime = Float.parseFloat(c.getSingleArgument()); }
		{ 
			ArgScriptCommand c = block.getCommand("texture"); 
			if (c != null) {
				texture.parse(c);
				
				ArgScriptOption oRepeat = c.getOption("repeat");
				if (oRepeat != null) textureRepeat = Float.parseFloat(oRepeat.getSingleArgument());
				
				ArgScriptOption oOffset = c.getOption("offset");
				if (oOffset != null) ArgScript.parseFloatList(oOffset.getSingleArgument(), textureOffset);
			}
		}
		{ ArgScriptCommand c = block.getCommand("mapEmitColor"); if (c != null) mapEmitColor.parse(c.getSingleArgument()); }

		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		
//		if (field_8 != 0) block.putCommand(new ArgScriptCommand("field_8", Hasher.hashToHex(field_8, "0x")));
//		if (field_C != 0) block.putCommand(new ArgScriptCommand("field_C", Integer.toString(field_C)));
//		if (field_D != 0) block.putCommand(new ArgScriptCommand("field_D", Integer.toString(field_D)));
//
//		if (color.length > 0) {
//			if (color.length > 1 || !color[0].isWhite()) {
//				block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
//			}
//		}
//		if (ArgScript.isWorth(alpha, alphaVary)) {
//			ArgScriptCommand c = new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha));
//			if (alphaVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(alphaVary)));
//			block.putCommand(c);
//		}
//		if (ArgScript.isWorth(size, sizeVary)) {
//			ArgScriptCommand c = new ArgScriptCommand("size", ArgScript.floatsToStrings(size));
//			if (sizeVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(sizeVary)));
//			block.putCommand(c);
//		}
//		if (ArgScript.isWorth(rotation, rotationVary, 0)) {
//			ArgScriptCommand c = new ArgScriptCommand("rotate", ArgScript.floatsToStrings(rotation));
//			if (rotationVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(rotationVary)));
//			block.putCommand(c);
//		}
//		
//		if (lifeTime != 0) block.putCommand(new ArgScriptCommand("life", Float.toString(lifeTime)));
//		
//		if (texture != null && !texture.isDefault()) {
//			ArgScriptCommand c = texture.toCommand("texture");
//			if (textureRepeat != 0) c.putOption(new ArgScriptOption("repeat", Float.toString(textureRepeat)));
//			if (textureOffset != null && textureOffset[0] != 0 && textureOffset[1] != 0) {
//				c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(textureOffset))); 
//			}
//			block.putCommand(c);
//		}
//		
//		if (mapEmitColor != null && !mapEmitColor.isDefault()) {
//			block.putCommand(new ArgScriptCommand("mapEmitColor", mapEmitColor.toString()));
//		}
		
		if (field_8 != 0) block.putCommand(new ArgScriptCommand("field_8", Hasher.hashToHex(field_8, "0x")));
		if (field_C != 0) block.putCommand(new ArgScriptCommand("field_C", Integer.toString(field_C)));
		if (field_D != 0) block.putCommand(new ArgScriptCommand("field_D", Integer.toString(field_D)));

		block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		{
			ArgScriptCommand c = new ArgScriptCommand("alpha", ArgScript.floatsToStrings(alpha));
			if (alphaVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(alphaVary)));
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("size", ArgScript.floatsToStrings(size));
			if (sizeVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(sizeVary)));
			block.putCommand(c);
		}
		{
			ArgScriptCommand c = new ArgScriptCommand("rotate", ArgScript.floatsToStrings(rotation));
			if (rotationVary != 0) c.putOption(new ArgScriptOption("vary", Float.toString(rotationVary)));
			block.putCommand(c);
		}
		
		block.putCommand(new ArgScriptCommand("life", Float.toString(lifeTime)));
		
		{
			ArgScriptCommand c = texture.toCommand("texture");
			if (textureRepeat != 0) c.putOption(new ArgScriptOption("repeat", Float.toString(textureRepeat)));
			if (textureOffset != null && textureOffset[0] != 0 && textureOffset[1] != 0) {
				c.putOption(new ArgScriptOption("offset", ArgScript.createFloatList(textureOffset))); 
			}
			block.putCommand(c);
		}
		
		if (!mapEmitColor.isDefault()) block.putCommand(new ArgScriptCommand("mapEmitColor", mapEmitColor.toString()));
		
		return true;
	}
	
	@Override
	public void write(BufferedWriter out, int level) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append('\t');
		}
		String indent = sb.toString();
		
		if (field_8 != 0) { out.write(indent + "field_8 " + Hasher.hashToHex(field_8, "0x")); out.newLine(); }
		if (field_C != 0) { out.write(indent + "field_C " + Integer.toString(field_C)); out.newLine(); }
		if (field_D != 0) { out.write(indent + "field_D " + Integer.toString(field_D)); out.newLine(); }
		
		if (color.length > 0) {
			if (color.length > 1 || !color[0].isWhite()) {
				out.write(indent + "color " + ArgScript.colorsToStrings(color)); out.newLine();
			}
		}
		if (ArgScript.isWorth(alpha, alphaVary)) {
			out.write(indent + "color " + ArgScript.colorsToStrings(color)); 
			if (alphaVary != 0) out.write(" -vary " + Float.toString(alphaVary));
			out.newLine();
		}
		if (ArgScript.isWorth(size, sizeVary)) {
			out.write(indent + "size " + ArgScript.floatsToStrings(size)); 
			if (sizeVary != 0) out.write(" -vary " + Float.toString(sizeVary));
			out.newLine();
		}
		if (ArgScript.isWorth(rotation, rotationVary, 0)) {
			out.write(indent + "rotate " + ArgScript.floatsToStrings(rotation)); 
			if (rotationVary != 0) out.write(" -vary " + Float.toString(rotationVary));
			out.newLine();
		}
		
		if (lifeTime != 0) { out.write(indent + "life " + Float.toString(lifeTime)); out.newLine(); } 
		
		if (texture != null && !texture.isDefault()) {
			texture.write(out, indent, "texture");
			if (textureRepeat != 0) {
				out.write(" -repeat " + Float.toString(textureRepeat));
			}
			if (textureOffset != null && textureOffset[0] != 0 && textureOffset[1] != 0) {
				out.write(" -offset " + ArgScript.createFloatList(textureOffset)); 
			}
		}
		
		if (mapEmitColor != null && !mapEmitColor.isDefault()) {
			out.write(indent + "mapEmitColor " + mapEmitColor.toString());
		}
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline decal effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		if (!mapEmitColor.isDefault()) {
			return new Effect[] {parent.getResource(MapResource.MASKED_TYPE, mapEmitColor)};
		} else {
			return null;
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
			"vary", "repeat", "offset"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"field_8", "field_C", "field_D", "rotate", "alpha", "size", "color", "life", "texture", "mapEmitColor"
		};
	}
}
