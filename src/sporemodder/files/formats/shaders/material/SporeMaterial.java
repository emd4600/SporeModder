package sporemodder.files.formats.shaders.material;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sporemodder.MainApp;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.formats.argscript.ArgScript;
import sporemodder.files.formats.argscript.ArgScriptBlock;
import sporemodder.files.formats.argscript.ArgScriptCommand;
import sporemodder.files.formats.argscript.ArgScriptOption;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DPRIMITIVETYPE;
import sporemodder.files.formats.renderWare4.D3DStateTypes.D3DRenderStateType;
import sporemodder.files.formats.renderWare4.RW4VertexFormat;
import sporemodder.utilities.Hasher;

public class SporeMaterial {
	
	private static final int FLAG_shader_constants = 0x8;
	private static final int FLAG_material_color = 0x10;
	private static final int FLAG_ambient_color = 0x20;
	private static final int FLAG_vertex_description = 0x100000;

	private static final int FLAG_use_booleans = 0x8000;

	private static final int FLAG3_render_states = 0x20000;
	private static final int FLAG3_texture_slots = 0xDFFFF;
	
	private int materialID;
	private int primitiveType;
	
	private boolean useMaterialColor;
	private boolean useAmbientColor;
	private final float[] materialColor = new float[4];
	private final float[] ambientColor = new float[3];
	
	private final List<D3DRenderStateType> renderStates = new ArrayList<D3DRenderStateType>();
	
	private final List<TextureSlot> textureSlots = new ArrayList<TextureSlot>();
	
	public void read(InputStreamAccessor in) throws IOException {
		
		in.readLEInt();  // size
		primitiveType = in.readLEInt();
		int flags1 = in.readLEInt();
		int flags2 = in.readLEInt();
		int flags3 = in.readLEInt();
		int field_14 = in.readLEInt();
		materialID = in.readLEInt();
		
		in.readInt();  // padding
		
		if (flags1 != 0) {
			
			if ((flags1 & 1) != 0) {
				if ((flags1 & 2) != 0) {
					in.readLEInt();
				}
				else {
					in.skipBytes(0x40);
				}
			}
			
			// vertex description
			if ((flags1 & FLAG_vertex_description) != 0) {
				new RW4VertexFormat().read(in, null);
			}
			
			// shader constants
			//TODO for now we don't show the shader constants, since tehre's no way we can't know their structure
			if ((flags1 & FLAG_shader_constants) != 0) {
				short index = in.readLEShort();
				
				while (index != 0) {
					if (index > 0) {
						in.readLEShort();  // offset
						int length = in.readLEInt();
						
						in.skipBytes(length);
					}
					else {
						in.readLEInt();
					}
					
					index = in.readLEShort();
				}
				
				in.skipBytes(6);
			}
			
			// materialColor
			if ((flags1 & FLAG_material_color) != 0) {
				useMaterialColor = true;
				in.readLEFloats(materialColor);
			}
			
			// ambientColor
			if ((flags1 & FLAG_ambient_color) != 0) {
				useAmbientColor = true;
				in.readLEFloats(ambientColor);
			}
			
			int ebx = flags1 & 0x3FC0;  // 0xFF << 6;
			if (ebx != 0) {
				for (int i = 0; i < 8; i++) {
					if ((flags1 & 1 << (6 + i)) != 0) {
						in.readLEInt();
					}
				}
			}
			
			if ((flags1 & FLAG_use_booleans) != 0) {
				in.skipBytes(0x11);
			}
			
			if ((flags1 & 0xF0000) != 0) {
				if ((flags1 & 0x10000) != 0) {
					in.skipBytes(4);
				}
				
				if ((flags1 & 0xE0000) != 0) {
					if ((flags1 & 0x20000) != 0) {
						in.skipBytes(0xC);
					}
					if ((flags1 & 0x40000) != 0) {
						in.skipBytes(4);
					}
					if ((flags1 & 0x80000) != 0) {
						in.skipBytes(4);
					}
				}
			}
		}
		
		if (field_14 != 0) {
			if ((field_14 & 0x20000) != 0) {
				in.skipBytes(0x1C);
			}
			if ((field_14 & 0x40000) != 0) {
				in.skipBytes(0x44);
			}
			if ((field_14 & 0x80000) != 0) {
				in.skipBytes(0x44);
			}
		}
		
		// render states
		if ((flags3 & FLAG3_render_states) != 0) {
			
			int unkIndex = in.readInt();  // usually 0
			
			while (unkIndex != -1) {
				int stateID = in.readLEInt();
				while (stateID != -1) {
					D3DRenderStateType state = D3DRenderStateType.getById(stateID);
					state.value = in.readLEInt();
					renderStates.add(state);
					stateID = in.readLEInt();
				}
				
				unkIndex = in.readLEInt();
			}
			
		}
		
		// textures
		if ((flags3 & FLAG3_texture_slots) != 0) {
			in.readLEInt();  // -1
			int number = 0;
			
			while (number != -1) {
				
				TextureSlot slot = new TextureSlot();
				slot.read(in);
				textureSlots.add(slot);
				
				number = in.readLEInt();
				in.skipBytes(-4);
			}
		}
	}
	
	public ArgScriptBlock toBlock(String name) {
		ArgScriptBlock block = new ArgScriptBlock("material", name);
		
		block.putCommand(new ArgScriptCommand("materialID", Hasher.getFileName(materialID, "0x")));
		
		if (primitiveType != 4) {
			block.putCommand(new ArgScriptCommand("primitiveType", D3DPRIMITIVETYPE.getById(primitiveType).toString()));
		}
		
		if (useMaterialColor || useAmbientColor) {
			block.addBlankLine();
			
			if (useMaterialColor) {
				ArgScriptCommand c = new ArgScriptCommand("materialColor", ArgScript.createFloatList(materialColor[0], materialColor[1], materialColor[2]));
				c.putOption(new ArgScriptOption("alpha", Float.toString(materialColor[3])));
				block.putCommand(c);
			}
			if (useAmbientColor) {
				block.putCommand(new ArgScriptCommand("ambientColor", ArgScript.createFloatList(ambientColor)));
			}
		}
		
		if (!renderStates.isEmpty()) {
			block.addBlankLine();
			
			for (D3DRenderStateType state : renderStates) {
				block.putCommand(new ArgScriptCommand("renderState", state.toString(), state.getValueToString()));
			}
		}
		
		for (TextureSlot slot : textureSlots) {
			block.addBlankLine();
			block.putBlock(slot.toBlock());
		}
		
		return block;
	}
	
	public static void main(String[] args) throws IOException {
		
		MainApp.init();
		
		String inputPath = "C:\\Users\\Eric\\Desktop\\water_compiled_state.rw4";
		
		try (InputStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			
			SporeMaterial material = new SporeMaterial();
			material.read(in);
			
			ArgScript as = new ArgScript();
			as.putBlock(material.toBlock("test"));
			
			System.out.print(as.toString());
		}
	}
}
