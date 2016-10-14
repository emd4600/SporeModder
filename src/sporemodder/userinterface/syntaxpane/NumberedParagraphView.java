package sporemodder.userinterface.syntaxpane;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Map;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;

public class NumberedParagraphView extends ParagraphView {
	
	private static final int NUMBERS_WIDTH = 25;
	
	private static Map<?, ?> desktopHints = null;
	
	public NumberedParagraphView(Element arg0) {
		super(arg0);
		short top = 0;
        short left = 0;
        short bottom = 0;
        short right = 0;
        this.setInsets(top, left, bottom, right);
	}

	@Override
	protected void setInsets(short top, short left, short bottom, short right) {
		super.setInsets(top, (short)(left + NUMBERS_WIDTH), bottom, right);
	}
	
	public int getPreviousLineCount() {
		int lineCount = 0;
		View parent = getParent();
		int count = parent.getViewCount();
		
		for (int i = 0; i < count; i++) {
			if (parent.getView(i) == this) {
				break;
			}
			else {
				lineCount += parent.getView(i).getViewCount();
			}
		}
		return lineCount;
	}
	
	@Override
	public void paintChild(Graphics g, Rectangle r, int n) {
		super.paintChild(g, r, n);
		int previousLineCount = getPreviousLineCount();
		int numberX = r.x - getLeftInset();
		int numberY = r.y + r.height - 5;
		if (desktopHints == null) { 
		    Toolkit tk = Toolkit.getDefaultToolkit(); 
		    desktopHints = (Map<?, ?>) (tk.getDesktopProperty("awt.font.desktophints")); 
		}
		if (desktopHints != null) { 
		    ((Graphics2D)g).addRenderingHints(desktopHints); 
		}
		g.drawString(Integer.toString(previousLineCount + n + 1), numberX, numberY);
	}
}
