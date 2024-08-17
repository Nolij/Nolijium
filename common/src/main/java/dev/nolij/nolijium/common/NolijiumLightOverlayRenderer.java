package dev.nolij.nolijium.common;

import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Objects;

public class NolijiumLightOverlayRenderer {
	private static final int BLOCK_RADIUS = 48;
	private static final int BLOCK_RADIUS_DIST_SQUARED = BLOCK_RADIUS * BLOCK_RADIUS;
	private static final int SECTION_RADIUS = Mth.roundToward(BLOCK_RADIUS, 16) >> 4;
	
	private static final int MINIMUM_LIGHT_LEVEL_FOR_INVISIBLE = 8;
	
	private static final int COLOR_BLOCK_LIGHT_0 = FastColor.ABGR32.color(255, 255, 0, 0);
	private static final int COLOR_BLOCK_LIGHT_1_TO_7 = FastColor.ABGR32.color(255, 255, 255, 0);
	
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
	
	private static final SectionLightOverlayData EMPTY_SECTION = new SectionLightOverlayData(new LongArrayList(), new ByteArrayList());
	
	private static final Long2ObjectOpenHashMap<SectionLightOverlayData> SECTION_CACHE = new Long2ObjectOpenHashMap<>();
	
	public static void invalidateChunk(Level level, ChunkPos pos) {
		int minSection = level.getMinSection(), maxSection = level.getMaxSection();
		for (int i = minSection; i <= maxSection; i++) {
			invalidateSection(SectionPos.asLong(pos.x, i, pos.z));
		}
	}
	
	public static void invalidateSection(int x, int y, int z) {
		invalidateSection(SectionPos.asLong(x, y, z));
	}
	
	public static void invalidateSection(long key) {
		if (Minecraft.getInstance().isSameThread()) {
			SECTION_CACHE.remove(key);
		} else {
			Minecraft.getInstance().submit(() -> SECTION_CACHE.remove(key));
		}
	}
	
	public static void render(Camera camera) {
		var level = Objects.requireNonNull(Minecraft.getInstance().level);
		double camPosX = camera.getPosition().x();
		double camPosY = camera.getPosition().y();
		double camPosZ = camera.getPosition().z();
		var buffers = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());
		int camMinSectionX = (camera.getBlockPosition().getX() - BLOCK_RADIUS) >> 4;
		int camMinSectionY = (camera.getBlockPosition().getY() - BLOCK_RADIUS) >> 4;
		int camMinSectionZ = (camera.getBlockPosition().getZ() - BLOCK_RADIUS) >> 4;
		int camMaxSectionX = (camera.getBlockPosition().getX() + BLOCK_RADIUS) >> 4;
		int camMaxSectionY = (camera.getBlockPosition().getY() + BLOCK_RADIUS) >> 4;
		int camMaxSectionZ = (camera.getBlockPosition().getZ() + BLOCK_RADIUS) >> 4;
		for(int y = camMinSectionY; y <= camMaxSectionY; y++) {
			for(int z = camMinSectionZ; z <= camMaxSectionZ; z++) {
				for(int x = camMinSectionX; x <= camMaxSectionX; x++) {
					long key = SectionPos.asLong(x, y, z);
					SectionLightOverlayData toRender = SECTION_CACHE.get(key);
					if (toRender == null) {
						toRender = computeLightLevelRendering(level, x, y, z);
						SECTION_CACHE.put(key, toRender);
					}
					if (!toRender.isEmpty()) {
						renderLightLevelSection(buffers, camPosX, camPosY, camPosZ, toRender);
					}
				}
			}
		}
	}
	
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
				positions.add(key);
				lightValues.add(lightLevel);
			}
		}
	}
	
	private static final float OVERLAY_NORMAL_MAGIC_VALUE = 1 / Mth.sqrt(2);
	
	private static void renderLightOverlay(VertexConsumer vConsumer, byte lightLevel, float xOff, float yOff, float zOff) {
		int color;
		if(lightLevel == 0) {
			color = COLOR_BLOCK_LIGHT_0;
		} else {
			color = COLOR_BLOCK_LIGHT_1_TO_7;
		}
		vConsumer.addVertex(xOff + 0, yOff + 0, zOff + 0).setColor(color).setNormal(OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		vConsumer.addVertex(xOff + 1, yOff + 0, zOff + 1).setColor(color).setNormal(OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		vConsumer.addVertex(xOff + 1, yOff + 0, zOff + 0).setColor(color).setNormal(-OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
		vConsumer.addVertex(xOff + 0, yOff + 0, zOff + 1).setColor(color).setNormal(-OVERLAY_NORMAL_MAGIC_VALUE, 0, OVERLAY_NORMAL_MAGIC_VALUE);
	}
	
	private static void renderLightLevelSection(VertexConsumer buffers, double x, double y, double z, SectionLightOverlayData toRender) {
		int size = toRender.positions.size();
		for(int i = 0; i < size; i++) {
			long key = toRender.positions.getLong(i);
			byte lightLevel = toRender.lightLevels.getByte(i);
			var blockPosX = BlockPos.getX(key);
			var blockPosY = BlockPos.getY(key);
			var blockPosZ = BlockPos.getZ(key);
			float dx = (float)(blockPosX - x);
			float dy = (float)(blockPosY - y);
			float dz = (float)(blockPosZ - z);
			if ((dx * dx + dy * dy + dz * dz) >= BLOCK_RADIUS_DIST_SQUARED) {
				continue;
			}
			renderLightOverlay(buffers, lightLevel, dx, dy + 1.01f, dz);
		}
	}
}
