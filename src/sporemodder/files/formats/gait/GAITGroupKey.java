package sporemodder.files.formats.gait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptException;

public class GAITGroupKey extends FileStructure {
	private int groupIndex = 0; // always 0?
	private float speedi;  // 0x1C4
	private float stepHeight;  // 0x120
	private int taxon;  // 0x124
	private List<Float> triggers = new ArrayList<Float>();
	private List<Float> dutyFactors = new ArrayList<Float>();
	private float stepGallop;
	private float stepPhaseBiasH;
	private float stepPhaseBiasV;
	private float stepSkew;
	private float footTilt;
	private float tiltPoint;
	private float toeCurlMax;
	private float toeCurlBegin;
	private float toeCurlEnd;
	private float swayAmplitude;
	private float swayPhase;
	private float trackWidthReduction;
	private float verticalPhaseOffset;
	private float sagittalPhaseOffset;
	private float lateralPhaseOffset;
	private float maxVerticalOffset;
	private float unk1;
	private float unk2;
	private float maxSagittalOffset;
	private float maxLateralOffset;
	private float yawPhaseOffset;
	private float pitchPhaseOffset;
	private float rollPhaseOffset;
	private float maxYawAngle;
	private float maxPitchAngle;
	private float maxRollAngle;
	
	private int numFeet;  // 0xCC
	private int numPaths;  // 0xC8
	private int numPathPosKeys;  // 0xC0
	private int numPathRotKeys;  // 0xBC
	private int numPathBndKeys;  // 0xB8
	private int tick;  // 0x04, 0x020, 0x78 ? 
	private float[] pos;  // 0x7C ?
	
	public void read(InputStreamAccessor in) throws IOException {
		System.out.println(in.getFilePointer());
		speedi = in.readLEFloat();
		stepHeight = in.readLEFloat();
		taxon = in.readLEInt();
		stepGallop = in.readLEFloat();
		stepPhaseBiasH = in.readLEFloat();
		stepPhaseBiasV = in.readLEFloat();
		stepSkew = in.readLEFloat();
		footTilt = in.readLEFloat();
		tiltPoint = in.readLEFloat();
		toeCurlMax = in.readLEFloat();
		toeCurlBegin = in.readLEFloat();
		toeCurlEnd = in.readLEFloat();
		swayAmplitude = in.readLEFloat();
		swayPhase = in.readLEFloat();
		trackWidthReduction = in.readLEFloat();
		verticalPhaseOffset = in.readLEFloat();
		sagittalPhaseOffset = in.readLEFloat();
		lateralPhaseOffset = in.readLEFloat();
		maxVerticalOffset = in.readLEFloat();
		unk1 = in.readLEFloat();
		unk2 = in.readLEFloat();
		maxSagittalOffset = in.readLEFloat();
		maxLateralOffset = in.readLEFloat();
		yawPhaseOffset = in.readLEFloat();
		pitchPhaseOffset = in.readLEFloat();
		rollPhaseOffset = in.readLEFloat();
		maxYawAngle = in.readLEFloat();
		maxPitchAngle = in.readLEFloat();
		maxRollAngle = in.readLEFloat();
		
		int count = in.readLEInt();
		expect(in.readLEInt(), count, "GAIT-007", in.getFilePointer());
		for (int f = 0; f < count; f++) {
			triggers.add(in.readLEFloat());
			dutyFactors.add(in.readLEFloat());
		}
//		System.out.println("\t" + in.getFilePointer());
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeLEFloat(speedi);
		out.writeLEFloat(stepHeight);
		out.writeLEInt(taxon);
		out.writeLEFloat(stepGallop);
		out.writeLEFloat(stepPhaseBiasH);
		out.writeLEFloat(stepPhaseBiasV);
		out.writeLEFloat(stepSkew);
		out.writeLEFloat(footTilt);
		out.writeLEFloat(tiltPoint);
		out.writeLEFloat(toeCurlMax);
		out.writeLEFloat(toeCurlBegin);
		out.writeLEFloat(toeCurlEnd);
		out.writeLEFloat(swayAmplitude);
		out.writeLEFloat(swayPhase);
		out.writeLEFloat(trackWidthReduction);
		out.writeLEFloat(verticalPhaseOffset);
		out.writeLEFloat(sagittalPhaseOffset);
		out.writeLEFloat(lateralPhaseOffset);
		out.writeLEFloat(maxVerticalOffset);
		out.writeLEFloat(unk1);
		out.writeLEFloat(unk2);
		out.writeLEFloat(maxSagittalOffset);
		out.writeLEFloat(maxLateralOffset);
		out.writeLEFloat(yawPhaseOffset);
		out.writeLEFloat(pitchPhaseOffset);
		out.writeLEFloat(rollPhaseOffset);
		out.writeLEFloat(maxYawAngle);
		out.writeLEFloat(maxPitchAngle);
		out.writeLEFloat(maxRollAngle);
		
		out.writeLEInt(triggers.size());
		out.writeLEInt(triggers.size());
		for (int i = 0; i < triggers.size(); i++) {
			out.writeLEFloat(triggers.get(i));
			out.writeLEFloat(dutyFactors.get(i));
		}
	}
	
	public ArgScriptBlock toBlock() {
		ArgScriptBlock block = new ArgScriptBlock("gaitgroupkey");
		
		block.putCommand(new ArgScriptCommand("groupindex", Integer.toString(groupIndex)));
		block.putCommand(new ArgScriptCommand("speedi", Float.toString(speedi)));
		block.putCommand(new ArgScriptCommand("stepheight", Float.toString(stepHeight)));
		
		ArgScriptBlock bTaxon = new ArgScriptBlock("taxon", Integer.toString(taxon));
		for (int i = 0; i < triggers.size(); i++) {
			bTaxon.putCommand(new ArgScriptCommand("trigger", Float.toString(triggers.get(i))));
			bTaxon.putCommand(new ArgScriptCommand("dutyfactor", Float.toString(dutyFactors.get(i))));
		}
		block.putBlock(bTaxon);
		
		block.putCommand(new ArgScriptCommand("stepgallop", Float.toString(stepGallop)));
		block.putCommand(new ArgScriptCommand("stepphasebiash", Float.toString(stepPhaseBiasH)));
		block.putCommand(new ArgScriptCommand("stepphasebiasv", Float.toString(stepPhaseBiasV)));
		block.putCommand(new ArgScriptCommand("stepskew", Float.toString(stepSkew)));
		block.putCommand(new ArgScriptCommand("foottilt", Float.toString(footTilt)));
		block.putCommand(new ArgScriptCommand("tiltpoint", Float.toString(tiltPoint)));
		block.putCommand(new ArgScriptCommand("toecurlmax", Float.toString(toeCurlMax)));
		block.putCommand(new ArgScriptCommand("toecurlbegin", Float.toString(toeCurlBegin)));
		block.putCommand(new ArgScriptCommand("toecurlend", Float.toString(toeCurlEnd)));
		block.putCommand(new ArgScriptCommand("swayamplitude", Float.toString(swayAmplitude)));
		block.putCommand(new ArgScriptCommand("swayphase", Float.toString(swayPhase)));
		block.putCommand(new ArgScriptCommand("trackWidthReduction", Float.toString(trackWidthReduction)));
		block.putCommand(new ArgScriptCommand("verticalphaseoffset", Float.toString(verticalPhaseOffset)));
		block.putCommand(new ArgScriptCommand("sagittalphaseoffset", Float.toString(sagittalPhaseOffset)));
		block.putCommand(new ArgScriptCommand("lateralphaseoffset", Float.toString(lateralPhaseOffset)));
		block.putCommand(new ArgScriptCommand("maxverticaloffset", Float.toString(maxVerticalOffset)));
		block.putCommand(new ArgScriptCommand("unk1", Float.toString(unk1)));
		block.putCommand(new ArgScriptCommand("unk2", Float.toString(unk2)));
		block.putCommand(new ArgScriptCommand("maxsagittaloffset", Float.toString(maxSagittalOffset)));
		block.putCommand(new ArgScriptCommand("maxlateraloffset", Float.toString(maxLateralOffset)));
		block.putCommand(new ArgScriptCommand("yawphaseoffset", Float.toString(yawPhaseOffset)));
		block.putCommand(new ArgScriptCommand("pitchphaseoffset", Float.toString(pitchPhaseOffset)));
		block.putCommand(new ArgScriptCommand("rollphaseoffset", Float.toString(rollPhaseOffset)));
		block.putCommand(new ArgScriptCommand("maxyawangle", Float.toString(maxYawAngle)));
		block.putCommand(new ArgScriptCommand("maxpitchangle", Float.toString(maxPitchAngle)));
		block.putCommand(new ArgScriptCommand("maxrollangle", Float.toString(maxRollAngle)));
		
		return block;
	}
	
	public void parse(ArgScriptBlock block) throws NumberFormatException, ArgScriptException {
		
		{ ArgScriptCommand c = block.getCommand("groupindex"); if (c != null) groupIndex = Integer.parseInt(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("speedi"); if (c != null) speedi = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("stepheight"); if (c != null) stepHeight = Float.parseFloat(c.getSingleArgument()); }
		
		{
			ArgScriptBlock b = block.getBlock("taxon");
			if (b != null) {
				taxon = Integer.parseInt(b.getSingleArgument());
				
				Collection<ArgScriptCommand> commands = b.getAllCommands();
				for (ArgScriptCommand c : commands) {
					if (c.getKeyword().equals("trigger")) triggers.add(Float.parseFloat(c.getSingleArgument()));
					if (c.getKeyword().equals("dutyfactor")) dutyFactors.add(Float.parseFloat(c.getSingleArgument()));
				}
			}
		}
		
		{ ArgScriptCommand c = block.getCommand("stepgallop"); if (c != null) stepGallop = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("stepphasebiash"); if (c != null) stepPhaseBiasH = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("stepphasebiasv"); if (c != null) stepPhaseBiasV = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("stepskew"); if (c != null) stepSkew = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("foottilt"); if (c != null) footTilt = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("tiltpoint"); if (c != null) tiltPoint = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("toecurlmax"); if (c != null) toeCurlMax = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("toecurlbegin"); if (c != null) toeCurlBegin = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("toecurlend"); if (c != null) toeCurlEnd = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("swayamplitude"); if (c != null) swayAmplitude = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("swayphase"); if (c != null) swayPhase = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("trackWidthReduction"); if (c != null) trackWidthReduction = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("verticalphaseoffset"); if (c != null) verticalPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("sagittalphaseoffset"); if (c != null) sagittalPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("lateralphaseoffset"); if (c != null) lateralPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxverticaloffset"); if (c != null) maxVerticalOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("unk1"); if (c != null) unk1 = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("unk2"); if (c != null) unk2 = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxsagittaloffset"); if (c != null) maxSagittalOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxlateraloffset"); if (c != null) maxLateralOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("yawphaseoffset"); if (c != null) yawPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("pitchphaseoffset"); if (c != null) pitchPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("rollphaseoffset"); if (c != null) rollPhaseOffset = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxyawangle"); if (c != null) maxYawAngle = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxpitchangle"); if (c != null) maxPitchAngle = Float.parseFloat(c.getSingleArgument()); }
		{ ArgScriptCommand c = block.getCommand("maxrollangle"); if (c != null) maxRollAngle = Float.parseFloat(c.getSingleArgument()); }
	}
}
