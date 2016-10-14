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
import sporemodder.files.formats.effects.ScreenFilter.TemporaryFilterBuffer;
import sporemodder.utilities.Hasher;

public class ScreenEffect extends EffectComponent {
	
	public static final int TYPE = 0x09;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "screen";
	
	private static final int FLAG_LOOP = 1;
	
	private static final int FLAG_MASK = FLAG_LOOP;
	
	private static final ArgScriptEnum ENUM_MODE = new ArgScriptEnum(new String[] {
		"additive", "blend", "tint", "brighten", "skybox", "background", "user1", "user2", "user3", "user4", "filterChain"
	}, new int[] {
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
	}); 
	
	private int mode;  // byte
	private int flags;  // & 1F, flags?
	private EffectColor[] color = { EffectColor.WHITE };
	private float[] strength = { 1.0f };
	private float[] distance = new float[0];
	private float lifeTime = 2.0f;
	private float delay;
	private float falloff;
	private float distanceBase;
	private final ResourceID texture = new ResourceID();
	
	private int field_68;  // short
	
	private List<ScreenFilter> filters = new ArrayList<ScreenFilter>();
	private List<TemporaryFilterBuffer> filterBuffers = new ArrayList<TemporaryFilterBuffer>();
	private List<Float> paramsFloat = new ArrayList<Float>();
	private List<float[]> paramsVector3 = new ArrayList<float[]>();
	private List<float[]> paramsVector2 = new ArrayList<float[]>();
	private List<ResourceID> paramsResource = new ArrayList<ResourceID>();

	public ScreenEffect(int type, int version) {
		super(type, version);
	}

	public ScreenEffect(ScreenEffect effect) {
		super(effect);
		
		mode = effect.mode;
		flags = effect.flags;
		color = EffectComponent.copyArray(effect.color);
		strength = EffectComponent.copyArray(effect.strength);
		distance = EffectComponent.copyArray(effect.distance);
		lifeTime = effect.lifeTime;
		delay = effect.delay;
		falloff = effect.falloff;
		distanceBase = effect.distanceBase;
		texture.copy(effect.texture);
		field_68 = effect.field_68;
		
		// don't need to copy all these. If the user uses 'filterChain' again, the lists will be cleared
		filters = new ArrayList<ScreenFilter>(effect.filters);
		filterBuffers = new ArrayList<TemporaryFilterBuffer>(effect.filterBuffers);
		paramsFloat = new ArrayList<Float>(effect.paramsFloat);
		paramsVector3 = new ArrayList<float[]>(effect.paramsVector3);
		paramsVector2 = new ArrayList<float[]>(effect.paramsVector2);
		paramsResource = new ArrayList<ResourceID>(effect.paramsResource);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		mode = in.readByte();
		flags = in.readInt();
		
		color = new EffectColor[in.readInt()];
		for (int i = 0; i < color.length; i++) {
			color[i] = new EffectColor();
			color[i].readLE(in);
		}
		
		strength = new float[in.readInt()];
		in.readFloats(strength);
		
		distance = new float[in.readInt()];
		in.readFloats(distance);
		
		lifeTime = in.readFloat();
		delay = in.readFloat();
		falloff = in.readFloat();
		distanceBase = in.readFloat();
		texture.read(in);
		
		field_68 = in.readShort();
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			int type = in.readByte();
			ScreenFilter obj = ScreenFilter.getFilter(type);
			obj.read(in);
			filters.add(obj);
		}
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			TemporaryFilterBuffer obj = new TemporaryFilterBuffer("buffer" + i);
			obj.read(in);
			filterBuffers.add(obj);
		}
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			paramsFloat.add(in.readFloat());
		}
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			float[] arr = new float[3];
			in.readLEFloats(arr);
			paramsVector3.add(arr);
		}
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			float[] arr = new float[2];
			in.readLEFloats(arr);
			paramsVector2.add(arr);
		}
		count = in.readInt();
		for (int i = 0; i < count; i++) {
			paramsResource.add(new ResourceID(in));
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeByte(mode);
		out.writeInt(flags);
		
		out.writeInt(color.length);
		for (EffectColor c : color) {
			c.writeLE(out);
		}
		
		out.writeInt(strength.length);
		out.writeFloats(strength);
		
		out.writeInt(distance.length);
		out.writeFloats(distance);
		
		out.writeFloat(lifeTime);
		out.writeFloat(delay);
		out.writeFloat(falloff);
		out.writeFloat(distanceBase);
		texture.write(out);
		
		out.writeShort(field_68);
		
		out.writeInt(filters.size());
		for (ScreenFilter obj : filters) {
			obj.write(out);
		}
		out.writeInt(filterBuffers.size());
		for (TemporaryFilterBuffer obj : filterBuffers) {
			obj.write(out);
		}
		out.writeInt(paramsFloat.size());
		for (float obj : paramsFloat) {
			out.writeFloat(obj);
		}
		out.writeInt(paramsVector3.size());
		for (float[] obj : paramsVector3) {
			out.writeLEFloats(obj);
		}
		out.writeInt(paramsVector2.size());
		for (float[] obj : paramsVector2) {
			out.writeLEFloats(obj);
		}
		out.writeInt(paramsResource.size());
		for (ResourceID obj : paramsResource) {
			obj.write(out);
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		ArgScriptCommand c = null;
		if ((c = block.getCommand("flags")) != null) 
		{
			flags = Hasher.decodeInt(c.getSingleArgument()) & ~FLAG_MASK;
		}
		if ((c = block.getCommand("mode")) != null) 
		{
			mode = ENUM_MODE.getValue(c.getSingleArgument());
		}
		if ((c = block.getCommand("color")) != null) 
		{
			color = ArgScript.stringsToColors(c.getArguments());
		}
		if ((c = block.getCommand("strength")) != null) 
		{
			strength = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("distance")) != null) 
		{
			distance = ArgScript.stringsToFloats(c.getArguments());
		}
		if ((c = block.getCommand("length")) != null) 
		{
			lifeTime = Float.parseFloat(c.getSingleArgument());
			if (c.hasFlag("loop")) flags |= FLAG_LOOP;
		}
		if ((c = block.getCommand("delay")) != null) 
		{
			delay = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("falloff")) != null) 
		{
			falloff = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("distanceBase")) != null) 
		{
			distanceBase = Float.parseFloat(c.getSingleArgument());
		}
		if ((c = block.getCommand("texture")) != null) 
		{
			texture.parse(c.getSingleArgument());
		}
		if ((c = block.getCommand("field_68")) != null) 
		{
			field_68 = Hasher.decodeShort(c.getSingleArgument());
		}
		
//		Collection<ArgScriptCommand> commands = block.getAllCommands();
//		for (ArgScriptCommand command : commands) {
//			String keyword = command.getKeyword();
//			if (keyword.equals("filter")) {
//				ScreenFilter obj = new ScreenFilter();
//				obj.parse(command, this);
//				filters.add(obj);
//			}
//			else if (keyword.equals("temporaryBuffer")) {
//				TemporaryFilterBuffer obj = new TemporaryFilterBuffer();
//				obj.parse(command);
//				filterBuffers.add(obj);
//			}
//			else if (keyword.equals("paramFloat")) {
//				paramsFloat.add(Float.parseFloat(command.getSingleArgument()));
//			}
//			else if (keyword.equals("paramVector3")) {
//				paramsVector3.add(ArgScript.parseFloatList(command.getSingleArgument(), 3));
//			}
//			else if (keyword.equals("paramVector2")) {
//				paramsVector2.add(ArgScript.parseFloatList(command.getSingleArgument(), 2));
//			}
//			else if (keyword.equals("paramResource")) {
//				paramsResource.add(new ResourceID(command.getSingleArgument()));
//			}
//		}
		
		ArgScriptBlock b = block.getBlock("filterChain");
		if (b != null) {
			filters.clear();
			filterBuffers.clear();
			paramsFloat.clear();
			paramsVector3.clear();
			paramsVector2.clear();
			paramsResource.clear();
			Collection<ArgScriptCommand> filterCommands = b.getAllCommands();
			for (ArgScriptCommand filterCommand : filterCommands) {
				String keyword = filterCommand.getKeyword();
				if (keyword.equals(TemporaryFilterBuffer.KEYWORD)) {
					TemporaryFilterBuffer obj = new TemporaryFilterBuffer(null);
					obj.parse(filterCommand);
					filterBuffers.add(obj);
				} else {
					ScreenFilter obj = ScreenFilter.getFilter(filterCommand.getKeyword());
					obj.parse(filterCommand, this);
					filters.add(obj);
				}
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		block.putCommand(new ArgScriptCommand("mode", ENUM_MODE.getKey(mode)));
		if ((flags & ~FLAG_MASK) != 0) {
			block.putCommand(new ArgScriptCommand("flags", String.format("%5s", Integer.toBinaryString(flags & ~FLAG_MASK)).replace(' ', '0') + "b"));
		}
		block.putCommand(new ArgScriptCommand("color", ArgScript.colorsToStrings(color)));
		if (strength.length > 0) block.putCommand(new ArgScriptCommand("strength", ArgScript.floatsToStrings(strength)));
		if (distance.length > 0) block.putCommand(new ArgScriptCommand("distance", ArgScript.floatsToStrings(distance)));
		if (lifeTime != 2.0f || (flags & FLAG_LOOP) == FLAG_LOOP) {
			ArgScriptCommand c = new ArgScriptCommand("length", Float.toString(lifeTime));
			if ((flags & FLAG_LOOP) == FLAG_LOOP) {
				c.putFlag("loop");
			}
			block.putCommand(c);
		}
		if (delay != 0.0f) block.putCommand(new ArgScriptCommand("delay", Float.toString(delay)));
		if (falloff != 0.0f) block.putCommand(new ArgScriptCommand("falloff", Float.toString(falloff)));
		if (distanceBase != 0.0f) block.putCommand(new ArgScriptCommand("distanceBase", Float.toString(distanceBase)));
		if (!texture.isDefault()) block.putCommand(new ArgScriptCommand("texture", texture.toString()));
		if (field_68 != 0) block.putCommand(new ArgScriptCommand("field_68", Integer.toString(field_68)));
		
		if (filters.size() > 0) {
			ArgScriptBlock b = new ArgScriptBlock("filterChain");
			for (TemporaryFilterBuffer obj : filterBuffers) {
				b.putCommand(obj.toCommand());
			}
			for (ScreenFilter obj : filters) {
				b.putCommand(obj.toCommand(this));
			}
			block.putBlock(b);
		}
		
//		for (TemporaryFilterBuffer obj : filterBuffers) {
//			block.putCommand(obj.toCommand());
//		}
//		for (ScreenFilter obj : filters) {
//			block.putCommand(obj.toCommand(this));
//		}
//		for (float f : paramsFloat) {
//			block.putCommand(new ArgScriptCommand("paramFloat", Float.toString(f)));
//		}
//		for (float[] f : paramsVector3) {
//			block.putCommand(new ArgScriptCommand("paramVector3", ArgScript.createFloatList(f)));
//		}
//		for (float[] f : paramsVector2) {
//			block.putCommand(new ArgScriptCommand("paramVector2", ArgScript.createFloatList(f)));
//		}
//		for (ResourceID f : paramsResource) {
//			block.putCommand(new ArgScriptCommand("paramResource", f.toString()));
//		}
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		throw new UnsupportedOperationException("Inline screen effect is not supported.");
	}

	public List<ScreenFilter> getFilters() {
		return filters;
	}

	public List<TemporaryFilterBuffer> getFilterBuffers() {
		return filterBuffers;
	}

	public List<Float> getParamsFloat() {
		return paramsFloat;
	}

	public List<float[]> getParamsVector3() {
		return paramsVector3;
	}

	public List<float[]> getParamsVector2() {
		return paramsVector2;
	}

	public List<ResourceID> getParamsResource() {
		return paramsResource;
	}

	
	public ScreenFilter getFilter(int index) {
		return filters.get(index);
	}
	public TemporaryFilterBuffer getFilterBuffer(int index) {
		if (index < 0 || index >= filterBuffers.size()) {
			return null;
		}
		return filterBuffers.get(index);
	}
	public float getFloat(int index) {
		return paramsFloat.get(index);
	}
	public float[] getVector3(int index) {
		return paramsVector3.get(index);
	}
	public float[] getVector2(int index) {
		return paramsVector2.get(index);
	}
	public ResourceID getResource(int index) {
		return paramsResource.get(index);
	}
	
	public byte getFilterIndex(ScreenFilter obj) {
		int index = filters.indexOf(obj);
		if (index == -1) {
			filters.add(obj);
			return (byte) (filters.size() - 1);
		}
		return (byte) index;
	}
	public byte getFilterBufferIndex(TemporaryFilterBuffer obj) {
		int index = filterBuffers.indexOf(obj);
		if (index == -1) {
			filterBuffers.add(obj);
			return (byte) (filterBuffers.size() - 1);
		}
		return (byte) index;
	}
	public byte getFloatIndex(float obj) {
		int index = paramsFloat.indexOf(obj);
		if (index == -1) {
			paramsFloat.add(obj);
			return (byte) (paramsFloat.size() - 1);
		}
		return (byte) index;
	}
	public byte getVector2Index(float[] obj) {
		int index = paramsVector2.indexOf(obj);
		if (index == -1) {
			paramsVector2.add(obj);
			return (byte) (paramsVector2.size() - 1);
		}
		return (byte) index;
	}
	public byte getVector3Index(float[] obj) {
		int index = paramsVector3.indexOf(obj);
		if (index == -1) {
			paramsVector3.add(obj);
			return (byte) (paramsVector3.size() - 1);
		}
		return (byte) index;
	}
	public byte getResourceIndex(ResourceID obj) {
		int index = paramsResource.indexOf(obj);
		if (index == -1) {
			paramsResource.add(obj);
			return (byte) (paramsResource.size() - 1);
		}
		return (byte) index;
	}
	
	public TemporaryFilterBuffer getFilterBuffer(String name) {
		for (TemporaryFilterBuffer obj : filterBuffers) {
			if (obj.name.equals(name)) {
				return obj;
			}
		}
		return null;
	}
	
	public int getFilterBufferIndex(String name) {
		int ind = 0;
		for (TemporaryFilterBuffer obj : filterBuffers) {
			if (obj.name.equals(name)) {
				return ind;
			}
			ind++;
		}
		return -1;
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"additive", "blend", "tint", "brighten", "skybox", "background", "user1", "user2", "user3", "user4", "filterChain",
			"source", "dest", "particles1", "particles2", "particles3"
		};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"loop", "distorter", "offsetX", "offsetY", "strength", "transXY", "tileXY",
			"scale", "scaleX", "scaleY", "pointSource", "blend", "add", "multiply", "sourceAlpha", "sourceColor",
			"maskTexture", "param7", "offsetXY", "invertMask", "bias", "scale", "mono",
			"texture", "pointAdd", "sourceMul", "addMul", "param8", "color", "param2", "param3",
			"normalMap", "param4", "param5", "sourceMul", "upper", "lower", "materialID",
			"sampler0", "sampler1", "sampler2", "sampler3", "customParams0", "customParams1",
			"customParams2", "customParams3", "customParams4", "customParams5", "customParams6",
			"customParams7", "param13", "maxDistance", "param15", "param16", "param17",
			"length", "fadeIn", "fadeOut", "ratio", "size"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"mode", "flags", "color", "strength", "distance", "length", "delay", "falloff",
			"distanceBase", "texture", "field_68", "filterChain", "texture",
			"distort", "blur1d", "copy", "compress", "add", "colorize", "blurx", "blury",
			"edge", "edgex", "edgey", "extract", "multiply", "dilate", "contrast",
			"customMaterial", "strengthFader"
		};
	}
}
