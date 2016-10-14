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
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class ModelEffect extends EffectComponent {
	
	public static final int TYPE = 0x08;
	public static final int MIN_VERSION = 1;
	public static final int MAX_VERSION = 1;
	public static final String KEYWORD = "model";
	
	private int flags;
	
	private float size = 1;
	private EffectColor color = EffectColor.WHITE;
	private float alpha = 1;
	
	private final ResourceID resourceId = new ResourceID();
	private final ResourceID materialId = new ResourceID();
	
	private int overrideSet; //byte
	
	private List<ModelAnim> animCurves = new ArrayList<ModelAnim>();

	public ModelEffect(ModelEffect effect) {
		super(effect);
		
		flags = effect.flags;
		size = effect.size;
		color = new EffectColor(effect.color);
		resourceId.copy(effect.resourceId);
		materialId.copy(effect.materialId);
		overrideSet = effect.overrideSet;
		
		if (effect.animCurves != null) {
			for (int i = 0; i < effect.animCurves.size(); i++) {
				animCurves.add(new ModelAnim(effect.animCurves.get(i)));
			}
		}
	}
	
	public ModelEffect(int type, int version) {
		super(type, version);
	}

	@Override
	public boolean read(InputStreamAccessor in) throws IOException {
		flags = in.readInt();
		
		resourceId.read(in);
		
		size = in.readFloat();
		color.readLE(in);
		alpha = in.readFloat();
		
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			ModelAnim animCurve = new ModelAnim();
			animCurve.read(in);
			animCurves.add(animCurve);
		}
		
		materialId.read(in);
		
		overrideSet = in.readByte();
		
		return true;
	}

	@Override
	public boolean write(OutputStreamAccessor out) throws IOException {
		out.writeInt(flags);
		
		resourceId.write(out);
		
		out.writeFloat(size);
		color.writeLE(out);
		out.writeFloat(alpha);
		
		out.writeInt(animCurves.size());
		for (ModelAnim w : animCurves) {
			w.write(out);
		}
		
		materialId.write(out);
		
		out.writeByte(overrideSet);
		
		return true;
	}

	@Override
	public boolean parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		{ ArgScriptCommand c = block.getCommand("resource"); if (c != null) resourceId.parse(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("name"); if (c != null) resourceId.parse(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("size"); if (c != null) size = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("color"); if (c != null) color.parse(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("alpha"); if (c != null) alpha = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("material"); if (c != null) materialId.parse(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("overrideSet"); if (c != null) overrideSet = Hasher.decodeByte(c.getSingleArgument()); }
		
		Collection<ArgScriptCommand> commands = block.getAllCommands();
		for (ArgScriptCommand c : commands) {
			if (c.getKeyword().equals("animate")) {
				ModelAnim animCurve = new ModelAnim();
				animCurve.parse(c);
				animCurves.add(animCurve);
			}
		}
		
		return true;
	}

	@Override
	public boolean toBlock(ArgScriptBlock block) {
		// TODO Auto-generated method stub
		if (resourceId != null) block.putCommand(new ArgScriptCommand("name", resourceId.toString()));
		if (size != 1.0f) block.putCommand(new ArgScriptCommand("size", Float.toString(size)));
		if (!color.isWhite()) block.putCommand(new ArgScriptCommand("color", color.toString()));
		if (alpha != 1.0f) block.putCommand(new ArgScriptCommand("alpha", Float.toString(alpha)));
		if (materialId != null && !materialId.isDefault()) block.putCommand(new ArgScriptCommand("material", materialId.toString()));
		if (overrideSet != 0) block.putCommand(new ArgScriptCommand("overrideSet", Integer.toString(overrideSet)));
		
		if (animCurves.size() > 0) {
			block.addBlankLine();
		}
		for (ModelAnim anim : animCurves) {
			block.putCommand(anim.toCommand());
		}
		
		return false;
	}

	@Override
	public void parseInline(ArgScriptCommand command) throws ArgScriptException {
		// TODO Auto-generated method stub
		{ ArgScriptOption option = command.getOption("name"); if (option != null) resourceId.parse(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("size"); if (option != null) size = Float.parseFloat(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("color"); if (option != null) color.parse(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("alpha"); if (option != null) alpha = Float.parseFloat(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("material"); if (option != null) materialId.parse(option.getSingleArgument()); }
		{ ArgScriptOption option = command.getOption("overrideSet"); if (option != null) overrideSet = Hasher.decodeByte(option.getSingleArgument()); }
	}
	
	@Override
	public Effect[] getEffects() {
		return new Effect[] { parent.getResource(MaterialResource.MASKED_TYPE, materialId) };
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
			"name", "size", "color", "alpha", "material", "overrideSet"
		};
	}
	
	public static String[] getCommandTags() {
		return new String[] {
			"name", "size", "color", "alpha", "material", "overrideSet", "resource"
		};
	}
}
