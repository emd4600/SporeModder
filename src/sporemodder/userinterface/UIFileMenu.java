package sporemodder.userinterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import sporemodder.MainApp;
import sporemodder.userinterface.dialogs.UIDialogPack;
import sporemodder.userinterface.dialogs.UIDialogUnpack;
import sporemodder.userinterface.dialogs.UIOpenProject;
import sporemodder.userinterface.dialogs.UIProgramSettings;
import sporemodder.userinterface.dialogs.UIProjectSettings;
import sporemodder.utilities.Project;

public class UIFileMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4066057296250150376L;
	
	private JMenuItem mntmNewProject;
	private JMenuItem mnLoadProject;
	private JMenu mnRecentProjects;
	private JMenuItem mntmUnpackDbpf;
	private JMenuItem mntmPackDbpf;
	private JMenuItem mntmSettings;
	private JMenuItem mntmProjectSettings;
	
	public UIFileMenu(String name) {
		super(name);
		
		mntmNewProject = new JMenuItem("New Project");
		mntmNewProject.setMnemonic(KeyEvent.VK_N);
		mntmNewProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new UIProjectSettings(new Project(MainApp.getNewProjectName()), UIProjectSettings.Mode.NEW);
			}
		});
		add(mntmNewProject);
		
		mnLoadProject = new JMenuItem("Open Project...");
		mnLoadProject.setMnemonic(KeyEvent.VK_O);
		mnLoadProject.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		mnLoadProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project proj = UIOpenProject.createOpenProjectDialog(MainApp.getUserInterface(), "Open Project...", "Select a project to open.");
				if (proj != null) {
					MainApp.setCurrentProject(proj);
				}
			}
		});
		
		//initMenuLoadProject();
		
//		mnLoadProject.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//			}
//		});
		add(mnLoadProject);
		
		mnRecentProjects = new JMenu("Recent Projects");
		mnRecentProjects.setMnemonic(KeyEvent.VK_R);
		initMenuRecentProjects(10);
		add(mnRecentProjects);
		
		add(new JSeparator());
		
		///////////////////////////////////////////////////////
		mntmUnpackDbpf = new JMenuItem("Unpack DBPF");
		mntmUnpackDbpf.setMnemonic(KeyEvent.VK_U);
		mntmUnpackDbpf.setAccelerator(KeyStroke.getKeyStroke('U', InputEvent.CTRL_DOWN_MASK));
		mntmUnpackDbpf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogUnpack();
			}
			
		});
		add(mntmUnpackDbpf);
		
		mntmPackDbpf = new JMenuItem("Pack DBPF");
		mntmPackDbpf.setMnemonic(KeyEvent.VK_P);
		mntmPackDbpf.setEnabled(false);
		mntmPackDbpf.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK));
		mntmPackDbpf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIDialogPack(MainApp.getUserInterface());
			}
			
		});
		add(mntmPackDbpf);
		
		add(new JSeparator());
		
		//////////////////////////////////////////////////////
		
		mntmProjectSettings = new JMenuItem("Project Settings");
		mntmProjectSettings.setMnemonic(KeyEvent.VK_R);
		mntmProjectSettings.setEnabled(MainApp.getCurrentProject() != null);
		mntmProjectSettings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (MainApp.getCurrentProject() != null) new UIProjectSettings(MainApp.getCurrentProject(), UIProjectSettings.Mode.NONE);
			}
			
		});
		add(mntmProjectSettings);
		
		mntmSettings = new JMenuItem("Settings");
		mntmSettings.setMnemonic(KeyEvent.VK_S);
		mntmSettings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new UIProgramSettings(MainApp.getUserInterface());
			}
			
		});
		add(mntmSettings);
		
		add(new JSeparator());
		
		///////////////////////////////////////////////////////
//		mntmExit = new JMenuItem("Exit");
//		add(mntmExit);
	}
	
//	private void initMenuLoadProject() {
//		JPopupMenu popupMenu = mnLoadProject.getPopupMenu();
//		popupMenu.setLayout(new BoxLayout(popupMenu, BoxLayout.PAGE_AXIS));
//		popupMenu.removeAll();
////		popupMenu.setLayout(new GridLayout(30, 3));
//		List<Project> projects = MainApp.getProjects();
//		for (final Project project : projects) {
//			JMenuItem projectItem = new JMenuItem(project.getProjectName());
//			projectItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					MainApp.setCurrentProject(project);
//				}
//			});
//			popupMenu.add(projectItem);
//		}
//	}
	
	private void initMenuRecentProjects(int maxProjects) {
		JPopupMenu popupMenu = mnRecentProjects.getPopupMenu();
		popupMenu.setLayout(new BoxLayout(popupMenu, BoxLayout.PAGE_AXIS));
		popupMenu.removeAll();
		List<Project> projects = MainApp.getLastProjects(maxProjects);
		
		for (final Project project : projects) {
			JMenuItem item = new JMenuItem(project.getProjectName());
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					MainApp.setCurrentProject(project);
				}
			});
			popupMenu.add(item);
		}
	}
	
	// Updates all those things dependant on the current project
	public void update() {
		mntmProjectSettings.setEnabled(MainApp.getCurrentProject() != null);
		mntmPackDbpf.setEnabled(MainApp.getCurrentProject() != null);
		initMenuRecentProjects(10);
	}
	
	public void updateProjects() {
		//initMenuLoadProject();
		initMenuRecentProjects(10);
	}
}
