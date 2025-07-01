package de.hskl.imst.i.cgma.raytracer.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;

public class STL_Mesh extends T_Mesh {

	@Override
	public void readContent(LineNumberReader br) throws IOException {

		// Check STL-File Header for Binary or ASCII Format
		br.mark(128);
		char[] header = new char[80];
		br.read(header);
		br.reset();

		List<float[]> tmpVertex = new ArrayList<>();
		List<int[]> tmpTriangles = new ArrayList<>();
		List<float[]> tmpTrianglesNormals = new ArrayList<>();

		if (new String(header).contains("solid")) {
			// STL in ASCII Format
			System.out.println("Loading ASCII-STL-File");

			int vertexCount = 0;
			String line = br.readLine();
			String[] splittedline;
			boolean endsolid = false;
			while (!endsolid) {

				// Read Triangle Normal
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;
				splittedline = line.split(" ");
				tmpTrianglesNormals.add(new float[] { Float.parseFloat(splittedline[2]),
						Float.parseFloat(splittedline[3]), Float.parseFloat(splittedline[4]) });

				// skip
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;

				// Read Vertex 1
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;
				splittedline = line.split(" ");
				tmpVertex.add(new float[] { Float.parseFloat(splittedline[1]), Float.parseFloat(splittedline[2]),
						Float.parseFloat(splittedline[3]) });
				vertexCount++;

				// Read Vertex 2
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;
				splittedline = line.split(" ");
				tmpVertex.add(new float[] { Float.parseFloat(splittedline[1]), Float.parseFloat(splittedline[2]),
						Float.parseFloat(splittedline[3]) });
				vertexCount++;

				// Read Vertex 3
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;
				splittedline = line.split(" ");
				tmpVertex.add(new float[] { Float.parseFloat(splittedline[1]), Float.parseFloat(splittedline[2]),
						Float.parseFloat(splittedline[3]) });
				vertexCount++;

				// skip
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;

				// skip
				line = br.readLine();
				if (endsolid = line.contains("endsolid"))
					break;

				// reference vertices of the triangle
				tmpTriangles.add(new int[] { vertexCount - 3, vertexCount - 2, vertexCount - 1 });

			}

		} else {
			// STL in Binary Format
			System.out.println("Loading Binary-STL-File");

			FileInputStream fin = new FileInputStream(currentFile);

			byte[] readBytes = null;
			readBytes = fin.readNBytes(80);
			readBytes = fin.readNBytes(4);
			ByteBuffer wrapped = ByteBuffer.wrap(readBytes).order(ByteOrder.LITTLE_ENDIAN); // big-endian by default
			short num = wrapped.getShort(); // 1
			System.out.println(num);

			int triangleNumber = num;
			int vertexCount = 0;

			for (int i = 0; i < triangleNumber; i++) {
				System.out.println("Triangle: " + i);
				// Read Triangle Normal
				tmpTrianglesNormals.add(new float[] { Float.intBitsToFloat(readByteNumber(fin, 4)),
						Float.intBitsToFloat(readByteNumber(fin, 4)), Float.intBitsToFloat(readByteNumber(fin, 4)) });

				// Read Vertex 1
				tmpVertex.add(new float[] { Float.intBitsToFloat(readByteNumber(fin, 4)),
						Float.intBitsToFloat(readByteNumber(fin, 4)), Float.intBitsToFloat(readByteNumber(fin, 4)) });
				vertexCount++;

				// Read Vertex 2
				tmpVertex.add(new float[] { Float.intBitsToFloat(readByteNumber(fin, 4)),
						Float.intBitsToFloat(readByteNumber(fin, 4)), Float.intBitsToFloat(readByteNumber(fin, 4)) });
				vertexCount++;

				// Read Vertex 3
				tmpVertex.add(new float[] { Float.intBitsToFloat(readByteNumber(fin, 4)),
						Float.intBitsToFloat(readByteNumber(fin, 4)), Float.intBitsToFloat(readByteNumber(fin, 4)) });
				vertexCount++;

				// skip "attribute byte count"
				fin.readNBytes(2);

				// reference vertices of the triangle
				tmpTriangles.add(new int[] { vertexCount - 3, vertexCount - 2, vertexCount - 1 });
			}

			fin.close();

		}

		vertices = new float[tmpVertex.size()][3];
		for (int i = 0; i < tmpVertex.size(); i++) {
			vertices[i] = tmpVertex.get(i);
		}

		triangles = new int[tmpTriangles.size()][3];
		for (int i = 0; i < tmpTriangles.size(); i++) {
			triangles[i] = tmpTriangles.get(i);
		}

		triangleNormals = new float[tmpTrianglesNormals.size()][3];
		for (int i = 0; i < tmpTrianglesNormals.size(); i++) {
			// if (i != 0)
			// triangleNormals[i] = tmpTrianglesNormals.get(i == 1 ? 2 : 1);
			// else
			triangleNormals[i] = tmpTrianglesNormals.get(i);
		}

		verticesMat = new int[vertices.length];
		materialsN = new int[vertices.length];
		for (int i = 0; i < verticesMat.length; i++) {
			// hardcoded material
			verticesMat[i] = 0;
			materialsN[i] = 10;
		}

		// hardcoded material values
		materials = new float[][] {
				{ 0.15f, 0.15f, 0.15f,
						0.8f, 0.6f, 0.5f,
						0.8f, 0.8f, 0.8f }
		};

		fgp = 'f';

		calcBoundingBox();

	}

	int readByteNumber(FileInputStream in, int length) throws IOException {
		byte[] readBytes = null;
		readBytes = in.readNBytes(length);
		ByteBuffer wrapped = ByteBuffer.wrap(readBytes).order(ByteOrder.LITTLE_ENDIAN); // big-endian by default
		int num = wrapped.getInt(); // 1

		return num;
	}

}
