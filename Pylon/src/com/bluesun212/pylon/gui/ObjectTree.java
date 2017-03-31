package com.bluesun212.pylon.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.Utils;
import com.bluesun212.pylon.types.PyBool;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyFloat;
import com.bluesun212.pylon.types.PyInteger;
import com.bluesun212.pylon.types.PyList;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyString;
import com.bluesun212.pylon.types.PyTuple;
import com.bluesun212.pylon.types.PyTypeInstance;

/* TODO List:
 * different font weight on desc.
 * enable/disable auto update on object
 */
public class ObjectTree extends JScrollPane {
	private static final long serialVersionUID = -3461047263728757683L;
	private static final Icon[] ICONS;
	
	static {
		String[] filenames = { "bool", "dict", "dict2", "float", "int", "list", "list2", "oldinst", "oldinst2", "str",
				"tuple", "tuple2", "typeinst", "typeinst2" };

		ICONS = new Icon[filenames.length];
		for (int i = 0; i < filenames.length; i++) {
			ICONS[i] = new ImageIcon(ObjectTree.class.getResource("icons/" + filenames[i] + ".png"),
					"type: " + filenames[i]);
		}
	}

	public ObjectTree(MemoryReader mr, long address) {
		// Create root and tree
		PyObject val = mr.getObject(address);
		ObjectNode root = new ObjectNode(null, val);

		populateNode(mr, root);
		JTree jt = new JTree(root);
		jt.setCellRenderer(new Renderer());

		jt.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				// Get node where clicked
				TreePath tp = jt.getPathForLocation(me.getX(), me.getY());
				if (tp == null) {
					return;
				}

				ObjectNode node = (ObjectNode) tp.getLastPathComponent();
				
				// Get address when right-clicked
				if (me.getButton() == MouseEvent.BUTTON3) {
					PyObject obj = node.val;
					System.out.println(Long.toHexString(obj.getAddress()));
					return;
				}

				// Expand or contract nodes
				if (node.isLeaf()) {
					populateNode(mr, node);
					jt.expandPath(tp);
				} else {
					node.removeAllChildren();
				}

				((DefaultTreeModel) jt.getModel()).nodeStructureChanged(node);
			}
		});

		setViewportView(jt);
	}

	private void populateNode(MemoryReader mr, ObjectNode parent) {
		PyObject obj = parent.val;

		PyDict dict = null;
		if (obj instanceof PyTypeInstance) { // Instance of a type object
			PyTypeInstance pti = (PyTypeInstance) obj;
			dict = pti.getDict();
		}

		if (obj instanceof PyOldInstance) { // Instance of an old class
			PyOldInstance poi = (PyOldInstance) obj;
			dict = poi.getDict();
		}

		if (obj instanceof PyDict) {
			dict = (PyDict) obj;
		}

		// Sort the corresponding pairs
		if (dict != null) {
			LinkedList<ObjectNode> sl = new LinkedList<ObjectNode>();

			Map<PyObject, PyObject> map = dict.get();
			for (Entry<PyObject, PyObject> pair : map.entrySet()) {
				PyObject key = pair.getKey();
				PyObject val = pair.getValue();
				sl.add(new ObjectNode(key, val));
			}

			sl.sort(new Comparator<ObjectNode>() {
				public int compare(ObjectNode arg0, ObjectNode arg1) {
					return arg0.parent.toString().compareTo(arg1.parent.toString());
				}
			});

			// Create the nodes
			for (ObjectNode node : sl) {
				parent.add(node);
			}
		}

		// Lists and tuples
		if (obj instanceof PyList) {
			List<PyObject> list = ((PyList) obj).get();
			for (PyObject val : list) {
				parent.add(new ObjectNode(null, val));
			}
		}

		if (obj instanceof PyTuple) {
			List<PyObject> list = ((PyTuple) obj).get();
			for (PyObject val : list) {
				parent.add( new ObjectNode(null, val));
			}
		}
	}

	private class ObjectNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -2972977079916062555L;
		private PyObject val;
		private PyObject parent;

		public ObjectNode(PyObject parent, PyObject val) {
			this.val = val;
			this.parent = parent;
		}
	}

	private class Renderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 47073247158360362L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			// Choose behavior based on object
			ObjectNode node = (ObjectNode) value;
			PyObject obj = node.val;

			int icon = 0;
			int size = -1;
			if (obj instanceof PyBool) {
				icon = 0;
			} else if (obj instanceof PyDict) {
				icon = 1;
				size = Utils.getDict(obj).size();
			} else if (obj instanceof PyFloat) {
				icon = 3;
			} else if (obj instanceof PyInteger) {
				icon = 4;
			} else if (obj instanceof PyList) {
				icon = 5;
				size = ((PyList) obj).get().size();
			} else if (obj instanceof PyOldInstance) {
				icon = 7;
				size = Utils.getDict(obj).size();
			} else if (obj instanceof PyString) {
				icon = 9;
			} else if (obj instanceof PyTuple) {
				icon = 10;
				size = ((PyTuple) obj).get().size();
			} else if (obj instanceof PyTypeInstance) {
				icon = 12;
				size = Utils.getDict(obj).size();
			}

			// Set text
			String title = obj.toString();
			if (obj instanceof PyString) {
				title = "\"" + title + "\"";
			}

			if (node.parent != null) {
				title = node.parent.toString() + ": " + title;
			}

			// Set icon
			if (size == 0) {
				title += " (empty)";
				icon++;
			} else if (size > 0) {
				title += " (" + size + " items)";
			}

			setText(title);
			setIcon(ICONS[icon]);
			this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

			return this;
		}
	}
}