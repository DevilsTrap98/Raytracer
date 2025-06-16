package de.hskl.imst.i.cgma.raytracer.gui;
 
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class RayTracerCanvas extends JPanel implements
RayTracerGui.RayTracerProgressChangedListener,
RayTracerGui.RayTracerResolutionChangedListener{
	private static final long serialVersionUID = 1L;

	RayTracerGui controller;
	
	protected RayTracerCanvas(RayTracerGui controller) {
		this.controller = controller;
		this.setPreferredSize(new Dimension(controller.getResX(), controller.getResY()));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage(controller.getImage(), null, 0, 0);
	}
	
	@Override
	public void rayTraceResolutionChanged(int resx, int resy) {
		this.setPreferredSize(new Dimension(resx, resy));
	}

	@Override
	public void rayTraceProgressChanged(long tDelta, int points, int pointsSet) {
		repaint();
	}
}
