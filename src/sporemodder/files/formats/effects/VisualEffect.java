package sporemodder.files.formats.effects;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class VisualEffect extends FileStructure implements Effect {
	
	public static final String KEYWORD = "effect";
	public static final int TYPE = 0;
	
	protected int position;  // just for debugging
	
	private int flags;  // 0x40: rigid  0x2000: realTime  0x4: cameraAttached ?
	private int componentAppFlagsMask;
	private int notifyMessageID;
	private final float[] screenSizeRange = new float[2];
	private float cursorActiveDistance;
	private int cursorButton; //byte
	private float[] lodDistances = new float[0];
	private final float[] extendedLodWeights = new float[3];
	private int seed;
	private List<VisualEffectBlock> blocks = new ArrayList<VisualEffectBlock>();
	
	// should this be handled somewhere else?
	private String name;
	private boolean isExported;
	
//	private TreeMap<Integer, List<Effect>> effectMap;
	private EffectMain parent;
	
	public VisualEffect() {};
	
	public VisualEffect(String name) {
		this.name = name;
	}
	
	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		componentAppFlagsMask = in.readInt();
		notifyMessageID = in.readInt();
		
		screenSizeRange[0] = in.readLEFloat();
		screenSizeRange[1] = in.readLEFloat();
		
		cursorActiveDistance = in.readFloat();
		cursorButton = in.readByte();
		
		int ldCount = in.readInt();
		lodDistances = new float[ldCount];
		for (int i = 0; i < ldCount; i++) {
			lodDistances[i] = in.readFloat();
		}
		
		extendedLodWeights[0] = in.readLEFloat();
		extendedLodWeights[1] = in.readLEFloat();
		extendedLodWeights[2] = in.readLEFloat();
		
		seed = in.readInt();
		
		int blockCount = in.readInt();
		for (int i = 0; i < blockCount; i++) {
			VisualEffectBlock block = new VisualEffectBlock();
			block.read(in);
			blocks.add(block);
		}
		
		return true;
	}
	
	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		out.writeInt(componentAppFlagsMask);
		out.writeInt(notifyMessageID);
		
		out.writeLEFloat(screenSizeRange[0]);
		out.writeLEFloat(screenSizeRange[1]);
		
		out.writeFloat(cursorActiveDistance);
		out.writeByte(cursorButton);
		
		if (lodDistances != null) {
			out.writeInt(lodDistances.length);
			for (float f : lodDistances) {
				out.writeFloat(f);
			}
		} else {
			out.writeInt(0);
		}
		
		out.writeLEFloat(extendedLodWeights[0]);
		out.writeLEFloat(extendedLodWeights[1]);
		out.writeLEFloat(extendedLodWeights[2]);
		
		out.writeInt(seed);
		
		out.writeInt(blocks.size());
		for (VisualEffectBlock block : blocks) {
			block.write(out);
		}
		
		return true;
	}
	
	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {
		ArgScriptOption o = null;
		
		System.out.println("Now parsing effect " + block.getArguments().get(0));
		if (block.getArguments().get(0).equals("#0429BB3D"))
		{
			System.out.println("BREAKPOINT");
		}
		
		{ o = block.getOption("flags"); if (o != null) flags = Hasher.decodeInt(o.getSingleArgument()); }
		{ o = block.getOption("notifyMessageID"); if (o != null) notifyMessageID = Hasher.decodeInt(o.getSingleArgument()); }
		
		o = block.getOption("screenSizeRange");
		if (o != null) {
			List<String> args = o.getArguments(2);
			screenSizeRange[0] = Float.parseFloat(args.get(0));
			screenSizeRange[1] = Float.parseFloat(args.get(1));
		}
		
		{ o = block.getOption("cursorActiveDistance"); if (o != null) cursorActiveDistance = Float.parseFloat(o.getSingleArgument()); }
		{ o = block.getOption("cursorButton"); if (o != null) cursorButton = Integer.parseInt(o.getSingleArgument()); }
		
		{ o = block.getOption("lodDistances"); if (o != null) lodDistances = ArgScript.stringsToFloats(o.getArguments()); }
		
		o = block.getOption("extendedLodWeights");
		if (o != null) {
			ArgScript.parseFloatList(o.getSingleArgument(), extendedLodWeights);
		}
		{ o = block.getOption("seed"); if (o != null) seed = Integer.parseInt(o.getSingleArgument()); }
		
		Collection<ArgScriptBlock> allBlocks = block.getAllBlocks();
		int selectionGroup = 1;
		for (ArgScriptBlock b : allBlocks) {
			if (b.getKeyword().equals("select")) {
				Collection<ArgScriptCommand> commands = b.getAllCommands();
				
				int firstBlock = blocks.size();
				int blockCount = 0;
				
				for (ArgScriptCommand c : commands) {
					VisualEffectBlock effectBlock = new VisualEffectBlock();
//					effectBlock.setEffectMap(effectMap);
					effectBlock.setParent(parent);
					effectBlock.parse(c);
					effectBlock.setSelectionGroup(selectionGroup);
					blocks.add(effectBlock);
					blockCount++;
				}
				
				int prob = 65535;
				int specifiedBlocks = 0;
				
				// automatically assign probability to those who don't specify it
				for (int i = 0; i < blockCount; i++) {
					VisualEffectBlock effectBlock = blocks.get(firstBlock + i);
					if (effectBlock.getSelectionChanceAbs() != 0) {
						prob -= blocks.get(i).getSelectionChanceAbs();
						specifiedBlocks++;
					}
				}
				if (blockCount - specifiedBlocks > 0) {
					int dif = prob / (blockCount - specifiedBlocks);
					for (int i = 0; i < blockCount; i++) {
						VisualEffectBlock effectBlock = blocks.get(firstBlock + i);
						if (effectBlock.getSelectionChanceAbs() == 0) {
							effectBlock.setSelectionChanceAbs(i+1 == blockCount ? prob : dif);
							prob -= dif;
						}
					}
				}
				
				selectionGroup++;
			}
		}
		
		
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand c : commands) {
			VisualEffectBlock effectBlock = new VisualEffectBlock();
//			effectBlock.setEffectMap(effectMap);
			effectBlock.setParent(parent);
			effectBlock.parse(c);
			blocks.add(effectBlock);
		}
		
		// calculate appFlagsMask
		componentAppFlagsMask = 0;
		for (VisualEffectBlock b : blocks) {
			componentAppFlagsMask |= b.getAppFlagsMask();
		}
		
		{ o = block.getOption("componentAppFlagsMask"); if (o != null) componentAppFlagsMask = Hasher.decodeInt(o.getSingleArgument()); }
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock b) {
		
		if (flags != 0) b.putOption(new ArgScriptOption("flags", "0x" + Integer.toHexString(flags)));
		int appFlagsMask = 0;
		for (VisualEffectBlock block : blocks) {
			appFlagsMask |= block.getAppFlagsMask();
		}
		if (componentAppFlagsMask != appFlagsMask) b.putOption(new ArgScriptOption("componentAppFlagsMask", "0x" + Integer.toHexString(componentAppFlagsMask)));
		if (notifyMessageID != 0) b.putOption(new ArgScriptOption("notifyMessageID", "0x" + Integer.toHexString(notifyMessageID)));
		
		if (screenSizeRange[0] != 0 || screenSizeRange[1] != 0) {
			b.putOption(new ArgScriptOption("screenSizeRange", Float.toString(screenSizeRange[0]), Float.toString(screenSizeRange[1])));
		}
		
		if (cursorActiveDistance != 0) b.putOption(new ArgScriptOption("cursorActiveDistance", Float.toString(cursorActiveDistance)));
		if (cursorButton != 0) b.putOption(new ArgScriptOption("cursorButton", Integer.toString(cursorButton)));
		
		if (lodDistances.length > 0) b.putOption(new ArgScriptOption("lodDistances", ArgScript.floatsToStrings(lodDistances)));
		if (extendedLodWeights[0] != 0 || extendedLodWeights[1] != 0 || extendedLodWeights[2] != 0) {
			b.putOption(new ArgScriptOption("extendedLodWeights", ArgScript.createFloatList(extendedLodWeights)));
		}
		
		if (seed != 0) b.putOption(new ArgScriptOption("seed", Integer.toString(seed)));
		
		HashMap<Integer, ArgScriptBlock> selectBlocks = new HashMap<Integer, ArgScriptBlock>();
		for (VisualEffectBlock effectBlock : blocks) {
			if (effectBlock.getSelectionGroup() != 0) {
				ArgScriptBlock selectBlock = selectBlocks.get(effectBlock.getSelectionGroup());
				if (selectBlock == null) {
					selectBlock = new ArgScriptBlock("select");
					selectBlocks.put(effectBlock.getSelectionGroup(), selectBlock);
				}
				selectBlock.putCommand(effectBlock.toCommand());
			}
			else {
				b.putCommand(effectBlock.toCommand());
			}
		}
		// does the order matter? Because if it does, these should go in the previous loop
		if (selectBlocks.entrySet().size() > 0) b.addBlankLine();
		for (ArgScriptBlock selectBlock : selectBlocks.values()) {
			b.putBlock(selectBlock);
		}
		
		return true;
	}
	
	@Override
	public ArgScriptBlock toBlock() {
		ArgScriptBlock b = new ArgScriptBlock(KEYWORD, name);
		
		toBlock(b);
		
		return b;
	}
	
//	@Override
//	public TreeMap<Integer, List<Effect>> getEffectMap() {
//		return effectMap;
//	}
//	
//	@Override
//	public void setEffectMap(TreeMap<Integer, List<Effect>> effects) {
//		effectMap = effects;
//		for (VisualEffectBlock block : blocks) {
//			block.setEffectMap(effectMap);
//			block.setEffect(effects);
//		}
//	}
	
	@Override
	public Effect[] getEffects() {
		TreeMap<Integer, List<Effect>> effectMap = parent.getEffectMap();
		if (effectMap == null) return null;
		Effect[] effects = new Effect[blocks.size()];
		
		for (int i = 0; i < effects.length; i++) {
			effects[i] = blocks.get(i).getEffect();
		}
		
		return effects;
	}
	
//	public VisualEffectBlock[] getEffectBlocks() {
//		return blocks;
//	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isExported() {
		return isExported;
	}
	
	public void setIsExported(boolean isExported) {
		this.isExported = isExported;
	}
	
	@Override
	public ArgScriptCommand toCommand() {
		throw new UnsupportedOperationException("VisualEffect doesn't support inline mode.");
	}

	@Override
	public boolean supportsBlock() {
		return true;
	}
	
	@Override
	public int getType() {
		return TYPE;
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		for (VisualEffectBlock b : blocks) {
			b.fixEffectIndices(baseIndices);
		}
	}
	
	
	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD, "export", "import"
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"flags", "componentAppFlagsMask", "notifyMessageID",
			"screenSizeRange", "cursorActiveDistance", "cursorButton",
			"lodDistances", "extendedLodWeights", "seed"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {"select"};
	}

	
	public void write(BufferedWriter out, int level) throws IOException {
		return;
	}

	@Override
	public void setParent(EffectMain parent) {
		this.parent = parent;
		for (VisualEffectBlock block : blocks) {
			block.setParent(parent);
//			block.setEffectMap(effectMap);
			block.setEffect(parent.getEffectMap());
		}
	}
}
