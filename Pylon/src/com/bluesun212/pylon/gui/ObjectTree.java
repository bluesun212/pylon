package com.bluesun212.pylon.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.types.PyObject;

/* TODO List:
 * enable/disable auto update on object
 */
public class ObjectTree extends JScrollPane {
	private static final long serialVersionUID = -3461047263728757683L;

	private MemoryInterface mr;
	JTree jt;
	private LinkedList<ObjectNode> autoUpdateNodes = new LinkedList<ObjectNode>();


	public ObjectTree(MemoryInterface mr, PyObject rootObj) {
		this.mr = mr;
		
		Timer autoUpdateTimer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				LinkedList<ObjectNode> removed = new LinkedList<ObjectNode>();
				for (ObjectNode node : autoUpdateNodes) {
					if (!node.onAutoUpdate()) {
						removed.add(node);
					}
				}
				
				autoUpdateNodes.removeAll(removed);
			}
		});
		
		autoUpdateTimer.setRepeats(true);
		autoUpdateTimer.start();
		

		// Create root and tree
		ObjectNode root = new ObjectNode(null, null, rootObj);
		jt = new JTree(root);
		root.setTree(jt);
		
		jt.setCellRenderer(new Renderer());
		jt.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				// Get node where clicked
				TreePath tp = jt.getPathForLocation(me.getX(), me.getY());
				if (tp == null) {
					return;
				}

				ObjectNode node = (ObjectNode) tp.getLastPathComponent();

				// Show context menu when right-clicked
				if (me.getButton() == MouseEvent.BUTTON3) {
					new ItemMenu(node).show(me.getComponent(), me.getX(), me.getY());
				} else {
					// Expand or contract nodes
					if (node.isLeaf()) {
						node.populate();
					} else {
						node.close();
					}
				}
			}
		});

		setViewportView(jt);
		root.populate();
	}

	private class Renderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 47073247158360362L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			ObjectNode node = (ObjectNode) value;
			node.onRender();
			
			setText("<html>" + node.getTitle() + "</html>");
			setIcon(node.getIcon());
			setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
			
			return this;
		}
	}

	private class ItemMenu extends JPopupMenu {
		private static final long serialVersionUID = -8532227199496399180L;
		private JMenuItem printBtn = new JMenuItem("Print Address");
		private JMenuItem updateBtn = new JMenuItem("Update");
		private JCheckBoxMenuItem autoBtn = new JCheckBoxMenuItem("Auto Update");

		public ItemMenu(ObjectNode obj) {
			// TODO: Don't allow auto update if parent is update not a list/map/tuple
			// TODO: Pre-select object if parent is auto
			super();
			add(updateBtn);
			add(autoBtn);
			add(new JSeparator());
			add(printBtn);
			
			if (autoUpdateNodes.contains(obj)) {
				autoBtn.setSelected(true);
			}

			printBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.out.println(Long.toHexString(obj.getData().getAddress()));
				}
			});
			
			updateBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (updateBtn.isEnabled()) {
						obj.update();
					}
				}
			});
			
			autoBtn.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {
					if (autoBtn.isEnabled()) {
						if (autoBtn.isSelected()) {
							autoUpdateNodes.add(obj);
						} else {
							System.out.println("Removing");
							autoUpdateNodes.remove(obj);
						}
					}
				}
			});
		}
	}
}