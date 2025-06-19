package de.hskl.imst.i.cgma.raytracer.file;
 
import java.io.LineNumberReader;
import java.util.List;

public class RTScene extends RTFile {

	public RTSceneGraphNode root = new RTSceneGraphNode(0, 0, 0, 0, 0, 0, null, null);
	
	@Override
	public String getHeader() {
		return "SCENE_GRAPH";
	}

	@Override
	public void readContent(LineNumberReader br) {
		// TODO Auto-generated method stub
	}

	public static class RTSceneGraphNode {
		public float x,y,z;
		public float rx, ry, rz;
		public RTSceneGraphNode parent;
		public List<RTSceneGraphNode> children;
		public RT_Object object;
		
		public RTSceneGraphNode(float x, float y, float z,
								float rx, float ry, float rz,
								RTSceneGraphNode parent, RT_Object object){
			this.x = x; this.y = y; this.z = z;
			this.rx = rx; this.ry = ry; this.rz = rz;
			this.parent = parent;
			parent.children.add(this);
			this.object = object;
		}
	}
}
