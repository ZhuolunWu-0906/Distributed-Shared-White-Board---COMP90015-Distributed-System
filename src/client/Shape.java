package client;

import java.awt.Color;

public class Shape {
	
	public int x1, y1, x2, y2, w, h, xMin, yMin;
	public String shapeName, text;
	public Color color;
	
	// Default constructor
	public Shape(String shapeName, Color color) {
		this.shapeName = shapeName;
		this.color = color;
	}
	
	// Line, circle, oval, rectangle
	public Shape(String shapeName, Color color, int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.w = Math.abs(x1 - x2);
		this.h = Math.abs(y1 - y2);
		this.xMin = Math.min(x1, x2);
		this.yMin = Math.min(y1, y2);
		this.shapeName = shapeName;
		this.color = color;
	}
	
	// Text
	public Shape(String shapeName, Color color, String text, int x1, int y1) {
		this.x1 = x1;
		this.y1 = y1;
		this.shapeName = shapeName;
		this.color = color;
		this.text = text;
	}
	
	// Calculate essential values
	public void calculate() {
		this.w = Math.abs(x1 - x2);
		this.h = Math.abs(y1 - y2);
		this.xMin = Math.min(x1, x2);
		this.yMin = Math.min(y1, y2);
	}
	
}
