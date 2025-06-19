package de.hskl.imst.i.cgma.raytracer.file;
 
public abstract class RT_Object extends RTFile {
	public float[] min = new float[3];
	public float[] max = new float[3];
	public abstract void calcBoundingBox();
}
