package sporemodder.extras.spuiviewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePreviewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8192387438205654968L;
	
	private BufferedImage image;
	private final int[] uvCoordinates = new int[4];
	private final Dimension dimension = new Dimension(0, 0);
	
	private float zoomLevel = 1.0f;
	private int posX;
	private int posY;
	private int imageWidth;
	private int imageHeight;
	
	public ImagePreviewPanel() {
		
	}
	
	public ImagePreviewPanel(BufferedImage image) {
		this.image = image;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image == null) {
			return;
		}
		
		 // Draw image on the center
        updatePosition();
        
        Graphics2D g2 = (Graphics2D)g;
		g2.drawImage(image, posX, posY, posX + imageWidth, posY + imageHeight, 
				uvCoordinates[0], uvCoordinates[1], uvCoordinates[2], uvCoordinates[3], this);
	}
	
	@Override
	public Dimension getPreferredSize() {
        int w = (int)(zoomLevel * dimension.getWidth());
        int h = (int)(zoomLevel * dimension.getHeight());
        return new Dimension(w, h);
    }
	
	private void updatePosition() {
        // 0, 0 -> upper left corner
 		Dimension panelSize = getSize();
 		imageWidth = (int) (dimension.width * zoomLevel);
 		imageHeight = (int) (dimension.height * zoomLevel);
 		posX = (panelSize.width - imageWidth) / 2;
 		posY = (panelSize.height - imageHeight) / 2;
	}
	
	public void setZoomLevel(float zoomLevel) {
		this.zoomLevel = zoomLevel;
		
		revalidate();  // update parent scrollPane
		repaint();
	}

	public float getZoomLevel() {
		return zoomLevel;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void update(int[] uvCoordinates, Dimension dimension) {
		this.uvCoordinates[0] = uvCoordinates[0];
		this.uvCoordinates[1] = uvCoordinates[1];
		this.uvCoordinates[2] = uvCoordinates[2];
		this.uvCoordinates[3] = uvCoordinates[3];
		this.dimension.width = dimension.width;
		this.dimension.height = dimension.height;
		revalidate();
		repaint();
	}
	
	public void setImage(BufferedImage image){
		this.image = image;
		repaint();
	}
}
