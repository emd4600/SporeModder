package sporemodder.files.formats.lvl;

import java.io.IOException;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileStructure;

public class CreatureArchetype extends FileStructure implements GameplayMarker{
	protected static final int HASH = 0x91FE517B;
	
	private enum NestTypeEnum {SANDY, GRASSY, ROCKY};
	private enum PersonalityEnum {NONE, EPIC_PREDATOR, MIGRATOR, DECORATOR, MONKEY, STALKER, GUARD, PET, WATER_PREDATOR, CARCASS};
	
	private int unk = 10; // ?
	private NestTypeEnum nestType = NestTypeEnum.GRASSY;
	private int herdSizeOverride; // 0 -> archetype
	private PersonalityEnum personality = PersonalityEnum.NONE;
	private boolean withoutNest;
	private float scaleMultiplier;
	private float hitpointOverride;
	private float damageMultiplier;
	private float territoryRadius;
	private int activateAtBrainLevel;
	private int deactivateAboveBrainLevel = 5;
	
	public void read(InputStreamAccessor in) throws IOException {
		expect(in.readInt(), 0, "LVL-CA-1", in.getFilePointer());
		
		unk = in.readLEInt();
		nestType = NestTypeEnum.values()[in.readLEInt()];
		herdSizeOverride = in.readLEInt();
		personality = PersonalityEnum.values()[in.readLEInt()];
		in.skipBytes(3);
		withoutNest = in.readBoolean();
		scaleMultiplier = in.readLEFloat();
		hitpointOverride = in.readLEFloat();
		damageMultiplier = in.readLEFloat();
		territoryRadius = in.readLEFloat();
		activateAtBrainLevel = in.readLEInt();
		deactivateAboveBrainLevel = in.readLEInt();
		
		for (int i = 0; i < 22; i++) expect(in.readInt(), 0, "LVL-CA-0", in.getFilePointer());
	}
	
	public void print() {
		System.out.println("unk: " + unk);
		System.out.println("nestType: " + nestType.toString());
		System.out.println("herdSizeOverride: " + herdSizeOverride);
		System.out.println("personality: " + personality.toString());
		System.out.println("withoutNest: " + withoutNest);
		System.out.println("scaleMultiplier: " + scaleMultiplier);
		System.out.println("hitpointOverride: " + hitpointOverride);
		System.out.println("damageMultiplier: " + damageMultiplier);
		System.out.println("territoryRadius: " + territoryRadius);
		System.out.println("activateAtBrainLevel: " + activateAtBrainLevel);
		System.out.println("deactivateAboveBrainLevel: " + deactivateAboveBrainLevel);
	}

	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		out.writeInt(0);
		out.writeLEInt(unk);
		out.writeLEInt(nestType.ordinal());
		out.writeLEInt(herdSizeOverride);
		out.writeLEInt(personality.ordinal());
		out.writePadding(3);
		out.writeBoolean(withoutNest);
		out.writeLEFloat(scaleMultiplier);
		out.writeLEFloat(hitpointOverride);
		out.writeLEFloat(damageMultiplier);
		out.writeLEFloat(territoryRadius);
		out.writeLEInt(activateAtBrainLevel);
		out.writeLEInt(deactivateAboveBrainLevel);
		out.writePadding(88);
	}
}
