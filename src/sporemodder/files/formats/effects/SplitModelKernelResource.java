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

public class SplitModelKernelResource extends Resource {
	
	public static final int TYPE = 0x10;
	public static final int MASKED_TYPE = TYPE | TYPE_MASK;
	public static final String KEYWORD = "splitter";
	
	private static final int PLANE = 0;
	private static final int SPHERE = 1;
	private static final int CYLINDER = 2;
	
	private static final ArgScriptEnum ENUM_TYPE = new ArgScriptEnum(new String[] {
			"plane", "sphere", "cylinder"
	}, new int[] {
			PLANE, SPHERE, CYLINDER
	});
	
	private static class SplitKernelCylinder {
		private final float[] origin = new float[3];
		private final float[] direction = new float[3];
		private float radius;
	}
	private static class SplitKernelPlane {
		private final float[] field_0 = new float[3];  // normal / origin
		private float field_C;  // offset / radius
	}
	
	private int type = -1;
	private final SplitKernelPlane[] plane = new SplitKernelPlane[2];
	private final SplitKernelCylinder[] cylinder = new SplitKernelCylinder[2];
	protected String name;  // this one uses names instead

	public SplitModelKernelResource() {
	}
	
	public SplitModelKernelResource(int index) {
		name = KEYWORD + "-" + index;
	}

	public SplitModelKernelResource(String name) {
		this.name = name;
	}

	public boolean read(InputStreamAccessor in) throws IOException {
		type = in.readInt();
		
		if (type == PLANE || type == SPHERE) {
			for (int i = 0; i < 2; i++) {
				plane[i] = new SplitKernelPlane();
				in.readLEFloats(plane[i].field_0);
				plane[i].field_C = in.readFloat();
			}
		} else {
			for (int i = 0; i < 2; i++) {
				cylinder[i] = new SplitKernelCylinder();
				in.readLEFloats(cylinder[i].origin);
				in.readLEFloats(cylinder[i].direction);
				cylinder[i].radius = in.readFloat();
			}
		}
		
		return true;
	}
	
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(type);
		if (type == PLANE || type == SPHERE) {
			for (int i = 0; i < 2; i++) {
				if (plane[i] == null) plane[i] = new SplitKernelPlane();
				out.writeLEFloats(plane[i].field_0);
				out.writeFloat(plane[i].field_C);
			}
		} else {
			for (int i = 0; i < 2; i++) {
				if (cylinder[i] == null) cylinder[i] = new SplitKernelCylinder();
				out.writeLEFloats(cylinder[i].origin);
				out.writeLEFloats(cylinder[i].direction);
				out.writeFloat(cylinder[i].radius);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		
		ArgScriptCommand c = null;
		if ((c = block.getCommand("type")) != null) {
			if (type >= 0) {
				throw new ArgScriptException(KEYWORD + ": Type already specified.");
			} else {
				type = ENUM_TYPE.getValue(c.getSingleArgument());
			}
		}
		if ((c = block.getCommand("offset")) != null) {
			if (type != PLANE) {
				throw new ArgScriptException(KEYWORD + ": Either type not specified or type does not support this parameter ('offset').");
			}
			if (plane[0] == null) plane[0] = new SplitKernelPlane();
			if (plane[1] == null) plane[1] = new SplitKernelPlane();
			plane[0].field_C = Float.parseFloat(c.getSingleArgument());
			String arg = c.getOptionArg("vary");
			if (arg != null) {
				plane[1].field_C = Float.parseFloat(arg);
			}
		}
		if ((c = block.getCommand("radius")) != null) {
			if (type == SPHERE) {
				if (plane[0] == null) plane[0] = new SplitKernelPlane();
				if (plane[1] == null) plane[1] = new SplitKernelPlane();
				plane[0].field_C = Float.parseFloat(c.getSingleArgument());
				String arg = c.getOptionArg("vary");
				if (arg != null) {
					plane[1].field_C = Float.parseFloat(arg);
				}
			}
			else if (type != CYLINDER) {
				if (cylinder[0] == null) cylinder[0] = new SplitKernelCylinder();
				if (cylinder[1] == null) cylinder[1] = new SplitKernelCylinder();
				cylinder[0].radius = Float.parseFloat(c.getSingleArgument());
				String arg = c.getOptionArg("vary");
				if (arg != null) {
					cylinder[1].radius = Float.parseFloat(arg);
				}
			} 
			else {
				throw new ArgScriptException(KEYWORD + ": Either type not specified or type does not support this parameter ('radius').");
			}
		}
		if ((c = block.getCommand("normal")) != null) {
			if (type != PLANE) {
				throw new ArgScriptException(KEYWORD + ": Either type not specified or type does not support this parameter ('normal').");
			}
			if (plane[0] == null) plane[0] = new SplitKernelPlane();
			if (plane[1] == null) plane[1] = new SplitKernelPlane();
			ArgScript.parseFloatList(c.getSingleArgument(), plane[0].field_0);
			String arg = c.getOptionArg("vary");
			if (arg != null) {
				ArgScript.parseFloatList(arg, plane[1].field_0);
			}
		}
		if ((c = block.getCommand("direction")) != null) {
			if (type != CYLINDER) {
				throw new ArgScriptException(KEYWORD + ": Either type not specified or type does not support this parameter ('direction').");
			}
			if (cylinder[0] == null) cylinder[0] = new SplitKernelCylinder();
			if (cylinder[1] == null) cylinder[1] = new SplitKernelCylinder();
			ArgScript.parseFloatList(c.getSingleArgument(), cylinder[0].direction);
			String arg = c.getOptionArg("vary");
			if (arg != null) {
				ArgScript.parseFloatList(arg, cylinder[1].direction);
			}
		}
		if ((c = block.getCommand("origin")) != null) {
			if (type == SPHERE) {
				if (plane[0] == null) plane[0] = new SplitKernelPlane();
				if (plane[1] == null) plane[1] = new SplitKernelPlane();
				ArgScript.parseFloatList(c.getSingleArgument(), plane[0].field_0);
				String arg = c.getOptionArg("vary");
				if (arg != null) {
					ArgScript.parseFloatList(arg, plane[1].field_0);
				}
			}
			else if (type != CYLINDER) {
				if (cylinder[0] == null) cylinder[0] = new SplitKernelCylinder();
				if (cylinder[1] == null) cylinder[1] = new SplitKernelCylinder();
				ArgScript.parseFloatList(c.getSingleArgument(), cylinder[0].origin);
				String arg = c.getOptionArg("vary");
				if (arg != null) {
					ArgScript.parseFloatList(arg, cylinder[1].origin);
				}
			} 
			else {
				throw new ArgScriptException(KEYWORD + ": Either type not specified or type does not support this parameter ('radius').");
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		block.putCommand(new ArgScriptCommand("type", ENUM_TYPE.getKey(type)));
		
		if (type == PLANE) {
			ArgScriptCommand c = new ArgScriptCommand("normal", ArgScript.createFloatList(plane[0].field_0));
			if (plane[1].field_0[0] != 0 || plane[1].field_0[1] != 0 || plane[1].field_0[2] != 0) {
				c.putOption(new ArgScriptOption("vary", ArgScript.createFloatList(plane[1].field_0)));
			}
			block.putCommand(c);
			
			c = new ArgScriptCommand("offset", Float.toString(plane[0].field_C));
			if (plane[1].field_C != 0) {
				c.putOption(new ArgScriptOption("vary", Float.toString(plane[1].field_C)));
			}
			block.putCommand(c);
		}
		else if (type == SPHERE) {
			ArgScriptCommand c = new ArgScriptCommand("origin", ArgScript.createFloatList(plane[0].field_0));
			if (plane[1].field_0[0] != 0 || plane[1].field_0[1] != 0 || plane[1].field_0[2] != 0) {
				c.putOption(new ArgScriptOption("vary", ArgScript.createFloatList(plane[1].field_0)));
			}
			block.putCommand(c);
			
			c = new ArgScriptCommand("radius", Float.toString(plane[0].field_C));
			if (plane[1].field_C != 0) {
				c.putOption(new ArgScriptOption("vary", Float.toString(plane[1].field_C)));
			}
			block.putCommand(c);
		}
		else if (type == CYLINDER) {
			ArgScriptCommand c = new ArgScriptCommand("origin", ArgScript.createFloatList(cylinder[0].origin));
			if (cylinder[1].origin[0] != 0 || cylinder[1].origin[1] != 0 || cylinder[1].origin[2] != 0) {
				c.putOption(new ArgScriptOption("vary", ArgScript.createFloatList(cylinder[1].origin)));
			}
			block.putCommand(c);
			
			c = new ArgScriptCommand("direction", ArgScript.createFloatList(cylinder[0].direction));
			if (cylinder[1].direction[0] != 0 || cylinder[1].direction[1] != 0 || cylinder[1].direction[2] != 0) {
				c.putOption(new ArgScriptOption("vary", ArgScript.createFloatList(cylinder[1].direction)));
			}
			block.putCommand(c);
			
			c = new ArgScriptCommand("radius", Float.toString(cylinder[0].radius));
			if (cylinder[1].radius != 0) {
				c.putOption(new ArgScriptOption("vary", Float.toString(cylinder[1].radius)));
			}
			block.putCommand(c);
		}
		
		return true;
	}

	@Override
	public ArgScriptBlock toBlock() {
		ArgScriptBlock block = new ArgScriptBlock(KEYWORD, name);
		toBlock(block);
		return block;
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
		return TYPE | TYPE_MASK;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"plane", "sphere", "cylinder"
			};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"vary"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
				"type", "origin", "offset", "normal", "direction", "radius"
		};
	}
}

