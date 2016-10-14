package sporemodder.files.formats.effects;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;

public class SkinpaintDistributeEffect extends EffectComponent {
	private static final class ParticleSelectPair {
		int index;
		float prob;
		
		private ParticleSelectPair(int index, float prob) {
			this.index = index;
			this.prob = prob;
		}
	}
	
	public static final int TYPE = 0x0023;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 2;
	public static final String KEYWORD = "SPSkinPaintDistribute";
	
	protected static final String[] BLOCK_KEYWORDS = new String[] {"particleSelect"};
	
	private static final int REGION_TORSO = 0x01;
	private static final int REGION_LIMBS = 0x02;
	private static final int REGION_PARTS = 0x04;
	private static final int REGION_JOINTS = 0x20;
	private static final int REGION_BACK = 0x08;  // this has a float
	private static final int REGION_BELLY = 0x10;  // this has a float
	
	private String field_8 = "";
	private int particleIndex = -1;
	private float spacing = 0.2f;
	private int distributeLimit = -1;
	private int region;
	private float regionBack;
	private float regionBelly;
	private final float[] regionBodyRange = new float[2];  // LE
	private boolean regionInverse;
	private boolean regionCenterOnly;
	private boolean cover;
	private boolean ordered;
	private ParticleSelectPair[] particleSelect = new ParticleSelectPair[0]; // it's an int[2] curve
	private boolean selectAll;
	
	public SkinpaintDistributeEffect(int type, int version) {
		super(type, version);
	}
	
	public SkinpaintDistributeEffect(SkinpaintDistributeEffect effect) {
		super(effect);
		field_8 = new String(effect.field_8);
		particleIndex = effect.particleIndex;
		spacing = effect.spacing;
		distributeLimit = effect.distributeLimit;
		region = effect.region;
		regionBack = effect.regionBack;
		regionBelly = effect.regionBelly;
		regionBodyRange[0] = effect.regionBodyRange[0];
		regionBodyRange[1] = effect.regionBodyRange[1];
		regionInverse = effect.regionInverse;
		regionCenterOnly = effect.regionCenterOnly;
		cover = effect.cover;
		ordered = effect.ordered;
		particleSelect = new ParticleSelectPair[effect.particleSelect.length];
		for (int i = 0; i < particleSelect.length; i++) {
			particleSelect[i] = new ParticleSelectPair(effect.particleSelect[i].index, effect.particleSelect[i].prob);
		}
		selectAll = effect.selectAll;
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException 
	{
		field_8 = in.readCString();
		
		particleIndex = in.readInt();
		spacing = in.readFloat();
		distributeLimit = in.readInt();
		region = in.readInt();
		regionBack = in.readFloat();
		regionBelly = in.readFloat();
		in.readLEFloats(regionBodyRange);
		regionInverse = in.readBoolean();
		regionCenterOnly = in.readBoolean();
		cover = in.readBoolean();
		ordered = in.readBoolean();
		
		particleSelect = new ParticleSelectPair[in.readInt()];
		for (int i = 0; i < particleSelect.length; i++) 
		{
			particleSelect[i] = new ParticleSelectPair(in.readInt(), in.readFloat());
		}
		
		selectAll = in.readBoolean();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException 
	{
		out.writeCString(field_8);
		
		out.writeInt(particleIndex);
		out.writeFloat(spacing);
		out.writeInt(distributeLimit);
		out.writeInt(region);
		out.writeFloat(regionBack);
		out.writeFloat(regionBelly);
		out.writeLEFloats(regionBodyRange);
		out.writeBoolean(regionInverse);
		out.writeBoolean(regionCenterOnly);
		out.writeBoolean(cover);
		out.writeBoolean(ordered);
		
		if (particleSelect != null) {
			out.writeInt(particleSelect.length);
			for (ParticleSelectPair i : particleSelect) {
				out.writeInt(i.index);
				out.writeFloat(i.prob);
			}
		}
		else {
			out.writeInt(0);
		}
		
		out.writeBoolean(selectAll);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException, ArgScriptException {
		{
			ArgScriptCommand c = block.getCommand("particle");
			if (c != null) {
				particleIndex = EffectMain.getEffectIndex(parent.getEffectMap(), SkinpaintParticleEffect.KEYWORD, c.getSingleArgument());
			}
		}
		{
			ArgScriptBlock b = block.getBlock(BLOCK_KEYWORDS[0]);
			if (b != null) {
				selectAll = b.hasFlag("all");
				//TODO if selectAll, all probs should be -1 ?
				Collection<ArgScriptCommand> commands = b.getAllCommands();
				particleSelect = new ParticleSelectPair[commands.size()];
				
				int i = 0;
				int negativeProbs = 0;
				float totalProbability = 0.0f;
				
				for (ArgScriptCommand c : commands) {
					String arg = c.getSingleArgument();
					
					particleSelect[i] = new ParticleSelectPair(
							EffectMain.getEffectIndex(parent.getEffectMap(), SkinpaintParticleEffect.KEYWORD, c.getKeyword()),
							Float.parseFloat(arg));
					
					if (particleSelect[i].prob < 0) {
						negativeProbs++;
					}
					i++;
				}
				// this distributes the remaining probability to the ones using negative probs
				for (ParticleSelectPair pair : particleSelect) {
					if (pair.prob < 0) {
						pair.prob = (1.0f - totalProbability) / (float) negativeProbs;
					}
				}
				
				if (totalProbability > 1.00010001659393310546875E0) {
					throw new ArgScriptException(KEYWORD + ": Total selection probability is greater than 1.0");
				}
			}
		}
		{
			ArgScriptCommand c = block.getCommand("spacing");
			if (c != null) {
				spacing = Float.parseFloat(c.getSingleArgument());
				cover = c.hasFlag("cover");
				ordered = c.hasFlag("ordered");
			}
		}
		{ ArgScriptCommand c = block.getCommand("limit"); if (c != null) distributeLimit = Integer.parseInt(c.getSingleArgument()); }
		{
			ArgScriptCommand c = block.getCommand("region");
			if (c != null) {
				if (c.hasFlag("torso")) region |= REGION_TORSO;
				if (c.hasFlag("limbs")) region |= REGION_LIMBS;
				if (c.hasFlag("parts")) region |= REGION_PARTS;
				if (c.hasFlag("joints")) region |= REGION_JOINTS;
				
				{ ArgScriptOption o = c.getOption("back"); if (o != null) regionBack = (float) Math.cos(Math.toRadians(Float.parseFloat(o.getSingleArgument()))); }
				{ ArgScriptOption o = c.getOption("belly"); if (o != null) regionBelly = (float) -Math.cos(Math.toRadians(Float.parseFloat(o.getSingleArgument()))); }
				{ 
					ArgScriptOption o = c.getOption("bodyRange");
					if (o != null) {
						List<String> args = o.getArguments(2);
						regionBodyRange[0] = Float.parseFloat(args.get(0));
						regionBodyRange[1] = Float.parseFloat(args.get(1));
					}
					else {
						regionBodyRange[0] = 0.0f;
						regionBodyRange[1] = 1.0f;
					}
				}
				regionInverse = c.hasFlag("inverse");
				regionCenterOnly = c.hasFlag("centerOnly");
			}
		}
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) 
	{
		if (particleIndex != -1)
		{
			block.putCommand(new ArgScriptCommand("particle", "SPSkinPaintParticle-" + particleIndex));
		}
		
		if (particleSelect.length > 0) {
			ArgScriptBlock bParticleSelect = new ArgScriptBlock(BLOCK_KEYWORDS[0]);
			if (selectAll) bParticleSelect.putFlag("all");
			for (ParticleSelectPair select : particleSelect) {
				String name = "SPSkinPaintParticle-" + select.index;
				bParticleSelect.putCommand(name, new ArgScriptCommand(name, Float.toString(select.prob)));
			}
			block.putBlock(bParticleSelect);
		}
		
		ArgScriptCommand cSpacing = new ArgScriptCommand("spacing", Float.toString(spacing));
		if (cover) cSpacing.putFlag("cover");
		if (ordered) cSpacing.putFlag("ordered");
		block.putCommand(cSpacing);
		
		if (distributeLimit != -1) block.putCommand("limit", new ArgScriptCommand("limit", Integer.toString(distributeLimit)));
		
		if (region != 0) {
			ArgScriptCommand cRegion = new ArgScriptCommand("region");
			if ((region & REGION_TORSO) == REGION_TORSO) cRegion.putFlag("torso");
			if ((region & REGION_LIMBS) == REGION_LIMBS) cRegion.putFlag("limbs");
			if ((region & REGION_PARTS) == REGION_PARTS) cRegion.putFlag("parts");
			if ((region & REGION_JOINTS) == REGION_JOINTS) cRegion.putFlag("joints");
			if ((region & REGION_BACK) == REGION_BACK) {
//				System.out.println("belly");
//				cRegion.putOption(new ArgScriptOption("back", Float.toString((float) (Math.toDegrees(Math.acos(regionBack))))));  // radians or degrees?
				cRegion.putOption(new ArgScriptOption("back", Float.toString((float) (Math.acos(regionBack) / (Math.PI / 2)))));  // radians or degrees?
//				cRegion.putOption(new ArgScriptOption("back", Float.toString(regionBack)));  // radians or degrees?
			}
			if ((region & REGION_BELLY) != 0) {
//				cRegion.putOption(new ArgScriptOption("belly", Float.toString((float) (Math.toDegrees(Math.acos(-regionBelly))))));  // radians or degrees?
				cRegion.putOption(new ArgScriptOption("belly", Float.toString((float) (Math.acos(-regionBelly) / (Math.PI / 2)))));  // radians or degrees?
			}
			
			if (regionBodyRange[0] != 0 || regionBodyRange[1] != 1) {
				cRegion.putOption(new ArgScriptOption("bodyRange", Float.toString(regionBodyRange[0]), Float.toString(regionBodyRange[1])));
			}
			
			if (regionInverse) cRegion.putFlag("inverse");
			if (regionCenterOnly) cRegion.putFlag("centerOnly");
			
			block.putCommand(cRegion);
		}
		
		return true;
	}
	
	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		distributeLimit = -1;
		region = -1;
		spacing = 0.2f;
		ordered = false;
		regionCenterOnly = false;
		cover = false;
		regionInverse = false;
		
		{
			ArgScriptOption o = command.getOption("effect");
			if (o == null || o.getArgumentCount() != 1) {
				throw new ArgScriptException(KEYWORD + ": Must specify block name or use inline -effect ... syntax.");
			}
			
			particleIndex = EffectMain.getEffectIndex(parent.getEffectMap(), SkinpaintParticleEffect.KEYWORD, o.getSingleArgument());
		}
		
		{ ArgScriptOption o = command.getOption("spacing"); if (o != null) spacing = Float.parseFloat(o.getSingleArgument()); }
		{ ArgScriptOption o = command.getOption("limit"); if (o != null) distributeLimit = Integer.parseInt(o.getSingleArgument()); }
		cover = command.hasFlag("cover");
		ordered = command.hasFlag("ordered");
	}
	
	@Override
	public Effect[] getEffects() {
		Effect[] effects;
		int arrayOffset = 0;
		if (particleIndex == -1) {
			effects = new Effect[particleSelect.length];
			arrayOffset = 0;
		}
		else {
			effects = new Effect[particleSelect.length + 1];
			effects[0] = EffectMain.getEffect(parent.getEffectMap(), SkinpaintParticleEffect.TYPE, particleIndex);
			arrayOffset = 1;
		}
		
		for (int i = 0; i < particleSelect.length; i++) {
			effects[i + arrayOffset] = EffectMain.getEffect(parent.getEffectMap(), SkinpaintParticleEffect.TYPE, particleSelect[i].index);
		}
		
		return effects;
	}
	
	@Override
	public void fixEffectIndices(TreeMap<Integer, Integer> baseIndices) {
		Integer index = baseIndices.get(SkinpaintParticleEffect.TYPE);
		if (index != null && index != -1) {
			if (particleIndex != -1) {
				particleIndex += index;
			}
			if (particleSelect != null) {
				for (ParticleSelectPair pair : particleSelect) {
					pair.index += index;
				}
			}
		}
	}
	
	

	// For Syntax Highlighting
	public static String[] getEnumTags() {
		return new String[] {};
	}
	
	public static String[] getBlockTags() {
		return new String[] {
			KEYWORD, "particleSelect"
		};
	}
	
	public static String[] getOptionTags() {
		return new String[] {
			"all", "cover", "ordered", "torso", "limbs", "parts", "joints", "back", "belly", "bodyRange", "inverse", "centerOnly"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"particle", "spacing", "limit", "region"
		};
	}
}
