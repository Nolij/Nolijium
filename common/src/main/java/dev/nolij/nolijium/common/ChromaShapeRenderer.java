package dev.nolij.nolijium.common;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class ChromaShapeRenderer {
	
	private static final INolijiumSubImplementation NOLIJIUM_IMPL = Objects.requireNonNull(NolijiumCommon.getImplementation());
	
	private static final RenderType CHROMA_OVERLAY = RenderType.create(
		"nolijium_chroma_overlay", 
		DefaultVertexFormat.POSITION_COLOR, 
		VertexFormat.Mode.TRIANGLE_STRIP, 
		1536, 
		false, 
		true, 
		RenderType.CompositeState.builder()
			.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
			.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
			.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
			.setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
			.setCullState(RenderStateShard.CULL)
			.createCompositeState(false));
	
	private static void renderInnerOverlay(PoseStack poseStack, MultiBufferSource bufferSource, VoxelShape shape, float red, float green, float blue, float alpha) {
		VertexConsumer boxConsumer = bufferSource.getBuffer(CHROMA_OVERLAY);
		shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> 
			LevelRenderer.addChainedFilledBoxVertices(poseStack, boxConsumer, x1, y1, z1, x2, y2, z2, red, green, blue, alpha));
	}
	
	private static void renderOuterOverlay(PoseStack poseStack, MultiBufferSource bufferSource, VoxelShape shape, float red, float green, float blue, float alpha) {
		var pose = poseStack.last();
		var lineConsumer = bufferSource.getBuffer(RenderType.lines());
		int color = FastColor.ARGB32.color((int) (alpha * 255F), (int) (red * 255F), (int) (green * 255F), (int) (blue * 255F));
		shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
			float nx = (float) (x2 - x1);
			float ny = (float) (y2 - y1);
			float nz = (float) (z2 - z1);
			float norm = (nx * nx + ny * ny + nz * nz);
			nx /= norm;
			ny /= norm;
			nz /= norm;
			NOLIJIUM_IMPL.addLineVertex(pose, lineConsumer, (float) x1, (float) y1, (float) z1, color, nx, ny, nz);
			NOLIJIUM_IMPL.addLineVertex(pose, lineConsumer, (float) x2, (float) y2, (float) z2, color, nx, ny, nz);
		});
	}
	
	public static void render(PoseStack poseStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		
		var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		renderInnerOverlay(poseStack, bufferSource, shape, red, green, blue, alpha);
		if (Nolijium.config.enableChromaBlockOutlines)
			renderOuterOverlay(poseStack, bufferSource, shape, red, green, blue, 1F);
		
		poseStack.popPose();
	}
	
}
