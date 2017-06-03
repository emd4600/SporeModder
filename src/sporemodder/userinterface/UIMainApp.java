package sporemodder.userinterface;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import sporemodder.MainApp;
import sporemodder.userinterface.fileview.FileView;

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
	
	private UIConvertMenu mnConvert;
	
	private ConsoleDialog consoleDialog;
	
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
		
		mnConvert = new UIConvertMenu("Convert");
		mnConvert.setMnemonic(KeyEvent.VK_C);
		menuBar.add(mnConvert);
		
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
        
        consoleDialog = ConsoleDialog.redirectConsole();
		
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
		mnConvert.update();
		//mntmSpuiEditor.setEnabled(activeFilePath != null && (activeFilePath.endsWith(".spui") || activeFilePath.endsWith(".spui_t")));
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
	
	public ConsoleDialog getConsoleDialog() {
		return consoleDialog;
	}
}
