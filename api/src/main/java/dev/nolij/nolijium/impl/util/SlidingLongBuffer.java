package dev.nolij.nolijium.impl.util;

public class SlidingLongBuffer {
	
	private int maxSize;
	private long[] array;
	
	private int base = 0;
	private int size = 0;
	
	public SlidingLongBuffer(int maxSize) {
		this.maxSize = maxSize;
		this.array = new long[maxSize + 1];
	}
	
	private int getIndex(int b, int i) {
		return (b + i) % array.length;
	}
	
	private int getIndex(int i) {
		return getIndex(base, i);
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public boolean isFull() {
		return size == maxSize;
	}
	
	public boolean any() {
		return size != 0;
	}
	
	public int maxSize() {
		return maxSize;
	}
	
	public int size() {
		return size;
	}
	
	public long get(int index) {
		if (size == 0 || index < 0 || index >= size)
			return 0L;
		
		return array[getIndex(index)];
	}
	
	public long peek() {
		return get(size - 1);
	}
	
	public long getUnsafe(int index) {
		return array[getIndex(index)];
	}
	
	public long peekUnsafe() {
		return getUnsafe(size - 1);
	}
	
	public synchronized void push(long value) {
		if (size < maxSize) {
			array[getIndex(base, size++)] = value;
		} else {
			array[getIndex(base++, size)] = value;
			base %= array.length;
		}
	}
	
	public synchronized void pop() {
		base = (base + 1) % array.length;
		size--;
	}
	
	public synchronized void resize(int newMaxSize) {
		if (newMaxSize == maxSize)
			return;
		
		final long[] newArray = new long[newMaxSize + 1];
		
		if (newMaxSize > size) {
			copy(size, newArray, base);
		} else {
			final int offsetBase = getIndex(base, size - newMaxSize);
			copy(newMaxSize, newArray, offsetBase);

			size = newMaxSize;
		}
		
		base = 0;
		array = newArray;
		maxSize = newMaxSize;
	}
	
	private void copy(int amount, long[] newArray, int offsetBase) {
		final int distanceToEdge = array.length - offsetBase;
		
		if (amount <= distanceToEdge) {
			System.arraycopy(array, offsetBase, newArray, 0, amount);
		} else {
			System.arraycopy(array, offsetBase, newArray, 0, distanceToEdge);
			System.arraycopy(array, 0, newArray, distanceToEdge, amount - distanceToEdge);
		}
	}
	
}
