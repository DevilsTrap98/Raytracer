package de.hskl.imst.i.cgma.raytracer.file;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RTFileReader {
	public static <FTYPE extends RTFile> FTYPE read(Class<FTYPE> _class, File f) throws IOException {
		if(!f.exists()) throw new FileNotFoundException("Datei nicht gefunden!");
		FTYPE result = FTYPE.read(_class, f);
		return result;
	}
}
