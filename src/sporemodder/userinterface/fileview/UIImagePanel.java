package sporemodder.userinterface.fileview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;

public class UIImagePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7240140487928742946L;
	
	private BufferedImage originalImage;
	private BufferedImage image;
	private JScrollPane parent;
	private float zoomLevel = 1.0f;
	private Color backgroundColor;
	
	public UIImagePanel(BufferedImage image, JScrollPane parent) {
		super();
		this.image = image;
		this.originalImage = image;
		this.parent = parent;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public float getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(float zoomLevel) {
		this.zoomLevel = zoomLevel;
		revalidate();  // update parent scrollPane
		repaint();
	}
	
	public void increaseZoom(float value) {
		setZoomLevel(zoomLevel + value);
	}
	
	@Override
	public Dimension getPreferredSize() {
        int w = (int)(zoomLevel * image.getWidth());
        int h = (int)(zoomLevel * image.getHeight());
        return new Dimension(w, h);
    }

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// 0, 0 -> upper left corner
		Dimension panelSize = getSize();
		int imageWidth = (int) (image.getWidth() * zoomLevel);
		int imageHeight = (int) (image.getHeight() * zoomLevel);
		int posX = (panelSize.width - imageWidth) / 2;
		int posY = (panelSize.height - imageHeight) / 2;
		
		if (backgroundColor != null) {
			g.drawImage(image, posX, posY, imageWidth, imageHeight, backgroundColor, this);
		}
		else {
			g.drawImage(image, posX, posY, imageWidth, imageHeight, this);
		}
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	private static BufferedImage copyImage(BufferedImage image) {
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		
		WritableRaster raster = result.getRaster();
		image.copyData(raster);
		
		return result;
	}
	
	public void setChannels(boolean alpha, boolean red, boolean green, boolean blue) {
		// copy the image only once, and only when we need it
		if (image == originalImage) {
			image = copyImage(originalImage);
		}
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		// we do this so we only check them once and not for each pixel
		int alphaMask = alpha ? 0xFF000000 : 0;
		int redMask = red ? 0x00FF0000 : 0;
		int greenMask = green ? 0x0000FF00 : 0;
		int blueMask = blue ? 0x000000FF : 0;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = originalImage.getRGB(x, y);
				int newColor = 0;
				newColor |= rgb & alphaMask;
				if (!alpha) newColor = 0xFF000000;
				newColor |= rgb & redMask;
				newColor |= rgb & greenMask;
				newColor |= rgb & blueMask;
				image.setRGB(x, y, newColor);
			}
		}
		repaint();
	}
	
	// thanks to http://stackoverflow.com/questions/10243257/java-scroll-image-by-mouse-dragging !
//	public static class HandScrollListener extends MouseAdapter {
//		private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//	    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//	    private final Point pp = new Point();
//	    private UIImagePanel component;
//	    
//	    public HandScrollListener(UIImagePanel component) {
//	    	this.component = component;
//	    }
//	    
//		public void mouseDragged(final MouseEvent e)
//	    {
//			JViewport vport = component.parent.getViewport();
//	        // JViewport vport = (JViewport) e.getSource();
//	        Point cp = e.getPoint();
//	        Point vp = vport.getViewPosition();
//	        vp.translate(pp.x-cp.x, pp.y-cp.y);
//	        component.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
//	        pp.setLocation(cp);
////	        component.repaint();
//	    }
//
//	    public void mousePressed(MouseEvent e)
//	    {
//	    	if (component.parent.isShowing()) {
//	    		component.setCursor(hndCursor);
//		        pp.setLocation(e.getPoint());
//	    	}
//	    }
//
//	    public void mouseReleased(MouseEvent e)
//	    {
//	    	if (component.parent.isShowing()) {
//	    		component.setCursor(defCursor);
//		        component.repaint();
//	    	}
//	    }
//	}
	
	public class HandScrollListener extends MouseAdapter {
		private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	    private final Point pp = new Point();
	    
//	    public HandScrollListener(UIImagePanel component) {
//	    	this.component = component;
//	    }
	    
		public void mouseDragged(final MouseEvent e)
	    {
			JViewport vport = parent.getViewport();
	        // JViewport vport = (JViewport) e.getSource();
	        Point cp = e.getPoint();
	        Point vp = vport.getViewPosition();
	        vp.translate(pp.x-cp.x, pp.y-cp.y);
	        scrollRectToVisible(new Rectangle(vp, vport.getSize()));
	        pp.setLocation(cp);
//	        component.repaint();
	    }

	    public void mousePressed(MouseEvent e)
	    {
	    	if (parent.isShowing()) {
	    		setCursor(hndCursor);
		        pp.setLocation(e.getPoint());
	    	}
	    }

	    public void mouseReleased(MouseEvent e)
	    {
	    	if (parent.isShowing()) {
	    		setCursor(defCursor);
		        repaint();
	    	}
	    }
	}
	
	protected static class ViewportDragScrollListener extends MouseAdapter implements
		    HierarchyListener {
		private static final int SPEED = 4;
		private static final int DELAY = 10;
		private final Cursor dc;
		private final Cursor hc = Cursor
		        .getPredefinedCursor(Cursor.HAND_CURSOR);
		private final Timer scroller;
		private final UIImagePanel comp;
		private final Point startPt = new Point();
		private final Point move = new Point();
		private boolean autoScroll = false;
		
		public ViewportDragScrollListener(UIImagePanel component, boolean autoScroll) {
		    this.comp = component;
		    this.autoScroll = autoScroll;
		    this.dc = comp.getCursor();
		    this.scroller = new Timer(DELAY, new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            JViewport vport = comp.parent.getViewport();
		            Point vp = vport.getViewPosition();
		            vp.translate(move.x, move.y);
		            comp.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
		        }
		    });
		}
		
		public void hierarchyChanged(HierarchyEvent e) {
		    JComponent c = (JComponent) e.getSource();
		    if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
		            && !c.isDisplayable() && autoScroll) {
		        scroller.stop();
		    }
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
		    JViewport vport = comp.parent.getViewport();
		    Point pt = e.getPoint();
		    int dx = startPt.x - pt.x;
		    int dy = startPt.y - pt.y;
		    Point vp = vport.getViewPosition();
		    vp.translate(dx, dy);
		    comp.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
		    move.setLocation(SPEED * dx, SPEED * dy);
		    startPt.setLocation(pt);
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		    ((JComponent) e.getSource()).setCursor(hc);
		    startPt.setLocation(e.getPoint());
		    move.setLocation(0, 0);
		    if (autoScroll) {
		        scroller.stop();
		    }
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		    ((JComponent) e.getSource()).setCursor(dc);
		    if (autoScroll) {
		        scroller.start();
		    }
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
		    ((JComponent) e.getSource()).setCursor(dc);
		    move.setLocation(0, 0);
		    if (autoScroll) {
		        scroller.stop();
		    }
		}
		}
}
