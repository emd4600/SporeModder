package sporemodder.userinterface;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import sporemodder.utilities.ProjectTreeNode;

public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2977100311321042080L;
	
	private static final ImageIcon ICON_XML = createImageIcon("/sporemodder/userinterface/images/Icon_XML_24.png", "Save");
	private static final ImageIcon ICON_SPUI = createImageIcon("/sporemodder/userinterface/images/Icon_SPUI_24.png", "Save");
	
	public static Color COLOR_SOURCE_MOD = Color.BLACK;
	public static Color COLOR_SOURCE = new Color(0xBF2F2F);
	public static Color COLOR_MOD = new Color(0x00C000);

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		JLabel label = (JLabel) c;
		
		if (value instanceof ProjectTreeNode) {
			ProjectTreeNode node = (ProjectTreeNode) value;
			
			if (!node.isRoot) {
				if (node.isSource && node.isMod) {
					c.setForeground(COLOR_SOURCE_MOD);
				}
				else if (node.isSource) {
					c.setForeground(COLOR_SOURCE);
//					c.setForeground(Color.GRAY);
				}
				else if (node.isMod) {
					c.setForeground(COLOR_MOD);
//					c.setForeground(new Color(0x94ADEE));
				}
				
				if (node.name.endsWith(".xml")) {
					label.setIcon(ICON_XML);
				}
				else if (node.name.endsWith(".spui")) {
					label.setIcon(ICON_SPUI);
				}
			}
			label.setText(node.name);
			
			
//			System.out.println(node.isVisible);
			
//			label.setVisible(node.isVisible);
//			if (!node.isVisible) {
//				return nullLabel;
////				label.setPreferredSize(new Dimension(0, 0));
//			} else {
//				
//				//label.setPreferredSize(null);
//				
//				return label;
//			}
		}
		
		return label;
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = ProjectTreeCellRenderer.class.getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	public static String generateColorString(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}
	
	public static Color hex2Rgb(String colorStr) {
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}
}
