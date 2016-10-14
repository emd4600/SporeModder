package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.utilities.Hasher;

public class VolumeEffect extends EffectComponent {
	
	public static final int TYPE = 0x28;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "volume";
	
	// Warning! Spore parsed 'color' 'alpha' too, but it doesn't read them anywhere!
	private final float[] field_8 = new float[3];  // 0x08
	private float sliceSpacing = 0.25f;  // 0x14
	private final ResourceID material = new ResourceID(0x554771E5, 0);  // 0x28
	private final float[][] field_30 = new float[8][6];

	public VolumeEffect(int type, int version) {
		super(type, version);
	}

	public VolumeEffect(VolumeEffect effect) {
		super(effect);
		
		field_8[0] = effect.field_8[0];
		field_8[1] = effect.field_8[1];
		field_8[2] = effect.field_8[2];
		sliceSpacing = effect.sliceSpacing;
		material.copy(effect.material);
		for (int i = 0; i < field_30.length; i++) {
			field_30[i] = new float[effect.field_30[i].length];
			for (int j = 0; j < field_30[i].length; j++) {
				field_30[i][j] = effect.field_30[i][j];
			}
		}
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		in.readLEFloats(field_8);
		sliceSpacing = in.readFloat();
		material.read(in);
		for (int i = 0; i < field_30.length; i++) {
			field_30[i] = new float[6];
			in.readLEFloats(field_30[i]);
		}
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloats(field_8);
		out.writeFloat(sliceSpacing);
		material.write(out);
		for (int i = 0; i < field_30.length; i++) {
			out.writeLEFloats(field_30[i]);
		}
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptCommand comm = null;
		
		if ((comm = block.getCommand("sliceSpacing")) != null) 
		{
			sliceSpacing = Float.parseFloat(comm.getSingleArgument());
		}
		if ((comm = block.getCommand("material")) != null) 
		{
//			material.setGroupID(Hasher.getFileHash(comm.getSingleArgument()));
//			material.setNameID(0);
			material.parse(comm.getSingleArgument());
		}
		
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("set")) 
			{
				List<String> args = c.getArguments(2, 8);
				if (args.get(0).equals("corner")) {
					if (args.size() < 5) {
						throw new ArgScriptException(KEYWORD + ": Wrong number of arguments in 'set corner'.");
					}
					int index = Byte.parseByte(args.get(1));
					if (index > 8) {
						int num = 0;
						if ((index & 1) != 0) {
							num = 1;
						}
						if (((index / 10) & 1) != 0) {
							num |= 2;
						}
						if (index >= 100) {
							num |= 4;
						}
						index = num;
					}
					field_30[index] = new float[6];
					field_30[index][0] = Float.parseFloat(args.get(2));
					field_30[index][1] = Float.parseFloat(args.get(3));
					field_30[index][2] = Float.parseFloat(args.get(4));
					if (args.size() >= 8) {
						field_30[index][3] = Float.parseFloat(args.get(5));
						field_30[index][4] = Float.parseFloat(args.get(6));
						field_30[index][5] = Float.parseFloat(args.get(7));
					}
				}
				else if (args.get(0).equals("bottom")) {
					if (args.size() < 2) {
						throw new ArgScriptException(KEYWORD + ": Wrong number of arguments in 'set bottom'.");
					}
					field_30[0][2] = field_30[1][2] = field_30[2][2] = field_30[3][2] = Float.parseFloat(args.get(1));
					if (args.size() >= 3) {
						field_30[0][5] = field_30[1][5] = field_30[2][5] = field_30[3][5] = Float.parseFloat(args.get(2));
					}
				}
				else if (args.get(0).equals("top")) {
					if (args.size() < 2) {
						throw new ArgScriptException(KEYWORD + ": Wrong number of arguments in 'set top'.");
					}
					field_30[4][2] = field_30[5][2] = field_30[6][2] = field_30[7][2] = Float.parseFloat(args.get(1));
					if (args.size() >= 3) {
						field_30[4][5] = field_30[5][5] = field_30[6][5] = field_30[7][5] = Float.parseFloat(args.get(2));
					}
				}
				else if (args.get(0).equals("width")) {
					if (args.size() < 2) {
						throw new ArgScriptException(KEYWORD + ": Wrong number of arguments in 'set width'.");
					}
					float num = Float.parseFloat(args.get(1)) * 0.5f;
					field_30[0][0] = -num;
					field_30[0][1] = -num;
					field_30[1][0] = num;
					field_30[1][1] = -num;
					field_30[2][0] = -num;
					field_30[2][1] = num;
					field_30[3][0] = num;
					field_30[3][1] = num;
					field_30[4][0] = -num;
					field_30[4][1] = -num;
					field_30[5][0] = num;
					field_30[5][1] = -num;
					field_30[6][0] = -num;
					field_30[6][1] = num;
					field_30[7][0] = num;
					field_30[7][1] = num;
				}
			}
		}
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {

		if (sliceSpacing != 0.25f) {
			block.putCommand(new ArgScriptCommand("sliceSpacing", Float.toString(sliceSpacing)));
		}
		// Hasher.getFileName(material.getGroupID())
		block.putCommand(new ArgScriptCommand("material", material.toString()));
		
		if (field_30[0][2] == field_30[1][2] && field_30[0][2] == field_30[2][2] && field_30[0][2] == field_30[3][2]) {
			ArgScriptCommand c = new ArgScriptCommand("set", "bottom", Float.toString(field_30[0][2]));
			
			if (field_30[0][5] == field_30[1][5] && field_30[0][5] == field_30[2][5] && field_30[0][5] == field_30[3][5]) {
				c.addArgument(Float.toString(field_30[0][5]));
			}
			block.putCommand(c);
		}
		if (field_30[4][2] == field_30[5][2] && field_30[4][2] == field_30[6][2] && field_30[4][2] == field_30[7][2]) {
			ArgScriptCommand c = new ArgScriptCommand("set", "top", Float.toString(field_30[4][2]));
			
			if (field_30[4][5] == field_30[5][5] && field_30[4][5] == field_30[6][5] && field_30[4][5] == field_30[7][5]) {
				c.addArgument(Float.toString(field_30[4][5]));
			}
			block.putCommand(c);
		}
		float num = field_30[0][0];
		
		// more checkings go here
		if (num == field_30[0][0] && num == field_30[0][1] && num == field_30[1][1] && num == field_30[2][0]) {
			block.putCommand(new ArgScriptCommand("set", "width", Float.toString(-num * 2)));
		}
		
		//TODO this isn't really correct, but it should pack again without problems
		for (int i = 0; i < field_30.length; i++) {
			boolean isWorth2 = field_30[i][3] != 0 || field_30[i][4] != 0 || field_30[i][5] != 0;
			
			if (field_30[i][0] != 0 || field_30[i][1] != 0 || field_30[i][2] != 0 || isWorth2) {
				ArgScriptCommand c = new ArgScriptCommand("set", "corner", Integer.toString(i),
						Float.toString(field_30[i][0]), Float.toString(field_30[i][1]), Float.toString(field_30[i][2]));
				
				if (isWorth2) {
					c.addArgument(Float.toString(field_30[i][3]));
					c.addArgument(Float.toString(field_30[i][4]));
					c.addArgument(Float.toString(field_30[i][5]));
				}
				
				block.putCommand(c);
			}
		}
		
		return true;
	}

	@Override
	public void parseInline(ArgScriptCommand command) {
		throw new UnsupportedOperationException("Inline volume effect is not supported.");
	}
	
	@Override
	public Effect[] getEffects() {
		//TODO this might be wrong!
		return new Effect[] { parent.getResource(MaterialResource.MASKED_TYPE, material) };
	}

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {
			"corner", "top", "bottom", "width"
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
			"sliceSpacing", "material", "set"
		};
	}

}
