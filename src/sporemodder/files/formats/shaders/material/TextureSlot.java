package sporemodder.files.formats.shaders.material;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DSamplerStateType;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DTextureStageStateType;


public class TextureSlot {

	private int slotIndex;
	private int stageStatesUnknown;
	private int samplerStatesUnknown;
	private final List<D3DTextureStageStateType> stageStates = new ArrayList<D3DTextureStageStateType>();
	private final List<D3DSamplerStateType> samplerStates = new ArrayList<D3DSamplerStateType>();
	
	public void read(InputStreamAccessor in) throws IOException {
		
		slotIndex = in.readLEInt();
		
		
		in.readLEInt();  // texture section index
		
		stageStatesUnknown = in.readLEInt();
		if (stageStatesUnknown != 0) {
			
			int tssID = in.readLEInt();
			while (tssID != -1) {
				D3DTextureStageStateType state = D3DTextureStageStateType.getById(tssID);
				state.value = in.readLEInt();
				stageStates.add(state);
				tssID = in.readLEInt();
			}
		}
		
		samplerStatesUnknown = in.readLEInt();
		if (samplerStatesUnknown != 0) {
			
			int ssID = in.readLEInt();
			while (ssID != -1) {
				D3DSamplerStateType state = D3DSamplerStateType.getById(ssID);
				state.value = in.readLEInt();
				samplerStates.add(state);
				ssID = in.readLEInt();
			}
		}
	}
	
	public ArgScriptBlock toBlock() {
		ArgScriptBlock block = new ArgScriptBlock("textureSlot", Integer.toString(slotIndex));
		
		if (stageStatesUnknown != 0) {
			block.putOption(new ArgScriptOption("stageStates", Integer.toString(stageStatesUnknown)));
		}
		if (samplerStatesUnknown != 0) {
			block.putOption(new ArgScriptOption("samplerStates", Integer.toString(samplerStatesUnknown)));
		}
		
		for (D3DTextureStageStateType state : stageStates) {
			block.putCommand(new ArgScriptCommand("stageState", state.toString(), state.getValueToString()));
		}
		
		for (D3DSamplerStateType state : samplerStates) {
			block.putCommand(new ArgScriptCommand("samplerState", state.toString(), state.getValueToString()));
		}
		
		return block;
	}
}
