package dev.nolij.nolijium.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

public class ChromaShapeRenderer {
	private static void renderInnerOverlay(PoseStack poseStack, MultiBufferSource bufferSource, VoxelShape shape, float red, float green, float blue, float alpha) {
		VertexConsumer boxConsumer = bufferSource.getBuffer(RenderType.debugFilledBox());
		shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
			LevelRenderer.addChainedFilledBoxVertices(poseStack, boxConsumer, x1 - 0.5, y1 - 0.5, z1 - 0.5, x2 - 0.5, y2 - 0.5, z2 - 0.5, red, green, blue, alpha);
		});
	}
	
	private static void renderOuterOverlay(PoseStack poseStack, MultiBufferSource bufferSource, VoxelShape shape, float red, float green, float blue, float alpha) {
		var pose = poseStack.last();
		var lineConsumer = bufferSource.getBuffer(RenderType.lines());
		var subImplementation = Objects.requireNonNull(NolijiumCommon.getImplementation());
		int color = FastColor.ARGB32.color((int) (alpha * 255f), (int) (red * 255f), (int) (green * 255f), (int) (blue * 255f));
		shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
			float nx = (float) (x2 - x1), ny = (float) (y2 - y1), nz = (float) (z2 - z1);
			float norm = (nx * nx + ny * ny + nz * nz);
			nx /= norm;
			ny /= norm;
			nz /= norm;
			subImplementation.addLineVertex(pose, lineConsumer, (float) (x1 - 0.5), (float) (y1 - 0.5), (float) (z1 - 0.5), color, nx, ny, nz);
			subImplementation.addLineVertex(pose, lineConsumer, (float) (x2 - 0.5), (float) (y2 - 0.5), (float) (z2 - 0.5), color, nx, ny, nz);
		});
	}
	
	public static void render(PoseStack poseStack, VoxelShape shape, double x, double y, double z, float red, float green, float blue, float alpha) {
		poseStack.pushPose();
		poseStack.translate(x + 0.5, y + 0.5, z + 0.5);
		float padding = 1.0f;
		if (padding != 1.0f) {
			poseStack.scale(padding, padding, padding);
		}
		var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		renderInnerOverlay(poseStack, bufferSource, shape, red, green, blue, alpha * 0.75f);
		if (Nolijium.config.enableChromaBlockOutlines) {
			renderOuterOverlay(poseStack, bufferSource, shape, red, green, blue, alpha);
		}
		poseStack.popPose();
	}
}
