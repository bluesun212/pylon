package com.bluesun212.pylon.gui;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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

class ObjectNode extends DefaultMutableTreeNode {	
	private static final long serialVersionUID = -2972977079916062555L;
	
	static final Comparator<ObjectNode> comp = new Comparator<ObjectNode>() {
		public int compare(ObjectNode o1, ObjectNode o2) {
			return o1.key.toString().compareTo(o2.key.toString());
		}
	};
	
	private static final Icon[] ICONS;
	static {
		String[] filenames = { "bool", "dict", "dict2", "float", "int", "list", "list2", "oldinst", "oldinst2", "str",
				"tuple", "tuple2", "typeinst", "typeinst2" };

		ICONS = new Icon[filenames.length];
		for (int i = 0; i < filenames.length; i++) {
			ICONS[i] = new ImageIcon(ObjectNode.class.getResource("icons/" + filenames[i] + ".png"),
					"type: " + filenames[i]);
		}
	}

	private JTree tree;
	private PyObject data;
	private PyObject key;
	
	private int size = -1;
	private Icon iconObj;
	private String title;

	public ObjectNode(JTree jt, PyObject parent, PyObject val) {
		this.tree = jt;
		this.data = val;
		this.key = parent;
	}

	public void populate() {
		PyDict dict = null;
		if (data instanceof PyTypeInstance) { // Instance of a type object
			PyTypeInstance pti = (PyTypeInstance) data;
			dict = pti.getDict();
		}

		if (data instanceof PyOldInstance) { // Instance of an old class
			PyOldInstance poi = (PyOldInstance) data;
			dict = poi.getDict();
		}

		if (data instanceof PyDict) {
			dict = (PyDict) data;
		}

		// Sort the corresponding pairs
		if (dict != null) {
			LinkedList<ObjectNode> sl = new LinkedList<ObjectNode>();
			Map<PyObject, PyObject> map = dict.get();
			
			for (Entry<PyObject, PyObject> pair : map.entrySet()) {
				PyObject key = pair.getKey();
				PyObject val = pair.getValue();
				sl.add(new ObjectNode(tree, key, val));
			}

			sl.sort(comp);

			// Create the nodes
			for (ObjectNode node : sl) {
				add(node);
			}
		}

		// Lists and tuples
		if (data instanceof PyList) {
			List<PyObject> list = ((PyList) data).get();
			for (PyObject val : list) {
				add(new ObjectNode(tree, null, val));
			}
		}

		if (data instanceof PyTuple) {
			List<PyObject> list = ((PyTuple) data).get();
			for (PyObject val : list) {
				add(new ObjectNode(tree, null, val));
			}
		}
		
		// TODO: Move these
		if (!isLeaf()) {
			tree.expandPath(new TreePath(getPath()));
			((DefaultTreeModel) tree.getModel()).nodeStructureChanged(this);
		}
	}
	
	public void close() {
		if (!isLeaf()) {
			removeAllChildren();
			((DefaultTreeModel) tree.getModel()).nodeStructureChanged(this);
		}
	}

	public void update() {
		if (!isLeaf()) {
			close();
			populate();
		}
	}
	
	public boolean onAutoUpdate() {
		if (getParent() != null) {
			update();
			return true;
		} else {
			// TODO: Remove from list
			return false;
		}
	}
	
	public void onRender() {
		int icon = 0;
		if (data instanceof PyBool) {
			icon = 0;
		} else if (data instanceof PyDict) {
			icon = 1;
			size = Utils.getDict(data).size();
		} else if (data instanceof PyFloat) {
			icon = 3;
		} else if (data instanceof PyInteger) {
			icon = 4;
		} else if (data instanceof PyList) {
			icon = 5;
			size = ((PyList) data).get().size();
		} else if (data instanceof PyOldInstance) {
			icon = 7;
			size = Utils.getDict(data).size();
		} else if (data instanceof PyString) {
			icon = 9;
		} else if (data instanceof PyTuple) {
			icon = 10;
			size = ((PyTuple) data).get().size();
		} else if (data instanceof PyTypeInstance) {
			icon = 12;
			size = Utils.getDict(data).size();
		}

		// Set text
		title = data.toString();
		if (data instanceof PyString) {
			title = "\"" + title + "\"";
		}

		if (key != null) {
			title = "<b>" + key.toString() + "</b>: " + title;
		}

		// Set icon
		if (size == 0) {
			title += " (empty)";
			icon++;
		} else if (size > 0) {
			title += " (" + size + " items)";
		}
		
		iconObj = ICONS[icon];
	}
	
	public String getTitle() {
		return title;
	}
	
	public Icon getIcon() {
		return iconObj;
	}
	
	public PyObject getData() {
		return data;
	}
	
	public void setTree(JTree tree) {
		this.tree = tree;
	}
}