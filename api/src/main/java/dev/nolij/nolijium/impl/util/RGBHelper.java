package dev.nolij.nolijium.impl.util;

public final class RGBHelper {
	
	private RGBHelper() {}
	
	public static int getColour(double alpha, double red, double green, double blue) {
		return ((int) (alpha * 255D) << 24) | ((int) (red * 255D) << 16) | ((int) (green * 255D) << 8) | (int) (blue * 255D);
	}
	
	public static double getAlpha(int colour) {
		return (colour >> 24 & 0xFF) / 255D;
	}
	
	public static double getRed(int colour) {
		return (colour >> 16 & 0xFF) / 255D;
	}
	
	public static double getGreen(int colour) {
		return (colour >> 8 & 0xFF) / 255D;
	}
	
	public static double getBlue(int colour) {
		return (colour & 0xFF) / 255D;
	}
	
	private static double chromaInternal(double speed, double shift, int offset) {
		return (1D + Math.sin(speed * shift + offset)) * 0.5D;
	}
	
	public static double chromaRed(double timestamp, double speed, int index) {
		return chromaInternal(speed, index + timestamp, 0);
	}
	
	public static double chromaGreen(double timestamp, double speed, int index) {
		return chromaInternal(speed, index + timestamp, 2);
	}
	
	public static double chromaBlue(double timestamp, double speed, int index) {
		return chromaInternal(speed, index + timestamp, 4);
	}
	
	public static int chroma(double timestamp, double speed, int index) {
		return getColour(
			1D,
			chromaRed(timestamp, speed, index), 
			chromaGreen(timestamp, speed, index),
			chromaBlue(timestamp, speed, index));
	}
	
	public static int chroma(double timestamp, double speed, int index, double factor) {
		return getColour(
			1D,
			chromaRed(timestamp, speed, index) * factor, 
			chromaGreen(timestamp, speed, index) * factor,
			chromaBlue(timestamp, speed, index) * factor);
	}
	
}
