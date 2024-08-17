package dev.nolij.nolijium.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.nolij.nolijium.impl.Nolijium;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.Objects;

/**
 * The core of the light overlay renderer.
 */
public class NolijiumLightOverlayRenderer {
	/**
	 * The radius of blocks around the camera that a light overlay can be shown on.
	 */
	private static final int BLOCK_RADIUS = 48;
	/**
	 * Precomputed square of the distance at which light overlays are shown, used in the vertex generation logic.
	 */
	private static final int BLOCK_RADIUS_DIST_SQUARED = BLOCK_RADIUS * BLOCK_RADIUS;
	
	/**
	 * The minimum light level at which overlays are not shown.
	 */
	private static final int MINIMUM_LIGHT_LEVEL_FOR_INVISIBLE = 8;
	
	/**
	 * The overlay color for light level 0.
	 */
	private static final int COLOR_BLOCK_LIGHT_0 = FastColor.ABGR32.color(255, 255, 0, 0);
	/**
	 * The overlay color for light levels 1 through 7.
	 */
	private static final int COLOR_BLOCK_LIGHT_1_TO_7 = FastColor.ABGR32.color(255, 255, 255, 0);
	
	/**
	 * The current marker version, incremented each time a section is invalidated. This is used to regenerate the
	 * vertex buffer on the GPU when the overlays need to be re-rendered.
	 */
	private static int currentUpdateVersion = -1;
	/**
	 * The last block position the camera was inside. This is used to regenerate the vertex buffer on the GPU when
	 * the camera moves to a new block.
	 */
	private static BlockPos lastCameraPosition = BlockPos.ZERO;
	
	/**
	 * A small wrapper class holding the current vertex buffer and the marker version it corresponds to.
	 */
	private static final class RenderedLightOverlays implements AutoCloseable {
		public final VertexBuffer buffer;
		public final int updateVersion;
		
		private RenderedLightOverlays(VertexBuffer buffer, int updateVersion) {
			this.buffer = buffer;
			this.updateVersion = updateVersion;
		}
		
		@Override
		public void close() {
			if (buffer != null) {
				buffer.close();
			}
		}
	}
	
	/**
	 * The current GPU-side light overlay buffer.
	 */
	private static RenderedLightOverlays currentLightOverlayBuffer = null;
	
	/**
	 * A wrapper class used to store the list of positions and light levels at which overlays should be rendered
	 * within a given chunk section.
	 */
	private static final class SectionLightOverlayData {
		private final LongArrayList positions;
		private final ByteArrayList lightLevels;
		
		private SectionLightOverlayData(LongArrayList positions, ByteArrayList lightLevels) {
			this.positions = positions;
			this.lightLevels = lightLevels;
		}
		
		public boolean isEmpty() {
			return positions.isEmpty();
		}
	}
	
	/**
	 * A cached singleton empty section object used for sections that don't need any light overlays rendered.
	 */
	private static final SectionLightOverlayData EMPTY_SECTION = new SectionLightOverlayData(new LongArrayList(), new ByteArrayList());
	
	/**
	 * The cache of section position -> overlay data for that section.
	 */
	private static final Long2ObjectOpenHashMap<SectionLightOverlayData> SECTION_CACHE = new Long2ObjectOpenHashMap<>();
	
	/**
	 * Invalidate all sections within the given chunk. 
	 * @param level used to find the height of the chunk
	 * @param pos the chunk position to invalidate
	 */
	public static void invalidateChunk(LevelHeightAccessor level, ChunkPos pos) {
		if (!Nolijium.config.enableLightLevelOverlay) {
			return;
		}
		
		int minSection = level.getMinSection(), maxSection = level.getMaxSection();
		for (int i = minSection; i <= maxSection; i++) {
			invalidateSection(SectionPos.asLong(pos.x, i, pos.z));
		}
	}
	
	/**
	 * Invalidate the section at the given coordinates.
	 */
	public static void invalidateSection(int x, int y, int z) {
		invalidateSection(SectionPos.asLong(x, y, z));
	}
	
	private static void invalidateSection(long key) {
		if (!Nolijium.config.enableLightLevelOverlay) {
			return;
		}
		
		if (!Minecraft.getInstance().isSameThread()) {
			Minecraft.getInstance().execute(() -> invalidateSection(key));
			return;
		}
		
		SECTION_CACHE.remove(key);
		currentUpdateVersion++;
	}
	
	/**
	 * {@return true if the GPU-side vertex buffer should be regenerated, either due to camera movement or section data
	 * invalidation}
	 */
	private static boolean bufferNeedsUpdate(Camera camera) {
		return currentLightOverlayBuffer == null
			|| !lastCameraPosition.equals(camera.getBlockPosition())
			|| currentLightOverlayBuffer.updateVersion != currentUpdateVersion;
	}
	
	/**
	 * Renders all light overlays for the current world.
	 * @param camera the player camera
	 * @param modelViewMatrix the current model view matrix
	 */
	public static void render(Camera camera, Matrix4f modelViewMatrix) {
		// Bail immediately if light overlay is not enabled
		if (!Nolijium.config.enableLightLevelOverlay) {
			// Free the GPU-side buffer, we don't need it
			if(currentLightOverlayBuffer != null) {
				currentLightOverlayBuffer.close();
				currentLightOverlayBuffer = null;
			}
			// Clear the section cache to save memory and pick up on any changes to section data later 
			SECTION_CACHE.clear();
			return;
		}
		
		var level = Objects.requireNonNull(Minecraft.getInstance().level);
		
		// Check if we can reuse the previously generated GPU vertex buffer
		if (bufferNeedsUpdate(camera)) {
			var bufferBuilder = Tesselator.getInstance().begin(RenderType.lines().mode(), RenderType.lines().format());
			int camPosX = camera.getBlockPosition().getX();
			int camPosY = camera.getBlockPosition().getY();
			int camPosZ = camera.getBlockPosition().getZ();
			
			// Loop through all sections needed to capture blocks at the specified radius
			int camMinSectionX = (camPosX - BLOCK_RADIUS) >> 4;
			int camMinSectionY = (camPosY - BLOCK_RADIUS) >> 4;
			int camMinSectionZ = (camPosZ - BLOCK_RADIUS) >> 4;
			int camMaxSectionX = (camPosX + BLOCK_RADIUS) >> 4;
			int camMaxSectionY = (camPosY + BLOCK_RADIUS) >> 4;
			int camMaxSectionZ = (camPosZ + BLOCK_RADIUS) >> 4;
			for(int y = camMinSectionY; y <= camMaxSectionY; y++) {
				for(int z = camMinSectionZ; z <= camMaxSectionZ; z++) {
					for(int x = camMinSectionX; x <= camMaxSectionX; x++) {
						long key = SectionPos.asLong(x, y, z);
						SectionLightOverlayData toRender = SECTION_CACHE.get(key);
						// Check if we have cached light overlay data for this section; if not, generate it
						if (toRender == null) {
							toRender = computeLightLevelRendering(level, x, y, z);
							SECTION_CACHE.put(key, toRender);
						}
						// Emit vertices into the buffer builder if the section has at least one light overlay
						if (!toRender.isEmpty()) {
							renderLightLevelSection(bufferBuilder, camPosX, camPosY, camPosZ, toRender);
						}
					}
				}
			}
			
			// Store the necessary cache keys & upload the completed buffer to the GPU if it's not empty
			lastCameraPosition = camera.getBlockPosition().immutable();
			
			if(currentLightOverlayBuffer != null) {
				currentLightOverlayBuffer.close();
			}
			
			MeshData data = bufferBuilder.build();
			if (data == null) {
				currentLightOverlayBuffer = new RenderedLightOverlays(null, currentUpdateVersion);
			} else {
				VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
				currentLightOverlayBuffer = new RenderedLightOverlays(buffer, currentUpdateVersion);
				buffer.bind();
				buffer.upload(data);
				VertexBuffer.unbind();
			}
		}
		
		if (currentLightOverlayBuffer != null && currentLightOverlayBuffer.buffer != null) {
			// Render the GPU-side vertex buffer at the appropriate position
			RenderType.lines().setupRenderState();
			Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
			matrix4fstack.pushMatrix();
			matrix4fstack.mul(modelViewMatrix);
			var camPos = camera.getPosition();
			matrix4fstack.translate((float)(lastCameraPosition.getX() - camPos.x), (float)(lastCameraPosition.getY() - camPos.y), (float)(lastCameraPosition.getZ() - camPos.z));
			RenderSystem.applyModelViewMatrix();
			currentLightOverlayBuffer.buffer.bind();
			currentLightOverlayBuffer.buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
			VertexBuffer.unbind();
			RenderType.lines().clearRenderState();
			matrix4fstack.popMatrix();
			RenderSystem.applyModelViewMatrix();
		}
	}
	
	/**
	 * Compute the light overlay data for the given chunk section.
	 */
	private static SectionLightOverlayData computeLightLevelRendering(LevelReader level, int sX, int sY, int sZ) {
		var chunk = level.getChunk(sX, sZ);
		
		int sectionIndex = chunk.getSectionIndexFromSectionY(sY);
		
		if (sectionIndex < 0 || sectionIndex >= chunk.getSectionsCount()) {
			// Outside of world
			return EMPTY_SECTION;
		}
		
		LevelChunkSection section = chunk.getSection(sectionIndex);
		
		if (section.hasOnlyAir()) {
			// None of the blocks in this section will render a light overlay on top of them
			return EMPTY_SECTION;
		}
		
		// Loop through all blocks in the section, checking if any are spawnable (sturdy top face) and the block above
		// has an empty collision shape
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int baseX = sX << 4, baseY = sY << 4, baseZ = sZ << 4;
		LongArrayList positions = new LongArrayList();
		ByteArrayList lightValues = new ByteArrayList();
		
		for(int y = 0; y < 16; y++) {
			for(int z = 0; z < 16; z++) {
				for(int x = 0; x < 16; x++) {
					// Read the state from the section directly to avoid going through the indirection of the chunk
					BlockState state = section.getBlockState(x, y, z);
					if (!state.isAir()) {
						cursor.set(baseX + x, baseY + y, baseZ + z);
						if (state.isFaceSturdy(level, cursor, Direction.UP)) {
							// This block could have a light overlay on it, do more checks and add one if needed
							considerBlockForLightOverlay(cursor, x, y, z, level, chunk, section, positions, lightValues);
						}
					}
				}
			}
		}
		
		// Return the section data, and use the singleton object if there is no data
		if(!positions.isEmpty()) {
			positions.trim();
			lightValues.trim();
			return new SectionLightOverlayData(positions, lightValues);
		} else {
			return EMPTY_SECTION;
		}
	}
	
	private static void considerBlockForLightOverlay(BlockPos.MutableBlockPos cursor, int x, int y, int z, LevelReader level, ChunkAccess chunk, LevelChunkSection section, LongArrayList positions, ByteArrayList lightValues) {
		long key = cursor.asLong();
		cursor.setY(cursor.getY() + 1);
		// Try to read the state from the section directly unless this is too high up
		var shapeAbove = (y == 15 ? chunk.getBlockState(cursor) : section.getBlockState(x, y + 1, z)).getCollisionShape(level, cursor);
		// Check if block above has empty collision shape
		if(shapeAbove == Shapes.empty() || (shapeAbove != Shapes.block() && shapeAbove.isEmpty())) {
			byte lightLevel = (byte)Math.max(Math.min(level.getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(cursor), 15), 0);
			if(lightLevel < MINIMUM_LIGHT_LEVEL_FOR_INVISIBLE) {
				// Mark this position as having an overlay
				positions.add(key);
				lightValues.add(lightLevel);
			}
		}
	}
	
	private static final float OVERLAY_NORMAL_MAGIC_VALUE = 1 / Mth.sqrt(2);
	
	/**
	 * Render an X shape at the given position.
	 */
	private static void renderLightOverlay(BufferBuilder vConsumer, byte lightLevel, float xOff, float yOff, float zOff) {
		int color;
		if(lightLevel == 0) {
			color = COLOR_BLOCK_LIGHT_0;
		} else {
			color = COLOR_BLOCK_LIGHT_1_TO_7;
		}
		vConsumer.addVertex(xOff + 0, yOff + 0, zOff + 0);
		vConsumer.setColor(color);
		vConsumer.setNormal(OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		
		vConsumer.addVertex(xOff + 1, yOff + 0, zOff + 1);
		vConsumer.setColor(color);
		vConsumer.setNormal(OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		
		vConsumer.addVertex(xOff + 1, yOff + 0, zOff + 0);
		vConsumer.setColor(color);
		vConsumer.setNormal(-OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		
		vConsumer.addVertex(xOff + 0, yOff + 0, zOff + 1);
		vConsumer.setColor(color);
		vConsumer.setNormal(-OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
	}
	
	/**
	 * Loop through the overlay data and render the overlays into the given buffer builder.
	 */
	private static void renderLightLevelSection(BufferBuilder buffers, int x, int y, int z, SectionLightOverlayData toRender) {
		int size = toRender.positions.size();
		for(int i = 0; i < size; i++) {
			long key = toRender.positions.getLong(i);
			byte lightLevel = toRender.lightLevels.getByte(i);
			var blockPosX = BlockPos.getX(key);
			var blockPosY = BlockPos.getY(key);
			var blockPosZ = BlockPos.getZ(key);
			int dx = blockPosX - x;
			int dy = blockPosY - y;
			int dz = blockPosZ - z;
			if ((dx * dx + dy * dy + dz * dz) >= BLOCK_RADIUS_DIST_SQUARED) {
				// Don't show overlays outside the block radius
				continue;
			}
			renderLightOverlay(buffers, lightLevel, dx, dy + 1.01f, dz);
		}
	}
}
