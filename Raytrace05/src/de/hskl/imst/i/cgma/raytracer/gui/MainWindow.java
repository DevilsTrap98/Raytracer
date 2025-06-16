package de.hskl.imst.i.cgma.raytracer.gui;
 
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.hskl.imst.i.cgma.raytracer.gui.RayTracerGui.RayTracerResolutionChangedListener;

public class MainWindow extends JFrame implements RayTracerResolutionChangedListener {
	private static final long serialVersionUID = 1L;
	
	private RayTracerGui controller;
	
	private RayTracerStatusBar statusBar;
	private RayTracerCanvas canvas;
	private RaySceneGraphWidget sgTree;
	
	@SuppressWarnings("deprecation")
	protected MainWindow(RayTracerGui controller) {
		this.controller = controller;
		
		statusBar = new RayTracerStatusBar();
		canvas = new RayTracerCanvas(this.controller);
		sgTree = new RaySceneGraphWidget(this.controller);
		controller.addProgressChangedListener(statusBar);
		controller.addProgressChangedListener(canvas);
		controller.addResolutionChangedListener(statusBar);
		controller.addResolutionChangedListener(canvas);
		controller.addSgChangedListeners(sgTree);
		
		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("Datei");
		menuBar.add(fileMenu);
		fileMenu.add(controller.getSaveAction());
		fileMenu.addSeparator();
		fileMenu.add(controller.getExitAction());
		
		
		if(controller.getInteractiveMode()) {
			JMenu resMenu = new JMenu("Auflösung");
			menuBar.add(resMenu);
			resMenu.add(controller.getChangeResolutionAction(250, 250));
			resMenu.add(controller.getChangeResolutionAction(500, 500));
			resMenu.add(controller.getChangeResolutionAction(640, 480));
			resMenu.add(controller.getChangeResolutionAction(800, 600));
			resMenu.add(controller.getChangeResolutionAction(2000, 1200));
			
			JMenu objMenu = new JMenu("Objekte");
			menuBar.add(objMenu);
			objMenu.add(controller.getLoadFileAction());
			JMenuItem miDo = objMenu.add(controller.getDeleteSgNodeAction());
			miDo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			
			JMenu actMenu = new JMenu("RayTracing");
			menuBar.add(actMenu);
			JMenuItem miRt = actMenu.add(controller.getStartRayTraceAction());
			miRt.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		
		// Content
		c.add(canvas, BorderLayout.LINE_START);
		
		// Tree
		c.add(sgTree, BorderLayout.CENTER);
		//DropTarget dropTarget = new DropTarget(sgTree, DnDConstants.ACTION_COPY, controller, true, null);
		new DropTarget(sgTree, DnDConstants.ACTION_COPY, controller, true, null);
		
		// StatusBar
		c.add(statusBar, BorderLayout.SOUTH);
	}

	@Override
	public void rayTraceResolutionChanged(int resx, int resy) {
		sgTree.setPreferredSize(new Dimension(Integer.MAX_VALUE, canvas.getSize().height));
		pack();
	}

}
