import dev.nolij.nolijium.impl.util.SlidingLongBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingLongArrayTest {
	
	@Test
	public void testAutoPop() {
		final SlidingLongBuffer array = new SlidingLongBuffer(10);
		for (int i = 0; i < 1000; i++) {
			if (i > 0)
				assertEquals(i - 1, array.peek());
			
			array.push(i);
			
			assertEquals(i, array.peek());
			assertEquals(Math.min(i + 1, array.maxSize()), array.size());
			
			for (int j = 0; j < array.size(); j++) {
				assertEquals(i - array.size() + j + 1, array.get(j));
			}
		}
	}
	
	@Test
	public void testManualPop() {
		final SlidingLongBuffer array = new SlidingLongBuffer(5);
		for (int i = 0; i < 6; i++)
			array.push(i);
		
		assertEquals(5, array.peek());
		assertEquals(1, array.get(0));
		assertEquals(5, array.size());
		
		array.pop();
		
		assertEquals(5, array.peek());
		assertEquals(2, array.get(0));
		assertEquals(4, array.size());
	}
	
	@Test
	public void testResizeExpand() {
		for (int padding = 0; padding < 250; padding++) {
			System.out.println(padding);
			
			final SlidingLongBuffer array = new SlidingLongBuffer(100);
			
			for (int i = 0; i < padding; i++)
				array.push(0);
			
			for (int i = 0; i < 80; i++)
				array.push(i);
			
			array.resize(200);
			
			assertEquals(80 + Math.min(padding, 20), array.size());
			
			int j = -Math.min(20, padding);
			for (int i = 0; i < array.size(); i++) {
				assertEquals(Math.max(0, j++), array.get(i));
			}
		}
	}
	
	@Test
	public void testResizeShrink() {
		for (int padding = 0; padding < 200; padding++) {
			System.out.println(padding);
			
			final SlidingLongBuffer array = new SlidingLongBuffer(150);
			
			for (int i = 0; i < padding; i++)
				array.push(0);
			
			for (int i = 0; i < 100; i++)
				array.push(i);
			
			array.resize(20);
			
			assertEquals(20, array.size());
			
			for (int i = 0; i < array.size(); i++) {
				assertEquals(80 + i, array.get(i));
			}
		}
	}
	
}
