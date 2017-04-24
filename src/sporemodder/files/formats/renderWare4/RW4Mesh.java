package sporemodder.files.formats.renderWare4;

import java.io.IOException;
import java.util.List;

import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;

public class RW4Mesh extends RW4Section {
	public static final int type_code = 0x20009;
	public static final int alignment = 4;
	
	private RW4TriangleArray triangleArray;
	private RW4VertexArray vertexArray;
	private int primitiveType = 4;
	private int tri_count;
	private int vertex_count;
	private int first_tri;
	private int first_vertex;
	private int tri_secNum;
	private int vertex_secNum;
	
	@Override
	public void read(InputStreamAccessor in, List<RW4Section> sections) throws IOException {
		expect(in.readLEInt(), 40, "RW4-ME001", in.getFilePointer());
		expect(primitiveType = in.readLEInt(), 4, "RW4-ME002", in.getFilePointer());
		tri_secNum = in.readLEInt();
		tri_count = in.readLEInt();
		expect(in.readLEInt(), 1, "RW4-ME003", in.getFilePointer());
		first_tri = in.readLEInt();
		expect(in.readLEInt(), tri_count*3, "RW4-ME004", in.getFilePointer());
		first_vertex = in.readLEInt();
		vertex_count = in.readLEInt();
		vertex_secNum = in.readLEInt();
		
		triangleArray = (RW4TriangleArray) sections.get(tri_secNum);
		if (vertex_secNum == 0x00400000) {
			//Blendshape
		} else {
			vertexArray = (RW4VertexArray) sections.get(vertex_secNum);
		}
	}
	
	@Override
	public void write(OutputStreamAccessor out, List<RW4Section> sections) throws IOException {
		tri_secNum = sections.indexOf(triangleArray);
		vertex_secNum = sections.indexOf(vertexArray);
		out.writeLEInts(40, primitiveType, tri_secNum, tri_count, 1, first_tri, tri_count*3, first_vertex, vertex_count, vertex_secNum);
	}
	
	@Override
	public void print() {
		System.out.println("### " + this.getClass().getSimpleName() + " section " + this.sectionInfo.number);
		System.out.println("\ttriangle array section: " + tri_secNum);
		System.out.println("\ttriangle count: " + tri_count);
		System.out.println("\tfirst triangle: " + first_tri);
		System.out.println("\tvertex array section: " + vertex_secNum);
		System.out.println("\tvertex count: " + vertex_count);
		System.out.println("\tfirst vertex: " + first_vertex);
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
