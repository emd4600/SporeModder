package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class TextureSlot {
	private static final ArgScriptEnum ENUM_DRAWMODE = new ArgScriptEnum(new String[] {
			"decal", "decalInvertDepth", "decalIgnoreDepth", "depthDecal", "decalDepth", "additive", "additiveInvertDepth", "additiveIgnoreDepth",
			"modulate", "normalMap", "depthNormalMap", "normalMapDepth", "alphaTestDissolve", "user1", "user2", "user3", "user4"
		}, new int[] {
				0, 1, 2, 3, 3, 4, 5, 6, 7, 8, 9, 9, 0xA, 0xB, 0xC, 0xD, 0xE
		});
	// 0xF -> no draw mode?
	protected static final int DRAWMODE_NONE = 0xF;
	
//	DrawMode {
//		"decal"	= 0,
//		"decalInvertDepth"	= 1,
//		"decalIgnoreDepth"	= 2,
//		"depthDecal"	= 3,
//		"decalDepth"	= 3,
//		"additive"	= 4,
//		"additiveInvertDepth"	= 5,
//		"additiveIgnoreDepth"	= 6,
//		"modulate"	= 7,
//		"normalMap"	= 8,
//		"depthNormalMap"	= 9,
//		"normalMapDepth"	= 9,
//		"alphaTestDissolve"	= 0xA,
//		"user1"	= 0xB,
//		"user2"	= 0xC,
//		"user3"	= 0xD,
//		"user4"	= 0xE
//		// 0xF -> no draw mode? 
//	}
	
	private static final int DRAWFLAG_LIGHT = 0x1;
	private static final int DRAWFLAG_NO_FOG= 0x2;
	private static final int DRAWFLAG_SHADOW = 0x8;
	private static final int DRAWFLAG_NO_SHADOW = 0xF7;
	private static final int DRAWFLAG_NO_CULL = 0x10;
	private static final int DRAWFLAG_USER1 = 0x20;
	private static final int DRAWFLAG_USER2 = 0x40;
	private static final int DRAWFLAG_USER3 = 0x80;

	private final ResourceID resource = new ResourceID();  // ?
	private int format;  // byte
	private int drawMode;  // byte
	private int drawFlags = 0;  // byte
	private int buffer;  // byte
	private int layer;  // short
	private float sortOffset;
	private final ResourceID resource2 = new ResourceID();
	
	public TextureSlot() {}
	public TextureSlot(TextureSlot other) {
		copy(other);
	}
	
	public void copy(TextureSlot other) {
		resource.copy(other.resource);
		resource2.copy(other.resource2);
		
		format = other.format;
		drawMode = other.drawMode;
		drawFlags = other.drawFlags;
		buffer = other.buffer;
		layer = other.layer;
		sortOffset = other.sortOffset;
	}
	
	public boolean read(InputStreamAccessor in) throws IOException 
	{
		resource.read(in);
		format = in.readByte();
		drawMode = in.readByte();
		drawFlags = in.readByte();
		buffer = in.readByte();
		layer = in.readShort();
		sortOffset = in.readFloat();
		resource2.read(in);
		
		return true;
	}
	
	public boolean write(OutputStreamAccessor out) throws IOException 
	{
		if (resource == null || resource2 == null) {
			return false;
		}
		resource.write(out);
		out.writeByte(format);
		out.writeByte(drawMode);
		out.writeByte(drawFlags);
		out.writeByte(buffer);
		out.writeShort(layer);
		out.writeFloat(sortOffset);
		resource2.write(out);
		
		return true;
	}
	
	public ArgScriptCommand toCommand(String keyword) {
		ArgScriptCommand command = new ArgScriptCommand(keyword, resource.toString());
		
		if (drawMode != DRAWMODE_NONE) command.putOption(new ArgScriptOption("draw", ENUM_DRAWMODE.getKey(drawMode)));
		if (buffer != 0) command.putOption(new ArgScriptOption("buffer", Integer.toString(buffer)));
		if (layer != 0) command.putOption(new ArgScriptOption("layer", Integer.toString(layer)));
		if (sortOffset != 0) command.putOption(new ArgScriptOption("sortOffset", Float.toString(sortOffset)));
		
		if ((drawFlags & DRAWFLAG_LIGHT) == DRAWFLAG_LIGHT) command.putFlag("light");
		if ((drawFlags & DRAWFLAG_NO_FOG) == DRAWFLAG_NO_FOG) command.putFlag("noFog");
		if ((drawFlags & DRAWFLAG_SHADOW) == DRAWFLAG_SHADOW) command.putFlag("shadow");
		if ((drawFlags & DRAWFLAG_NO_CULL) == DRAWFLAG_NO_CULL) command.putFlag("noCull");
		if ((drawFlags & DRAWFLAG_USER1) == DRAWFLAG_USER1) command.putFlag("user1");
		if ((drawFlags & DRAWFLAG_USER2) == DRAWFLAG_USER2) command.putFlag("user2");
		if ((drawFlags & DRAWFLAG_USER3) == DRAWFLAG_USER3) command.putFlag("user3");
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		
		if (format != 0) command.putOption(new ArgScriptOption("format", "0x" + Integer.toHexString(format)));
		
		if (!resource2.isDefault()) command.putOption(new ArgScriptOption("resource2", resource2.toString()));
		
		return command;
	}
	
	/**
	 * Puts all the properties into the given command except resource and resource2.
	 * @param command
	 * @return
	 */
	public ArgScriptCommand toCommand(ArgScriptCommand command) {
		
		if (drawMode != DRAWMODE_NONE) command.putOption(new ArgScriptOption("draw", ENUM_DRAWMODE.getKey(drawMode)));
		if (buffer != 0) command.putOption(new ArgScriptOption("buffer", Integer.toString(buffer)));
		if (layer != 0) command.putOption(new ArgScriptOption("layer", Integer.toString(layer)));
		if (sortOffset != 0) command.putOption(new ArgScriptOption("sortOffset", Float.toString(sortOffset)));
		
		if ((drawFlags & DRAWFLAG_LIGHT) == DRAWFLAG_LIGHT) command.putFlag("light");
		if ((drawFlags & DRAWFLAG_NO_FOG) == DRAWFLAG_NO_FOG) command.putFlag("noFog");
		if ((drawFlags & DRAWFLAG_SHADOW) == DRAWFLAG_SHADOW) command.putFlag("shadow");
		if ((drawFlags & DRAWFLAG_NO_CULL) == DRAWFLAG_NO_CULL) command.putFlag("noCull");
		if ((drawFlags & DRAWFLAG_USER1) == DRAWFLAG_USER1) command.putFlag("user1");
		if ((drawFlags & DRAWFLAG_USER2) == DRAWFLAG_USER2) command.putFlag("user2");
		if ((drawFlags & DRAWFLAG_USER3) == DRAWFLAG_USER3) command.putFlag("user3");
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		
		if (format != 0) command.putOption(new ArgScriptOption("format", "0x" + Integer.toHexString(format)));
		
		return command;
	}
	
	public void write(BufferedWriter out, String indent, String keyword) throws IOException {
		out.write(indent + keyword + " " + resource.toString());
		
		if (drawMode != DRAWMODE_NONE) out.write(" -draw " + ENUM_DRAWMODE.getKey(drawMode));
		if (buffer != 0) out.write(" -buffer " + Integer.toString(buffer));
		if (layer != 0) out.write(" -layer " + Integer.toString(layer));
		if (sortOffset != 0) out.write(" -sortOffset " + Float.toString(sortOffset));
		
		if ((drawFlags & DRAWFLAG_LIGHT) == DRAWFLAG_LIGHT) out.write(" -light");
		if ((drawFlags & DRAWFLAG_NO_FOG) == DRAWFLAG_NO_FOG) out.write(" -noFog");
		if ((drawFlags & DRAWFLAG_SHADOW) == DRAWFLAG_SHADOW) out.write(" -shadow");
		if ((drawFlags & DRAWFLAG_NO_CULL) == DRAWFLAG_NO_CULL) out.write(" -noCull");
		if ((drawFlags & DRAWFLAG_USER1) == DRAWFLAG_USER1) out.write(" -user1");
		if ((drawFlags & DRAWFLAG_USER2) == DRAWFLAG_USER2) out.write(" -user2");
		if ((drawFlags & DRAWFLAG_USER3) == DRAWFLAG_USER3) out.write(" -user3");
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		
		if (format != 0) out.write(" -format 0x" + Integer.toHexString(format));
		
		if (!resource2.isDefault()) out.write(" -resource2 " + resource2.toString());
	}
	
	public boolean parse(ArgScriptCommand command) throws IOException, ArgScriptException {
		resource.parse(command.getSingleArgument());
		
		ArgScriptOption oDrawMode = command.getArgOption("draw");
		if (oDrawMode != null) drawMode = ENUM_DRAWMODE.getValue(oDrawMode.getSingleArgument());
		
		ArgScriptOption oBuffer = command.getArgOption("buffer");
		if (oBuffer != null) buffer = Integer.parseInt(oBuffer.getSingleArgument());
		
		ArgScriptOption oLayer = command.getArgOption("layer");
		if (oLayer != null) layer = Integer.parseInt(oLayer.getSingleArgument());
		
		ArgScriptOption oSortOffset = command.getArgOption("sortOffset");
		if (oSortOffset != null) sortOffset = Float.parseFloat(oSortOffset.getSingleArgument());
		
		if (command.hasFlag("light")) drawFlags |= DRAWFLAG_LIGHT;
		if (command.hasFlag("noFog")) drawFlags |= DRAWFLAG_NO_FOG;
		if (command.hasFlag("shadow")) drawFlags |= DRAWFLAG_SHADOW;
		if (command.hasFlag("noShadow")) drawFlags &= DRAWFLAG_NO_SHADOW;
		if (command.hasFlag("noCull")) drawFlags |= DRAWFLAG_NO_CULL;
		if (command.hasFlag("user1")) drawFlags |= DRAWFLAG_USER1;
		if (command.hasFlag("user2")) drawFlags |= DRAWFLAG_USER2;
		if (command.hasFlag("user3")) drawFlags |= DRAWFLAG_USER3;
		
		
		// Apparently, these aren't used by Spore. We'll leave them here anyways
		
		ArgScriptOption oFormat = command.getArgOption("format");
		if (oFormat != null) format = Integer.parseInt(oFormat.getSingleArgument());
		
		ArgScriptOption oResource = command.getArgOption("resource2");
		if (oResource != null) resource2.parse(oResource.getSingleArgument());
		return true;
	}
	
	public boolean isDefault() {
		return resource2.isDefault() && resource.isDefault() && format == 0 && drawMode == 0 && drawFlags == 0 && buffer == 0 && layer == 0 && sortOffset == 0;
	}
	
	
	public ResourceID getResource() {
		return resource;
	}
	public int getFormat() {
		return format;
	}
	public int getDrawMode() {
		return drawMode;
	}
	public int getDrawFlags() {
		return drawFlags;
	}
	public int getBuffer() {
		return buffer;
	}
	public int getLayer() {
		return layer;
	}
	public float getSortOffset() {
		return sortOffset;
	}
	public ResourceID getResource2() {
		return resource2;
	}
	public void setResource(ResourceID resource) {
		this.resource.copy(resource);
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public void setDrawMode(int drawMode) {
		this.drawMode = drawMode;
	}
	public void setDrawFlags(int drawFlags) {
		this.drawFlags = drawFlags;
	}
	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}
	public void setSortOffset(float sortOffset) {
		this.sortOffset = sortOffset;
	}
	public void setResource2(ResourceID resource2) {
		this.resource2.copy(resource2);
	}
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
				"decal", "decalInvertDepth", "decalIgnoreDepth", "depthDecal", "decalDepth", "additive", "additiveInvertDepth", "additiveIgnoreDepth",
				"modulate", "normalMap", "depthNormalMap", "normalMapDepth", "alphaTestDissolve", "user1", "user2", "user3", "user4"};
	}
	
	public static String[] getBlockTags() {
		return new String[] {};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"draw", "buffer", "layer", "sortOffset", "light", "noFog", "shadow", "noShadow", "noCull", "user1", "user2", "user3"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {};
	}
}
