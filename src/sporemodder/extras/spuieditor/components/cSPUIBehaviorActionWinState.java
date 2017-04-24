package sporemodder.extras.spuieditor.components;

import sporemodder.extras.spuieditor.SPUIViewer;
import sporemodder.files.formats.spui.InvalidBlockException;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIBuilder;
import sporemodder.files.formats.spui.SPUIObject;

public class cSPUIBehaviorActionWinState extends SPUIDefaultComponent {

	public static final int TYPE = 0x033F7AD6;
	
	public cSPUIBehaviorActionWinState(SPUIBlock block) throws InvalidBlockException {
		super(block);
		//24B5C98
		addUnassignedShort(block, 0x03335C12, null);
		addUnassignedShort(block, 0x03335C13, null);
		addUnassignedShort(block, 0x03335C14, null);
		addUnassignedShort(block, 0x03335C15, null);
		addUnassignedInt(block, 0x033F7F81, 0);
		addUnassignedInt(block, 0x033F7F8C, 0);
		addUnassignedInt(block, 0x033F7F89, 0);
		addUnassignedInt(block, 0x0341F297, 0);
	}
	
	public cSPUIBehaviorActionWinState(SPUIViewer viewer) {
		super(viewer);
		
		unassignedProperties.put(0x03335C12, null);
		unassignedProperties.put(0x03335C13, null);
		unassignedProperties.put(0x03335C14, null);
		unassignedProperties.put(0x03335C15, null);
		unassignedProperties.put(0x033F7F81, (int) 0);
		unassignedProperties.put(0x033F7F8C, (int) 0);
		unassignedProperties.put(0x033F7F89, (int) 0);
		unassignedProperties.put(0x0341F297, (int) 0);
	}
	
	@Override
	public SPUIObject saveComponent(SPUIBuilder builder) {
		
		SPUIBlock block = (SPUIBlock) super.saveComponent(builder);
				
		saveReference(builder, block, 0x03335C12);
		saveReference(builder, block, 0x03335C13);
		saveReference(builder, block, 0x03335C14);
		saveReference(builder, block, 0x03335C15);
		saveInt(builder, block, 0x033F7F81);
		saveInt(builder, block, 0x033F7F8C);
		saveInt(builder, block, 0x033F7F89);
		saveInt(builder, block, 0x0341F297);
		
		return block;
	}
	
	private cSPUIBehaviorActionWinState() {
		super();
	}
	
	@Override
	public SPUIComponent copyComponent(boolean propagate) {
		cSPUIBehaviorActionWinState other = new cSPUIBehaviorActionWinState();
		copyComponent(other, propagate);
		return other;
	}
	
}
