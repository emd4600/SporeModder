package sporemodder.extras.spuiviewer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.files.formats.spui.SPUIMain;
import sporemodder.files.formats.spui.SPUINumberSections.SectionInt;
import sporemodder.files.formats.spui.SPUINumberSections.SectionShort;
import sporemodder.files.formats.spui.SPUISection;
import sporemodder.files.formats.spui.SPUIStructResource;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionDimension;
import sporemodder.files.formats.spui.SPUIVectorSections.SectionVec4;

public class ImageBlock {
	
	public static final int CHANNEL_UNKNOWN = 0x01BE0004;
	public static final int CHANNEL_DIMENSION = 0x01BE0003;
	public static final int CHANNEL_UVS = 0x01BE0002;
	public static final int CHANNEL_RESOURCE = 0x01BE0001;

	private SPUIBlock block;
	private SectionDimension secDimensions;  // 'dimensions'
	private SectionVec4 secUVCoords;  // 'uvCoords'
	private SectionShort secResourceIndex;  // 'resource'
	private SectionInt secUnknown;  // '#01BE0004'
	private SPUIFileResource fileResource;
	
	public ImageBlock() {
		
	}
	
	public ImageBlock(SPUIBlock block) throws ImageBlockException {
		this.block = block;
		
		validate();
		fileResource = block.getParent().getResources().get(secResourceIndex.data[0], SPUIFileResource.class);
	}
	
	public Dimension getDimensions() {
		if (secDimensions == null) {
			return null;
		}
		return new Dimension(secDimensions.data[0][0], secDimensions.data[0][1]);
	}
	
	public float[] getUVCoordinates() {
		if (secUVCoords == null) {
			return null;
		}
		return secUVCoords.data[0];
	}
	
	public SPUIFileResource getFileResource() {
		return fileResource;
	}
	
	public int getUnknown() {
		if (secUnknown == null) {
			return 0;
		}
		return secUnknown.data[0];
	}
	
	public SPUIBlock getBlock() {
		return block;
	}
	
	public void setUVCoordinates(int[] imageSpaceUVs, BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		if (secUVCoords == null) {
			secUVCoords = new SectionVec4();
			secUVCoords.setChannel(CHANNEL_UVS);
		}
		if (secUVCoords.data == null) {
			secUVCoords.data = new float[1][4];
		}
		
		secUVCoords.data[0][0] = imageSpaceUVs[0] / (float) width;
		secUVCoords.data[0][1] = imageSpaceUVs[1] / (float) height;
		secUVCoords.data[0][2] = imageSpaceUVs[2] / (float) width;
		secUVCoords.data[0][3] = imageSpaceUVs[3] / (float) height;
	}
	
	public void setDimension(Dimension dimension) {
		if (secDimensions == null) {
			secDimensions = new SectionDimension();
			secDimensions.setChannel(CHANNEL_DIMENSION);
		}
		if (secDimensions.data == null) {
			secDimensions.data = new int[1][2];
		}
		secDimensions.data[0][0] = dimension.width;
		secDimensions.data[0][1] = dimension.height;
	}
	
	public void setUnknown(int unknown) {
		if (secUnknown == null) {
			secUnknown = new SectionInt();
			secUnknown.setChannel(CHANNEL_UNKNOWN);
		}
		if (secUnknown.data == null) {
			secUnknown.data = new int[1];
		}
		secUnknown.data[0] = unknown;
	}
	
	public void setResource(SPUIFileResource resource) {
		// if this ImageBlock already existed in the SPUI, just modify the resource
		// if not, create a new resource and set the correct index
		
		if (secResourceIndex != null) {
			fileResource.setAtlas(resource.isAtlas());
			fileResource.setGroupID(resource.getGroupID());
			fileResource.setFileID(resource.getFileID());
			fileResource.setTypeID(resource.getTypeID());
		} else {
			fileResource = resource;
		}
	}
	
	public SPUIBlock generateBlock(SPUIMain parent, boolean addResource) throws ImageBlockException {
		
		if (block != null) {
			// the sections have already been updated
			return block;
		}
		
		// if this ImageBlock already existed in the SPUI, just modify the resource
		// if not, create a new resource and set the correct index
		
		if (fileResource == null) {
			throw new ImageBlockException("FileResource must not be null to generate SPUIBlock!");
		}
				
		if (secResourceIndex == null) {
			int index = -1;
			if (fileResource.isAtlas()) {
				index = parent.getResources().getNextAtlasIndex();
			} else {
				index = parent.getResources().getNextFileIndex();
			}
			
			if (secResourceIndex == null) {
				secResourceIndex = new SectionShort();
				secResourceIndex.setChannel(CHANNEL_RESOURCE);
			}
			if (secResourceIndex.data == null) {
				secResourceIndex.data = new short[1];
			}
			secResourceIndex.data[0] = (short) index;
			if (addResource) {
				parent.getResources().add(fileResource);
			}
		}
		
		if (secUnknown == null) {
			setUnknown(0);
		}
		if (secDimensions == null) {
			if (secUVCoords == null) {
				throw new ImageBlockException("Either UV coordinates or dimensions must be specified.");
			}
			secDimensions = new SectionDimension();
			secDimensions.data = new int[][] {new int[] {(int) (secUVCoords.data[0][2] - secUVCoords.data[0][0]), (int) (secUVCoords.data[0][3] - secUVCoords.data[0][1])}};
		}
		if (secUVCoords == null) {
			if (secDimensions == null) {
				throw new ImageBlockException("Either UV coordinates or dimensions must be specified.");
			}
			secUVCoords = new SectionVec4();
			secUVCoords.data = new float[][] { new float[] {0, 0, 1, 1} };
		}
		
		block = new SPUIBlock(parent);
		SPUIStructResource struct = new SPUIStructResource();
		struct.setHash(0x01BE6B15);
		block.setResource(struct);
		
		List<SPUISection> sections = block.getSections();
		sections.add(secResourceIndex);
		sections.add(secUVCoords);
		sections.add(secDimensions);
		sections.add(secUnknown);
		
		return block;
	}
	
	public int[] getUVCoordinatesImageSpace() throws IOException {
		if (secUVCoords == null) {
			return null;
		}
		BufferedImage image = getAtlasImage();
		int width = image.getWidth();
		int height = image.getHeight();
		int[] result = new int[4];
		
		result[0] = Math.round(secUVCoords.data[0][0] * width);
		result[1] = Math.round(secUVCoords.data[0][1] * height);
		result[2] = Math.round(secUVCoords.data[0][2] * width);
		result[3] = Math.round(secUVCoords.data[0][3] * height);
		
		return result;
	}
	
	public BufferedImage getAtlasImage() throws IOException {
		return ResourceLoader.loadImage(fileResource, SpuiViewer.ActiveFile);
	}
	
	private void validate() throws ImageBlockException {
		if (block.getResource().getHash() != 0x01BE6B15) {
			throw new ImageBlockException(Messages.getString("ImagePanel.setBlock.wrongBlockType"));
		}
		
		secUnknown = block.getSection(CHANNEL_UNKNOWN, SectionInt.class);  // '#01BE0004'
		secDimensions = block.getSection(CHANNEL_DIMENSION, SectionDimension.class);  // 'dimensions'
		secUVCoords = block.getSection(CHANNEL_UVS, SectionVec4.class);  // 'uvCoords'
		secResourceIndex = block.getSection(CHANNEL_RESOURCE, SectionShort.class);  // 'resource'
		
		if (secResourceIndex == null || secResourceIndex.data.length != 1 || secResourceIndex.data[0] < 0) {
			throw new ImageBlockException(Messages.getString("ImagePanel.processImage.wrongResource"));
		}
		if (secUVCoords == null || secUVCoords.data.length != 1 || secUVCoords.data[0].length != 4) {
			throw new ImageBlockException(Messages.getString("ImagePanel.processImage.wrongUVs"));
		}
	}
	
	
	public BufferedImage processImage() throws ImageBlockException, IOException {
		
		BufferedImage originalImage = null;
		try {
			fileResource = SpuiViewer.ActiveSPUI.getResources().get(secResourceIndex.data[0], SPUIFileResource.class);
			originalImage = ResourceLoader.loadImage(fileResource, SpuiViewer.ActiveFile);
		} catch (IOException e) {
			throw new ImageBlockException(Messages.getString("ImagePanel.processImage.errorLoading"));
		}
		
		int imageWidth = originalImage.getWidth();
		int imageHeight = originalImage.getHeight();
		
		Dimension size = new Dimension();
		
		float[] uvCoords = secUVCoords.data[0];
		
		if (secDimensions != null && secDimensions.data.length > 0) {
			size.width = secDimensions.data[0][0];
			size.height = secDimensions.data[0][1];
		} else {
			// Calculate from UVs.
			size.width = (int) ((uvCoords[2] - uvCoords[0]) * imageWidth);
			size.height = (int) ((uvCoords[3] - uvCoords[1]) * imageHeight);
		}
		
		BufferedImage result = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = result.createGraphics();
		
		// draw the image with the correct UVs
//		g.drawImage(originalImage, 0, size.height, size.width, 0, 
//				(int) (uvCoords[0]*imageWidth), (int) (uvCoords[1]*imageHeight), (int) (uvCoords[2]*imageWidth), (int) (uvCoords[3]*imageHeight), null);
		
		int[] coords = getUVCoordinatesImageSpace();
		
		g.drawImage(originalImage, 0, 0, size.width, size.height, 
				coords[0], coords[1], coords[2], coords[3], null);
		
		return result;
	}
}
