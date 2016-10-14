package sporemodder.extras.spuiviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import sporemodder.files.formats.spui.SPUIBlock;

public class ImageEditPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4581357733754397181L;
	
	private static final int SELECT_NONE = -1;
	private static final int SELECT_POINT1 = 0;
	private static final int SELECT_POINT2 = 1;
	
	private static final int GRID_SIZE = 20;
	private static final Color GRID_COLOR1 = Color.white;
	private static final Color GRID_COLOR2 = Color.lightGray;

	private static final float SCALING_FACTOR = 4f;
	private static final float LINE_WIDTH = 1.0f;
	private static final float POINT_RADIUS = 4.0f;
	private static final Color POINT_COLOR = Color.red;
//	private static final Color POINT_COLOR_HOVER = POINT_COLOR.brighter();
//	private static final Color POINT_COLOR_SELECT = POINT_COLOR.darker();
	
	private static final Color POINT_COLOR_HOVER = Color.green;
	private static final Color POINT_COLOR_SELECT = Color.blue;
	
	private float zoomLevel = 1.0f;
	private final Point point1;  // topLeft
	private final Point point2;  // bottomRight
	private Color point1Color = POINT_COLOR;
	private Color point2Color = POINT_COLOR;
	private int selectionIndex = SELECT_NONE;
	private BufferedImage image;
	
	private int posX;
	private int posY;
	private int imageWidth;
	private int imageHeight;
	
	private float pointRadius = POINT_RADIUS;
	private float lineWidth = LINE_WIDTH;
	
	private ImageBlock imageBlock;	
	private ImageEditDialog parent;
	
	public ImageEditPanel(ImageEditDialog parent) {
		super();
		this.parent = parent;
		point1 = new Point();
		point2 = new Point();
	}
	
	public ImageEditPanel(ImageEditDialog parent, ImageBlock imageBlock) throws IOException {
		super();
		this.parent = parent;
		if (imageBlock == null) {
			this.imageBlock = new ImageBlock();
			point1 = new Point();
			point2 = new Point();
		} else {
			this.imageBlock = imageBlock;
			image = imageBlock.getAtlasImage();
			int width = image.getWidth();
			int height = image.getHeight();
			
			float[] UVs = imageBlock.getUVCoordinates();
			if (UVs != null) {
				point1 = new Point((int) (UVs[0] * width), (int) (UVs[1] * height));
				point2 = new Point((int) (UVs[2] * width), (int) (UVs[3] * height));
			} else {
				point1 = new Point(0, 0);
				point2 = new Point(width, height);
			}
		}
		
		//previewDialog = new ImagePreviewDialog(image);
		
		setBackground(Color.gray);
		addMouseListener(new ImageMouseListener());
		addMouseMotionListener(new ImageMouseMotionListener());
	}
	
	
	public void setZoomLevel(float zoomLevel) {
		float difference = this.zoomLevel - zoomLevel;
		// = POINT_RADIUS * (1 - zoomLevel) * (1 - zoomLevel);
		//lineWidth = LINE_WIDTH * (1 - zoomLevel) * (1 - zoomLevel);ç
		
		pointRadius = POINT_RADIUS + ((zoomLevel - 1) / SCALING_FACTOR);
		lineWidth = LINE_WIDTH + ((zoomLevel - 1) / SCALING_FACTOR);
		
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
	
	@Override
	public Dimension getPreferredSize() {
		if (image == null) {
			return super.getPreferredSize();
		}
        int w = (int)(zoomLevel * image.getWidth());
        int h = (int)(zoomLevel * image.getHeight());
        return new Dimension(w, h);
    }

	@Override
	public void paintComponent( Graphics g ) {
        super.paintComponent(g);
        
        if (image == null) {
        	return;
        }
        
        // Draw image on the center
        updatePosition();
        
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setStroke(new BasicStroke(lineWidth));
        
        drawBackgroundGrid(g2);
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
 		g2.drawImage(image, posX, posY, imageWidth, imageHeight, this);
        
        // g2.drawImage(image, 0, 0, this);

        //Line2D line = new Line2D.Double(10, 10, 40, 40);
//        g2.setColor(Color.red);
//        // g2.setStroke(new BasicStroke(10));
//        
//        Ellipse2D.Double pointCircle1 = new Ellipse2D.Double(25, 50, 20, 20);
//        g2.fill(pointCircle1);
//        //g2.draw(pointCircle1);
        
        //g2.setStroke(new BasicStroke(10));
        
 		g2.setColor(POINT_COLOR);
        drawLines(g2);
        
 		g2.setColor(point1Color);
        drawPoint(g2, point1);
        
        g2.setColor(point2Color);
        drawPoint(g2, point2);
    }
	
	private void drawLines(Graphics2D g2) {
		int leftX = (int) (posX + point1.x*zoomLevel);
		int rightX  = (int) (posX + point2.x*zoomLevel);
		int topY = (int) (posY + point1.y*zoomLevel);
		int bottomY  = (int) (posY + point2.y*zoomLevel);
		
		drawRectangleLines(g2, leftX, rightX, topY, bottomY);
	}
	
	private void drawRectangleLines(Graphics2D g2, int leftX, int rightX, int topY, int bottomY) {
		g2.drawLine(leftX, topY, leftX, bottomY);  // left
		g2.drawLine(leftX, topY, rightX, topY);  // top
		g2.drawLine(leftX, bottomY, rightX, bottomY);  // bottom
		g2.drawLine(rightX, topY, rightX, bottomY);  // right
	}
	
	private void drawPoint(Graphics2D g2, Point p) {
//		int radius = (int) (POINT_RADIUS * zoomLevel);
//		int size = (int) (POINT_SIZE * zoomLevel);
        // g2.fill(new Ellipse2D.Double(posX + p.x*zoomLevel - radius, posY + p.y*zoomLevel - radius, size, size));
		float[] coords = getPointCoordinates(p);
		g2.fill(new Ellipse2D.Double(coords[0], coords[1], coords[2], coords[3]));
	}
	
	private void drawBackgroundGrid(Graphics2D g2) {
//		// just for testing
//		g2.setColor(Color.GREEN);
//		drawRectangleLines(g2, posX, posX + imageWidth, posY, posY + imageHeight);
		
		int limitX = posX + imageWidth;
		int limitY = posY + imageHeight;
		
		g2.setColor(GRID_COLOR1);
		
		for (int i = posX; i < limitX; i += GRID_SIZE * 2) {
			for (int j = posY; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		for (int i = posX + GRID_SIZE; i < limitX; i += GRID_SIZE * 2) {
			for (int j = posY + GRID_SIZE; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		
		g2.setColor(GRID_COLOR2);
		
		for (int i = posX + GRID_SIZE; i < limitX; i += GRID_SIZE * 2) {
			for (int j = posY; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
		for (int i = posX; i < limitX; i += GRID_SIZE * 2) {
			for (int j = posY + GRID_SIZE; j < limitY; j += GRID_SIZE * 2) {
				g2.fillRect(i, j, 
						i + GRID_SIZE > limitX ? limitX - i : GRID_SIZE,
								j + GRID_SIZE > limitY ? limitY - j : GRID_SIZE);
			}
		}
	}
	
	private void updatePosition() {
        // 0, 0 -> upper left corner
 		Dimension panelSize = getSize();
 		imageWidth = (int) (image.getWidth() * zoomLevel);
 		imageHeight = (int) (image.getHeight() * zoomLevel);
 		posX = (panelSize.width - imageWidth) / 2;
 		posY = (panelSize.height - imageHeight) / 2;
	}
	
	public void setUVCoordinates(int[] coordinates) {
		assert coordinates.length == 4;
		
		point1.x = coordinates[0];
		point1.y = coordinates[1];
		point2.x = coordinates[2];
		point2.y = coordinates[3];
		
		repaint();
	}
	
	// returns (x, y, width, height)
	private float[] getPointCoordinates(Point p) {
		return new float[] { posX + p.x*zoomLevel - pointRadius, posY + p.y*zoomLevel - pointRadius, pointRadius*2, pointRadius*2 };
	}
	
	private Point toImageSpace(int x, int y) {
		return new Point((int) ((x - posX) / zoomLevel), (int) ((y - posY) / zoomLevel));
	}
	
	private boolean isInPoint(Point p, int x, int y) {
		float[] coords = getPointCoordinates(p);
		Rectangle rect = new Rectangle((int) coords[0], (int) coords[1], (int) coords[2], (int) coords[3]);
		return rect.contains(x, y);
	}
	
	
	private class ImageMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			int x = arg0.getX();
			int y = arg0.getY();
			
			selectionIndex = SELECT_NONE;
			
			if (isInPoint(point1, x, y)) {
				point1Color = POINT_COLOR_SELECT;
				selectionIndex = SELECT_POINT1;
			} else {
				point1Color = POINT_COLOR;
			}
			
			if (isInPoint(point2, x, y)) {
				point2Color = POINT_COLOR_SELECT;
				selectionIndex = SELECT_POINT2;
			} else {
				point2Color = POINT_COLOR;
			}
			
			if (selectionIndex != SELECT_NONE) {
				ImageEditPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				ImageEditPanel.this.setCursor(null);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			int x = arg0.getX();
			int y = arg0.getY();
			
			checkPointHover(x, y);
			
			repaint();
		}
		
	}
	
	private class ImageMouseMotionListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent arg0) {
			int x = arg0.getX();
			int y = arg0.getY();
			if (selectionIndex == SELECT_POINT1) {
				
				point1.setLocation(toImageSpace(x, y));
				parent.updateCoordinates(point1, point2);
				
			} else if (selectionIndex == SELECT_POINT2) {
				
				point2.setLocation(toImageSpace(x, y));
				parent.updateCoordinates(point1, point2);
				
			}
			
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			int x = arg0.getX();
			int y = arg0.getY();
			
			checkPointHover(x, y);
			
			repaint();
		}

	}
	
	private void checkPointHover(int x, int y) {
		boolean isHovered = false;

		if (isInPoint(point1, x, y)) {
			point1Color = POINT_COLOR_HOVER;
			isHovered = true;
		} else {
			point1Color = POINT_COLOR;
		}
		
		if (isInPoint(point2, x, y)) {
			point2Color = POINT_COLOR_HOVER;
			isHovered = true;
		} else {
			point2Color = POINT_COLOR;
		}
		
		if (isHovered) {
			ImageEditPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			ImageEditPanel.this.setCursor(null);
		}
	}
	
//	protected void updatePreview(){
//		if (previewDialog != null) {
//			previewDialog.update();
//		}
//	}
}
