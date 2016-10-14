package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import sporemodder.MainApp;
import sporemodder.extras.spuiviewer.SpuiViewer;
import sporemodder.extras.spuiviewer.SpuiViewerDialog;
import sporemodder.userinterface.dialogs.UIDialogEffects;
import sporemodder.userinterface.dialogs.UIDialogGait;
import sporemodder.userinterface.dialogs.UIDialogPctp;
import sporemodder.userinterface.dialogs.UIDialogProp;
import sporemodder.userinterface.dialogs.UIDialogRast;
import sporemodder.userinterface.dialogs.UIDialogRw4;
import sporemodder.userinterface.dialogs.UIDialogSpui;
import sporemodder.userinterface.dialogs.UIDialogTlsa;
import sporemodder.userinterface.fileview.FileView;
import sporemodder.userinterface.fileview.TextFileView;

public class UIMainApp extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6838962608534652730L;

	public static void init()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				UIMainApp userInterface = new UIMainApp();
				MainApp.setUserInterface(userInterface);
			}
			
		});
	}
	
//	private JToolBar toolBar;
	private UIToolBar toolBar;
	private UIButtonsBar buttonsBar;
	private JPanel mainPanel;
	
	private UIProjectPanel projectPanel;
	private UIDisplayPanel displayPanel;
	private UIUtilitiesPanel utilitiesPanel;
	
	private JMenuBar menuBar;
	
	private UIFileMenu mnFile;
	
	private UIEditMenu mnEdit;
	
	private UIProjectMenu mnProject;
	
	private JMenu mnConvert;
	private JMenuItem mntmProp;
	private JMenuItem mntmRw4;
	private JMenuItem mntmTlsa;
	private JMenuItem mntmRast;
	private JMenuItem mntmEffects;
	private JMenuItem mntmSpui;
	private JMenuItem mntmPctp;
	private JMenuItem mntmGait;
	private JMenuItem mntmTexturePatcher;
	private JMenuItem mntmSpuiImageEditor;
	
	public UIProjectPanel getProjectPanel() {
		return projectPanel;
	}
	public UIDisplayPanel getDisplayPanel() {
		return displayPanel;
	}
	public UIUtilitiesPanel getUtilitiesPanel() {
		return utilitiesPanel;
	}
	public UIFileMenu getFileMenu() {
		return mnFile;
	}
	public UIProjectMenu getProjectMenu() {
		return mnProject;
	}
	
	
	private void addMenu() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		//#############################################################
		//#############################################################
		
		mnFile = new UIFileMenu("File");
		mnFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(mnFile);
		
		mnEdit = new UIEditMenu("Edit");
		mnEdit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(mnEdit);
		
		mnProject = new UIProjectMenu("Project");
		mnProject.setMnemonic(KeyEvent.VK_P);
		menuBar.add(mnProject);
		
		mnConvert = new JMenu("Convert");
		mnConvert.setMnemonic(KeyEvent.VK_C);
		menuBar.add(mnConvert);
		
		///////////////////////////////////////////////////
		mntmProp = new JMenuItem("Spore Property List (PROP)");
		mntmProp.setMnemonic(KeyEvent.VK_P);
		mntmProp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogProp();
			}
			
		});
		mnConvert.add(mntmProp);
		
		///////////////////////////////////////////////////
		mntmRw4 = new JMenuItem("Spore RenderWare4 Texture (RW4)");
		mntmRw4.setMnemonic(KeyEvent.VK_R);
		mntmRw4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogRw4();
			}
		});
		mnConvert.add(mntmRw4);

		///////////////////////////////////////////////////
		mntmTlsa = new JMenuItem("Spore Animation List (TLSA)");
		mntmTlsa.setMnemonic(KeyEvent.VK_T);
		mntmTlsa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogTlsa();
			}
		});
		mnConvert.add(mntmTlsa);
		
		////////////////////////////////////////////////////
				
		mntmEffects = new JMenuItem("Spore Effects (EFFDIR/PFX)");
		mntmEffects.setMnemonic(KeyEvent.VK_E);
		mntmEffects.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogEffects();
			}
		});
		
		mnConvert.add(mntmEffects);
		
		////////////////////////////////////////////////////
		
		mntmSpui = new JMenuItem("Spore User Interface (SPUI)");
		mntmSpui.setMnemonic(KeyEvent.VK_S);
		mntmSpui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogSpui();
			}
		});
		
		mnConvert.add(mntmSpui);
		
		////////////////////////////////////////////////////
				
		mntmPctp = new JMenuItem("Spore Capability List (PCTP)");
		mntmPctp.setMnemonic(KeyEvent.VK_C);
		mntmPctp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogPctp();
			}
		});
		
		mnConvert.add(mntmPctp);
		
		////////////////////////////////////////////////////
		
		mntmGait = new JMenuItem("Spore Gait (GAIT)");
		mntmGait.setMnemonic(KeyEvent.VK_G);
		mntmGait.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogGait();
			}
		});
		
		mnConvert.add(mntmGait);
		
		////////////////////////////////////////////////////
				
		mntmRast = new JMenuItem("Spore RAST Texture (RAST)");
		mntmRast.setMnemonic(KeyEvent.VK_A);
		mntmRast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogRast();
			}
		});
		
		mnConvert.add(mntmRast);
		
		
		mnConvert.add(new JSeparator());
		///////////////////////////////////////////////////
		
		mntmTexturePatcher = new JMenuItem("Texture patcher");
		mntmTexturePatcher.setMnemonic(KeyEvent.VK_X);
		mntmTexturePatcher.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UITexturePatcher.findFile();
			}
		});
		mnConvert.add(mntmTexturePatcher);
		
		mntmSpuiImageEditor = new JMenuItem("SPUI Image Editor");
		mntmSpuiImageEditor.setEnabled(false);
		//mntmSpuiImageEditor.setMnemonic(KeyEvent.VK_X);
		mntmSpuiImageEditor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SpuiViewerDialog(UIMainApp.this, MainApp.getActiveFile());
				// update file viewer
				MainApp.setActiveFile(MainApp.getActiveFilePath());
			}
		});
		mnConvert.add(mntmSpuiImageEditor);
		
	}
	
	public UIMainApp()
	{
		// it uses a border layout
		setLayout(new BorderLayout());
		setTitle("SporeModder");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainApp.Loader.getResource("sporemodder/userinterface/images/SporeModderIcon.png")));
		
//		try {
//			setIconImage(ImageIO.read(MainApp.Loader.getResourceAsStream("sporemodder/userinterface/images/SporeModderIcon.png")));
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Container pane = getContentPane();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				MainApp.processAutoSave();
			}
		});
		
		mainPanel = new JPanel();
//		mainPanel.setLayout(new GridBagLayout());
//		pane.add(mainPanel, BorderLayout.CENTER);
		
		//// Add contents ////
		
        Border eBorder = BorderFactory.createEtchedBorder();
        
//        toolBar = new UIToolBar();
//        gbc.gridx = gbc.gridy = 0;
//        gbc.gridheight = 1;
//        gbc.gridwidth = 3;
//        gbc.fill = GridBagConstraints.BOTH;
//        gbc.anchor = GridBagConstraints.NORTH;
//        gbc.weightx = 100;
//        gbc.weighty = 20;
//        pane.add(toolBar, BorderLayout.NORTH);
//
//        projectPanel = new JPanel();
//        projectPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Project Panel"));
//        gbc.gridy = 1;
//        gbc.gridwidth = gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.weightx = 20;
//        gbc.weighty = 80;
//        mainPanel.add(projectPanel, gbc);
//        
//        editorPanel = new JPanel();
//        editorPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Editor Panel"));
//        gbc.gridx = 1;
//        gbc.gridy = 1;
//        gbc.weightx = 65;
//        mainPanel.add(editorPanel, gbc);
//        
//        // Utilities panel, it will use 20% of width by default
//        utilitiesPanel = new UIUtilitiesPanel();
//        utilitiesPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Utilities Panel"));
//        gbc.gridx = 2;
//        gbc.gridy = 1;
//        gbc.weightx = 15;
//        mainPanel.add(utilitiesPanel, gbc);
        
        Dimension minimumSize = new Dimension(100, 50);
        projectPanel = new UIProjectPanel();
//        projectPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Project Panel"));
        projectPanel.setMinimumSize(minimumSize);
        
        displayPanel = new UIDisplayPanel();
//        editorPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Editor Panel"));
        displayPanel.setMinimumSize(minimumSize);
        
        utilitiesPanel = new UIUtilitiesPanel();
        //utilitiesPanel.setBorder(BorderFactory.createTitledBorder(eBorder, "Utilities Panel"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectPanel, displayPanel);
        splitPane.setDividerLocation(250);
        
        //JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, utilitiesPanel);
        //splitPane2.resetToPreferredSizes();
        
        
        pane.add(splitPane);
        //pane.add(splitPane2);
        
//        pane.add(projectPanel, BorderLayout.WEST);
//        pane.add(editorPanel, BorderLayout.CENTER);
        pane.add(utilitiesPanel, BorderLayout.EAST);
        
        toolBar = new UIToolBar();
        buttonsBar = new UIButtonsBar();
//        pane.add(toolBar, BorderLayout.NORTH);
//        pane.add(buttonsBar, BorderLayout.NORTH);
        
        JPanel barPanel = new JPanel();
        barPanel.setLayout(new BorderLayout());
        
        barPanel.add(toolBar, BorderLayout.WEST);
        barPanel.add(buttonsBar, BorderLayout.EAST);
        
        pane.add(barPanel, BorderLayout.NORTH);
        
        addMenu();
		
		//// ------------ ////
		
		// Display the window
		pack();
		setVisible(true);
	}
	
	// Updates all those things dependant on the current project
	public void update() {
		FileView fileView = MainApp.getActiveFileView();
		String activeFilePath = MainApp.getActiveFilePath();
		mnFile.update();
		mnProject.update(fileView);
		mnEdit.update(fileView);
		mntmSpuiImageEditor.setEnabled(activeFilePath != null && (activeFilePath.endsWith(".spui") || activeFilePath.endsWith(".spui_t")));
		toolBar.update();
		displayPanel.update(fileView);
		buttonsBar.update(fileView);
	}
	
	public void updateProjects() {
		mnFile.updateProjects();
	}
	
	public void updateProjectName() {
		projectPanel.updateProjectName();
	}
}
