package dev.nolij.nolijium.common;

import com.sun.management.OperatingSystemMXBean;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import dev.nolij.nolijium.impl.util.MathHelper;
import dev.nolij.nolijium.impl.util.RGBHelper;
import dev.nolij.nolijium.impl.util.SlidingLongBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
	
	private static final long SYSTEM_STATS_UPDATE_COOLDOWN = TimeUnit.MILLISECONDS.toNanos(100);
	private long lastSystemStatsUpdateTimestamp;
	private double cpuUsage, memoryUsage;
	private long usedMemoryAllocation, memoryAllocation;
	
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
	
	private void resizeFrameTimeBuffer() {
		lastResizeTimestamp = System.nanoTime();
		
		if (frameTimeBuffer.isEmpty())
			return;
		
		var duration = 0L;
		for (int i = 0; i < frameTimeBuffer.size(); i++) {
			duration += frameTimeBuffer.get(i);
		}
		
		final long target = (long) (Nolijium.config.hudFrameTimeBufferSize * 1E9);
		final int newSize = (int) MathHelper.clamp(
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
	
	private List<Line> getLines() {
		var result = new ArrayList<String>();
		
		if (Nolijium.config.hudShowFPS != DetailLevel.NONE) {
			synchronized (frameTimeBuffer) {
				final int fps = getFrameTimeFPS(lastFrameTime);
				result.add("FPS: %d".formatted(fps));
				
				if (Nolijium.config.hudShowFPS == DetailLevel.EXTENDED) {
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
						
						result.add("%sMIN: %d".formatted(leftPad, getFrameTimeFPS(max)));
						result.add("%sMAX: %d".formatted(leftPad, getFrameTimeFPS(min)));
						result.add("%sAVG: %d".formatted(leftPad, getFrameTimeFPS(avg)));
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
		
		if (lastFrameTimestamp - lastSystemStatsUpdateTimestamp > SYSTEM_STATS_UPDATE_COOLDOWN) {
			lastSystemStatsUpdateTimestamp = lastFrameTimestamp;
			
			if (Nolijium.config.hudShowCPU) {
				cpuUsage = OS_BEAN.getProcessCpuLoad();
				if (cpuUsage == -1)
					cpuUsage = OS_BEAN.getCpuLoad();
			}
			
			if (Nolijium.config.hudShowMemory) {
				final long maxMemory = Runtime.getRuntime().maxMemory();
				final long totalMemory = Runtime.getRuntime().totalMemory();
				final long freeMemory = Runtime.getRuntime().freeMemory();
				final long usedMemory = totalMemory - freeMemory;
				
				memoryUsage = usedMemory * 100D / maxMemory;
				usedMemoryAllocation = usedMemory / (1024 * 1024);
				memoryAllocation = maxMemory / (1024 * 1024);
			}
		}
		
		if (Nolijium.config.hudShowCPU) {
			result.add("CPU: %2.2f%%".formatted(cpuUsage * 100D));
		}
		
		if (Nolijium.config.hudShowMemory) {
			result.add("RAM: %2.2f%%  %d/%dMiB".formatted(memoryUsage, usedMemoryAllocation, memoryAllocation));
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
		
		if (Nolijium.config.hudShowFPS != DetailLevel.NONE) {
			final long timestamp = System.nanoTime();
			
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
			
			lastFrameTimestamp = timestamp;
		}
		
		if (Nolijium.config.hudRefreshRateTicks == 0 ||
			System.nanoTime() - lastUpdateTimestamp > Nolijium.config.hudRefreshRateTicks * 50E6)
			update();
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
							? RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, -i)
							: TEXT_COLOUR, 
						Nolijium.config.hudShadow);
				}
				
				linePosY += LINE_HEIGHT;
			}
		});
	}
	
}
