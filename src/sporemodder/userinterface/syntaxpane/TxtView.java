package sporemodder.userinterface.syntaxpane;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.TextFileView;
import sporemodder.utilities.names.NameHashPair;
import sporemodder.utilities.names.NameRegistry;

public class TxtView extends HighlightedView {

	public TxtView(Element arg0, TextFileView textFileView) {
		super(arg0, textFileView);
	}

	
	@Override
	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0, int p1) throws BadLocationException {
		addRenderingHints(graphics);
		
		Document doc = getDocument();
		String text = doc.getText(p0, p1 - p0);
		Segment segment = getLineBuffer();
		
		SortedMap<Integer, Integer> searchStartMap = new TreeMap<Integer, Integer>();
		SortedMap<Integer, Integer> startMap = new TreeMap<Integer, Integer>();
		SortedMap<Integer, Color> colorMap = new TreeMap<Integer, Color>();
		
		// first search the searched words
		processSearchedWords(text, searchStartMap, startMap, colorMap);
		
		int i = 0;
		// Colour the parts
		for (Map.Entry<Integer, Integer> entry : startMap.entrySet()) {
			
			int start = entry.getKey();
			int end = entry.getValue();
			
			if (i < start) {
				graphics.setColor(Color.black);
				doc.getText(p0 + i, start - i, segment);
				x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
			}
			
			graphics.setColor(colorMap.get(start));
			i = end;
			doc.getText(p0 + start, i - start, segment);
			drawHighlightedWord(searchStartMap, segment, start, x, y, graphics);
			x = Utilities.drawTabbedText(segment, x, y, graphics, this, start);
		}
		
		// Paint possible remaining text black
		if (i < text.length()) {
			graphics.setColor(Color.black);
			doc.getText(p0 + i, text.length() - i, segment);
			x = Utilities.drawTabbedText(segment, x, y, graphics, this, i);
		}
		
		return x;
	}
	
	public static class TxtViewFactory implements ViewFactory {
		
		private TextFileView textFileView;
		
		public TxtViewFactory(TextFileView textFileView) {
			this.textFileView = textFileView;
		}

		@Override
		public View create(Element arg0) {
			return new TxtView(arg0, textFileView);
		}
		
	}
	
	public static class TxtEditorKit extends StyledEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8878195176046066538L;
	
		private ViewFactory txtViewFactory;
		
		public TxtEditorKit(TextFileView textFileView) {
			txtViewFactory = new TxtViewFactory(textFileView);
		}
		
		@Override
		public ViewFactory getViewFactory() {
			return txtViewFactory;
		}
		
		@Override
		public String getContentType() {
			return "text/txt";
		}
	}
}
