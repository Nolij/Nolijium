package dev.nolij.nolijium.neoforge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.NotNull;

public class ChromaMultiBufferSource implements MultiBufferSource {
	
	private final MultiBufferSource delegate;
	private final double timestamp;
	
	public ChromaMultiBufferSource(MultiBufferSource delegate) {
		this.delegate = delegate;
		this.timestamp = System.nanoTime() * 1E-9D;
	}
	
	@ProGuardKeep
	@Override
	public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
		return new VertexConsumerWrapper(this.delegate.getBuffer(renderType)) {
			@ProGuardKeep
			@Override
			public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
				if (red == 0 && green == 0 && blue == 0 && alpha == 102) {
					final double timestamp = ChromaMultiBufferSource.this.timestamp;
					final double speed = Nolijium.config.chromaSpeed;
					
					red = (int) (ColourUtil.chromaRed(timestamp, speed, 0) * 255);
					green = (int) (ColourUtil.chromaGreen(timestamp, speed, 0) * 255);
					blue = (int) (ColourUtil.chromaBlue(timestamp, speed, 0) * 255);
				}
				
				super.setColor(red, green, blue, alpha);
				
				return this;
			}
		};
	}
	
}
