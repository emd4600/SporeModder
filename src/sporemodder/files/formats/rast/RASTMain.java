package sporemodder.files.formats.rast;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.FileStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.dds.DDSImageReader;
import sporemodder.files.formats.dds.DDSImageReaderSpi;
import sporemodder.files.formats.dds.DDSTexture;

public class RASTMain implements FileFormatStructure {
	
	private DDSTexture texture;
	
	public RASTMain() {
		initTexture();
	}
	
	public RASTMain(DDSTexture texture) {
		this.texture = texture;
	}
	
	public DDSTexture getTexture() {
		return texture;
	}

	public void setTexture(DDSTexture texture) {
		this.texture = texture;
	}

	private void initTexture() {
		if (texture == null) {
			texture = new DDSTexture();
		}
	}

	public DDSTexture read(InputStreamAccessor in) throws IOException {
		if (in.readLEInt() != 1) {
			throw new IOException("Unsupported RAST version.");
		}
		initTexture();
		
		texture.setWidth(in.readLEInt());
		texture.setHeight(in.readLEInt());
		texture.setMipmapCount(in.readLEInt());
		int pixelWidth = in.readLEInt();
		texture.setTextureType(in.readLEInt());
		
		//TODO adapt this for when pixelWidth != 8 ?
		int size = 0;
		byte[][] textureData = new byte[texture.getMipmapCount()][];
		for (int i = 0; i < texture.getMipmapCount(); i++) {
			textureData[i] = new byte[in.readLEInt()];
			size += textureData[i].length;
			in.read(textureData[i]);
		}
		texture.setData(new byte[size]);
		int pos = 0;
		for (byte[] data : textureData) {
			System.arraycopy(data, 0, texture.getData(), pos, data.length);
			pos += data.length;
		}
		return texture;
	}
	
	public void write(OutputStreamAccessor out) throws IOException {
//		// we must write each mipMap separately, we'll use this
//		ImageInputStream stream = ImageIO.createImageInputStream(input);
//		DDSImageReader ddsImageReader = new DDSImageReader(new DDSImageReaderSpi());
//		ddsImageReader.setInput(stream);
//		
//		int mipmapCount = ddsImageReader.getNumImages(false);
//		
//		out.writeLEInt(1);
//		out.writeLEInt(ddsImageReader.getWidth(0));
//		out.writeLEInt(ddsImageReader.getHeight(0));
//		out.writeLEInt(mipmapCount);
//		out.writeLEInt(8);  // pixel width
//		out.writeLEInt(ddsImageReader.getFormat().fourCC);
//		
//		for (int i = 0; i < mipmapCount; i++)
//		{
//			byte[] data = ddsImageReader.getData(i, null);
//			out.writeLEInt(data.length);
//			out.write(data);
//		}
		if (texture == null) {
			throw new UnsupportedOperationException("Can't write RAST file if no texture is specified.");
		}
		
		out.writeLEInt(1);
		out.writeLEInt(texture.getWidth());
		out.writeLEInt(texture.getHeight());
		out.writeLEInt(texture.getMipmapCount());
		out.writeLEInt(8);  // pixel width
		out.writeLEInt(texture.getTextureType());
		
		for (int i = 0; i < texture.getMipmapCount(); i++)
		{
			byte[] data = texture.getMipmapData(i);
			out.writeLEInt(data.length);
			out.write(data);
		}
	}
	
	public void setTexture(File file) throws IOException {
		try (InputStreamAccessor input = new FileStreamAccessor(file, "r")) {
			texture = new DDSTexture();
			texture.read(input);
		}
	}
	public void setTexture(String path) throws IOException {
		try (InputStreamAccessor input = new FileStreamAccessor(path, "r")) {
			texture = new DDSTexture();
			texture.read(input);
		}
	}
	public void setTexture(InputStreamAccessor in) throws IOException {
		texture = new DDSTexture();
		texture.read(in);
	}
	
	/* -- RAST to DDS -- */
	
	public static RASTMain rastToDDS(InputStreamAccessor in, OutputStreamAccessor out) throws IOException {
		RASTMain main = new RASTMain();
		main.read(in);
		main.getTexture().write(out);
		return main;
	}
	
	public static RASTMain rastToDDS(File inFile, File outFile) throws IOException {
		try (FileStreamAccessor in = new FileStreamAccessor(inFile, "r");
				FileStreamAccessor out = new FileStreamAccessor(outFile, "rw")) {
			return rastToDDS(in, out);
		}
	}
	
	
	/* -- DDS to RAST -- */
	
	public static RASTMain ddsToRast(File inFile, OutputStreamAccessor out) throws IOException {
		RASTMain main = new RASTMain();
		main.setTexture(inFile);
		main.write(out);
		return main;
	}
	
	public static RASTMain ddsToRast(InputStreamAccessor in, OutputStreamAccessor out) throws IOException {
		RASTMain main = new RASTMain();
		main.setTexture(in);
		main.write(out);
		return main;
	}
	
	
	public static BufferedImage toBufferedImage(File file) throws IOException {
		FileStreamAccessor in = null;
		ByteArrayStreamAccessor out = null;
		try {
			
			in = new FileStreamAccessor(file, "r");
			
			RASTMain main = new RASTMain();
			main.read(in);
			out = main.getTexture().getByteArrayStream();
			
			ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(out.toByteArray()));
			DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		}
		finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static BufferedImage toBufferedImage(File file, DDSImageReader reader) throws IOException {
		FileStreamAccessor in = null;
		ByteArrayStreamAccessor out = null;
		ImageInputStream input = null;
		try {
			
			in = new FileStreamAccessor(file, "r");
			
			RASTMain main = new RASTMain();
			main.read(in);
			out = main.getTexture().getByteArrayStream();
			
			input = ImageIO.createImageInputStream(new ByteArrayInputStream(out.toByteArray()));
			reader.setInput(input);
			return reader.read(0);
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
	}

	@Override
	public List<FileStructureError> getAllErrors() {
		return null;
	}
}
