package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class ScreenFilter {
	
	protected int type;  // byte
	protected int destination;  // byte
	protected final ResourceID source = new ResourceID();
	protected byte[] parameters = new byte[0];  // bytes
	
	public ScreenFilter() {
		
	}
	public ScreenFilter(int type) {
		this.type = type;
	}
	
	public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
		ArgScriptCommand c = new ArgScriptCommand("filter", source.toString(), Integer.toString(destination));
		c.putOption(new ArgScriptOption("type", Integer.toString(type)));
		c.putOption(new ArgScriptOption("parameters", ArgScript.bytesToStrings(parameters)));
		
		return c;
	}
	
	public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
		List<String> args = c.getArguments(2);
		source.parse(args.get(0));
		destination = Integer.parseInt(args.get(1));
		
		String arg = null;
		if ((arg = c.getOptionArg("type")) != null) {
			type = Integer.parseInt(arg);
		}
		if ((args = c.getOptionArgs("parameters", 0, Integer.MAX_VALUE)) != null) {
			parameters = ArgScript.stringsToBytes(args);
		}
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		// type is read outside here
		destination = in.readByte();
		source.read(in);
		parameters = new byte[in.readInt()];
		in.readBytes(parameters);
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeByte(type);
		out.writeByte(destination);
		source.write(out);
		out.writeInt(parameters.length);
		out.write(parameters);
	}
	
	protected void fillParameters(int count) {
		parameters = new byte[count];
		Arrays.fill(parameters, (byte) -1);
	}
	
	protected static String getSourceString(ResourceID source, ScreenEffect screenEffect) {
		int nameID = source.getNameID();
		if (nameID == 0) {
			return "source";
		} else if (nameID > 0 && nameID - 2 <= 3) {
			return "particles" + (nameID - 2);
		} else if ((nameID & 0x20) == 0x20) {
			TemporaryFilterBuffer buffer = screenEffect.getFilterBuffer(nameID & ~0x20);
			if (buffer != null) {
				return buffer.name;
			} else {
				return source.toString();
			}
		} else {
			return source.toString();
		}
	}
	protected String getSourceString(ScreenEffect screenEffect) {
		return getSourceString(source, screenEffect);
	}
	
	protected static String getDestinationString(int destination, ScreenEffect screenEffect) {
		if (destination == 1) {
			return "dest";
		} else if (destination - 2 <= 3) {
			return "particles" + (destination - 2);
		} else if ((destination & 0x20) == 0x20) {
			TemporaryFilterBuffer buffer = screenEffect.getFilterBuffer(destination & ~0x20);
			if (buffer != null) {
				return buffer.name;
			} else {
				return Integer.toHexString(destination);
			}
		} else {
			return Integer.toHexString(destination);
		}
	}
	protected String getDestinationString(ScreenEffect screenEffect) {
		return getDestinationString(destination, screenEffect); 
	}
	
	protected static void parseSourceString(String str, ScreenEffect screenEffect, ResourceID dest) {
		dest.setGroupID(0);
		if (str.equals("source")) {
			dest.setNameID(0);
		} else if (str.startsWith("particles")) {
			dest.setNameID(Integer.parseInt(str.substring("particles".length())) + 2);
		} else {
			int bufferIndex = screenEffect.getFilterBufferIndex(str);
			if (bufferIndex != -1) {
				dest.setNameID(0x20 | bufferIndex);
			} else {
				dest.parse(str);
			}
		}
	}
	protected void parseSourceString(String str, ScreenEffect screenEffect) {
		parseSourceString(str, screenEffect, source);
	}
	
	protected static int parseDestinationString(String str, ScreenEffect screenEffect) {
		int destination = -1;
		if (str.equals("dest")) {
			destination = 1;
		} else if (str.startsWith("particles")) {
			destination = Integer.parseInt(str.substring("particles".length())) + 2;
		} else {
			int bufferIndex = screenEffect.getFilterBufferIndex(str);
			if (bufferIndex != -1) {
				destination = 0x20 | bufferIndex;
			} else {
				destination = Integer.parseInt(str, 16);
			}
		}
		return destination;
	}

	public static class TemporaryFilterBuffer {
		public static final String KEYWORD = "texture";
		
		protected String name;
		private int screenRatio = 2;
		private int size = 256;
		
		public TemporaryFilterBuffer(String name) {
			this.name = name;
		}
		
		public void read(InputStreamAccessor in) throws IOException {
			screenRatio = in.readInt();
			size = in.readInt();
		}
		
		public void write(OutputStreamAccessor out) throws IOException {
			out.writeInt(screenRatio);
			out.writeInt(size);
		}
		
		public ArgScriptCommand toCommand() {
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, name);
			c.putOption(new ArgScriptOption("ratio", Integer.toString(screenRatio)));
			if (size != 256) {
				c.putOption(new ArgScriptOption("size", Integer.toString(size)));
			}
			
			return c;
		}
		
		public void parse(ArgScriptCommand c) throws ArgScriptException {
			name = c.getSingleArgument();
			
			String arg = null;
			if ((arg = c.getOptionArg("ratio")) != null) {
				screenRatio = Integer.parseInt(arg);
			}
			if ((arg = c.getOptionArg("size")) != null) {
				size = Integer.parseInt(arg);
			}
		}
	}
	
	public static ScreenFilter getFilter(int type) {
		switch(type) {
		case Copy.TYPE: return new ScreenFilter.Copy();
		case Compress.TYPE: return new ScreenFilter.Compress();
		case Add.TYPE: return new ScreenFilter.Add();
		case Colorize.TYPE: return new ScreenFilter.Colorize();
		case BlurX.TYPE: return new ScreenFilter.BlurX();
		case BlurY.TYPE: return new ScreenFilter.BlurY();
		case Blur1D.TYPE: return new ScreenFilter.Blur1D();
		case Edge.TYPE: return new ScreenFilter.Edge();
		case EdgeX.TYPE: return new ScreenFilter.EdgeX();
		case EdgeY.TYPE: return new ScreenFilter.EdgeY();
		case Distort.TYPE: return new ScreenFilter.Distort();
		case Extract.TYPE: return new ScreenFilter.Extract();
		case Multiply.TYPE: return new ScreenFilter.Multiply();
		case Dilate.TYPE: return new ScreenFilter.Dilate();
		case Contrast.TYPE: return new ScreenFilter.Dilate();
		case CustomMaterial.TYPE: return new ScreenFilter.CustomMaterial();
		case StrengthFader.TYPE: return new ScreenFilter.StrengthFader();
		default: return new ScreenFilter(type);
		}
	}
	
	public static ScreenFilter getFilter(String keyword) {
		switch(keyword) {
		case Copy.KEYWORD: return new ScreenFilter.Copy();
		case Compress.KEYWORD: return new ScreenFilter.Compress();
		case Add.KEYWORD: return new ScreenFilter.Add();
		case Colorize.KEYWORD: return new ScreenFilter.Colorize();
		case BlurX.KEYWORD: return new ScreenFilter.BlurX();
		case BlurY.KEYWORD: return new ScreenFilter.BlurY();
		case Blur1D.KEYWORD: return new ScreenFilter.Blur1D();
		case Edge.KEYWORD: return new ScreenFilter.Edge();
		case EdgeX.KEYWORD: return new ScreenFilter.EdgeX();
		case EdgeY.KEYWORD: return new ScreenFilter.EdgeY();
		case Distort.KEYWORD: return new ScreenFilter.Distort();
		case Extract.KEYWORD: return new ScreenFilter.Extract();
		case Multiply.KEYWORD: return new ScreenFilter.Multiply();
		case Dilate.KEYWORD: return new ScreenFilter.Dilate();
		case Contrast.KEYWORD: return new ScreenFilter.Contrast();
		case CustomMaterial.KEYWORD: return new ScreenFilter.CustomMaterial();
		case StrengthFader.KEYWORD: return new ScreenFilter.StrengthFader();
		default: return new ScreenFilter();  // type is an option in the default filter
		}
	}
	
	
	public static class Distort extends ScreenFilter {
		private static final String KEYWORD = "distort";
		private static final int TYPE = 10;
		private static final int PARAM_COUNT = 6;
		public Distort() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("distorter", getSourceString(screenEffect.getResource(parameters[0]), screenEffect)));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("offsetX", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			if (parameters[2] != -1) {
				c.putOption(new ArgScriptOption("offsetY", Float.toString(screenEffect.getFloat(parameters[2]))));
			}
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("strength", Float.toString(screenEffect.getFloat(parameters[3]))));
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("transXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[4]))));
			}
			if (parameters[5] != -1) {
				c.putOption(new ArgScriptOption("tileXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[5]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("distorter")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[0] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("offsetX")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("offsetY")) != null) {
				parameters[2] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("strength")) != null) {
				parameters[3] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("transXY")) != null) {
				parameters[4] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
			if ((arg = c.getOptionArg("tileXY")) != null) {
				parameters[5] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
		}
	}
	
	public static class Blur1D extends ScreenFilter {
		private static final String KEYWORD = "blur1d";
		private static final int TYPE = 6;
		private static final int PARAM_COUNT = 3;
		public Blur1D() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("scale", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("scaleX", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			if (parameters[2] != -1) {
				c.putOption(new ArgScriptOption("scaleY", Float.toString(screenEffect.getFloat(parameters[2]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("scale")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("scaleX")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("scaleY")) != null) {
				parameters[2] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}
	
	public static class Copy extends ScreenFilter {
		private static final String KEYWORD = "copy";
		private static final int TYPE = 0;
		private static final int PARAM_COUNT = 11;
		public Copy() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != 0) {
				c.putFlag("pointSource");
			}
			if (parameters[1] != 0) {
				c.putFlag("blend");
			}
			if (parameters[2] != 0) {
				c.putFlag("add");
			}
			if (parameters[3] != 0) {
				c.putFlag("multiply");
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("sourceAlpha", Float.toString(screenEffect.getFloat(parameters[4]))));
			}
			if (parameters[5] != -1) {
				c.putOption(new ArgScriptOption("sourceColor", ArgScript.createFloatList(screenEffect.getVector3(parameters[5]))));
			}
			if (parameters[6] != -1) {
				c.putOption(new ArgScriptOption("maskTexture", getSourceString(screenEffect.getResource(parameters[6]), screenEffect)));
			}
			if (parameters[7] != -1) {
				c.putOption(new ArgScriptOption("maskChannel", Float.toString(screenEffect.getFloat(parameters[7]))));
			}
			// maybe it's offsetXY and tileXY
			if (parameters[8] != -1) {
				c.putOption(new ArgScriptOption("tileXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[8]))));
			}
			if (parameters[9] != 0) {
				c.putFlag("invertMask");
			}
			if (parameters[10] != -1) {
				c.putOption(new ArgScriptOption("offsetXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[10]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			parameters[0] = (byte) (c.hasFlag("pointSource") ? 1 : 0);
			parameters[1] = (byte) (c.hasFlag("blend") ? 1 : 0);
			parameters[2] = (byte) (c.hasFlag("add") ? 1 : 0);
			parameters[3] = (byte) (c.hasFlag("multiply") ? 1 : 0);
			if ((arg = c.getOptionArg("sourceAlpha")) != null) {
				parameters[4] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("sourceColor")) != null) {
				parameters[5] = screenEffect.getVector3Index(ArgScript.parseFloatList(arg, 3));
			}
			if ((arg = c.getOptionArg("maskTexture")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[6] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("maskChannel")) != null) {
				parameters[7] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("tileXY")) != null) {
				parameters[8] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
			parameters[9] = (byte) (c.hasFlag("param9") ? 1 : 0);
			if ((arg = c.getOptionArg("offsetXY")) != null) {
				parameters[10] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
		}
	}
	
	public static class Compress extends ScreenFilter {
		private static final String KEYWORD = "compress";
		private static final int TYPE = 1;
		private static final int PARAM_COUNT = 3;
		public Compress() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("bias", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("scale", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			if (parameters[2] != 0) {
				c.putFlag("mono");
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("bias")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("scale")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[2] = (byte) (c.hasFlag("mono") ? 1 : 0);
		}
	}
	
	public static class Add extends ScreenFilter {
		private static final String KEYWORD = "add";
		private static final int TYPE = 2;
		private static final int PARAM_COUNT = 9;
		public Add() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("texture", getSourceString(screenEffect.getResource(parameters[0]), screenEffect)));
			}
			if (parameters[1] != 0) {
				c.putFlag("pointSource");
			}
			if (parameters[2] != 0) {
				c.putFlag("pointAdd");
			}
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("sourceMul", Float.toString(screenEffect.getFloat(parameters[3]))));
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("addMul", Float.toString(screenEffect.getFloat(parameters[4]))));
			}
			if (parameters[5] != -1) {
				c.putOption(new ArgScriptOption("tileXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[5]))));
			}
			if (parameters[6] != -1) {
				c.putOption(new ArgScriptOption("maskTexture", getSourceString(screenEffect.getResource(parameters[6]), screenEffect)));
			}
			if (parameters[7] != -1) {
				c.putOption(new ArgScriptOption("param7", Float.toString(screenEffect.getFloat(parameters[7]))));
			}
			if (parameters[8] != 0) {
				c.putFlag("param8");
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("texture")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[0] = screenEffect.getResourceIndex(res);
			}
			parameters[1] = (byte) (c.hasFlag("pointSource") ? 1 : 0);
			parameters[2] = (byte) (c.hasFlag("pointAdd") ? 1 : 0);
			if ((arg = c.getOptionArg("sourceMul")) != null) {
				parameters[3] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("addMul")) != null) {
				parameters[4] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("tileXY")) != null) {
				parameters[5] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
			if ((arg = c.getOptionArg("maskTexture")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[6] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("param7")) != null) {
				parameters[7] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[8] = (byte) (c.hasFlag("param8") ? 1 : 0);
		}
	}
	
	public static class Colorize extends ScreenFilter {
		private static final String KEYWORD = "colorize";
		private static final int TYPE = 3;
		private static final int PARAM_COUNT = 4;
		public Colorize() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("color", ArgScript.createFloatList(screenEffect.getVector3(parameters[0]))));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("strength", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			if (parameters[2] != -1) {
				c.putOption(new ArgScriptOption("param2", getSourceString(screenEffect.getResource(parameters[2]), screenEffect)));
			}
			if (parameters[3] != 0) {
				c.putFlag("param3");
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("color")) != null) {
				parameters[0] = screenEffect.getVector3Index(ArgScript.parseFloatList(arg, 3));
			}
			if ((arg = c.getOptionArg("strength")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("param2")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[2] = screenEffect.getResourceIndex(res);
			}
			parameters[3] = (byte) (c.hasFlag("param3") ? 1 : 0);
		}
	}
	
	public static class BlurX extends ScreenFilter {
		private static final String KEYWORD = "blurx";
		private static final int TYPE = 4;
		private static final int PARAM_COUNT = 1;
		public BlurX() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("scale", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("scale")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}
	
	public static class BlurY extends ScreenFilter {
		private static final String KEYWORD = "blury";
		private static final int TYPE = 5;
		private static final int PARAM_COUNT = 1;
		public BlurY() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("scale", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("scale")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}
	
	public static class Edge extends ScreenFilter {
		private static final String KEYWORD = "edge";
		private static final int TYPE = 7;
		private static final int PARAM_COUNT = 6;
		public Edge() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != 0) {
				c.putFlag("normalMap");
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("scale", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			if (parameters[2] != 0) {
				c.putFlag("param2");
			}
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("param3", ArgScript.createFloatList(screenEffect.getVector3(parameters[3]))));
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("param4", ArgScript.createFloatList(screenEffect.getVector3(parameters[4]))));
			}
			if (parameters[5] != -1) {
				c.putOption(new ArgScriptOption("param5", Float.toString(screenEffect.getFloat(parameters[5]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			parameters[0] = (byte) (c.hasFlag("normalMap") ? 1 : 0);
			if ((arg = c.getOptionArg("scale")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[2] = (byte) (c.hasFlag("param2") ? 1 : 0);
			if ((arg = c.getOptionArg("param3")) != null) {
				parameters[3] = screenEffect.getVector3Index(ArgScript.parseFloatList(arg, 3));
			}
			if ((arg = c.getOptionArg("param4")) != null) {
				parameters[4] = screenEffect.getVector3Index(ArgScript.parseFloatList(arg, 3));
			}
			if ((arg = c.getOptionArg("param5")) != null) {
				parameters[5] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}  
	
	public static class EdgeX extends ScreenFilter {
		private static final String KEYWORD = "edgex";
		private static final int TYPE = 8;
		private static final int PARAM_COUNT = 0;
		public EdgeX() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			return new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
		}
	}
	
	public static class EdgeY extends ScreenFilter {
		private static final String KEYWORD = "edgey";
		private static final int TYPE = 9;
		private static final int PARAM_COUNT = 0;
		public EdgeY() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			return new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
		}
	}
	
	//TODO type 11
	
	public static class Extract extends ScreenFilter {
		private static final String KEYWORD = "extract";
		private static final int TYPE = 12;
		private static final int PARAM_COUNT = 1;
		public Extract() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("color", ArgScript.createFloatList(screenEffect.getVector3(parameters[0]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = c.getOptionArg("color");
			if (arg != null) {
				parameters[0] = screenEffect.getVector3Index(ArgScript.parseFloatList(arg, 3));
			}
		}
	}
	
	public static class Multiply extends ScreenFilter {
		private static final String KEYWORD = "multiply";
		private static final int TYPE = 13;
		private static final int PARAM_COUNT = 9;
		public Multiply() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("texture", getSourceString(screenEffect.getResource(parameters[0]), screenEffect)));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("sourceMul", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			if (parameters[2] != -1) {
				c.putOption(new ArgScriptOption("param2", Float.toString(screenEffect.getFloat(parameters[2]))));
			}
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("tileXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[3]))));
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("color", ArgScript.createFloatList(screenEffect.getVector3(parameters[4]))));
			}
			if (parameters[5] != 0) {
				c.putFlag("replace");
			}
			if (parameters[6] != -1) {
				c.putOption(new ArgScriptOption("offsetXY", ArgScript.createFloatList(screenEffect.getVector2(parameters[6]))));
			}
			if (parameters[7] != -1) {
				c.putOption(new ArgScriptOption("param7", Float.toString(screenEffect.getFloat(parameters[7]))));
			}
			if (parameters[8] != -1) {
				c.putOption(new ArgScriptOption("param8", Float.toString(screenEffect.getFloat(parameters[8]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("texture")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[0] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("sourceMul")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("param2")) != null) {
				parameters[2] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("tileXY")) != null) {
				parameters[5] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
			parameters[5] = (byte) (c.hasFlag("replace") ? 1 : 0);
			if ((arg = c.getOptionArg("offsetXY")) != null) {
				parameters[6] = screenEffect.getVector2Index(ArgScript.parseFloatList(arg, 2));
			}
			if ((arg = c.getOptionArg("param7")) != null) {
				parameters[7] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("param8")) != null) {
				parameters[8] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}
	
	public static class Dilate extends ScreenFilter {
		private static final String KEYWORD = "dilate";
		private static final int TYPE = 14;
		private static final int PARAM_COUNT = 0;
		public Dilate() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			return new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
		}
	}
	
	public static class Contrast extends ScreenFilter {
		private static final String KEYWORD = "contrast";
		private static final int TYPE = 15;
		private static final int PARAM_COUNT = 2;
		public Contrast() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("upper", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("lower", Float.toString(screenEffect.getFloat(parameters[1]))));
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("upper")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("lower")) != null) {
				parameters[1] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
		}
	}
	
	public static class CustomMaterial extends ScreenFilter {
		private static final String KEYWORD = "customMaterial";
		private static final int TYPE = 16;
		private static final int PARAM_COUNT = 18;
		public CustomMaterial() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("materialID", getSourceString(screenEffect.getResource(parameters[0]), screenEffect)));
			}
			if (parameters[1] != -1) {
				c.putOption(new ArgScriptOption("sampler0", getSourceString(screenEffect.getResource(parameters[1]), screenEffect)));
			}
			if (parameters[2] != -1) {
				c.putOption(new ArgScriptOption("sampler1", getSourceString(screenEffect.getResource(parameters[2]), screenEffect)));
			}
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("sampler2", getSourceString(screenEffect.getResource(parameters[3]), screenEffect)));
			}
			if (parameters[4] != -1) {
				c.putOption(new ArgScriptOption("sampler3", getSourceString(screenEffect.getResource(parameters[4]), screenEffect)));
			}
			if (parameters[5] != -1) {
				c.putOption(new ArgScriptOption("customParams0", Float.toString(screenEffect.getFloat(parameters[5]))));
			}
			if (parameters[6] != -1) {
				c.putOption(new ArgScriptOption("customParams1", Float.toString(screenEffect.getFloat(parameters[6]))));
			}
			if (parameters[7] != -1) {
				c.putOption(new ArgScriptOption("customParams2", Float.toString(screenEffect.getFloat(parameters[7]))));
			}
			if (parameters[8] != -1) {
				c.putOption(new ArgScriptOption("customParams3", Float.toString(screenEffect.getFloat(parameters[8]))));
			}
			if (parameters[9] != -1) {
				c.putOption(new ArgScriptOption("customParams4", Float.toString(screenEffect.getFloat(parameters[9]))));
			}
			if (parameters[10] != -1) {
				c.putOption(new ArgScriptOption("customParams5", Float.toString(screenEffect.getFloat(parameters[10]))));
			}
			if (parameters[11] != -1) {
				c.putOption(new ArgScriptOption("customParams6", Float.toString(screenEffect.getFloat(parameters[11]))));
			}
			if (parameters[12] != -1) {
				c.putOption(new ArgScriptOption("customParams7", Float.toString(screenEffect.getFloat(parameters[12]))));
			}
			if (parameters[13] != 0) {
				c.putFlag("param13");
			}
			if (parameters[14] != -1) {
				c.putOption(new ArgScriptOption("maxDistance", Float.toString(screenEffect.getFloat(parameters[14]))));
			}
			if (parameters[15] != 0) {
				c.putFlag("param15");
			}
			if (parameters[16] != 0) {
				c.putFlag("param16");
			}
			if (parameters[17] != 0) {
				c.putFlag("param17");
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("materialID")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[0] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("sampler0")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[1] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("sampler1")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[2] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("sampler2")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[3] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("sampler3")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[4] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("customParams0")) != null) {
				parameters[5] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams1")) != null) {
				parameters[6] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams2")) != null) {
				parameters[7] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams3")) != null) {
				parameters[8] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams4")) != null) {
				parameters[9] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams5")) != null) {
				parameters[10] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams6")) != null) {
				parameters[12] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			if ((arg = c.getOptionArg("customParams7")) != null) {
				parameters[12] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[13] = (byte) (c.hasFlag("param13") ? 1 : 0);
			if ((arg = c.getOptionArg("maxDistance")) != null) {
				parameters[14] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[15] = (byte) (c.hasFlag("param15") ? 1 : 0);
			parameters[16] = (byte) (c.hasFlag("param16") ? 1 : 0);
			parameters[17] = (byte) (c.hasFlag("param17") ? 1 : 0);
		}
	}
	
	public static class StrengthFader extends ScreenFilter {
		private static final String KEYWORD = "strengthFader";
		private static final int TYPE = 17;
		private static final int PARAM_COUNT = 4;
		public StrengthFader() {
			super(TYPE);
		}
		@Override
		public ArgScriptCommand toCommand(ScreenEffect screenEffect) {
			if (parameters.length != PARAM_COUNT) {
				return super.toCommand(screenEffect);
			}
			ArgScriptCommand c = new ArgScriptCommand(KEYWORD, getSourceString(screenEffect), getDestinationString(screenEffect));
			if (parameters[3] != -1) {
				c.putOption(new ArgScriptOption("texture", getSourceString(screenEffect.getResource(parameters[3]), screenEffect)));
			}
			if (parameters[0] != -1) {
				c.putOption(new ArgScriptOption("length", Float.toString(screenEffect.getFloat(parameters[0]))));
			}
			if (parameters[1] != 0) {
				c.putFlag("fadeIn");
			}
			if (parameters[2] != 0) {
				c.putFlag("fadeOut");
			}
			return c;
		}
		
		@Override
		public void parse(ArgScriptCommand c, ScreenEffect screenEffect) throws ArgScriptException {
			fillParameters(PARAM_COUNT);
			List<String> args = c.getArguments(2);
			parseSourceString(args.get(0), screenEffect);
			destination = parseDestinationString(args.get(1), screenEffect);
			
			String arg = null;
			if ((arg = c.getOptionArg("texture")) != null) {
				ResourceID res = new ResourceID();
				parseSourceString(arg, screenEffect, res);
				parameters[3] = screenEffect.getResourceIndex(res);
			}
			if ((arg = c.getOptionArg("length")) != null) {
				parameters[0] = screenEffect.getFloatIndex(Float.parseFloat(arg));
			}
			parameters[1] = (byte) (c.hasFlag("fadeIn") ? 1 : 0);
			parameters[2] = (byte) (c.hasFlag("fadeOut") ? 1 : 0);
		}
	}
	
	/*
	 * sub_6F8E50
	 * 
	 * ResourceIDs are at:
	 * filter 0:
	 * 		parameters[6] (optional ?)
	 * filter 1, 4, 5, 6, 7, 8, 9, 11, 12, 14, 15:
	 * 		no ResourceID parameters
	 * filter 2:
	 * 		parameters[0], parameters[6]
	 * filter 3: 
	 * 		parameters[2]
	 * filter 10, 13:
	 * 		parameters[0]
	 * filter 16:
	 * 		parameters[1], parameters[2], parameters[3], parameters[4]
	 * filter 17:
	 * 		parameters[3]
	 * 
	 * sub_6F89E0 process filter?
	 */
}
