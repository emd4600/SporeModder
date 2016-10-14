package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Buffer extends RW4Section {
	public static final int type_code = 0x10030;
	public static final int alignment = 4;
	public byte[] data;

	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		data = new byte[this.sectionInfo.size];
		System.out.println(in.getFilePointer());
		in.read(data);
	}
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.write(data);
	}

	//TODO Fix this
	public static abstract class Structure {public abstract void read(InputStreamAccessor in) throws IOException, FileStructureException;}

	public static class Texture extends Structure {
		public byte[] data;
		public int size;
		public void read(InputStreamAccessor in) throws IOException {
			data = new byte[size];
			for (byte b : data) {
				b = in.readByte();
			}
		}
	}
	
//	public static class Vertex {
//		public Vector3f pos;
//		public Vector3f normal, tangent;
//		public Vector2f uv;
//		public Vector4f boneWeights;
//		public Integer[] boneIndices;
////		public int packed_bone_indices, packed_bone_weights;
////		public static int PackNormal(float x, float y, float z) {
////	        float invl = 127.5F;
////	        byte xb = (byte)(x*invl + 127.5);
////	        byte yb = (byte)(y*invl + 127.5);
////	        byte zb = (byte)(z*invl + 127.5);
////	        return ((int)xb) + ((int)yb << 8) + ((int)zb << 16);
////	    }
////	    public static float UnpackNormal(int packed, int dim) {
////	        byte b = (byte)((packed >> (dim * 8)) & 0xff);
////	        return (((float)b) - 127.5f) / 127.5f;
////	    }
////	    public static int UnpackBoneIndices (int packed, int dim) { //We have to divide each index by 3, don't know why
////	    	return ((packed >> (dim*8)) & 0xff) / 3;
////	    }
////	    public static int PackBoneIndices(int[] indices) {
////	    	byte b0 = (byte)indices[0];
////	    	byte b1 = (byte)indices[1];
////	    	byte b2 = (byte)indices[2];
////	    	byte b3 = (byte)indices[3];
////	    	return ((int)b3) + ((int)b2 << 8) + ((int)b1 << 16) + ((int)b0 << 24);
////	    }
//	}
	
	public static class Triangle {
		public int i, j, k;
		public byte unk1;           //< 4 bits found in "SimpleMesh", in a parallel section
		public void read(InputStreamAccessor in) throws IOException {
			i = in.readLEUShort();
			j = in.readLEUShort();
			k = in.readLEUShort();
		}
	}

	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
	}
	
	@Override
	public int getSectionTypeCode() {
		return type_code;
	}
	@Override
	public int getSectionAlignment() {
		return alignment;
	}
}
