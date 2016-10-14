package sporemodder.files.formats.effects;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptEnum;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class MapResource extends Resource {
	
	public static final int TYPE = 0;
	public static final int MASKED_TYPE = TYPE | TYPE_MASK;
	public static final String KEYWORD = "effectMap";
	
	private static final ArgScriptEnum ENUM_CHANNEL = new ArgScriptEnum(new String[] {
			// ?
		"red", "green", "blue", "alpha", "all"
	}, new int[] {
		0, 1, 2, 3, 4
	});
	
	private static final ArgScriptEnum ENUM_MAPTYPE = new ArgScriptEnum(new String[] {
			// ?
		"imageUnk0", "bitImage", "bitImage8", "bitImage32", "imageUnk4", "imageUnk5"
	}, new int[] {
		0, 1, 2, 3, 4, 5
	});
	
	private static final ArgScriptEnum ENUM_OPTYPE = new ArgScriptEnum(new String[] {
			// ?
		"set", "add", "mul", "sub", "max", "min", "mad"
	}, new int[] {
		0, 1, 2, 3, 4, 5, 6
	});
	
	private static final int MAPTYPE_1BIT = 1;  // ? .#03E421E9 files
	private static final int MAPTYPE_8BIT = 2;  // ? .#03E421EC files
	private static final int MAPTYPE_32BIT = 3;  // ?
	private static final int MAPTYPE_NONE = 6;  // ?
	
	private static final int ARG_RES_0 = 0x4;  // ?
	private static final int ARG_RES_1 = 0x8;  // ?
	private static final int ARG_RES_2 = 0x10;  // ?
	private static final int ARG_RES_3 = 0x20;  // ?
	private static final int ARG_VALUE_0 = 0x40;  // ?
	private static final int ARG_VALUE_1 = 0x80;  // ?
	private static final int ARG_VALUE_2 = 0x100;  // ?
	private static final int ARG_VALUE_3 = 0x200;  // ?
	private static final int ARG_MASK = 0x3FC;
	
	private int flags;
	private int mapType = 6;
	private final ResourceID imageID = new ResourceID();
	private final float[] bounds = new float[4];
	private int channel = 4;  // byte
	private int opType;  // byte
	
	private final ResourceID[] opArgMaps = new ResourceID[4];
	private final float[][] opArgValues = new float[4][4];

	public MapResource() {
	}

	public boolean read(InputStreamAccessor in) throws IOException {
		resourceID.read(in);
		flags = in.readInt();
		mapType = in.readByte();
		imageID.read(in);
		in.readLEFloats(bounds);
		
		channel = in.readByte();
		opType = in.readByte();
		
		for (int i = 0; i < 4; i++) {
			opArgMaps[i] = new ResourceID(in);
		}
		for (int i = 0; i < 4; i++) {
			opArgValues[i] = new float[4];
			in.readLEFloats(opArgValues[i]);
		}
		
//		System.out.println(mapID.toString());
//		System.out.println("\tflags: " + Integer.toBinaryString(flags));
//		System.out.println("\tmapType: " + mapType);
//		System.out.println("\timageID: " + imageID.toString());
//		System.out.println("\tbounds: " + bounds[0] + " " + bounds[1] + " " + bounds[2] + " " + bounds[3]);
//		System.out.println("\tchannel: " + channel);
//		System.out.println("\topType: " + opType);
//		for (int i = 0; i < 4; i++) {
//			System.out.println("\topArgMaps[" + i + "]: " + opArgMaps[i].toString());
//		}
//		for (int i = 0; i < 4; i++) {
//			System.out.println("\topArgValues[" + i + "]: " + Float.intBitsToFloat(opArgValues[i][0]) + " " + Float.intBitsToFloat(opArgValues[i][1]) + 
//					" " + Float.intBitsToFloat(opArgValues[i][2]) + " " + Float.intBitsToFloat(opArgValues[i][3]));
//			System.out.println("\topArgValues[" + i + "]: " + Hasher.getFileName(opArgValues[i][0]) + " " + Hasher.getFileName(opArgValues[i][1]) + 
//					" " + Hasher.getFileName(opArgValues[i][2]) + " " + Hasher.getFileName(opArgValues[i][3]));
//		}
		
		return true;
	}
	
	public boolean write(OutputStreamAccessor out) throws IOException {
		resourceID.write(out);
		out.writeInt(flags);
		out.writeByte(mapType);
		imageID.write(out);
		out.writeLEFloats(bounds);
		
		out.writeByte(channel);
		out.writeByte(opType);
		
		for (int i = 0; i < 4; i++) {
			if (opArgMaps[i] == null) {
				opArgMaps[i] = new ResourceID();
			}
			opArgMaps[i].write(out);
		}
		for (int i = 0; i < 4; i++) {
			if (opArgValues[i] == null) {
				opArgValues[i] = new float[4];
			}
			out.writeLEFloats(opArgValues[i]);
		}
		
		return true;
	}
	
	@Override
	public boolean supportsBlock() {
		return false;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		throw new UnsupportedOperationException(KEYWORD + ": Block format is not supported.");
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		throw new UnsupportedOperationException(KEYWORD + ": Block format is not supported.");
	}

	@Override
	public ArgScriptBlock toBlock() {
		throw new UnsupportedOperationException(KEYWORD + ": Block format is not supported.");
	}
	
	// arr is expected to have length >= 1
	private static boolean match(float[] arr) {
		boolean result = true;
		float value = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] != value) return false;
		}
		return result;
	}

	@Override
	public ArgScriptCommand toCommand() {
		ArgScriptCommand c = new ArgScriptCommand(KEYWORD, resourceID.toString());
		
		if (!imageID.isDefault()) {
			c.putOption(new ArgScriptOption(ENUM_MAPTYPE.getKey(mapType), imageID.toString()));
		}
		c.putOption(new ArgScriptOption("channel", ENUM_CHANNEL.getKey(channel)));
		c.putOption(new ArgScriptOption("rect", ArgScript.createFloatList(bounds)));
		
		if ((flags & ARG_RES_0) == ARG_RES_0) c.putOption(new ArgScriptOption("map0", opArgMaps[0].toString()));
		if ((flags & ARG_RES_1) == ARG_RES_1) c.putOption(new ArgScriptOption("map1", opArgMaps[1].toString()));
		if ((flags & ARG_RES_2) == ARG_RES_2) c.putOption(new ArgScriptOption("map2", opArgMaps[2].toString()));
		if ((flags & ARG_RES_3) == ARG_RES_3) c.putOption(new ArgScriptOption("map3", opArgMaps[3].toString()));
		
		if ((flags & ARG_VALUE_0) == ARG_VALUE_0) {
			if (match(opArgValues[0])) c.putOption(new ArgScriptOption("value0", Float.toString(opArgValues[0][0])));
			else c.putOption(new ArgScriptOption("value0", ArgScript.createFloatList(opArgValues[0])));
		}
		if ((flags & ARG_VALUE_1) == ARG_VALUE_1) {
			if (match(opArgValues[1])) c.putOption(new ArgScriptOption("value1", Float.toString(opArgValues[1][0])));
			else c.putOption(new ArgScriptOption("value1", ArgScript.createFloatList(opArgValues[1])));
		}
		if ((flags & ARG_VALUE_2) == ARG_VALUE_2) {
			if (match(opArgValues[2])) c.putOption(new ArgScriptOption("value2", Float.toString(opArgValues[2][0])));
			else c.putOption(new ArgScriptOption("value2", ArgScript.createFloatList(opArgValues[2])));
		}
		if ((flags & ARG_VALUE_3) == ARG_VALUE_3) {
			if (match(opArgValues[3])) c.putOption(new ArgScriptOption("value3", Float.toString(opArgValues[3][0])));
			else c.putOption(new ArgScriptOption("value3", ArgScript.createFloatList(opArgValues[3])));
		}
		
		if ((flags & ~ARG_MASK) != 0) {
			c.putOption(new ArgScriptOption("flags", "0x" + Integer.toHexString(flags & ~ARG_MASK)));
		}
		if ((flags & ARG_MASK) != 0) {
			c.putOption(new ArgScriptOption("op", ENUM_OPTYPE.getKey(opType)));
		}
		
		return c;
	}

	@Override
	public boolean parseCommand(ArgScriptCommand c) throws ArgScriptException, IOException {
		resourceID.parse(c.getSingleArgument());
		String arg = null;
		String[] keys = ENUM_MAPTYPE.getKeys();
		int[] values = ENUM_MAPTYPE.getValues();
		for (int i = 0; i < keys.length; i++) {
			if ((arg = c.getOptionArg(keys[i])) != null) {
				imageID.parse(arg);
				mapType = values[i];
				break;
			}
		}
		
		if ((arg = c.getOptionArg("channel")) != null) {
			channel = ENUM_CHANNEL.getValue(arg);
		}
		if ((arg = c.getOptionArg("rect")) != null) {
			ArgScript.parseFloatList(arg, bounds);
		}
		if ((arg = c.getOptionArg("map0")) != null) {
			opArgMaps[0] = new ResourceID(arg);
			flags |= ARG_RES_0;
		}
		if ((arg = c.getOptionArg("map1")) != null) {
			opArgMaps[1] = new ResourceID(arg);
			flags |= ARG_RES_1;
		}
		if ((arg = c.getOptionArg("map2")) != null) {
			opArgMaps[2] = new ResourceID(arg);
			flags |= ARG_RES_2;
		}
		if ((arg = c.getOptionArg("map3")) != null) {
			opArgMaps[3] = new ResourceID(arg);
			flags |= ARG_RES_3;
		}
		if ((arg = c.getOptionArg("value0")) != null) {
			flags |= ARG_VALUE_0;
			opArgValues[0] = new float[4];
			if (arg.contains("(")) {
				opArgValues[0] = new float[4];
				ArgScript.parseFloatList(arg, opArgValues[0]);
			} else {
				opArgValues[0] = new float[4];
				opArgValues[0][0] = opArgValues[0][1] = opArgValues[0][2] = opArgValues[0][3] = Float.parseFloat(arg);
			}
		}
		if ((arg = c.getOptionArg("value1")) != null) {
			flags |= ARG_VALUE_1;
			opArgValues[1] = new float[4];
			if (arg.contains("(")) {
				opArgValues[1] = new float[4];
				ArgScript.parseFloatList(arg, opArgValues[1]);
			} else {
				opArgValues[1] = new float[4];
				opArgValues[1][0] = opArgValues[1][1] = opArgValues[1][2] = opArgValues[1][3] = Float.parseFloat(arg);
			}
		}
		if ((arg = c.getOptionArg("value2")) != null) {
			flags |= ARG_VALUE_2;
			opArgValues[2] = new float[4];
			if (arg.contains("(")) {
				opArgValues[2] = new float[4];
				ArgScript.parseFloatList(arg, opArgValues[2]);
			} else {
				opArgValues[2] = new float[4];
				opArgValues[2][0] = opArgValues[2][1] = opArgValues[2][2] = opArgValues[2][3] = Float.parseFloat(arg);
			}
		}
		if ((arg = c.getOptionArg("value3")) != null) {
			flags |= ARG_VALUE_3;
			opArgValues[3] = new float[4];
			if (arg.contains("(")) {
				opArgValues[3] = new float[4];
				ArgScript.parseFloatList(arg, opArgValues[3]);
			} else {
				opArgValues[3] = new float[4];
				opArgValues[3][0] = opArgValues[3][1] = opArgValues[3][2] = opArgValues[3][3] = Float.parseFloat(arg);
			}
		}
		if ((arg = c.getOptionArg("op")) != null) {
			opType = ENUM_OPTYPE.getValue(arg);
		}
		if ((arg = c.getOptionArg("flags")) != null) {
			// we don't want the user to modify the arg flags
			flags |= (Hasher.decodeInt(arg) & ~ARG_MASK);
		}
		
		return true;
	}
	
	@Override
	public int getType() {
		return TYPE | TYPE_MASK;
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"red", "green", "blue", "alpha", "all",
			"set", "add", "mul", "sub", "max", "min", "mad"
			};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"imageUnk0", "bitImage", "bitImage8", "bitImage32", "imageUnk4", "imageUnk5", "op", "flags",
			"map0", "map1", "map2", "map3", "value0", "value1", "value2", "value3", "channel", "rect"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
		};
	}
}
