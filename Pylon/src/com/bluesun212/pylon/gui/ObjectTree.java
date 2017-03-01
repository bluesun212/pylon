package com.bluesun212.pylon.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyTypeInstance;

/* TODO List:
 * icon to show type
 * right click to show address
 * different font weight on desc.
 * enable/disable auto update on object
 */
public class ObjectTree extends JScrollPane{
	private static final long serialVersionUID = -3461047263728757683L;

	public ObjectTree(MemoryReader mr, long address) {
		DefaultMutableTreeNode root = createRoot(mr, address);
		populateNode(mr, root);
		JTree jt = new JTree(root);
		
		jt.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				TreePath tp = jt.getPathForLocation(me.getX(), me.getY());
				if (tp == null) {
					return;
				}
				
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tp.getLastPathComponent();

				if (me.getButton() == MouseEvent.BUTTON3) {
					System.out.println(Long.toHexString((long)dmtn.getUserObject()));
					return;
				}
				
				if (dmtn.isLeaf()) {
					populateNode(mr, dmtn);
					jt.expandPath(tp);
				} else {
					dmtn.removeAllChildren();
				}
				
				((DefaultTreeModel) jt.getModel()).nodeStructureChanged(dmtn);
			}
		});
		
		setViewportView(jt);
	}
	
	private static DefaultMutableTreeNode createRoot(MemoryReader mr, long addr) {
		Object val = mr.getReader().read(addr);
		return new ObjectNode(null, val, addr);
	}
	
	@SuppressWarnings("unchecked")
	private static void populateNode(MemoryReader mr, DefaultMutableTreeNode parent) {
		long address = (long) parent.getUserObject();
		Object obj = mr.getReader().read(address);

		HashMap<Integer, Integer> dict = null;
		if (obj instanceof PyTypeInstance) { // Instance of a type object
			PyTypeInstance pti = (PyTypeInstance) obj;
			dict = pti.getDict();
		}
		
		if (obj instanceof PyOldInstance) { // Instance of a class
			PyOldInstance poi = (PyOldInstance) obj;
			dict = poi.getDict();
		}
		
		// Dictionary (Unsafe conversion)
		if (obj instanceof HashMap<?, ?>) {
			dict = (HashMap<Integer, Integer>) obj;
		}
		
		if (dict != null) {
			// Sort elements
			LinkedList<SortElement> sl = new LinkedList<SortElement>();
			
			for (Entry<Integer, Integer> pair : dict.entrySet()) {
				Object key = mr.getReader().read(pair.getKey());
				Object val = mr.getReader().read(pair.getValue());
				if (val.toString().equals("null")) {
					System.out.println(key.toString());
				}
				sl.add(new SortElement(pair.getValue(), key, val));
			}
			
			sl.sort(new Comparator<SortElement>() {
				public int compare(SortElement arg0, SortElement arg1) {
					return arg0.key.toString().compareTo(arg1.key.toString());
				}
			});
				
			// Create the nodes
			for (SortElement se : sl) {
				ObjectNode on = new ObjectNode(se.key.toString(), se.val, se.valAddr);
				parent.add(on);
			}
		}
		
		// Lists and tuples (Unsafe)
		if (obj instanceof LinkedList<?>) {
			LinkedList<Integer> list = (LinkedList<Integer>) obj;
			
			for (Integer valAddr : list) {
				Object val = mr.getReader().read(valAddr);
				ObjectNode on = new ObjectNode(null, val, valAddr);
				parent.add(on);
			}
		}
	}
	
	private static class SortElement {
		private long valAddr;
		private Object key;
		private Object val;
		
		public SortElement(long valAddr, Object key, Object val) {
			this.valAddr = valAddr;
			this.key = key;
			this.val = val;
		}
	}
	
	private static class ObjectNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -2972977079916062555L;
		private String parent;
		private String title;
		
		public ObjectNode(String pName, Object val, long address) {
			parent = pName;
			this.setUserObject(address);
			
			/**
			 * icon list:
			 * -number
			 * -string
			 * -bool
			 * 
			 * -list
			 * -tuple
			 * -dict
			 * 
			 * -old instance
			 * -type instance
			 * 
			 * -greyed out if empty
			 */
			
			if (val instanceof HashMap<?, ?>) {
				HashMap<?, ?> ds = (HashMap<?, ?>) val;
				
				if (ds.isEmpty()) {
					title = "dict (empty)";
				} else {
					title = "dict (" + ds.size() + " items)";
				}
			} else if (val instanceof LinkedList<?>) {
				LinkedList<?> ds = (LinkedList<?>) val;

				if (ds.isEmpty()) {
					title = "list (empty)";
				} else {
					title = "list (" + ds.size() + " items)";
				}
			} else {
				title = val.toString();
			}
		}
		
		@Override
		public String toString() {
			if (parent != null) {
				return parent + ": " + title;
			}
			
			return title;
		}
	}
}