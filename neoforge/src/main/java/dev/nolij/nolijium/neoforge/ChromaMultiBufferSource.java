package dev.nolij.nolijium.neoforge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class ChromaMultiBufferSource implements MultiBufferSource {
	private final MultiBufferSource delegate;
	private final float timestamp;
	
	public ChromaMultiBufferSource(MultiBufferSource delegate) {
		this.delegate = delegate;
		this.timestamp = System.nanoTime() * 1E-9D;
	}
	
	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return new VertexConsumerWrapper(this.delegate.getBuffer(renderType)) {
			@Override
			public VertexConsumer setColor(int r, int g, int b, int a) {
				float timestamp = ChromaMultiBufferSource.this.timestamp;
				double speed = Nolijium.config.chromaSpeed;
				super.setColor(
					(int)(ColourUtil.chromaRed(timestamp,speed, 0) * 255),
					(int)(ColourUtil.chromaGreen(timestamp,speed, 0) * 255),
					(int)(ColourUtil.chromaBlue(timestamp,speed, 0) * 255),
					a
				);
				return this;
			}
		};
	}
}
