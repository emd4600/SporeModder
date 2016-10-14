package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;
import javax.swing.text.View;

import sporemodder.userinterface.fileview.TextFileView;

public class SearchedTextView extends PlainView {

	private TextFileView textFileView;
	private SortedMap<Integer, Integer> searchWordStarts = new TreeMap<Integer, Integer>();
	
	public SearchedTextView(Element arg0, TextFileView view) {
		super(arg0);
		this.textFileView = view;
		getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
	}

	private static JComponent getJComponent(View view) {
	    if (view != null) {
	        Component component = view.getContainer();
	        if (component instanceof JComponent) {
	            return (JComponent)component;
	        }
	    }
	    return null;
	}

	private static FontMetrics getFontMetrics(JComponent c, Graphics g, Font font) {
        if (c != null) {
            // Note: We assume that we're using the FontMetrics
            // from the widget to layout out text, otherwise we can get
            // mismatches when printing.
            return c.getFontMetrics(font);
        }
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }
	
	protected final int drawTabbedText(Segment s, int x, int y,
			Graphics g, TabExpander e, int startOffset) {
		return drawTabbedText(null, s, x, y, g, e, startOffset);
	}
	
	protected final int drawTabbedText(View view, Segment s, int x, int y,
			Graphics g, TabExpander e, int startOffset) {
		JComponent component = getJComponent(view);
		FontMetrics metrics = getFontMetrics(component, g, g.getFont());
		int nextX = x;
		char[] txt = s.array;
		int txtOffset = s.offset;
		int flushLen = 0;
		int flushIndex = s.offset;
		int spaceAddon = 0;
		int spaceAddonLeftoverEnd = -1;
		int startJustifiableContent = 0;
		int endJustifiableContent = 0;

		int n = s.offset + s.count;
		for (int i = txtOffset; i < n; i++) {
			if (txt[i] == '\t'
				|| ((spaceAddon != 0 || i <= spaceAddonLeftoverEnd)
					&& (txt[i] == ' ')
					&& startJustifiableContent <= i
					&& i <= endJustifiableContent
					)) {
				if (flushLen > 0) {
					//nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
					
					drawSearchWordChars(g, metrics, txt, flushIndex, flushLen, x, y);
					//setSearchWordColor(g, s.array, flushIndex, flushLen, x, y);
					g.drawChars(txt, flushIndex, flushLen, x, y);
					nextX = x + metrics.charsWidth(txt, flushIndex, flushLen);
					flushLen = 0;
				}
				flushIndex = i + 1;
				if (txt[i] == '\t') {
					if (e != null) {
						nextX = (int) e.nextTabStop((float) nextX, startOffset + i - txtOffset);
					} else {
						nextX += metrics.charWidth(' ');
					}
				} else if (txt[i] == ' ') {
					nextX += metrics.charWidth(' ') + spaceAddon;
					if (i <= spaceAddonLeftoverEnd) {
						nextX++;
					}
				}
				x = nextX;
			} else if ((txt[i] == '\n') || (txt[i] == '\r')) {
				if (flushLen > 0) {
					//nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
					
					drawSearchWordChars(g, metrics, txt, flushIndex, flushLen, x, y);
					//drawSearchWordChars(g, txt, flushIndex, flushLen, x, y);
					//g.drawChars(txt, flushIndex, flushLen, x, y);
					nextX = x + metrics.charsWidth(txt, flushIndex, flushLen);
					flushLen = 0;
				}
				flushIndex = i + 1;
				x = nextX;
			} else {
				flushLen += 1;
			}
		}
		if (flushLen > 0) {
			//nextX = SwingUtilities2.drawChars(component, g, txt, flushIndex, flushLen, x, y);
			
			//setSearchWordColor(g, s.array, flushIndex, flushIndex + flushLen, x, y);
			drawSearchWordChars(g, metrics, txt, flushIndex, flushLen, x, y);
			g.drawChars(txt, flushIndex, flushLen, x, y);
			nextX = x + metrics.charsWidth(txt, flushIndex, flushLen);
		}
		
		return nextX;
	}
	
	//TODO sometimes it doesn't render the entire text
	private void drawSearchWordChars(Graphics graphics, FontMetrics metrics, char[] text, int start, int len, int x, int y) {
		int oldStart = start;
		if (searchWordStarts != null) {
			for (Map.Entry<Integer, Integer> entry : searchWordStarts.entrySet()) {
				// if the given string contains a searchable word
				int startIndex = entry.getKey();
				int endIndex = entry.getValue();
				if (startIndex >= start && endIndex <= start + len) {
					// draw the remaining non-search text first
					int nonSearchedTextLen = startIndex - start;
					if (nonSearchedTextLen > 0) {
						graphics.drawChars(text, start, nonSearchedTextLen, x, y);
						x += metrics.charsWidth(text, start, nonSearchedTextLen);
						start += nonSearchedTextLen;
					}
					Rectangle2D rect = metrics.getStringBounds(text, start, endIndex, graphics);
					int textWidth = (int) rect.getWidth();
					Color oldColor = graphics.getColor();
					graphics.setColor(TextFileView.SEARCH_COLOR);
					graphics.fillRect(x, y - metrics.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());
					graphics.setColor(Color.BLACK);
					graphics.drawChars(text, start, endIndex - startIndex, x, y);
					x += metrics.charsWidth(text, start, endIndex - startIndex);
					start += endIndex - startIndex;
					// restore the old color
					// graphics.setColor(oldColor);
				}
			}
		}
		// draw the remaining non-search text
		int nonSearchedTextLen = oldStart + len - start;
		if (nonSearchedTextLen > 0) {
			graphics.drawChars(text, start, nonSearchedTextLen, x, y);
		}
	}
	
	protected void findSearchWords(Segment segment) {
		searchWordStarts.clear();
		if (textFileView != null) {
			List<String> searchTerms = textFileView.getSearchTerms();
			if (searchTerms != null) {
				String text = new String(segment.array);
				for (String s : searchTerms) {
					int indexOf = 0;
					int length = 0;
					while ((indexOf = text.indexOf(s, indexOf + length)) != -1) {
						length = s.length();
//						startMap.put(indexOf, indexOf + length);
//						colorMap.put(indexOf, TextFileView.SEARCH_COLOR);
						searchWordStarts.put(indexOf, indexOf + length);
					}
				}
			}
		}
	}
	
	protected void findSearchWords(String text, int startPos, SortedMap<Integer, Integer> startMap, SortedMap<Integer, Color> colorMap) {
		searchWordStarts.clear();
		if (textFileView != null) {
			List<String> searchTerms = textFileView.getSearchTerms();
			if (searchTerms != null) {
				for (String s : searchTerms) {
					int indexOf = 0;
					int length = 0;
					while ((indexOf = text.indexOf(s, indexOf + length)) != -1) {
						length = s.length();
//						startMap.put(indexOf, indexOf + length);
//						colorMap.put(indexOf, TextFileView.SEARCH_COLOR);
						searchWordStarts.put(startPos + indexOf, startPos + indexOf + length);
					}
				}
			}
		}
	}
}
