package sporemodder.userinterface.fileview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

import sporemodder.MainApp;
import sporemodder.files.formats.BitMapImages;
import sporemodder.files.formats.BitMapImages.BitMapType;
import sporemodder.files.formats.dds.DDSHeader;
import sporemodder.files.formats.dds.DDSImageReader;
import sporemodder.files.formats.dds.DDSImageReaderSpi;
import sporemodder.files.formats.dds.DDSTexture;
import sporemodder.files.formats.rast.RASTMain;
import sporemodder.files.formats.renderWare4.RW4Main;
import sporemodder.userinterface.ErrorManager;

public class ImageFileView extends FileView {
	
	public static final int VIEWTYPE_IMAGE = 2;
	
	public static final List<String> VALID_EXTENSIONS = Arrays.asList(".dds", ".rast", ".raster", ".rw4", ".png", ".jpg", ".jpeg",
			".#03E421E9", ".#03E421EC", ".#03E421ED", ".#03E421EF", ".bitImage", ".8bitImage", ".32bitImage", ".48bitImage"); 
	
	private static final FileNameExtensionFilter FILEFILTER_DDS = new FileNameExtensionFilter("Direct Draw Surface Texture (*.dds)", "dds");
	private static final FileNameExtensionFilter FILEFILTER_PNG = new FileNameExtensionFilter("Portable Network Graphics (*.png)", "png");
	
	/**
	 * The minimum value the zoom can have. The image won't be zoomed out if the zoom is below this number.
	 * <b>Note:</b> A zoom value of 1.0 is the real scale image.
	 */
	private static final float MIN_ZOOM = 0.25f;
	/**
	 * The maximum value the zoom can have. The image won't be zoomed in if the zoom is above this number.
	 * <b>Note:</b> A zoom value of 1.0 is the real scale image.
	 */
	private static final float MAX_ZOOM = 10.0f;
	
	/**
	 * This value determines how many zoom is increased/decreased each time the zoom key is pressed.
	 */
	private static final float ZOOM_STEP = 0.25f;
	
	/**
	 * The main panel that contains everything else.
	 */
	private JPanel mainPanel;
	/**
	 * A <code>JScrollPane</code> that contains the image, and occupies most of the main panel. 
	 * It's scrollable so the user can still see the image even if it's too big.
	 */
	private JScrollPane scrollPane;
	/**
	 * This <code>JPanel</code> contains the image. This panel must have the key listeners for the zoom functionality.
	 */
	private UIImagePanel imagePanel;
	/**
	 * The file this FileView represents.
	 */
	private File file;
	/**
	 * The <code>BufferedImage</code> that is being displayed. We keep track of this to display the appropiate info.
	 */
	private BufferedImage image = null;
	/**
	 * An object holding the DDS information to be shown in the info panel. 
	 */
	private DDSHeader ddsInfo;
	
	/**
	 * This panel goes in the right side, and has buttons for hiding/showing the multiple color channels and changing the background color.
	 */
	private ImageOptionsPanel imageOptionsPanel;
	
	/**
	 * This panel, that should go on the bottom left corner, must have the "Zoom: " label and the zoom spinner.
	 */
	private JPanel zoomPanel;
	private JLabel lblZoom;
	private final SpinnerModel zoomSpinnerModel = new SpinnerNumberModel(1, MIN_ZOOM, MAX_ZOOM, ZOOM_STEP);
	private JSpinner zoomSpinner;
	
	private JPanel buttonsPanel;
	private JButton btnSavePNG;
	private JButton btnSaveDDS;
	private JButton btnInfo;
 
	public ImageFileView(String path) {
		super(path);
	}

	public ImageFileView(String path, File file) {
		super(path, file);
	}

	@Override
	public JComponent getPanel() {
		
		file = MainApp.getCurrentProject().getFile(path);
		String fileName = file.getName();
		
		try {
			if (fileName.endsWith(".dds")) {
				DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
				image = DDSTexture.toBufferedImage(file, reader);
				ddsInfo = reader.ddsHeader;
			}
			else if (fileName.endsWith(".rast") || fileName.endsWith(".raster")) {
				DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
				image =  RASTMain.toBufferedImage(file, reader);
				ddsInfo = reader.ddsHeader;
			}
			else if (fileName.endsWith(".rw4")) {
				DDSImageReader reader = new DDSImageReader(new DDSImageReaderSpi());
				image = RW4Main.toBufferedImage(file, reader);
				ddsInfo = reader.ddsHeader;
			}
			else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
				image = ImageIO.read(file);
			}
			else if (fileName.endsWith(".#03E421E9") || fileName.endsWith(".bitImage")) {
				image = BitMapImages.read(file, BitMapType.BIT_1);
			}
			else if (fileName.endsWith(".#03E421EC") || fileName.endsWith(".8bitImage")) {
				image = BitMapImages.read(file, BitMapType.BIT_8);
			}
			else if (fileName.endsWith(".#03E421ED") || fileName.endsWith(".32bitImage")) {
				image = BitMapImages.read(file, BitMapType.BIT_32);
			}
			else if (fileName.endsWith(".#03E421EF") || fileName.endsWith(".48bitImage")) {
				image = BitMapImages.read(file, BitMapType.BIT_48);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (image == null) {
			return null;
		}
		
		scrollPane = new JScrollPane();
		
		imagePanel = new UIImagePanel(image, scrollPane);
		imagePanel.addMouseWheelListener(new ImageMouseWheelListener());
		imagePanel.addKeyListener(new ImageKeyListener());
		imagePanel.addMouseListener(new ImageMouseListener(imagePanel));
		imagePanel.setFocusable(true);
		imagePanel.requestFocusInWindow();
		
//		HandScrollListener l = imagePanel.new HandScrollListener();
//		imagePanel.addMouseListener(l);
//		imagePanel.addMouseMotionListener(l);
		
		UIImagePanel.ViewportDragScrollListener l = new UIImagePanel.ViewportDragScrollListener(imagePanel, false);
		
		imagePanel.addMouseListener(l);
		imagePanel.addMouseMotionListener(l);
		
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new ImageKeyDispatcher());
		
		scrollPane.setViewportView(imagePanel);
//		scrollPane.addKeyListener(new ImageKeyListener());
//		scrollPane.setFocusable(true);
//		scrollPane.requestFocusInWindow();
		
		imageOptionsPanel = new ImageOptionsPanel(imagePanel);
		JPanel viewPanel = new JPanel();
		viewPanel.setLayout(new BorderLayout());
		viewPanel.add(scrollPane, BorderLayout.CENTER);
		viewPanel.add(imageOptionsPanel, BorderLayout.EAST);
		
		/* -- ZOOM PANEL -- */
		
		zoomPanel = new JPanel();
		zoomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		zoomSpinner = new JSpinner(zoomSpinnerModel);
		zoomSpinner.setEditor(new JSpinner.NumberEditor(zoomSpinner, "####%"));
		zoomSpinner.addChangeListener(new ZoomSpinnerListener());
		
		lblZoom = new JLabel("Zoom: ");
		zoomPanel.add(lblZoom);
		zoomPanel.add(zoomSpinner);
		
		/* -- BUTTONS PANEL -- */
		btnSavePNG = new JButton("Save as PNG");
		btnSavePNG.addActionListener(new ALSavePNG());
		if (ddsInfo != null) {
			btnSaveDDS = new JButton("Save as DDS");
			btnSaveDDS.addActionListener(new ALSaveDDS());
		}
		btnInfo = new JButton("Info");
		btnInfo.addActionListener(new ALInfo());
		
		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(btnInfo);
		if (ddsInfo != null) buttonsPanel.add(btnSaveDDS);
		buttonsPanel.add(btnSavePNG);
		
		/* -- SOUTH PANEL -- */
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(zoomPanel, BorderLayout.WEST);
		southPanel.add(buttonsPanel, BorderLayout.EAST);
		
		/* -- MAIN PANEL -- */
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(viewPanel, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.addMouseListener(new ImageMouseListener(mainPanel));
		mainPanel.addKeyListener(new ImageKeyListener());
		mainPanel.setFocusable(true);
		mainPanel.requestFocusInWindow();
		
		return mainPanel;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setEditable(boolean editable) {
		return;
	}
	
	private void zoom(float value) {
		float currentZoom = imagePanel.getZoomLevel();
		if (currentZoom + value > MAX_ZOOM || currentZoom + value < MIN_ZOOM) {
			return;
		}
//		imagePanel.increaseZoom(value);
		zoomSpinner.setValue((double)(currentZoom + value));
	}

	private class ImageKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			if ((arg0.getKeyCode() == KeyEvent.VK_PLUS || arg0.getKeyCode() == KeyEvent.VK_ADD) && (arg0.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
				zoom(ZOOM_STEP);
//				zoomSpinner.setValue(((Double)zoomSpinner.getValue()).floatValue() + ZOOM_STEP);
			}
			else if ((arg0.getKeyCode() == KeyEvent.VK_MINUS || arg0.getKeyCode() == KeyEvent.VK_SUBTRACT) && (arg0.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
				zoom(-ZOOM_STEP);
//				zoomSpinner.setValue(((Double)zoomSpinner.getValue()).floatValue() - ZOOM_STEP);
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ImageMouseListener implements MouseListener {
		
		private JComponent comp;
		private ImageMouseListener(JComponent comp) {
			this.comp = comp;
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			comp.requestFocusInWindow();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ImageMouseWheelListener implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent arg0) {
			if ((arg0.getModifiersEx() & MouseWheelEvent.ALT_DOWN_MASK) == MouseWheelEvent.ALT_DOWN_MASK) {
				// we must change the sign
				zoom((float) (-arg0.getPreciseWheelRotation() * ZOOM_STEP));
//				zoomSpinner.setValue(((Double)zoomSpinner.getValue()).floatValue() - arg0.getPreciseWheelRotation() * ZOOM_STEP);
			}
		}
		
	}
	
	private class ZoomSpinnerListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
//			zoom(((Double) zoomSpinner.getValue()).floatValue());
			imagePanel.setZoomLevel(((Double) zoomSpinner.getValue()).floatValue());
		}
		
	}
	
	private static void setDefaultFile(JFileChooser chooser, String lastPath, String fileName) {
		try {
	        FileChooserUI fcUi = chooser.getUI();
	        chooser.setSelectedFile(new File(lastPath + "\\" + fileName));
	        Class<? extends FileChooserUI> fcClass = fcUi.getClass();
	        Method setFileName = fcClass.getMethod("setFileName", String.class);
	        setFileName.invoke(fcUi, fileName);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private class ALSavePNG implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(FILEFILTER_PNG);
			setDefaultFile(chooser, MainApp.getLastFileChooserPath().getAbsolutePath(), file.getName() + ".png");
			
			if (chooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
				File outputFile = chooser.getSelectedFile();
				MainApp.setLastFileChooserPath(outputFile.getParentFile());
				try {
					ImageIO.write(image, "PNG", outputFile);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(mainPanel, "Error converting to PNG:\n" + ErrorManager.getStackTraceString(e), 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
	}
	
	private class ALSaveDDS implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(FILEFILTER_DDS);
			setDefaultFile(chooser, MainApp.getLastFileChooserPath().getAbsolutePath(), file.getName() + ".dds");
			
			if (chooser.showSaveDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
				File outputFile = chooser.getSelectedFile();
				MainApp.setLastFileChooserPath(outputFile.getParentFile());
				String fileName = file.getName();
				
				if (fileName.endsWith(".dds")) {
					try {
						Files.copy(file.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(mainPanel, "Error converting to DDS:\n" + ErrorManager.getStackTraceString(e1), 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else if (fileName.endsWith(".rw4")) {
					try {
						RW4Main.rw4ToTexture(file, outputFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(mainPanel, "Error converting to DDS:\n" + ErrorManager.getStackTraceString(e1), 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else if (fileName.endsWith(".rast") || fileName.endsWith(".raster")) {
					try {
						RASTMain.rastToDDS(file, outputFile);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(mainPanel, "Error converting to DDS:\n" + ErrorManager.getStackTraceString(e1), 
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					JOptionPane.showMessageDialog(mainPanel, "Selected image is not a DDS image.", 
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		
	}
	
	private class ALInfo implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			new InfoDialog();
		}
		
	}
	
	private class InfoDialog extends JDialog {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8591606858149295605L;

		private String getTypeString(int type) {
			switch(type) {
			case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
			case BufferedImage.TYPE_4BYTE_ABGR: return "ABGR";
			case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "ABGR Premultiplied";
			case BufferedImage.TYPE_BYTE_BINARY: return "Binary";
			case BufferedImage.TYPE_BYTE_GRAY: return "Grayscale";
			case BufferedImage.TYPE_BYTE_INDEXED: return "Indexed";
			case BufferedImage.TYPE_CUSTOM: return "CUSTOM";
			case BufferedImage.TYPE_INT_ARGB: return "ARGB";
			case BufferedImage.TYPE_INT_ARGB_PRE: return "ARGB Premultiplied";
			case BufferedImage.TYPE_INT_BGR: return "BGR Int";
			case BufferedImage.TYPE_INT_RGB: return "RGB";
			case BufferedImage.TYPE_USHORT_555_RGB: return "5-5-5 RGB";
			case BufferedImage.TYPE_USHORT_565_RGB: return "5-6-5 RGB";
			case BufferedImage.TYPE_USHORT_GRAY: return "Grayscale UShort";
			default: return "CUSTOM";
			}
		}
		
		public InfoDialog() {
			super(MainApp.getUserInterface());
			setTitle("Info about image " + file.getName());
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setModalityType(ModalityType.TOOLKIT_MODAL);
			setLocationRelativeTo(null);
			
			JLabel lblFileName = new JLabel("File name: " + file.getName());
			JLabel lblDimensions = new JLabel("Dimensions: " + image.getWidth() + "x" + image.getHeight() + " px");
			
			JLabel lblImageType = new JLabel("Image type: " + getTypeString(image.getType()));
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			lblFileName.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblDimensions.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblImageType.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(lblFileName);
			panel.add(lblDimensions);
			panel.add(lblImageType);
			
			if (ddsInfo != null) {
				JLabel lblDDSFormat = new JLabel("DDS Format: " + ddsInfo.getFormat().getName());
				lblDDSFormat.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(lblDDSFormat);
			}
			
			
			JButton btnClose = new JButton("Close");
			btnClose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
			
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonsPanel.add(btnClose);
			
			
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(panel, BorderLayout.CENTER);
			getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
			
			pack();
			setVisible(true);
		}
	}

	@Override
	public int getViewType() {
		return VIEWTYPE_IMAGE;
	}
}
