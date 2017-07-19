package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4VertexFormat extends RW4Section {
	public static class Vertex {
		public final float[] pos = new float[3];
		public final float[] normal = new float[3];
		public final float[] tangent = new float[3];
		public final float[] uv = new float[2];
		public final float[] boneWeights = new float[4];
		public final int[] boneIndices = new int[4];
	}
	
	public static final int type_code = 0x20004;
	public static final int alignment = 4;
	public int fmtCount;
	public int vertexSize;
	public int[] unks = new int[2];
	public Format[] formats;
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		
		expect(in.readInt(), 0, "RW4-VF001", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-VF002", in.getFilePointer());
		expect(in.readInt(), 0, "RW4-VF003", in.getFilePointer());
		fmtCount = in.readLEUShort();
		vertexSize = in.readUShort();
		unks[0] = in.readLEInt(); unks[1] = in.readLEInt();
		formats = new Format[fmtCount];
		
		for (int i = 0; i < fmtCount; i++) {
			expect(in.readShort(), 0, "RW4-VF004", in.getFilePointer());
			int offset = in.readLEUShort();
			int[] unks = new int[] {in.readLEUShort(), in.readLEUShort()};
			int typeCode = in.readLEInt();
			formats[i] = Format.getByTypeCode(typeCode);
			
			if (formats[i] != null) {
				formats[i].offset = offset;
				formats[i].unks = unks;
			}
//			System.out.println("\t"+formats[i].toString()+"\t"+formats[i].offset);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		out.writeInt(0);
		out.writeInt(0);
		out.writeInt(0);
		out.writeLEUShort(formats.length);
		out.writeUShort(vertexSize);
		out.writeLEInt(unks[0]);
		out.writeLEInt(unks[1]);
		for (Format format : formats) {
			out.writeShort(0);
			out.writeLEUShort(format.offset);
			out.writeLEUShort(format.unks[0]);
			out.writeLEUShort(format.unks[1]);
			out.writeLEInt(format.typeCode);
		}
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		for (Format fmt : formats) {
			System.out.println("\t"+fmt.toString()+"\t"+fmt.offset);
		}
	}
	
	public enum Format {
		POSITIONS {
			@Override
			protected float[] read(InputStreamAccessor in) throws IOException {
				float[] data = new float[3];
				data[0] = in.readLEFloat(); 
				data[1] = in.readLEFloat(); 
				data[2] = in.readLEFloat();
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeLEFloat(vertex.pos[0]);
				out.writeLEFloat(vertex.pos[1]);
				out.writeLEFloat(vertex.pos[2]);
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				float[] res = read(in);
				for (int i = 0; i < vertex.pos.length; i++) vertex.pos[i] = res[i];
			}
		},
		NORMALS {
			@Override
			protected float[] read(InputStreamAccessor in) throws IOException {
				float[] data = new float[3];
				int norm = in.readLEInt();
				data[0] = unpackTangent(norm, 0);
				data[1] = unpackTangent(norm, 1);
				data[2] = unpackTangent(norm, 2);
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeLEInt(packTangents(vertex.normal[0], vertex.normal[1], vertex.normal[2]));
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				float[] res = read(in);
				for (int i = 0; i < vertex.normal.length; i++) vertex.normal[i] = res[i];
			}
		},
		UVS {
			@Override
			protected float[] read(InputStreamAccessor in) throws IOException {
				float[] data = new float[2];
				data[0] = in.readLEFloat(); 
				data[1] = in.readLEFloat();
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeLEFloat(vertex.uv[0]);
				out.writeLEFloat(vertex.uv[1]);
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				float[] res = read(in);
				for (int i = 0; i < vertex.uv.length; i++) vertex.uv[i] = res[i];
			}
		},
		BONEASSIGNMENTS {
			@Override
			protected int[] read(InputStreamAccessor in) throws IOException {
				int[] data = new int[4];
				int ind = in.readLEInt();
				data[0] = UnpackBoneIndex(ind, 0);
				data[1] = UnpackBoneIndex(ind, 1);
				data[2] = UnpackBoneIndex(ind, 2);
				data[3] = UnpackBoneIndex(ind, 3);
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeByte(vertex.boneIndices[3] * 3);
				out.writeByte(vertex.boneIndices[2] * 3);
				out.writeByte(vertex.boneIndices[1] * 3);
				out.writeByte(vertex.boneIndices[0] * 3);
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				int[] res = read(in);
				for (int i = 0; i < vertex.boneIndices.length; i++) vertex.boneIndices[i] = res[i];
			}
			private int UnpackBoneIndex (int packed, int dim) { //We have to divide each index by 3, don't know why
		    	return ((packed >> (dim*8)) & 0xff) / 3;
		    }
		},
		SKINWEIGHTS {
			@Override
			protected float[] read(InputStreamAccessor in) throws IOException {
				float[] data = new float[4];
				data[0] = in.readUByte() / 255f;
				data[1] = in.readUByte() / 255f;
				data[2] = in.readUByte() / 255f;
				data[3] = in.readUByte() / 255f;
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeByte(Math.round(vertex.boneWeights[0] * 255));
				out.writeByte(Math.round(vertex.boneWeights[1] * 255));
				out.writeByte(Math.round(vertex.boneWeights[2] * 255));
				out.writeByte(Math.round(vertex.boneWeights[3] * 255));
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				float[] res = read(in);
				for (int i = 0; i < vertex.boneWeights.length; i++) vertex.boneWeights[i] = res[i];
			}
		},
		TANGENTS {
			@Override
			protected float[] read(InputStreamAccessor in) throws IOException {
				float[] data = new float[3];
				int tang = in.readLEInt();
				data[0] = unpackTangent(tang, 0);
				data[1] = unpackTangent(tang, 1);
				data[2] = unpackTangent(tang, 2);
				return data;
			}
			@Override
			protected void write(OutputStreamAccessor out, Vertex vertex) throws IOException {
				out.writeLEInt(packTangents(vertex.tangent[0], vertex.tangent[1], vertex.tangent[2]));
			}
			@Override
			public void process(Vertex vertex, InputStreamAccessor in) throws IOException {
				float[] res = read(in);
				for (int i = 0; i < vertex.boneWeights.length; i++) vertex.tangent[i] = res[i];
			}
		};
		
		public int[] unks = new int[2];
		public int typeCode;
		public int offset;
		static {
			POSITIONS.typeCode = 0;
			NORMALS.typeCode = 2;
			UVS.typeCode = 6;
			BONEASSIGNMENTS.typeCode = 0x0E;
			SKINWEIGHTS.typeCode = 0x0F;
			TANGENTS.typeCode = 0x13;
		}
		
		public static Format getByTypeCode(int typeCode) {
			Format[] values = Format.values();
			Format result = null;
			for (int i = 0; i < values.length; i++) {
				if (values[i].typeCode == typeCode) {
					result = values[i];
				}
			}
			return result;
		}
		protected abstract Object read(InputStreamAccessor in) throws IOException;
		protected abstract void write(OutputStreamAccessor out, Vertex vertex) throws IOException;
		public abstract void process(Vertex vertex, InputStreamAccessor in) throws IOException;
	}
	
	private static float unpackTangent(int packed, int dim) {
        byte b = (byte)((packed >> (dim * 8)) & 0xff);
        return (((float)b) - 127.5f) / 127.5f;
    }
	private static int packTangents(float x, float y, float z) {
	    byte xb = (byte) (Math.round(x * 127.5 + 127.5) & 0xFF);
	    byte yb = (byte) (Math.round(y * 127.5 + 127.5) & 0xFF);
	    byte zb = (byte) (Math.round(z * 127.5 + 127.5) & 0xFF);
	    return (xb + (yb << 8) + (zb << 16));
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
