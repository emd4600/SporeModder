package sporemodder.files.formats.dds;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import sporemodder.files.ByteArrayStreamAccessor;
import sporemodder.files.InputStreamAccessor;
import sporemodder.files.OutputStreamAccessor;
import sporemodder.files.formats.FileFormatStructure;
import sporemodder.files.formats.FileStructure;
import sporemodder.files.formats.FileStructureError;
import sporemodder.files.formats.dds.DDSPixelFormat.Format;

public class DDSTexture extends FileStructure implements FileFormatStructure {
	private static final int HEADER_SIZE = 128;
	
	private int width;
	private int height;
	private int pitchOrLinearSize;
	private int mipmapCount;
	private int flags;
	private int textureType;
	private int caps;
	private byte[] data;
	
	public DDSTexture(int width, int height, int mipmapCount, int textureType,
			byte[] data) {
		this.width = width;
		this.height = height;
		this.mipmapCount = mipmapCount;
		this.textureType = textureType;
		this.data = data;
		
		this.pitchOrLinearSize = textureType == 0x15 ? width*height*4  : width * height;
		this.caps = textureType == 0x15 ? 0x00401008 : 0;
		this.flags = textureType == 0x15 ? 0x41 : 4;
	}
	
	public DDSTexture() {};
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getMipmapCount() {
		return mipmapCount;
	}
	public int getTextureType() {
		return textureType;
	}
	public byte[] getData() {
		return data;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setMipmapCount(int mipmapCount) {
		this.mipmapCount = mipmapCount;
	}
	public void setTextureType(int textureType) {
		this.textureType = textureType;
		// we must change this if the type is 0x15
		this.pitchOrLinearSize = textureType == 0x15 ? width*height*4 : width * height;
		this.caps = textureType == 0x15 ? 0x00401008 : 0;
		this.flags = textureType == 0x15 ? 0x41 : 4;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public boolean hasSameProperties(DDSTexture other) {
		return other.width == width && other.height == height && other.mipmapCount == mipmapCount && other.textureType == textureType;
	}
	
	public void readHeader(InputStreamAccessor in) throws IOException {
		expect(in.readInt(), 0x44445320, "DDS-H001", in.getFilePointer());//magic
		expect(in.readInt(), 0x7C000000, "DDS-H002", in.getFilePointer());//dwSize
		expect(in.readInt(), 0x07100A00, "DDS-H003", in.getFilePointer());//dwFlags
		height = in.readLEInt();
		width = in.readLEInt();
		pitchOrLinearSize = in.readLEInt();
		expect(in.readInt(), 0, "DDS-H005", in.getFilePointer());
		mipmapCount = in.readLEInt();
		in.skipBytes(44);
		expect(in.readLEInt(), 32, "DDS-H006", in.getFilePointer());
		flags = in.readLEInt();
		textureType = in.readLEInt();
		expect(in.readLEInt(), 32, "DDS-H008", in.getFilePointer());
		expect(in.readLEInt(), 0x00FF0000, "DDS-H009", in.getFilePointer());
		expect(in.readLEInt(), 0x0000FF00, "DDS-H010", in.getFilePointer());
		expect(in.readLEInt(), 0x000000FF, "DDS-H011", in.getFilePointer());
		expect(in.readLEInt(), 0xFF000000, "DDS-H012", in.getFilePointer());
		caps = in.readLEInt();
		in.skipBytes(16);
		
		if (textureType == 0 && flags == 0x41) {
			textureType = 0x15;
		}
		//texData.Read_Blob(in, num);
	}
	
	public void writeHeader(OutputStreamAccessor out) throws IOException {
		out.writeInt(0x44445320);//magic
		out.writeInt(0x7C000000);//dwSize
		out.writeInt(0x07100A00);//dwFlags
		out.writeLEInt(height);
		out.writeLEInt(width);
		out.writeLEInt(pitchOrLinearSize);
		out.writeInt(0);
		out.writeLEInt(mipmapCount);
		out.writePadding(44);
		out.writeLEInt(32);
		out.writeLEInt(flags);
		out.writeLEInt(textureType == 0x15 ? 0 : textureType);
		out.writeLEInt(32);
		out.writeLEInt(0x00FF0000);
		out.writeLEInt(0x0000FF00);
		out.writeLEInt(0x000000FF);
		out.writeLEInt(0xFF000000);
		out.writeLEInt(caps);
		out.writePadding(16);
	}
	
	public void read(InputStreamAccessor in) throws IOException {
		readHeader(in);
		data = new byte[in.length() - 128];
		in.read(data);
	}
	
	@Override
	public void write(OutputStreamAccessor out) throws IOException {
		writeHeader(out);
		out.write(data);
	}
	
	public ByteArrayStreamAccessor getByteArrayStream() throws IOException {
		ByteArrayStreamAccessor out = new ByteArrayStreamAccessor(HEADER_SIZE + data.length);
		write(out);
		return out;
	}
	
//	public DDSPixelFormat generatePixelFormat() {
//		return new DDSPixelFormat(32, 4, textureType, 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
//	}
//	
//	
//	public DDSHeader generateDDSHeader() {
//		return new DDSHeader(124, 0x0A1007, height, width, height*width, 0, mipmapCount, generatePixelFormat(), 0, 0, 0, 0, null);
//	}
//	
//	public BufferedImage generateImage() throws IOException {
//		ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(getData()));
//		DDSImageReader instance = new DDSImageReader( new DDSImageReaderSpi() );
//		
//		instance.setInput(input);
//		instance.ddsHeader = generateDDSHeader();
//		return  instance.read(0);
//	}
	
	public byte[] getMipmapData(int imageIndex) throws IOException {
		if (imageIndex >= mipmapCount) {
			throw new IllegalArgumentException("MipMap index not found.");
		}
		
		int skipsBytes = 0;
		for (int i = 0; i < imageIndex; i++){
			skipsBytes = (int) (skipsBytes + skipImage( i));
		}
		
		int width = (int) getWidth(imageIndex);
		int height = (int) getHeight(imageIndex);
		
		// is it 8 ?
		return readAll(skipsBytes, textureType, 8, width, height);
	}
	
	private long skipImage(int index) {
		long skipsBytes;
		if (textureType != Format.UNCOMPRESSED.fourCC){
			int fixedHeight = fixSize((int)getHeight(index));
			int fixedWidth = fixSize((int)getWidth(index));
			if (textureType == Format.DXT1.fourCC || textureType == Format.ATI1.fourCC){ //DXT1 & ATI (8 bytes)
				long bytes = (8 * (Math.max(1, (fixedHeight / 4) * Math.max(1, fixedWidth/ 4))));
				bytes = Math.max(bytes, 8);
				skipsBytes = bytes;
			} else { //DXT3 & DXT5 & ATI2 (16 bytes)
				long bytes = (16 * (Math.max(1, (fixedHeight / 4) * Math.max(1, fixedWidth/ 4))));
				bytes = Math.max(bytes, 16);
				skipsBytes = bytes;
			}
		} else { //Uncompressed
//			long bytes =  (ddsHeader.getPixelFormat().getRgbBitCount() / 8) * ddsHeader.getHeight(index) * ddsHeader.getWidth(index);
//			skipsBytes = bytes;
			skipsBytes = getHeight(index) * getWidth(index);
		}
		return skipsBytes;
	}
	
	public long getHeight(int mipMap) {
		int fixedMipMap = mipMap;
		if (fixedMipMap >= mipmapCount) {
			fixedMipMap = fixedMipMap - ((fixedMipMap / (int)mipmapCount) * (int)mipmapCount);
		}
		return Math.max(height >> fixedMipMap, 1);
	}
	public long getWidth(int mipMap) {
		int fixedMipMap = mipMap;
		if (fixedMipMap >= mipmapCount) {
			fixedMipMap = fixedMipMap - ((fixedMipMap / (int)mipmapCount) * (int)mipmapCount);
		}
		return Math.max(width >> fixedMipMap, 1);
	}
	
	/**
	 * Fix size to be compatible with "bad size" ex.: 25x25 (instead of 24x24)
	 * @param size width or height
	 * @return fixed width or height 
	 */
	public int fixSize(int size) {
		while(size % 4 != 0) {
			size++;
		}
		return size;
	}
	
	private byte[] readAll(int offset, int format, int bitCount, int width, int height) throws IOException {
		byte[] bytes;
		if (format == Format.UNCOMPRESSED.fourCC) {
			int byteCount = (int)(bitCount/8);
			bytes = new byte[(height*width*byteCount)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.RGBG.fourCC || format == Format.GRGB.fourCC ||
				format == Format.UYVY.fourCC || format == Format.UYVY.fourCC) {
			bytes = new byte[(height*width*2)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.DXT1.fourCC || format == Format.ATI1.fourCC) {
			width = fixSize(width);
			height = fixSize(height);
			bytes = new byte[8*Math.max(1, width/4)*Math.max(1, height/4)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else if (format == Format.DXT3.fourCC || format == Format.DXT5.fourCC || format == Format.ATI2.fourCC) {
			width = fixSize(width);
			height = fixSize(height);
			bytes = new byte[16*(int)Math.max(1, width/4)*(int)Math.max(1, height/4)];
			System.arraycopy(data, offset, bytes, 0, bytes.length);
		}
		else {
			throw new IOException("0x" + Integer.toHexString(format) + " is not a supported format!");
		}
		return bytes;
	}
	
	public static BufferedImage toBufferedImage(File file) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
			reader.setInput(input);
			return reader.read(0);
		}
	}
	
	public static BufferedImage toBufferedImage(File file, DDSImageReader reader) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			reader.setInput(input);
			return reader.read(0);
		}
	}
	
	public static BufferedImage toBufferedImage(DDSTexture tex, DDSImageReader reader) throws IOException {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(tex.getByteArrayStream().toByteArray()))) {
			reader.setInput(input);
			return reader.read(0);
		}
	}

	@Override
	public List<FileStructureError> getAllErrors() {
		return getErrors();
	}
}
