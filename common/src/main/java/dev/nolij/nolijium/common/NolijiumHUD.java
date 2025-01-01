package dev.nolij.nolijium.common;

import com.sun.management.OperatingSystemMXBean;
import dev.nolij.libnolij.collect.SlidingLongBuffer;
import dev.nolij.libnolij.util.MathUtil;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NolijiumHUD {
	
	private static final boolean DEBUG = false;
	
	private static final int TEXT_COLOUR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOUR = 0x90505050;
	
	private static final Font FONT = Minecraft.getInstance().font;
	private static final int LINE_HEIGHT = FONT.lineHeight + 3;
	
	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	
	private static final int FRAME_TIME_BUFFER_SIZE_MAX = 2 << 16;
	private static final int FRAME_TIME_BUFFER_SIZE_MIN = 2 << 6;
	private static final int FRAME_TIME_BUFFER_RESIZE_THRESHOLD = 2 << 8;
	private static final long FRAME_TIME_BUFFER_RESIZE_INTERVAL = TimeUnit.SECONDS.toNanos(1);
	
	private long lastUpdateTimestamp = 0L;
	private int screenWidth = 0, screenHeight = 0;
	
	private static class Line {
		
		public String text;
		public int posX;
		public int width;
		
		public Line(String text, int posX, int width) {
			this.text = text;
			this.posX = posX;
			this.width = width;
		}
		
	}
	
	private List<Line> lines = List.of();
	private int posY = 0;
	private boolean background = false;
	
	private final SlidingLongBuffer frameTimeBuffer = new SlidingLongBuffer(FRAME_TIME_BUFFER_SIZE_MIN);
	private long lastResizeTimestamp = 0L;
	private long lastFrameTimestamp = 0L;
	private long lastFrameTime = 0L;
	
	private SystemStatsThread systemStatsThread;
	
	private void resizeFrameTimeBuffer() {
		lastResizeTimestamp = System.nanoTime();
		
		if (frameTimeBuffer.isEmpty())
			return;
		
		var duration = 0L;
		for (int i = 0; i < frameTimeBuffer.size(); i++) {
			duration += frameTimeBuffer.get(i);
		}
		
		final long target = (long) (Nolijium.config.hudFrameTimeBufferSize * 1E9);
		final int newSize = (int) MathUtil.clamp(
			(frameTimeBuffer.size() * (double) target / duration),
			FRAME_TIME_BUFFER_SIZE_MIN,
			FRAME_TIME_BUFFER_SIZE_MAX);
		
		if (Math.abs(newSize - frameTimeBuffer.maxSize()) > FRAME_TIME_BUFFER_RESIZE_THRESHOLD) {
			frameTimeBuffer.resize(newSize);
		}
	}
	
	private static int getFrameTimeFPS(long frameTime) {
		return (int) (1E9D / frameTime);
	}
	
	private static String getFrameTimeString(long frameTime) {
		if (frameTime >= 1E7) {
			return "%dms".formatted((int) (frameTime / 1E6));
		} else if (frameTime >= 1E4) {
			return "%dµs".formatted((int) (frameTime / 1E3));
		} else {
			return "%dns".formatted(frameTime);
		}
	}
	
	private List<Line> getLines() {
		var result = new ArrayList<String>();
		
		if (Nolijium.config.hudShowFPS != DetailLevel.NONE) {
			synchronized (frameTimeBuffer) {
				if (Nolijium.config.hudShowFPS == DetailLevel.SIMPLE) {
					result.add("FPS: %d".formatted(getFrameTimeFPS(lastFrameTime)));
				} else if (Nolijium.config.hudShowFPS == DetailLevel.EXTENDED) {
					result.add("FPS: %d (%s)".formatted(getFrameTimeFPS(lastFrameTime), getFrameTimeString(lastFrameTime)));
					
					final String leftPad = Nolijium.config.hudAlignmentX == Alignment.X.LEFT ? "  " : "";
					if (!frameTimeBuffer.isEmpty()) {
						long max = -Long.MAX_VALUE;
						long min = Long.MAX_VALUE;
						long avg = 0L;
						
						for (int i = 0; i < frameTimeBuffer.size(); i++) {
							long frameTime = frameTimeBuffer.getUnsafe(i);
							max = Math.max(max, frameTime);
							min = Math.min(min, frameTime);
							avg += frameTime;
						}
						avg /= frameTimeBuffer.size();
						
						result.add("%sMIN: %d (%s)".formatted(leftPad, getFrameTimeFPS(max), getFrameTimeString(max)));
						result.add("%sMAX: %d (%s)".formatted(leftPad, getFrameTimeFPS(min), getFrameTimeString(min)));
						result.add("%sAVG: %d (%s)".formatted(leftPad, getFrameTimeFPS(avg), getFrameTimeString(avg)));
					} else {
						result.add("%sMIN: ???".formatted(leftPad));
						result.add("%sMAX: ???".formatted(leftPad));
						result.add("%sAVG: ???".formatted(leftPad));
					}
					if (DEBUG)
						result.add("%sSIZE: %d".formatted(leftPad, frameTimeBuffer.maxSize()));
					result.add("");
				}
			}
		}
		
		if (Nolijium.config.hudShowCPU || Nolijium.config.hudShowMemory) {
			// Start thread
			if (this.systemStatsThread == null) {
				this.systemStatsThread = new SystemStatsThread();
			}
			
			var stats = this.systemStatsThread.getCurrentStats();
			
			if (Nolijium.config.hudShowCPU) {
				result.add("CPU: %2.2f%%".formatted(stats.cpuUsage() * 100D));
			}
			
			if (Nolijium.config.hudShowMemory) {
				result.add("RAM: %2.2f%%  %d/%dMiB".formatted(stats.memoryUsage(), stats.usedMemoryMb(), stats.allocatedMemoryMb()));
			}
		} else {
			// Stop thread as it's no longer needed
			if (this.systemStatsThread != null) {
				this.systemStatsThread.shutdown();
				this.systemStatsThread = null;
			}
		}
		
		if (Nolijium.config.hudShowCoordinates) {
			if (!result.isEmpty())
				result.add("");
			
			final LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				result.add("X: %.2f Y: %.2f Z: %.2f".formatted(player.getX(), player.getY(), player.getZ()));
			} else {
				result.add("X: ?.?? Y: ?.?? Z: ?.??");
			}
		}
		
		return result
			.stream()
			.map(x -> new Line(x, 0, FONT.width(x)))
			.toList();
	}
	
	private void update() {
		lastUpdateTimestamp = System.nanoTime();
		
		lines = getLines();
		
		background = Nolijium.config.hudBackground;
		
		if (Nolijium.config.hudAlignmentX == Alignment.X.RIGHT) {
			lines.forEach(line ->
				line.posX = screenWidth - line.width - Nolijium.config.hudMarginX);
		} else {
			lines.forEach(line ->
				line.posX = Nolijium.config.hudMarginX);
		}
		
		if (Nolijium.config.hudAlignmentY == Alignment.Y.BOTTOM) {
			final int height = lines.size() * LINE_HEIGHT;
			
			posY = screenHeight - height - Nolijium.config.hudMarginY + 2;
		} else {
			posY = Nolijium.config.hudMarginY - 2;
		}
	}
	
	protected abstract boolean isDebugScreenOpen();
	
	private boolean isHidden() {
		return
			!Nolijium.config.hudEnabled ||
				Minecraft.getInstance().options.hideGui ||
				(Nolijium.config.hudAlignmentY == Alignment.Y.TOP && isDebugScreenOpen());
	}
	
	private void onFrame(@NotNull GuiGraphics guiGraphics) {
		screenWidth = guiGraphics.guiWidth();
		screenHeight = guiGraphics.guiHeight();
		
		final long timestamp = System.nanoTime();
		
		if (Nolijium.config.hudShowFPS != DetailLevel.NONE) {
			if (lastFrameTimestamp != 0L) {
				lastFrameTime = timestamp - lastFrameTimestamp;
				if (Nolijium.config.hudShowFPS == DetailLevel.EXTENDED) {
					if (timestamp - lastResizeTimestamp > FRAME_TIME_BUFFER_RESIZE_INTERVAL) {
						resizeFrameTimeBuffer();
					}
					
					frameTimeBuffer.push(lastFrameTime);
				}
			} else {
				lastResizeTimestamp = System.nanoTime();
			}
		}
		
		if (Nolijium.config.hudRefreshRateTicks == 0 ||
			timestamp - lastUpdateTimestamp > Nolijium.config.hudRefreshRateTicks * 50E6)
			update();
		
		lastFrameTimestamp = timestamp;
	}
	
	protected void render(@NotNull GuiGraphics guiGraphics) {
		if (isHidden())
			return;
		
		onFrame(guiGraphics);
		
		//noinspection deprecation
		guiGraphics.drawManaged(() -> {
			final double timestamp = lastFrameTimestamp * 1E-9D;
			
			var linePosY = posY;
			for (int i = 0; i < lines.size(); i++) {
				final Line line = lines.get(i);
				if (!line.text.isEmpty()) {
					if (background)
						guiGraphics.fill(
							line.posX - 2, linePosY,
							line.posX + line.width + (Nolijium.config.hudShadow ? 2 : 1), linePosY + LINE_HEIGHT,
							BACKGROUND_COLOUR);
					
					guiGraphics.drawString(
						FONT, line.text,
						line.posX, linePosY + 2,
						Nolijium.config.enableChromaHUD 
							? ColourUtil.chroma(timestamp, Nolijium.config.chromaSpeed, -i)
							: TEXT_COLOUR, 
						Nolijium.config.hudShadow);
				}
				
				linePosY += LINE_HEIGHT;
			}
		});
	}
	
	private static class SystemStatsThread extends Thread {
		private static final int UPDATE_DELAY_MS = 100;
		/**
		 * The number of times an invalid CPU usage can be reported before we give up on trying to retrieve it entirely.
		 * On Windows, calling this function in the errored state seems to be extremely slow. 
		 */
		private static final int MAX_INVALID_CPU_USAGE_VALUES = 10;
		
		public static final class Stats {
			private final double cpuUsage;
			private final long usedMemoryMb;
			private final long allocatedMemoryMb;
			
			public Stats(double cpuUsage, long usedMemoryMb, long allocatedMemoryMb) {
				this.cpuUsage = cpuUsage;
				this.usedMemoryMb = usedMemoryMb;
				this.allocatedMemoryMb = allocatedMemoryMb;
			}
			
			public double memoryUsage() {
						return ((double) usedMemoryMb / allocatedMemoryMb) * 100;
					}
			
			public double cpuUsage() {
				return cpuUsage;
			}
			
			public long usedMemoryMb() {
				return usedMemoryMb;
			}
			
			public long allocatedMemoryMb() {
				return allocatedMemoryMb;
			}
		}
		
		private final AtomicReference<Stats> statsReference;
		private volatile boolean running;
		
		private int numInvalidCpuUsageValues;
		
		public SystemStatsThread() {
			super("Nolijium system stats thread");
			this.statsReference = new AtomicReference<>(new Stats(0, 0, 0));
			this.running = true;
			// Lower priority to reduce contention
			this.setPriority(Thread.MIN_PRIORITY);
			this.start();
		}
		
		public void shutdown() {
			this.running = false;
		}
		
		@Override
		public void run() {
			while (this.running) {
				this.statsReference.set(collectStats());
				try {
					Thread.sleep(UPDATE_DELAY_MS);
				} catch(InterruptedException e) {
					return;
				}
			}
		}
		
		private Stats collectStats() {
			double cpuUsage;
			if(numInvalidCpuUsageValues < MAX_INVALID_CPU_USAGE_VALUES) {
				cpuUsage = OS_BEAN.getProcessCpuLoad();
				if (cpuUsage < 0) {
					cpuUsage = OS_BEAN.getCpuLoad();
					if (cpuUsage < 0) {
						numInvalidCpuUsageValues++;
					}
				}
			} else {
				cpuUsage = -1;
			}
			
			final long maxMemory = Runtime.getRuntime().maxMemory();
			final long totalMemory = Runtime.getRuntime().totalMemory();
			final long freeMemory = Runtime.getRuntime().freeMemory();
			final long usedMemory = totalMemory - freeMemory;
			
			long usedMemoryAllocation = usedMemory / (1024 * 1024);
			long memoryAllocation = maxMemory / (1024 * 1024);
			
			return new Stats(cpuUsage, usedMemoryAllocation, memoryAllocation);
		}
		
		public Stats getCurrentStats() {
			return this.statsReference.get();
		}
	}
	
}
