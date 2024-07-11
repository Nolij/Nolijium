package dev.nolij.nolijium.forge;

import com.mojang.blaze3d.platform.Window;
import com.sun.management.OperatingSystemMXBean;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.NolijiumConstants;
import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.util.DetailLevel;
import dev.nolij.nolijium.impl.util.MathHelper;
import dev.nolij.nolijium.impl.util.SlidingLongBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = NolijiumConstants.MOD_ID)
public class NolijiumHUDRenderLayer  {
	
	private static final boolean DEBUG = false;
	
	private static final int TEXT_COLOUR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOUR = 0x90505050;
	
	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	
	private static final int FRAME_TIME_BUFFER_SIZE_MAX = 2 << 16;
	private static final int FRAME_TIME_BUFFER_SIZE_MIN = 2 << 6;
	private static final int FRAME_TIME_BUFFER_RESIZE_THRESHOLD = 2 << 8;
	private static final long FRAME_TIME_BUFFER_RESIZE_INTERVAL = (long) 1E9;
	
	private static long lastUpdateTimestamp = 0L;
	private static int screenWidth = 0, screenHeight = 0;
	
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
	
	private static List<Line> lines = List.of();
	private static int posY = 0;
	private static boolean background = false;
	
	private static final SlidingLongBuffer frameTimeBuffer = new SlidingLongBuffer(FRAME_TIME_BUFFER_SIZE_MIN);
	private static long lastResizeTimestamp = 0L;
	private static long lastFrameTimestamp = 0L;
	private static long lastFrameTime = 0L;
	
	private static void resizeFrameTimeBuffer() {
		lastResizeTimestamp = System.nanoTime();
		
		if (!frameTimeBuffer.isFull())
			return;
		
		var duration = 0L;
		
		for (int i = 0; i < frameTimeBuffer.size(); i++) {
			duration += frameTimeBuffer.get(i);
		}
		
		final long target = (long) (Nolijium.config.hudFrameTimeBufferSize * 1E9);
		final int newSize = (int) MathHelper.clamp(
			(frameTimeBuffer.maxSize() * (double) target / duration),
			FRAME_TIME_BUFFER_SIZE_MIN,
			FRAME_TIME_BUFFER_SIZE_MAX);
		
		if (Math.abs(newSize - frameTimeBuffer.maxSize()) > FRAME_TIME_BUFFER_RESIZE_THRESHOLD) {
			frameTimeBuffer.resize(newSize);
		}
	}
	
	private static int getFrameTimeFPS(long frameTime) {
		return (int) (1E9D / frameTime);
	}
	
	private static List<Line> getLines() {
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
						if (DEBUG)
					        result.add("%sSIZE: %d".formatted(leftPad, frameTimeBuffer.maxSize()));
						result.add("");
					} else {
						result.add("%sMIN: ???".formatted(leftPad));
						result.add("%sMAX: ???".formatted(leftPad));
						result.add("%sAVG: ???".formatted(leftPad));
						if (DEBUG)
					        result.add("%sSIZE: %d".formatted(leftPad, frameTimeBuffer.maxSize()));
						result.add("");
					}
				}
			}
		}
		
		if (Nolijium.config.hudShowCPU) {
			var cpuUsage = OS_BEAN.getProcessCpuLoad();
			if (cpuUsage == -1)
				cpuUsage = OS_BEAN.getCpuLoad();
			
			result.add("CPU: %2.2f%%".formatted(cpuUsage * 100D));
		}
		
		if (Nolijium.config.hudShowMemory) {
			final long maxMemory = Runtime.getRuntime().maxMemory();
			final long totalMemory = Runtime.getRuntime().totalMemory();
			final long freeMemory = Runtime.getRuntime().freeMemory();
			final long usedMemory = totalMemory - freeMemory;
			
			result.add("RAM: %2.2f%%  %d/%dMiB"
				.formatted(
					usedMemory * 100D / maxMemory, 
					usedMemory / (1024 * 1024), 
					maxMemory / (1024 * 1024)));
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
			.map(x -> new Line(x, 0, Minecraft.getInstance().font.width(x)))
			.toList();
	}
	
	private static void onFrame(@NotNull Window window) {
		screenWidth = window.getScreenWidth();
		screenHeight = window.getScreenHeight();
		
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
	}
	
	private static void update(int lineHeight) {
		lastUpdateTimestamp = System.nanoTime();
		
		lines = getLines();
		
		background = Nolijium.config.hudBackground;
		
		if (Nolijium.config.hudAlignmentX == Alignment.X.RIGHT) {
			lines.parallelStream().forEach(line -> 
				line.posX = screenWidth - line.width - Nolijium.config.hudMarginX);
		} else {
			lines.parallelStream().forEach(line ->
				line.posX = Nolijium.config.hudMarginX);
		}
		
		if (Nolijium.config.hudAlignmentY == Alignment.Y.BOTTOM) {
			final int height = lines.size() * lineHeight;
			
			posY = screenHeight - height - Nolijium.config.hudMarginY + 2;
		} else {
			posY = Nolijium.config.hudMarginY - 2;
		}
	}
	
	private static boolean isHidden() {
		return 
			!Nolijium.config.hudEnabled ||
			Minecraft.getInstance().options.hideGui ||
			(Nolijium.config.hudAlignmentY == Alignment.Y.TOP && Minecraft.getInstance().options.renderDebug);
	}

	@SubscribeEvent
	public static void render(RenderGameOverlayEvent.Post e) {
		if (isHidden())
			return;
		
		Font font = Minecraft.getInstance().font;
		int lineHeight = font.lineHeight + 3;
		
		onFrame(e.getWindow());
		
		if (Nolijium.config.hudRefreshRateTicks == 0 || 
			System.nanoTime() - lastUpdateTimestamp > Nolijium.config.hudRefreshRateTicks * 50E6)
			update(lineHeight);
		
		var linePosY = posY;
		for (var line : lines) {
			if (!line.text.isEmpty()) {
				if (background)
					GuiComponent.fill(e.getMatrixStack(),
						line.posX - 2, linePosY,
						line.posX + line.width + (Nolijium.config.hudShadow ? 2 : 1), linePosY + lineHeight,
						BACKGROUND_COLOUR);
				
				font.drawShadow(e.getMatrixStack(),
					line.text,
					line.posX, linePosY + 2,
					TEXT_COLOUR, Nolijium.config.hudShadow);
			}
			
			linePosY += lineHeight;
		}
	}
	
}
