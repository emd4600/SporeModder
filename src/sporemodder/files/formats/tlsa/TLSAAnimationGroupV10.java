package sporemodder.files.formats.tlsa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class TLSAAnimationGroupV10 extends FileStructure implements TLSAAnimationGroup {
	public List<TLSAAnimationV10> anims;

	private int id;
	private String name;
	
	private float priorityOverride;
	private float blendInTime = -1.0f;
	private boolean idle, allowLocomotion;
	private int matchVariantForToolMask;
	private int disableToolOverlayMask;
	private int endMode;
	
	@Override
	public void read(InputStreamAccessor in) throws IOException, FileStructureException {
		id = in.readInt();
		
		name = in.readLEString16(in.readInt());
		
		priorityOverride = in.readFloat();  // priorityOverride ?
		blendInTime = in.readFloat();
		
		idle = in.readBoolean();
		allowLocomotion = in.readBoolean();
		expect(in.readByte(), 0, "TLSA-GR10-002", in.getFilePointer());  // boolean
		
		matchVariantForToolMask = in.readInt();
		disableToolOverlayMask = in.readInt();
		endMode = in.readInt();
		
		int numAnims = in.readInt();
		anims = new ArrayList<TLSAAnimationV10>(numAnims);
		
		for (int i = 0; i < numAnims; i++) {
			TLSAAnimationV10 anim = new TLSAAnimationV10();
			anim.read(in);
			anims.add(anim);
		}
		
	}
	
	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(id);
		
		out.writeInt(name.length());
		out.write(name.getBytes("UTF-16LE"));

		out.writeFloat(priorityOverride);
		out.writeFloat(blendInTime);
		
		out.writeBoolean(idle);
		out.writeBoolean(allowLocomotion);
		out.writeByte(0);
		
		out.writeLong(0);
		
		out.writeInt(endMode);

		out.writeInt(anims.size());
		
		for (TLSAAnimationV10 anim : anims) {
			anim.write(out);
		}
	}
	
	@Override
	public void parse(ArgScriptBlock block) throws IOException, ArgScriptException {
		if (!block.getKeyword().equals("group")) throw new ArgScriptException("Unexpected animation group formatting in keyword: " + block.getKeyword());
		
		List<String> args = block.getArguments(2);
		id = Hasher.getFileHash(args.get(0));
		name = args.get(1);
		
		ArgScriptOption oPriorityOverride = block.getOption("priorityOverride");
		if (oPriorityOverride != null) priorityOverride = Float.parseFloat(oPriorityOverride.getSingleArgument());
		
		ArgScriptOption oBlendInTime = block.getOption("blendInTime");
		if (oBlendInTime != null) blendInTime = Float.parseFloat(oBlendInTime.getSingleArgument());
		
		idle = block.hasFlag("idle");
		allowLocomotion = block.hasFlag("allowLocomotion");
		
		ArgScriptOption oDisableToolOverlayMask = block.getOption("disableToolOverlay");
		if (oDisableToolOverlayMask != null) disableToolOverlayMask = Hasher.decodeInt(oDisableToolOverlayMask.getSingleArgument());
		
		ArgScriptOption oMatchVariantForTool = block.getOption("matchVariantForTool");
		if (oMatchVariantForTool != null) matchVariantForToolMask = Hasher.decodeInt(oMatchVariantForTool.getSingleArgument());
		
		ArgScriptOption oEndMode = block.getOption("endMode");
		if (oEndMode != null) endMode = Hasher.decodeInt(oEndMode.getSingleArgument());
		
		List<TLSAAnimationV10> anims = new ArrayList<TLSAAnimationV10>();
		
		for (ArgScriptBlock subblock : block.getAllBlocks()) {
			TLSAAnimationV10 anim = new TLSAAnimationV10();
			anim.parse(subblock);
			anims.add(anim);
		}
	}
	
	@Override
	public ArgScriptBlock toBlock() throws IOException {
		ArgScriptBlock block = new ArgScriptBlock("group", Hasher.getFileName(id), name);
		
		if (priorityOverride != 1.0f) block.putOption(new ArgScriptOption("priorityOverride", Float.toString(priorityOverride)));
		if (blendInTime != -1.0f) block.putOption(new ArgScriptOption("blendInTime", Float.toString(blendInTime)));
		if (idle) block.putFlag("idle");
		if (allowLocomotion) block.putFlag("allowLocomotion");
		if (disableToolOverlayMask != 0) block.putOption(new ArgScriptOption("disableToolOverlay", Hasher.hashToHex(disableToolOverlayMask, "0x")));
		if (matchVariantForToolMask != 0) block.putOption(new ArgScriptOption("matchVariantForTool", Hasher.hashToHex(matchVariantForToolMask, "0x")));
		if (endMode != 0) block.putOption(new ArgScriptOption("endMode", Integer.toString(endMode)));
		
		for (TLSAAnimationV10 anim : anims) {
			block.putBlock(anim.toBlock());
		}
				
		return block;
	}
}
