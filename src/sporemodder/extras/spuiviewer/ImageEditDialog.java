package sporemodder.extras.spuiviewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.Color;

import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JScrollPane;

import sporemodder.files.formats.ResourceKey;
import sporemodder.files.formats.spui.SPUIBlock;
import sporemodder.files.formats.spui.SPUIFileResource;
import sporemodder.userinterface.dialogs.UIChooseResource;

public class ImageEditDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9141701982865660448L;
	
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
	
	private static final Dimension IMAGEPANEL_MIN = new Dimension(256, 256);
	
	private static final Dimension PREVIEW_SIZE = new Dimension(128, 128);
	
	private JScrollPane scrollPane;
	private final JPanel contentPanel = new JPanel();
	private final JLabel lblCoordinates1 = new JLabel("Top-left:");
	private JSpinner spinnerCoord2X;
	private JSpinner spinnerCoord2Y;
	private JSpinner spinnerCoord1X;
	private JSpinner spinnerCoord1Y;
	private JSpinner spinnerDimWidth;
	private JSpinner spinnerDimHeight;
	private ImageEditPanel imagePanel;
	
	private final SpinnerModel zoomSpinnerModel = new SpinnerNumberModel(1, MIN_ZOOM, MAX_ZOOM, ZOOM_STEP);
	private JSpinner spinnerZoom;
	
	private boolean isNewBlock;
	private ImageBlock imageBlock;
	private JSpinner spinnerUnknown;
	private JCheckBox cbDimensions;
	
	private JScrollPane scrollPanePreview;
	private ImagePreviewPanel previewPanel;
	private JSpinner spinnerPreviewZoom;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ImageEditDialog dialog = new ImageEditDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ImageEditDialog(Window frame, ImageBlock imageBlock) {
		super(frame);
		
		this.imageBlock = imageBlock;
		isNewBlock = imageBlock == null;
	
		setTitle("SPUI Image Editor");
		setBounds(100, 100, 601, 373);
		setModalityType(ModalityType.TOOLKIT_MODAL);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				if (imageBlock == null) {
					imagePanel = new ImageEditPanel(this);
					previewPanel = new ImagePreviewPanel();
				}
				else {
					try {
						imagePanel = new ImageEditPanel(this, imageBlock);
						previewPanel = new ImagePreviewPanel(imageBlock.getAtlasImage());
					} catch (IOException e) {
						JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
						imagePanel = new ImageEditPanel(this);
						previewPanel = new ImagePreviewPanel();
					}
				}
				imagePanel.setMinimumSize(IMAGEPANEL_MIN);
				scrollPane.setViewportView(imagePanel);
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Data", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.EAST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel dataPanel = new JPanel();
				dataPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				panel.add(dataPanel, BorderLayout.NORTH);
				dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
				lblCoordinates1.setAlignmentX(Component.CENTER_ALIGNMENT);
				dataPanel.add(lblCoordinates1);
				{
					JPanel panelCoord1 = new JPanel();
					dataPanel.add(panelCoord1);
					GridBagLayout gbl_panelCoord1 = new GridBagLayout();
					gbl_panelCoord1.columnWeights = new double[]{0.0, 0.0};
					gbl_panelCoord1.rowWeights = new double[]{0.0, 0.0};
					panelCoord1.setLayout(gbl_panelCoord1);
					{
						JLabel lblCoord1X = new JLabel("X: ");
						GridBagConstraints gbc_lblCoord1X = new GridBagConstraints();
						gbc_lblCoord1X.insets = new Insets(0, 0, 5, 5);
						gbc_lblCoord1X.gridx = 0;
						gbc_lblCoord1X.gridy = 0;
						panelCoord1.add(lblCoord1X, gbc_lblCoord1X);
					}
					{
						spinnerCoord1X = new JSpinner();
						spinnerCoord1X.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								coordinatesChanged();
							}
						});
						GridBagConstraints gbc_spinnerCoord1X = new GridBagConstraints();
						gbc_spinnerCoord1X.weightx = 1.0;
						gbc_spinnerCoord1X.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord1X.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord1X.insets = new Insets(0, 0, 5, 0);
						gbc_spinnerCoord1X.gridx = 1;
						gbc_spinnerCoord1X.gridy = 0;
						panelCoord1.add(spinnerCoord1X, gbc_spinnerCoord1X);
						spinnerCoord1X.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
						spinnerCoord1X.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinnerCoord1X.getPreferredSize().height));
					}
					{
						JLabel lblCoord1Y = new JLabel("Y: ");
						GridBagConstraints gbc_lblCoord1Y = new GridBagConstraints();
						gbc_lblCoord1Y.insets = new Insets(0, 0, 0, 5);
						gbc_lblCoord1Y.gridx = 0;
						gbc_lblCoord1Y.gridy = 1;
						panelCoord1.add(lblCoord1Y, gbc_lblCoord1Y);
					}
					{
						spinnerCoord1Y = new JSpinner();
						spinnerCoord1Y.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								coordinatesChanged();
							}
						});
						spinnerCoord1Y.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
						GridBagConstraints gbc_spinnerCoord1Y = new GridBagConstraints();
						gbc_spinnerCoord1Y.weightx = 1.0;
						gbc_spinnerCoord1Y.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord1Y.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord1Y.gridx = 1;
						gbc_spinnerCoord1Y.gridy = 1;
						panelCoord1.add(spinnerCoord1Y, gbc_spinnerCoord1Y);
					}
				}
				{
					Component verticalStrut = Box.createVerticalStrut(10);
					dataPanel.add(verticalStrut);
				}
				{
					JLabel lblCoordinates2 = new JLabel("Bottom-right:");
					dataPanel.add(lblCoordinates2);
					lblCoordinates2.setAlignmentX(Component.CENTER_ALIGNMENT);
				}
				{
					JPanel panelCoord2 = new JPanel();
					dataPanel.add(panelCoord2);
					GridBagLayout gbl_panelCoord2 = new GridBagLayout();
					gbl_panelCoord2.columnWidths = new int[] {0, 0};
					gbl_panelCoord2.rowHeights = new int[] {0, 0};
					gbl_panelCoord2.columnWeights = new double[]{0.0, 0.0};
					gbl_panelCoord2.rowWeights = new double[]{0.0, 0.0};
					panelCoord2.setLayout(gbl_panelCoord2);
					{
						JLabel lblCoord2X = new JLabel("X: ");
						GridBagConstraints gbc_lblCoord2X = new GridBagConstraints();
						gbc_lblCoord2X.insets = new Insets(0, 0, 5, 5);
						gbc_lblCoord2X.gridx = 0;
						gbc_lblCoord2X.gridy = 0;
						panelCoord2.add(lblCoord2X, gbc_lblCoord2X);
					}
					{
						spinnerCoord2X = new JSpinner();
						spinnerCoord2X.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								coordinatesChanged();
							}
						});
						GridBagConstraints gbc_spinnerCoord2X = new GridBagConstraints();
						gbc_spinnerCoord2X.weightx = 1.0;
						gbc_spinnerCoord2X.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord2X.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord2X.insets = new Insets(0, 0, 5, 0);
						gbc_spinnerCoord2X.gridx = 1;
						gbc_spinnerCoord2X.gridy = 0;
						panelCoord2.add(spinnerCoord2X, gbc_spinnerCoord2X);
						spinnerCoord2X.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
					}
					{
						JLabel label = new JLabel("Y: ");
						GridBagConstraints gbc_label = new GridBagConstraints();
						gbc_label.insets = new Insets(0, 0, 0, 5);
						gbc_label.gridx = 0;
						gbc_label.gridy = 1;
						panelCoord2.add(label, gbc_label);
					}
					{
						spinnerCoord2Y = new JSpinner();
						spinnerCoord2Y.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								coordinatesChanged();
							}
						});
						GridBagConstraints gbc_spinnerCoord2Y = new GridBagConstraints();
						gbc_spinnerCoord2Y.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord2Y.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord2Y.weightx = 1.0;
						gbc_spinnerCoord2Y.gridx = 1;
						gbc_spinnerCoord2Y.gridy = 1;
						panelCoord2.add(spinnerCoord2Y, gbc_spinnerCoord2Y);
						spinnerCoord2Y.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(1)));
					}
				}
				{
					Component verticalStrut = Box.createVerticalStrut(20);
					dataPanel.add(verticalStrut);
				}
				{
					cbDimensions = new JCheckBox("Dimensions: ");
					cbDimensions.setAlignmentX(0.5f);
					cbDimensions.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent arg0) {
							boolean isSelected = cbDimensions.isSelected();
							spinnerDimWidth.setEnabled(isSelected);
							spinnerDimHeight.setEnabled(isSelected);
							coordinatesChanged();
						}
					});
					dataPanel.add(cbDimensions);
				}
				{
					JPanel panelDimensions = new JPanel();
					dataPanel.add(panelDimensions);
					GridBagLayout gbl_panelCoord1 = new GridBagLayout();
					gbl_panelCoord1.columnWeights = new double[]{0.0, 0.0};
					gbl_panelCoord1.rowWeights = new double[]{0.0, 0.0};
					panelDimensions.setLayout(gbl_panelCoord1);
					{
						JLabel lblDimWidth = new JLabel("Width: ");
						GridBagConstraints gbc_lblCoord1X = new GridBagConstraints();
						gbc_lblCoord1X.insets = new Insets(0, 0, 5, 5);
						gbc_lblCoord1X.gridx = 0;
						gbc_lblCoord1X.gridy = 0;
						panelDimensions.add(lblDimWidth, gbc_lblCoord1X);
					}
					{
						spinnerDimWidth = new JSpinner();
						spinnerDimWidth.setEnabled(false);
						GridBagConstraints gbc_spinnerCoord1X = new GridBagConstraints();
						gbc_spinnerCoord1X.weightx = 1.0;
						gbc_spinnerCoord1X.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord1X.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord1X.insets = new Insets(0, 0, 5, 0);
						gbc_spinnerCoord1X.gridx = 1;
						gbc_spinnerCoord1X.gridy = 0;
						panelDimensions.add(spinnerDimWidth, gbc_spinnerCoord1X);
						spinnerDimWidth.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
						spinnerDimWidth.setMaximumSize(new Dimension(Integer.MAX_VALUE, spinnerCoord1X.getPreferredSize().height));
						spinnerDimWidth.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								dimensionsChanged();
							}
						});
					}
					{
						JLabel lblDimHeight= new JLabel("Height: ");
						GridBagConstraints gbc_lblCoord1Y = new GridBagConstraints();
						gbc_lblCoord1Y.insets = new Insets(0, 0, 0, 5);
						gbc_lblCoord1Y.gridx = 0;
						gbc_lblCoord1Y.gridy = 1;
						panelDimensions.add(lblDimHeight, gbc_lblCoord1Y);
					}
					{
						spinnerDimHeight = new JSpinner();
						spinnerDimHeight.setEnabled(false);
						spinnerDimHeight.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
						spinnerDimHeight.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								dimensionsChanged();
							}
						});
						GridBagConstraints gbc_spinnerCoord1Y = new GridBagConstraints();
						gbc_spinnerCoord1Y.weightx = 1.0;
						gbc_spinnerCoord1Y.anchor = GridBagConstraints.LINE_END;
						gbc_spinnerCoord1Y.fill = GridBagConstraints.HORIZONTAL;
						gbc_spinnerCoord1Y.gridx = 1;
						gbc_spinnerCoord1Y.gridy = 1;
						panelDimensions.add(spinnerDimHeight, gbc_spinnerCoord1Y);
					}
				}
				{
					Component verticalStrut = Box.createVerticalStrut(20);
					dataPanel.add(verticalStrut);
				}
				{
					JLabel lblUnknown = new JLabel("Unknown:");
					lblUnknown.setAlignmentX(Component.CENTER_ALIGNMENT);
					dataPanel.add(lblUnknown);
				}
				{
					spinnerUnknown = new JSpinner();
					dataPanel.add(spinnerUnknown);
				}
				{
					Component verticalStrut = Box.createVerticalStrut(20);
					dataPanel.add(verticalStrut);
				}
				{
					JLabel lblPreview = new JLabel("Preview: ");
					lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
					dataPanel.add(lblPreview);
				}
				{
					scrollPanePreview = new JScrollPane(previewPanel);
					scrollPanePreview.setPreferredSize(PREVIEW_SIZE);
					dataPanel.add(scrollPanePreview);
				}
				{
					JPanel panel_1 = new JPanel();
					dataPanel.add(panel_1);
					{
						JLabel lblZoom_1 = new JLabel("Zoom: ");
						panel_1.add(lblZoom_1);
					}
					{
						spinnerPreviewZoom = new JSpinner(new SpinnerNumberModel(1, MIN_ZOOM, MAX_ZOOM, ZOOM_STEP));
						spinnerPreviewZoom.setEditor(new JSpinner.NumberEditor(spinnerPreviewZoom, "####%"));
						spinnerPreviewZoom.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(ChangeEvent arg0) {
								previewPanel.setZoomLevel(((Double) spinnerPreviewZoom.getValue()).floatValue());
							}
						});
						panel_1.add(spinnerPreviewZoom);
					}
				}
				{
					Component horizontalStrut = Box.createHorizontalStrut(100);
					dataPanel.add(horizontalStrut);
				}
			}
		}
		{
			JPanel infoPanel = new JPanel();
			infoPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
			contentPanel.add(infoPanel, BorderLayout.SOUTH);
			infoPanel.setLayout(new BorderLayout(0, 0));
			{
				JTextField lblCursorInfo = new JTextField("");
				lblCursorInfo.setColumns(50);
				lblCursorInfo.setBorder(null);
				lblCursorInfo.setEditable(false);
				lblCursorInfo.setForeground(Color.GRAY);
				lblCursorInfo.setFont(new Font("Tahoma", Font.ITALIC, 11));
				infoPanel.add(lblCursorInfo, BorderLayout.CENTER);
			}
			{
				JPanel panel = new JPanel();
				infoPanel.add(panel, BorderLayout.WEST);
				{
					JLabel lblZoom = new JLabel("Zoom: ");
					panel.add(lblZoom);
				}
				{
					spinnerZoom = new JSpinner(zoomSpinnerModel);
					spinnerZoom.setEditor(new JSpinner.NumberEditor(spinnerZoom, "####%"));
					spinnerZoom.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent arg0) {
							imagePanel.setZoomLevel(((Double) spinnerZoom.getValue()).floatValue());
						}
					});
					panel.add(spinnerZoom);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (ImageEditDialog.this.imageBlock == null) {
							ImageEditDialog.this.imageBlock = new ImageBlock();
						}
						try {
							ImageEditDialog.this.imageBlock.setDimension(getDimension());
							ImageEditDialog.this.imageBlock.setUnknown(getUnknown());
							ImageEditDialog.this.imageBlock.setUVCoordinates(getUVCoordinates(), imagePanel.getImage());
							if (isNewBlock) {
								SPUIBlock block = ImageEditDialog.this.imageBlock.generateBlock(SpuiViewer.ActiveSPUI, isNewBlock);
								SpuiViewer.ActiveSPUI.getBlocks().add(block);
							}
//								else {
//									ImageEditDialog.this.imageBlock.setDimension(getDimension());
//									ImageEditDialog.this.imageBlock.setUnknown(getUnknown());
//									ImageEditDialog.this.imageBlock.setUVCoordinates(getUVCoordinates(), imagePanel.getImage());
//								}
							// if the block already existed it has already been updated
							
							dispose();
							
						} catch (ImageBlockException e) {
							JOptionPane.showMessageDialog(ImageEditDialog.this, "Error", e.toString(), JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// it was cancelled, this is so createImageEditDialog doesn't return anything
						ImageEditDialog.this.imageBlock = null;
						dispose();
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		{
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			{
				JMenu mnFile = new JMenu("File");
				menuBar.add(mnFile);
				{
					JMenuItem mntmSetAtlas = new JMenuItem("Set atlas....");
					mntmSetAtlas.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							changeAtlas();
						}
					});
					mnFile.add(mntmSetAtlas);
				}
			}
		}
		
		if (imageBlock != null) {
			try {
				setImageData();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		pack();
		setVisible(true);
	}

	private void setImageData() throws IOException {
		int[] uvs = imageBlock.getUVCoordinatesImageSpace();
		Dimension dimensions = imageBlock.getDimensions();
		
		if (uvs == null) {
			uvs = new int[] {0, 0, 0, 0};
		}
		
		spinnerCoord1X.setValue(uvs[0]);
		spinnerCoord1Y.setValue(uvs[1]);
		spinnerCoord2X.setValue(uvs[2]);
		spinnerCoord2Y.setValue(uvs[3]);
		
		if (dimensions != null) {
			spinnerDimWidth.setValue(dimensions.width);
			spinnerDimHeight.setValue(dimensions.height);
			
			if (dimensions.width != uvs[2] - uvs[0] || dimensions.height != uvs[3] - uvs[1]) {
				// Manual dimensions
				spinnerDimWidth.setEnabled(true);
				spinnerDimHeight.setEnabled(true);
			} else {
				spinnerDimWidth.setEnabled(false);
				spinnerDimHeight.setEnabled(false);
			}
		} else {
			spinnerDimWidth.setValue(uvs[2] - uvs[0]);
			spinnerDimHeight.setValue(uvs[3] - uvs[1]);
			spinnerDimWidth.setEnabled(false);
			spinnerDimHeight.setEnabled(false);
		}
		
//		BufferedImage image = imagePanel.getImage();
//		int width = image.getWidth();
//		int height = image.getHeight();
//		spinnerCoord1X.
		
		spinnerUnknown.setValue(imageBlock.getUnknown());
		
		coordinatesChanged();
	}
	
	private void coordinatesChanged() {
		// notify the ImageEditPanel and the ImagePreviewDialog, and change the dimensions if they are set to automatic
		
		int[] coords = new int[] { (int) spinnerCoord1X.getValue(), (int) spinnerCoord1Y.getValue(), (int) spinnerCoord2X.getValue(), (int) spinnerCoord2Y.getValue() };
		
		if (!cbDimensions.isSelected()) {
			spinnerDimWidth.setValue(coords[2] - coords[0]);
			spinnerDimHeight.setValue(coords[3] - coords[1]);
		}
		
		if (imagePanel != null) {
			imagePanel.setUVCoordinates(coords);
		}
		
		//TODO notify ImagePreviewDialog
		if (previewPanel != null) {
			previewPanel.update(coords, new Dimension((Integer) spinnerDimWidth.getValue(), (Integer) spinnerDimHeight.getValue()));
		}
	}
	
	private void dimensionsChanged() {
		if (previewPanel != null) {
			int[] coords = new int[] { (int) spinnerCoord1X.getValue(), (int) spinnerCoord1Y.getValue(), (int) spinnerCoord2X.getValue(), (int) spinnerCoord2Y.getValue() };
			previewPanel.update(coords, new Dimension((Integer) spinnerDimWidth.getValue(), (Integer) spinnerDimHeight.getValue()));
		}
	}
	
	protected void updateCoordinates(Point point1, Point point2) {
		spinnerCoord1X.setValue(point1.x);
		spinnerCoord1Y.setValue(point1.y);
		spinnerCoord2X.setValue(point2.x);
		spinnerCoord2Y.setValue(point2.y);
		coordinatesChanged();
	}
	
	
	private void changeAtlas() {
		ResourceKey key = UIChooseResource.createResourceChooser(this, "Choose Atlas");
		SPUIFileResource resource = new SPUIFileResource(key, true);
		if (imageBlock == null) {
			imageBlock = new ImageBlock();
		}
		imageBlock.setResource(resource);
		
		try {
			setImageData();
			updateImageBlock();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void updateImageBlock() {
		if (imageBlock == null) {
			imagePanel = new ImageEditPanel(this);
			previewPanel = new ImagePreviewPanel();
		}
		else {
			try {
				imagePanel = new ImageEditPanel(this, imageBlock);
				previewPanel = new ImagePreviewPanel(imageBlock.getAtlasImage());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(SpuiViewer.ActiveFrame, Messages.getString("ImagePanel.processImage.errorLoading"), "Error", JOptionPane.ERROR_MESSAGE);
				imagePanel = new ImageEditPanel(this);
				previewPanel = new ImagePreviewPanel();
			}
		}
		imagePanel.setMinimumSize(IMAGEPANEL_MIN);
		scrollPane.setViewportView(imagePanel);
		
		if (scrollPanePreview != null) {
			scrollPanePreview.setViewportView(previewPanel);
		}
	}
	
	public Dimension getDimension() {
		return new Dimension((int) spinnerDimWidth.getValue(), (int) spinnerDimHeight.getValue());
	}
	
	public int[] getUVCoordinates() {
		return new int[] { (int) spinnerCoord1X.getValue(), (int) spinnerCoord1Y.getValue(), 
				(int) spinnerCoord2X.getValue(), (int) spinnerCoord2Y.getValue()};
	}
	
	public int getUnknown() {
		return (int) spinnerUnknown.getValue();
	}
	
	
	// use this for new blocks
	public static ImageBlock createImageEditDialog(Window parent) {
		ImageEditDialog dialog = new ImageEditDialog(parent, null);
		return dialog.imageBlock;
	}
}
