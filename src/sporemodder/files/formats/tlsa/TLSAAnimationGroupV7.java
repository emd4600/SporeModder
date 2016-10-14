package sporemodder.files.formats.tlsa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.utilities.Hasher;

public class TLSAAnimationGroupV7 extends FileStructure implements TLSAAnimationGroup {
	private List<TLSAAnimationV7> anims = new ArrayList<TLSAAnimationV7>();
	private int id;
	//Params
	private float unk1 = 1.0f;  // priority ?
	private float unk2 = -1.0f;  // duration ?
	private boolean idle;  // Idle ?  // 0x1C  // 0x28
	private float blendInTime = -1.0f;  // BlendInTime ?  // 0x18  // 0x24
	private boolean allowLocomotion;  // AllowLocomotion ?  // 0x1D // 0x29
	private int disableToolOverlayMask;  // DisableToolOverlayMask ?  // 0x20  // 0x2C
	private int matchVariantForToolMask;  // MatchVariantForToolMask ?  // 0x24  // 0x30
	private int endMode;  // EndMode ?  // 0x28  // 0x34
	
	public List<TLSAAnimationV7> animsList;
	
	@Override
	public void read(InputStreamAccessor in) throws IOException, FileStructureException {
		id = in.readInt();
		int numAnims = in.readInt();
		
		for (int i = 0; i < numAnims; i++) {
			TLSAAnimationV7 anim = new TLSAAnimationV7();
			anim.description = in.readLEString16(in.readInt());
			anims.add(anim);
		}
		for (int i = 0; i < numAnims; i++) {
			TLSAAnimationV7 anim = anims.get(i);
			anim.id = in.readInt();
		}
		
		unk1 = in.readFloat();
		unk2 = in.readFloat();
		idle = in.readBoolean();
		blendInTime = in.readFloat();
		allowLocomotion = in.readBoolean();
		disableToolOverlayMask = in.readInt();
		matchVariantForToolMask = in.readInt();
		endMode = in.readInt();
	}
	
	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(id);
		out.writeInt(anims.size());
		
		for (TLSAAnimationV7 anim : anims) {
			out.writeInt(anim.description.length());
			out.writeLEString16(anim.description);
		}
		
		for (TLSAAnimationV7 anim : anims) {
			out.writeInt(anim.id);
		}
		
		out.writeFloat(unk1);
		out.writeFloat(unk2);
		out.writeBoolean(idle);
		out.writeFloat(blendInTime);
		out.writeBoolean(allowLocomotion);
		out.writeInt(disableToolOverlayMask); out.writeInt(matchVariantForToolMask);
		out.writeInt(endMode);
	}
	
	@Override
	public void parse(ArgScriptBlock block) throws IOException,
			ArgScriptException {

		if (!block.getKeyword().equals("group")) throw new ArgScriptException("Unexpected animation group formatting in keyword: " + block.getKeyword());
		
		id = Hasher.getFileHash(block.getSingleArgument());
		
		ArgScriptOption oUnk1 = block.getOption("unk1");
		if (oUnk1 != null) unk1 = Float.parseFloat(oUnk1.getSingleArgument());
		
		ArgScriptOption oUnk2 = block.getOption("unk2");
		if (oUnk2 != null) unk2 = Float.parseFloat(oUnk2.getSingleArgument());
		
		idle = block.hasFlag("idle");
		allowLocomotion = block.hasFlag("allowLocomotion");
		
		ArgScriptOption oBlendInTime = block.getOption("blendInTime");
		if (oBlendInTime != null) blendInTime = Float.parseFloat(oBlendInTime.getSingleArgument());
		
		ArgScriptOption oDisableToolOverlayMask = block.getOption("disableToolOverlay");
		if (oDisableToolOverlayMask != null) disableToolOverlayMask = Hasher.decodeInt(oDisableToolOverlayMask.getSingleArgument());
		
		ArgScriptOption oMatchVariantForTool = block.getOption("matchVariantForTool");
		if (oMatchVariantForTool != null) matchVariantForToolMask = Hasher.decodeInt(oMatchVariantForTool.getSingleArgument());
		
		ArgScriptOption oEndMode = block.getOption("endMode");
		if (oEndMode != null) endMode = Hasher.decodeInt(oEndMode.getSingleArgument());
		
		
		for (ArgScriptCommand command : block.getAllCommands()) {
			TLSAAnimationV7 anim = new TLSAAnimationV7();
			anim.parse(command);
			anims.add(anim);
		}
	}
	@Override
	public ArgScriptBlock toBlock() throws IOException {
		
//		long time1 = System.nanoTime();
		ArgScriptBlock block = new ArgScriptBlock("group", Hasher.getFileName(id));
		
		if (unk1 != 1.0f) block.putOption(new ArgScriptOption("unk1", Float.toString(unk1)));
		if (unk2 != -1.0f) block.putOption(new ArgScriptOption("unk2", Float.toString(unk2)));
		if (idle) block.putFlag("idle");
		if (allowLocomotion) block.putFlag("allowLocomotion");
		if (blendInTime != -1.0f) block.putOption(new ArgScriptOption("blendInTime", Float.toString(blendInTime)));
		if (disableToolOverlayMask != 0) block.putOption(new ArgScriptOption("disableToolOverlay", Hasher.hashToHex(disableToolOverlayMask, "0x")));
		if (matchVariantForToolMask != 0) block.putOption(new ArgScriptOption("matchVariantForTool", Hasher.hashToHex(matchVariantForToolMask, "0x")));
		if (endMode != 0) block.putOption(new ArgScriptOption("endMode", Integer.toString(endMode)));
		
//		long time1End = System.nanoTime() - time1;
//		
//		long time2 = System.nanoTime();
//		
		for (TLSAAnimationV7 anim : anims) {
			block.putCommand(anim.toCommand());
		}
		
//		long time2End = System.nanoTime() - time2;
//		
//		System.out.println(time1End + "\t" + time2End);
		
		return block;
	}
}
