package replicatorg.model.j3d;

import java.io.IOException;
import java.io.OutputStream;

import org.scijava.java3d.Geometry;
import org.scijava.java3d.Shape3D;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TriangleArray;

public abstract class ModelWriter {
	protected OutputStream ostream;
	
	public ModelWriter(OutputStream ostream) {
		this.ostream = ostream;
	}
	
	public void close() throws IOException {
		ostream.close();
	}
	
	protected TriangleArray getGeometry(Shape3D shape) {
		Geometry g = shape.getGeometry();
		if (g instanceof TriangleArray) { return (TriangleArray)g; }
		return null;
	}
	
	/**
	 * Write the given shape to the output stream, applying the given transform to all points.
	 * @param shape
	 * @param transform
	 */
	abstract public void writeShape(Shape3D shape, Transform3D transform);
}