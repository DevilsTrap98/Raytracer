package de.hskl.imst.i.cgma.raytracer.file;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public abstract class RTFile {
	public abstract String getHeader();
	protected String fileName;
	public String getFileName() { return fileName; }
	
	@SuppressWarnings("serial")
	public static Map<String, Class<? extends RTFile> > classMapping = new HashMap<String, Class<? extends RTFile>>() {
		{
			put("TRIANGLE_MESH", T_Mesh.class);
			put("IMPLICIT_SPHERE", I_Sphere.class);
			put("SCENE_GRAPH", RTScene.class);
		}
	};
	
	public static Class<? extends RTFile> getType(File f) {
		FileReader fr;
		try {
			fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String header = readLine(br);
			if(classMapping.containsKey(header))
				return classMapping.get(header);
			return null;
		} catch (IOException e) {
			return null;
		}		
	}

	protected abstract void readContent(LineNumberReader f) throws IOException;
	
	public static <FTYPE extends RTFile> FTYPE read(Class<FTYPE> _class, File f) throws IOException {
		try {
			@SuppressWarnings("deprecation")
			FTYPE result = _class.newInstance();
			//FTYPE result = _class.newInstance();
			result.fileName = f.getName();
			// check header
			FileReader fr = new FileReader(f);
			LineNumberReader br = new LineNumberReader(fr);
			if(!readLine(br).toLowerCase().equals(result.getHeader().toLowerCase()))
				throw new IOException("Ungültiger header");
			result.readContent(br);
			return result;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected static String readLine(BufferedReader br) throws IOException {
		String result = br.readLine().trim();
		while(result.startsWith("#") || result.trim().isEmpty())
			result = br.readLine().trim();
		return result;
	}
}
