package com.bluesun212.pylon;

import com.bluesun212.pylon.types.PyObject;

public interface ObjectFilter {
	public abstract boolean filter(PyObject obj);
	
	public static class PassAllFilter implements ObjectFilter {
		@Override
		public boolean filter(PyObject obj) {
			return true;
		}
	}
}