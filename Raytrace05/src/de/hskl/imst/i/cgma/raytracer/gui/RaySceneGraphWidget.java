package de.hskl.imst.i.cgma.raytracer.gui;
 
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.hskl.imst.i.cgma.raytracer.file.RT_Object;
import de.hskl.imst.i.cgma.raytracer.gui.RayTracerGui.SceneGraphChangedListener;

public class RaySceneGraphWidget extends JPanel implements TreeSelectionListener, SceneGraphChangedListener {
	private static final long serialVersionUID = 1L;
	
	private JTree tree;
	private JScrollPane scrollPane;
	private RayTracerGui controller;
	
	private HashMap<DefaultMutableTreeNode, RT_Object> nodeObjectMap = new HashMap<DefaultMutableTreeNode, RT_Object>();
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("SceneGraph");

	public RaySceneGraphWidget(RayTracerGui sController) {
		
		controller = sController;
		tree = new JTree(root);
		scrollPane = new JScrollPane(tree);
		tree.setVisibleRowCount(0);
		tree.addTreeSelectionListener(this);
		
		JPanel addDeletePanel = new JPanel(new GridLayout(1, 2));
		JButton addButton = new JButton(controller.getLoadFileAction());
		JButton deleteButton = new JButton(controller.getDeleteSgNodeAction());
		addDeletePanel.add(addButton);
		addDeletePanel.add(deleteButton);
		
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(addDeletePanel, BorderLayout.PAGE_END);
		
		tree.expandPath(new TreePath(root));
	}
	
	public void refreshModel() {
		Vector<RT_Object> objects = controller.getObjects();
		root.removeAllChildren();
		for (RT_Object obj : objects) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(obj.getFileName());
			nodeObjectMap.put(node, obj);
			root.add(node);
		}
		((DefaultTreeModel)tree.getModel()).reload();
	}

	@Override
	public void valueChanged(TreeSelectionEvent evt) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		controller.setSelectedObject(nodeObjectMap.get(node));
	}

	@Override
	public void sceneGraphChanged() {
		refreshModel();
	}
}
