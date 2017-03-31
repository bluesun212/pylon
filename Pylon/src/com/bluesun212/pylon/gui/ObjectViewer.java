package com.bluesun212.pylon.gui;

import javax.swing.JFrame;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyObject;

public class ObjectViewer extends JFrame {
	private static final long serialVersionUID = 7525262949349640919L;

	public ObjectViewer(MemoryReader mr, PyObject rootObj) {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		ObjectTree tree = new ObjectTree(mr, rootObj);
		getContentPane().add(tree);
		pack();
		setVisible(true);
	}
}

