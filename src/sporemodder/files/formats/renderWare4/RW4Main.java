package sporemodder.files.formats.renderWare4;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import sporemodder.MainApp;
import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.FileStructureException;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.dds.DDSImageReader;
import sporemodder.files.formats.dds.DDSImageReaderSpi;
import sporemodder.files.formats.dds.DDSTexture;

public class RW4Main extends FileStructure implements FileFormatStructure {
	private final RW4Header header = new RW4Header();
	private final List<RW4Section> sections = new ArrayList<RW4Section>();
	
	
	public void read(InputStreamAccessor in) throws IOException, InstantiationException, IllegalAccessException {
		header.read(in);
		in.seek(header.section_index_begin);
        for (int i = 0; i < header.section_count; i++) {
        	RW4Section.SectionInfo si = new RW4Section.SectionInfo();
        	si.read(in, header.section_index_end1, i);
        	RW4Section section = RW4Section.getType(si.type_code).newInstance();
        	section.sectionInfo = si;
        	sections.add(section);
        }
        for (RW4Section s : sections) {
        	if (s.sectionInfo.type_code != 0x200af) {
	        	in.seek(s.sectionInfo.pos);
	        	s.read(in, sections); 
        	}
        }
	}
	
	public void readSections(InputStreamAccessor in) throws IOException, InstantiationException, IllegalAccessException {
		in.seek(header.section_index_begin);
        for (int i = 0; i < header.section_count; i++) {
        	RW4Section.SectionInfo si = new RW4Section.SectionInfo();
        	si.read(in, header.section_index_end1, i);
        	RW4Section section = RW4Section.getType(si.type_code).newInstance();
        	section.sectionInfo = si;
        	sections.add(section);
        }
	}
	
	// Doesn't read the 'skipSections' types
	@SafeVarargs
	public final void read(InputStreamAccessor in, Class<? extends RW4Section> ... skipSections) throws IOException, InstantiationException, IllegalAccessException {
		header.read(in);
		in.seek(header.section_index_begin);
        for (int i = 0; i < header.section_count; i++) {
        	RW4Section.SectionInfo si = new RW4Section.SectionInfo();
        	si.read(in, header.section_index_end1, i);
        	
        	RW4Section section = RW4Section.getType(si.type_code).newInstance();
        	section.sectionInfo = si;
        	sections.add(section);
        }
        for (RW4Section s : sections) {
        	if (s.sectionInfo.type_code != 0x200af) {
        		boolean read = true;
        		for (Class<? extends RW4Section> skipClass : skipSections) {
        			if (skipClass.isInstance(s)) {
        				read = false;
        				break;
        			}
            	}
        		
        		if (read) {
		        	in.seek(s.sectionInfo.pos);
		        	s.read(in, sections); 
        		}
        	}
        }
	}
	
	// Only reads sections inside readSections
	@SafeVarargs
	public final void readSections(InputStreamAccessor in, Class<? extends RW4Section> ... readSections) throws IOException, InstantiationException, IllegalAccessException {
		in.seek(header.section_index_begin);
        for (int i = 0; i < header.section_count; i++) {
        	RW4Section.SectionInfo si = new RW4Section.SectionInfo();
        	si.read(in, header.section_index_end1, i);
        	
        	RW4Section section = RW4Section.getType(si.type_code).newInstance();
        	section.sectionInfo = si;
        	sections.add(section);
        }
        for (RW4Section s : sections) {
        	if (s.sectionInfo.type_code != 0x200af) {
        		for (Class<? extends RW4Section> skipClass : readSections) {
        			if (skipClass.isInstance(s)) {
        				in.seek(s.sectionInfo.pos);
			        	s.read(in, sections); 
			        	break;
        			}
            	}
        	}
        }
	}
	
	// gets a list with the section types and sets the type_code_indirect for each sections
	private List<Integer> getSectionTypes() {
		List<Integer> result = new ArrayList<Integer>();
		result.add(0);
		result.add(0x10030);
		result.add(0x10031);
		result.add(0x10032);
		result.add(0x10010);
		
		// They must be sorted!!
		List<Integer> sectionTypes = new ArrayList<Integer>();
		
		for (RW4Section section : sections) {
			if (section.sectionInfo.type_code == RW4Buffer.type_code) {
				section.sectionInfo.type_code_indirect = result.indexOf(RW4Buffer.type_code);
			}
			else {
				int index = sectionTypes.indexOf(section.sectionInfo.type_code);
				if (index == -1) {
					sectionTypes.add(section.sectionInfo.type_code);
					section.sectionInfo.type_code_indirect = sectionTypes.size() - 1 + result.size();
				}
				else {
					section.sectionInfo.type_code_indirect = index;
				}
			}
		}
		Collections.sort(sectionTypes);
		
		result.addAll(sectionTypes);
		
		return result;
	}
		
	public void write(OutputStreamAccessor out) throws IOException {
		LinkedHashMap<Integer, Integer> fixups = new LinkedHashMap<Integer, Integer>();
		List<Integer> sectionTypes = getSectionTypes();
		
		// add fixup_offsets for those sections that need it
		for (int i = 0; i < sections.size(); i++) {
			if (sections.get(i) instanceof RW4Animations) {
				fixups.put(i, 8);
			}
			else if (sections.get(i) instanceof RW4Matrices4x3) {
				fixups.put(i, 16);
			}
		}
		
		header.section_count = sections.size();
		header.write(out, sectionTypes, sections, fixups);
		
		for (RW4Section sec : sections) {
			if (sec.sectionInfo.type_code != RW4Buffer.type_code) {
				int padding = ((out.getFilePointer() + sec.sectionInfo.alignment - 1) & ~(sec.sectionInfo.alignment - 1)) - out.getFilePointer();
				out.writePadding(padding);
				
				sec.sectionInfo.pos = out.getFilePointer();
				sec.write(out, sections);
				sec.sectionInfo.size = out.getFilePointer() - sec.sectionInfo.pos;
			}
		}
		
		header.section_index_begin = out.getFilePointer();
		for (RW4Section sec : sections) {
			sec.sectionInfo.write(out);
		}
		for (Map.Entry<Integer, Integer> fixup : fixups.entrySet()) {
			out.writeLEInt(fixup.getKey());
			out.writeLEInt(fixup.getValue());
		}
		header.section_index_end = out.getFilePointer();
		
		for (int i = 0; i < header.section_count; i++) {
			RW4Section section = sections.get(i);
			if (section.sectionInfo.type_code == RW4Buffer.type_code) {
				int position = out.getFilePointer();
				section.sectionInfo.pos = position - header.section_index_end;
				section.write(out, sections);
				section.sectionInfo.size = out.getFilePointer() - position;
				int padding = ((out.getFilePointer() + 3) & ~3) - out.getFilePointer();
				out.writePadding(padding);
				
				// write correct section info
				int oldPos = out.getFilePointer();
				
				out.seek(header.section_index_begin + i * RW4Section.INFO_SIZE);
				section.sectionInfo.write(out);
				
				out.seek(oldPos);
			}
		}
		
		header.fileSize = out.getFilePointer(); 
		
		out.seek(0);
		header.write(out, sectionTypes, sections, fixups);
		
		// when writing to a DBPF, this is important
		out.seek(header.fileSize);
	}
	
	public static RW4Main fromTexture(InputStreamAccessor in) throws IOException {
		DDSTexture dds = new DDSTexture();
		dds.read(in);
		RW4Texture texture = new RW4Texture();
		texture.fromDDSTexture(dds);
		RW4Buffer buffer = new RW4Buffer();
		buffer.data = dds.getData();
		texture.texData = buffer;
		
		RW4Main rw4 = new RW4Main();
		rw4.sections.add(buffer);
		rw4.sections.add(texture);
		
		rw4.header.file_type = RW4Header.TYPE_TEXTURE;
		
		return rw4;
	}
	
	public void readHeader(InputStreamAccessor in) throws IOException {
		in.seek(0);
		header.read(in);
	}
	
	public void print() {
		for (RW4Section s : sections) {
			s.printInfo();
		}
		
		System.out.println();
		
		for (RW4Section s : sections) {
			s.print();
		}
	}
	
	public void print(boolean printInfo, boolean printData) {
		if (printInfo) {
			for (RW4Section s : sections) {
				s.printInfo();
			}
		}
		
		System.out.println();
		
		if (printData) {
			for (RW4Section s : sections) {
				s.print();
			}
		}
	}
	
	public boolean isModel() {
		return header.file_type == RW4Header.TYPE_MODEL;
	}
	
	public boolean isTexture() {
		return header.file_type == RW4Header.TYPE_TEXTURE;
	}
	
	public boolean isSpecial() {
		return header.file_type == RW4Header.TYPE_UNKNOWN;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends RW4Section> List<T> getSections(Class<T> type) {
		List<T> secs = new ArrayList<T>();
		
		for (RW4Section s : sections) {
			if (type.isInstance(s)) secs.add((T) s);
		}
		
		return secs;
	}
	
	// Returns a DDSTexture, only if the rw4 is of TEXTURE type
	public DDSTexture toTexture() throws IOException {
		if (header.file_type != RW4Header.TYPE_TEXTURE) return null;
		
		List<RW4Texture> textures = getSections(RW4Texture.class);
		if (textures.size() != 1) throw new IOException("Unexpected number of textures in TEXTURE type rw4.");
		
		return textures.get(0).toDDSTexture();
	}
	
	@Override
	public List<FileStructureError> getAllErrors() {
		List<FileStructureError> errors = new ArrayList<FileStructureError>(getErrors());
		errors.addAll(header.getErrors());
		
		for (RW4Section section : sections) {
			errors.addAll(section.getErrors());
		}
		
		return errors;
	}
	
	
	public static RW4Main fromFile(String path) throws IOException {
		RW4Main main = null;
		FileStreamAccessor in = new FileStreamAccessor(path, "r");
		try {
			main = new RW4Main();
			main.read(in);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		
		return main;
	}
	public static RW4Main fromFile(File file) throws IOException {
		RW4Main main = null;
		FileStreamAccessor in = new FileStreamAccessor(file, "r");
		try {
			main = new RW4Main();
			main.read(in);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		
		return main;
	}
	
	// returns null if input rw4 is not a texture
	public static RW4Main rw4ToTexture(InputStreamAccessor in, OutputStreamAccessor out) throws InstantiationException, IllegalAccessException, IOException {
		
		RW4Main rw4 = new RW4Main();
		rw4.readHeader(in);
		if (!rw4.isTexture()) {
			return null;
		}
		// only read necessary sections
		rw4.readSections(in, RW4Texture.class, RW4Buffer.class);
		
		DDSTexture texture = rw4.toTexture();
		if (texture == null) return null;
		
		texture.write(out);
		
		return rw4;
	}
	
	public static RW4Main rw4ToTexture(File input, File output) throws InstantiationException, IllegalAccessException, IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(input, "r");
				FileStreamAccessor out = new FileStreamAccessor(output, "rw")) {
			return rw4ToTexture(in, out);
		}
	}
	
	
	/* -- DDS to RW4 */
	
	public static RW4Main ddsToRw4(InputStreamAccessor in, OutputStreamAccessor out) throws IOException {
		RW4Main main = fromTexture(in);
		main.header.file_type = RW4Header.TYPE_TEXTURE;
		main.write(out);
		return main;
	}
	
	public static RW4Main ddsToRw4(File input, File output) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(input, "r");
				FileStreamAccessor out = new FileStreamAccessor(output, "rw")) {
			return ddsToRw4(in, out);
		}
	}
	
	public static RW4Main ddsToRw4(String input, String output) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(input, "r");
				FileStreamAccessor out = new FileStreamAccessor(output, "rw")) {
			return ddsToRw4(in, out);
		}
	}
	
	
	public static BufferedImage toBufferedImage(File file) throws IOException {
		FileStreamAccessor in = null;
		ByteArrayStreamAccessor out = null;
		ImageInputStream input = null;
		try {
			
			in = new FileStreamAccessor(file, "r");
			
			RW4Main rw4 = new RW4Main();
			rw4.readHeader(in);
			if (!rw4.isTexture()) {
				return null;
			}
			// only read necessary sections
			rw4.readSections(in, RW4Texture.class, RW4Buffer.class);
			
			DDSTexture texture = rw4.toTexture();
			if (texture == null) return null;
			
			out = texture.getByteArrayStream();
			
			input = ImageIO.createImageInputStream(new ByteArrayInputStream(out.toByteArray()));
			DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (input != null) {
				input.close();
			}
		}
		return null;
	}
	
	public static BufferedImage toBufferedImage(File file, DDSImageReader reader) throws IOException {
		FileStreamAccessor in = null;
		ByteArrayStreamAccessor out = null;
		ImageInputStream input = null;
		try {
			
			in = new FileStreamAccessor(file, "r");
			
			RW4Main rw4 = new RW4Main();
			rw4.readHeader(in);
			if (!rw4.isTexture()) {
				return null;
			}
			// only read necessary sections
			rw4.readSections(in, RW4Texture.class, RW4Buffer.class);
			
			DDSTexture texture = rw4.toTexture();
			if (texture == null) return null;
			
			out = texture.getByteArrayStream();
			
			input = ImageIO.createImageInputStream(new ByteArrayInputStream(out.toByteArray()));
			reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (input != null) {
				input.close();
			}
		}
		return null;
	}
	
//	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, FileStructureException {
////		NameRegistry_old.read();
////		long time1 = System.currentTimeMillis();
////		
////		File path = new File("E:\\Eric\\SporeModder  v0.6.0\\Projects\\Spore_Graphics\\part_models~\\");
////		
////		File[] files = path.listFiles(new FilenameFilter() {
////			@Override
////			public boolean accept(File dir, String name) {
////				return name.endsWith(".rw4") && name.startsWith("ce_");
////			}
////		});
////		System.out.println("length: " + files.length);
////		BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\Eric\\Desktop\\RW4 Test.txt"));
////		try {
////			for (File f : files) {
////				try {
////					FileStreamAccessor in = new FileStreamAccessor(f, "r");
////					try {
////						processModel(in, out, f);
////					} catch (InstantiationException | IllegalAccessException
////							| FileStructureException e) {
////						e.printStackTrace();
////					} finally {
////						in.close();
////					}
////				} catch (IOException e) {
////					System.out.println(f.getName());
////					e.printStackTrace();
////				}
////			}
////		} finally {
////			out.close();
////		}
//		
//		MainApp.init();
//		
//		long time1 = System.currentTimeMillis();
//		
////		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\editor_rigblocks~\\ce_details_armor_03.rw4\\raw.rw4";
////		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\editor_rigblocks~\\ce_grasper_radial_01.rw4\\raw.rw4";
////		String path = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\editor_rigblocks~\\ce_grasper_radial_01-symmetric.rw4\\raw.rw4";
////		String path = "E:\\Eric\\SporeMaster 2.0 beta\\SR_NS.package.unpacked\\editor_rigblocks~\\SRNS-ce_test_bones1.rw4";
////		String path = "E:\\Eric\\SporeMaster 2.0 beta\\SR_NS.package.unpacked\\editor_rigblocks~\\SRNS-test_normalMap.rw4";
//		//String path = "E:\\Eric\\SporeMaster 2.0 beta\\!!!!z0!!!!!2016_BranchingEvolution.package.unpacked\\part_models~\\fe_canopy_10.rw4\\raw.rw4";
//		String path = "E:\\Eric\\SporeMaster 2.0 beta\\!!!!z0!!!!!2016_BranchingEvolution.package.unpacked\\part_models~\\fe_canopy_10.rw4\\zzzzzzzzzzzzzzzzzzzraw.rw4";
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(path, "r")) {
//			RW4Main rw4 = new RW4Main();
//			rw4.read(in);
//			rw4.print();
//		}
//		
//		System.out.println("Done in " + (System.currentTimeMillis() - time1) + " ms");
//	}
	
//	private static void processModel(InputStreamAccessor in, BufferedWriter out, File f) throws InstantiationException, IllegalAccessException, IOException, FileStructureException {
//		RW4Main rw4 = new RW4Main();
//		rw4.read(in);
//		List<RW4TexMetadata> texMets = rw4.<RW4TexMetadata>getSections(RW4TexMetadata.class);
//		List<RW4MeshMatAssignment> meshMats = rw4.<RW4MeshMatAssignment>getSections(RW4MeshMatAssignment.class);
////					System.out.println(f.getName() + "\tunk2: " + texMets.get(0).unk2);
//		
//		VertexBuffer vertexBuffer = new VertexBuffer(meshMats.get(0).mesh.vertex_count, meshMats.get(0).mesh.vertexArray.vertexFormat.formats);
//        in.seek(meshMats.get(0).mesh.vertexArray.vertexBuffer.sectionInfo.pos + 
//        		meshMats.get(0).mesh.first_vertex*meshMats.get(0).mesh.vertexArray.vertexFormat.vertexSize);
//        vertexBuffer.read(in);
//        
//        int max = 0;
//        Vertex[] vertices = vertexBuffer.getVertices();
//        for (Vertex vertex : vertices) {
//        	if (vertex.boneWeights.w > 0) {
//        		max++;
//        		if (vertex.boneWeights.z > 0) {
//        			max++;
//        			if (vertex.boneWeights.y > 0) {
//	        			max++;
//	        			if (vertex.boneWeights.x > 0) {
//		        			max++;
//		        		}
//	        		}
//        		}
//        	}
//        }
//		
//		out.write((max == texMets.get(0).unk2) + "\t" + f.getName() + "\tunk2: " + texMets.get(0).unk2);
//		out.newLine();
//	}
	
	public static void main(String[] args) throws IOException {
		MainApp.init();
		
//		String inputPath = "C:\\Users\\Eric\\Desktop\\ce_sense_ear_top_04-symmetric.rw4.dds";
//		String outputPath = "C:\\Users\\Eric\\Desktop\\test.rw4";
		
//		String inputPath = "E:\\Eric\\SporeModder\\Projects\\SRNS_Effects\\animations~\\SRNS_axis_cube__diffuse.rw4.dds";
//		String outputPath = "C:\\Users\\Eric\\Desktop\\axis_test.rw4";
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r");
//				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw")) {
//			RW4Main main = fromTexture(in);
//			main.write(out, RW4Header.TYPE_TEXTURE);
//		}
		
//		String outputPath2 = "C:\\Users\\Eric\\Desktop\\test.rw4.dds";
//		String inputPath2 = "C:\\Users\\Eric\\Desktop\\test.rw4";
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(inputPath2, "r");
//				FileStreamAccessor out = new FileStreamAccessor(outputPath2, "rw")) {
//			RW4Main.rw4ToTexture(in, out).print();
//		} catch (InstantiationException | IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		String inputPath = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\part_skin_textures~\\ce_sense_ear_top_04-symmetric__specBump.rw4\\raw.rw4";
//		String outputPath = "C:\\Users\\Eric\\Desktop\\ce_test.rw4.dds";
//		
//		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r");
//				FileStreamAccessor out = new FileStreamAccessor(outputPath, "rw")) {
//			RW4Main.rw4ToTexture(in, out).print();
//		} catch (InstantiationException | IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		//String inputPath = "E:\\Eric\\SporeModder\\Projects\\CreaturePartTest\\editor_rigblocks~\\TEST-ce_mouths_chameleorex.rw4\\TEST-ce_mouths_chameleorex.rw4";
		
		//String inputPath = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\editor_rigblocks~\\ce_grasper_radial_01-symmetric.rw4\\raw.rw4";
		
//		String inputPath = "C:\\Users\\Eric\\Desktop\\ce_detail_supernatural_02.rw4";
		
		//String inputPath = "E:\\Eric\\SporeMaster 2.0 beta\\spore.unpacked\\editor_rigblocks~\\ce_details_armor_03.rw4\\raw.rw4";
		
		//String inputPath = "C:\\Users\\Eric\\Desktop\\SporeModder Addons Test\\SkeletonTest.rw4";
		String inputPath = "C:\\Users\\Eric\\Desktop\\SporeModder Addons Test\\SkeletonTest2.rw4";
		//String inputPath = "C:\\Users\\Eric\\Desktop\\SporeModder Addons Test\\Working_SkeletonTest2.rw4";
		
		//String inputPath = "C:\\Users\\Eric\\Desktop\\SporeModder Addons Test\\Horn.rw4";
		//String inputPath = "C:\\Users\\Eric\\Desktop\\SporeModder Addons Test\\StaticModel_Cube.rw4";
		//String inputPath = "E:\\Eric\\SporeModder\\Projects\\SporeModder Models Test\\ModAPITestModels\\StaticModel_Cube.rw4";
		
		//String inputPath = "C:\\Users\\Eric\\Desktop\\Working_StaticModel_Cube.rw4";
		
		//String inputPath = "C:\\Users\\Eric\\Desktop\\trg_sacrifice_altar1.rw4";
		RW4TexMetadata.READ_COMPILED_STATE = false;
		
		try (FileStreamAccessor in = new FileStreamAccessor(inputPath, "r")) {
			RW4Main main = new RW4Main();
			main.read(in);
			main.print();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

