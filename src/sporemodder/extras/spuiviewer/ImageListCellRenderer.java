package sporemodder.extras.spuiviewer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ImageListCellRenderer implements ListCellRenderer<ImagePanel> {
	
	private JList<ImagePanel> imageList;
	
	public ImageListCellRenderer(JList<ImagePanel> imageList) {
		super();
		this.imageList = imageList;
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends ImagePanel> list, ImagePanel value, int index,
			boolean isSelected, boolean cellHasFocus) {
		
		System.out.println(value.getPreferredSize());
		if (isSelected) {
			value.setBackground(imageList.getSelectionBackground());
			value.setForeground(imageList.getSelectionForeground());
		} else {
			value.setBackground(imageList.getBackground());
			value.setForeground(imageList.getForeground());
		}
		Border border = null;
		if (cellHasFocus) {
		    if (isSelected) {
		        border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
		    }
		    if (border == null) {
		        border = UIManager.getBorder("List.focusCellHighlightBorder");
		    }
		} else {
		    border = new EmptyBorder(1, 1, 1, 1);
		}
		value.setBorder(border);
		 
		return value;
	}
}
