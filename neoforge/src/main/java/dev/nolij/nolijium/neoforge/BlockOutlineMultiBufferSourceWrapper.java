package dev.nolij.nolijium.neoforge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.libnolij.util.ColourUtil;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.zumegradle.proguard.ProGuardKeep;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.jetbrains.annotations.NotNull;

public class BlockOutlineMultiBufferSourceWrapper implements MultiBufferSource {
	
	private final MultiBufferSource delegate;
	private final double timestamp;
	
	public BlockOutlineMultiBufferSourceWrapper(MultiBufferSource delegate) {
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
					final var newColour = Nolijium.transformBlockOutlineColour(timestamp, ColourUtil.getARGB(alpha, red, green, blue));
					red = ColourUtil.getRedI(newColour);
					green = ColourUtil.getGreenI(newColour);
					blue = ColourUtil.getBlueI(newColour);
					alpha = ColourUtil.getAlphaI(newColour);
				}
				
				super.setColor(red, green, blue, alpha);
				
				return this;
			}
		};
	}
	
}
