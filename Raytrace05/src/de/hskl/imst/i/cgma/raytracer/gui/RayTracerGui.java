package de.hskl.imst.i.cgma.raytracer.gui;
 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import de.hskl.imst.i.cgma.raytracer.file.RTFile;
import de.hskl.imst.i.cgma.raytracer.file.RTFileReader;
import de.hskl.imst.i.cgma.raytracer.file.RT_Object;

public class RayTracerGui implements DropTargetListener {
	private int resX;
	private int resY;
	
	private long sTime = -1;
	private int sPoints = 0;
	private int points;
	
	private BufferedImage bufferedImage;
	
	private MainWindow window;
	
	private IRayTracerImplementation implementation;

	private boolean interactiveMode = false;
	
	private boolean traceInProgress = false;
	
	private Vector<RT_Object> objects= new Vector<RT_Object>();
	public Vector<RT_Object> getObjects() { return objects; }
	private RT_Object selectedObject = null;
	protected RT_Object getSelectedObject() { return selectedObject; }
	protected void setSelectedObject(RT_Object obj) {
		selectedObject = obj;
		if(selectedObject!=null && !traceInProgress)
			getDeleteSgNodeAction().setEnabled(true);
		else
			getDeleteSgNodeAction().setEnabled(false);
	}
	
	private volatile UpdateWorker updateWorker = new UpdateWorker();
	
	public RayTracerGui(IRayTracerImplementation impl) {
		resX = 800;
		resY = 600;

		implementation = impl;
		
		interactiveMode = true;
		
		getStartRayTraceAction().setEnabled(false);

		init();
	}
	
	private void init() {
		points = resX * resY;
		
		bufferedImage = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_RGB);
		
		window = new MainWindow(this);
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		
		
		callResolutionChangedListeners(resX, resY);
		reset();
		updateWorker.start();
	}
	
	public void addObject(RT_Object obj) {
		if(!objects.contains(obj))
			objects.add(obj);
		if(objects.size()>0)
			getStartRayTraceAction().setEnabled(true);
		else
			getStartRayTraceAction().setEnabled(false);
		callSgChangedListeners();
	}
	
	public void removeObject(RT_Object obj) {
		if(objects.contains(obj))
			objects.remove(objects.indexOf(obj));
	}
	
	public void setResolution(int resX, int resY) {
		this.resX = resX;
		this.resY = resY;
		points = resX * resY;
		
		bufferedImage = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_RGB);
		
		callResolutionChangedListeners(resX, resY);
		
		window.pack();
		reset();
	}
	
	public void setPixel(int x, int y, int rgb) {
		if(x < 0 || y < 0 || x > resX || y > resY)
			return;
		if(sTime < 0)
			sTime = System.currentTimeMillis();
		bufferedImage.setRGB(x, y, rgb);
		sPoints++;
		updateWorker.requestUpdate();
		/*callProgressChangedListeners(System.currentTimeMillis()-sTime, points, sPoints);*/
	}
	
	public void reset() {
		sTime = -1;
		sPoints = 0;
		bufferedImage = new BufferedImage(resX, resY, BufferedImage.TYPE_INT_RGB);
		callProgressChangedListeners(0, resX * resY, 0);
	}
	
	protected BufferedImage getImage() {
		return bufferedImage;
	}
	
	public int getResX() {
		return resX;
	}
	
	public int getResY() {
		return resY;
	}
	
	protected boolean getInteractiveMode() {
		return interactiveMode;
	}
	
	public void saveImage(File destination) {
		try {
			ImageIO.write(bufferedImage, "png", destination);
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(window, e.getMessage());
		}
	}
	
	private void exit() {
		
		updateWorker.stopWorker();
		window.dispose();
		System.exit(0);
	}
	
	private SaveAction saveAction = new SaveAction(this, "Bild speichern");
	protected SaveAction getSaveAction() {
		return saveAction;
	}
	class SaveAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private RayTracerGui controller;
        
        public SaveAction(RayTracerGui c, String string) {
            super(string);
            controller = c;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
        	JFileChooser fc = new JFileChooser();
        	fc.setFileFilter(new ExtensionFilter(".png", "PNG Bilddateien"));
        	int ret = fc.showSaveDialog(window);
        	if(ret == JFileChooser.APPROVE_OPTION) {
        		controller.saveImage(fc.getSelectedFile());
        	}
        }
	}
	
	private LoadFileAction loadFileAction = new LoadFileAction("Hinzufügen");
	protected LoadFileAction getLoadFileAction() {
		return loadFileAction;
	}
    class LoadFileAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        // Todo
        private String prevFolderPath = Paths.get(System.getProperty("user.dir"), "data").toString();

        public LoadFileAction(String string) {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JFileChooser fc;
            if (prevFolderPath == null)
                fc = new JFileChooser();
            else
                fc = new JFileChooser(prevFolderPath);
            fc.setFileFilter(new ExtensionFilter(".dat", "DAT Dateien"));
            int ret = fc.showOpenDialog(window);
            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    Class<? extends RTFile> type = RTFile.getType(fc.getSelectedFile());
                    if (type != null) {
                        if (RT_Object.class.isAssignableFrom(type)) {
                            prevFolderPath = fc.getSelectedFile().getParent();
                            addObject((RT_Object) RTFileReader.read(type, fc.getSelectedFile()));
                            callSgChangedListeners();
                            System.out.println("foo");
                        }
                    } else
                        throw new IOException("Ungültige Datei");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(window, e.getMessage());
                }
            }
        }
    }
	
	
	private DeleteSgNodeAction deleteSgNodeAction = new DeleteSgNodeAction("Löschen");
	protected DeleteSgNodeAction getDeleteSgNodeAction() {
		return deleteSgNodeAction;
	}
	class DeleteSgNodeAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public DeleteSgNodeAction( String string) {
			super(string);
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			if(selectedObject == null)
				return;
			if(objects.contains(selectedObject)) {
				objects.remove(selectedObject);
				selectedObject = null;
				getDeleteSgNodeAction().setEnabled(false);
				callSgChangedListeners();
			}
		}
	}
	
	List<SceneGraphChangedListener> sgChangedListener = new ArrayList<SceneGraphChangedListener>();
	private void callSgChangedListeners() {
		for(SceneGraphChangedListener listener : sgChangedListener) {
			listener.sceneGraphChanged();
		}
	}
	public void addSgChangedListeners(SceneGraphChangedListener listener) {
		sgChangedListener.add(listener);
	}
	public interface SceneGraphChangedListener {
		public void sceneGraphChanged();
	}
	
	private ExitAction exitAction = new ExitAction(this, "Beenden");
    protected ExitAction getExitAction() {
        return exitAction;
    }
    class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private RayTracerGui controller;
        
        public ExitAction(RayTracerGui c, String string) {
            super(string);
            controller = c;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            controller.exit();
        }
    }
    
    private Map<String, ChangeResolutionAction> registeredResActions = new HashMap<String, ChangeResolutionAction>();
    protected ChangeResolutionAction getChangeResolutionAction(int resx, int resy) {
    	String res = String.format("%dx%d", resx,resy);
    	if(registeredResActions.containsKey(res))
    		return registeredResActions.get(res);
    	ChangeResolutionAction ret = new ChangeResolutionAction(this, res, resx, resy);
    	registeredResActions.put(res, ret);
    	return ret;
    }
    protected void setChangeResolutionActionsEnabled(boolean val) {
		for(Iterator<String> it=registeredResActions.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			ChangeResolutionAction value = registeredResActions.get(key);
			value.setEnabled(val);
		}
    }
    class ChangeResolutionAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		int resx; int resy;
		private RayTracerGui controller;
		
		public ChangeResolutionAction(RayTracerGui c, String string, int resx, int resy) {
			super(string);
			this.controller = c;
			this.resx = resx; this. resy = resy;
		}
		
		@Override
    	public void actionPerformed(ActionEvent e) {
    		controller.setResolution(resx, resy);
    	}
    }
    
    private StartRayTraceAction startRayTraceAction = new StartRayTraceAction("Starten");
    protected StartRayTraceAction getStartRayTraceAction() {
        return startRayTraceAction;
    }
    class StartRayTraceAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
        
        public StartRayTraceAction(String string) {
            super(string);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
        	reset();
        	traceInProgress = true;
        	startRayTraceAction.setEnabled(false);
        	setChangeResolutionActionsEnabled(false);
        	loadFileAction.setEnabled(false);
        	deleteSgNodeAction.setEnabled(false);
        	
            Thread  t = new Thread(new Runnable() {
				@Override
				public void run() {
					implementation.doRayTrace();
					startRayTraceAction.setEnabled(true);
					setChangeResolutionActionsEnabled(true);
					loadFileAction.setEnabled(true);
					if(selectedObject != null)
						deleteSgNodeAction.setEnabled(true);
					else
						deleteSgNodeAction.setEnabled(false);
					traceInProgress = false;
				}
			});
            t.start();
        }
    }
	
	List<RayTracerResolutionChangedListener> resChangedListeners = new ArrayList<RayTracerResolutionChangedListener>();
	private void callResolutionChangedListeners(int resx, int resy) {
		for(RayTracerResolutionChangedListener listener : resChangedListeners) {
			listener.rayTraceResolutionChanged(resx, resy);
		}
	}
	public void addResolutionChangedListener(RayTracerResolutionChangedListener listener) {
		resChangedListeners.add(listener);
	}
	public interface RayTracerResolutionChangedListener {
		public void rayTraceResolutionChanged(int resx, int resy);
	}
	
	List<RayTracerProgressChangedListener> progressChangedListeners = new ArrayList<RayTracerProgressChangedListener>();
	private void callProgressChangedListeners(long tDelta, int points, int pointsSet) {
		for (RayTracerProgressChangedListener listener : progressChangedListeners) {
			listener.rayTraceProgressChanged(tDelta, points, pointsSet);
		}
	}
	public void addProgressChangedListener(RayTracerProgressChangedListener listener) {
		progressChangedListeners.add(listener);
	}
	public interface RayTracerProgressChangedListener {
		public void rayTraceProgressChanged(long tDelta, int points, int pointsSet);
	}
	
	class ExtensionFilter extends FileFilter {
		private String endsWith;
		private String description;
		
		public ExtensionFilter(String endsWith, String description) {
			this.endsWith = endsWith;
			this.description = description;
		}
		
		@Override
		public boolean accept(File f) {
			if(f.isDirectory())
				return true;
			return f.getName().toLowerCase().endsWith(endsWith);
		}

		@Override
		public String getDescription() {
			return description;
		}
		
	}
	
	class UpdateWorker extends Thread {
		private boolean running = true;
		
		public UpdateWorker() {
			super("UpdateWorker");
		}
		
		protected synchronized void requestUpdate() {
			//System.out.println(this.getState());
			if(this.getState() == State.WAITING)
				this.notify();
		}
		
		protected synchronized void stopWorker() {
			running = false;
			this.notify();
		}
		
		@Override
		public synchronized void run() {
			do {
				if(sTime < 0)
					callProgressChangedListeners(0, points, sPoints);
				else
					callProgressChangedListeners(System.currentTimeMillis() - sTime, points, sPoints);
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}while(running);
		}
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		
	}
	@Override
	public void dragExit(DropTargetEvent dte) {

	}
	@Override
	public void dragOver(DropTargetDragEvent dtde) {

	}
	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
		Transferable trans = dtde.getTransferable();
		DataFlavor[] flavors = dtde.getCurrentDataFlavors();
		for (DataFlavor fl : flavors) {
			if(fl.isFlavorJavaFileListType()) {
				dtde.acceptDrop(dtde.getDropAction());
				List<File> files = (List<File>) trans.getTransferData(fl);
				for (File f : files) {
					Class<? extends RTFile> type = RTFile.getType(f);
					if(type!=null) {
						addObject((RT_Object)RTFileReader.read(type, f));
						callSgChangedListeners();
					}
				}
				return;
			}
		}
		}catch(Exception e) {
			//e.printStackTrace();
		}
		try {
		dtde.rejectDrop();
		}catch(Exception e){
			JOptionPane.showMessageDialog(window, "Ungültige datei(en)","Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}
	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {

	}
}
