package dev.nolij.nolijium.lexforge20;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.nolij.nolijium.common.ChromaShapeRenderer;
import dev.nolij.nolijium.common.INolijiumSubImplementation;
import dev.nolij.nolijium.common.NolijiumCommon;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.config.NolijiumConfigImpl;
import dev.nolij.nolijium.impl.util.MathHelper;
import dev.nolij.nolijium.impl.util.MethodHandleHelper;
import dev.nolij.nolijium.impl.util.RGBHelper;
import dev.nolij.nolijium.lexforge20.integration.embeddium.NolijiumEmbeddiumConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import static dev.nolij.nolijium.impl.NolijiumConstants.*;

@Mod(MOD_ID)
public class NolijiumLexForge20 implements INolijiumSubImplementation {
	
	public NolijiumLexForge20() {
		if (!FMLEnvironment.dist.isClient())
			return;
		
		new NolijiumCommon(this, FMLPaths.CONFIGDIR.get());
		
		ModLoadingContext.get().registerExtensionPoint(
			ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new Screen(Component.nullToEmpty(null)) {
				@Override
				public void tick() {
					NolijiumConfigImpl.openConfigFile();
					Minecraft.getInstance().setScreen(parent);
				}
			}));
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlays);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onAddToast);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onRenderTooltip);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::renderFog);
		MinecraftForge.EVENT_BUS.addListener(this::renderLevelStage);
		
		if (MethodHandleHelper.PUBLIC.getClassOrNull("org.embeddedt.embeddium.api.OptionGUIConstructionEvent") != null)
			new NolijiumEmbeddiumConfigScreen();
	}
	
	private void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("hud", new NolijiumHUDOverlay());
	}
	
	private void onAddToast(ToastAddEvent event) {
		if (event.isCanceled())
			return;
		
		if (Nolijium.config.hideAllToasts) {
			event.setCanceled(true);
			return;
		}
		
		final Toast toast = event.getToast();
		
		//noinspection IfCanBeSwitch
		if (toast instanceof AdvancementToast)
			event.setCanceled(Nolijium.config.hideAdvancementToasts);
		else if (toast instanceof RecipeToast)
			event.setCanceled(Nolijium.config.hideRecipeToasts);
		else if (toast instanceof SystemToast)
			event.setCanceled(Nolijium.config.hideSystemToasts);
		else if (toast instanceof TutorialToast)
			event.setCanceled(Nolijium.config.hideTutorialToasts);
	}
	
	private void onRenderTooltip(RenderTooltipEvent.Color event) {
		if (Nolijium.config.tooltipColourOverride) {
			event.setBorderStart(Nolijium.config.tooltipBorderStart);
			event.setBorderEnd(Nolijium.config.tooltipBorderEnd);
			event.setBackgroundStart(Nolijium.config.tooltipBackgroundStart);
			event.setBackgroundEnd(Nolijium.config.tooltipBackgroundEnd);
			
			return;
		}
		
		if (!Nolijium.config.enableChromaToolTips)
			return;
		
		final double timestamp = System.nanoTime() * 1E-9D;
		
		event.setBorderStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 0));
		event.setBorderEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, -2));
		event.setBackgroundStart(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, 0, 0.25D));
		event.setBackgroundEnd(RGBHelper.chroma(timestamp, Nolijium.config.chromaSpeed, -2, 0.25D));
	}
	
	private void renderFog(ViewportEvent.RenderFog event) {
		if (event.getType() != FogType.NONE)
			return;
		
		if (Nolijium.config.disableFog) {
			if (event.getMode() == FogRenderer.FogMode.FOG_SKY)
				return;
			
			event.setCanceled(true);
			
			event.setNearPlaneDistance(Float.MAX_VALUE);
			event.setFarPlaneDistance(Float.MAX_VALUE);
			return;
		}
		
		if (Nolijium.config.fogOverride != 0) {
			event.setCanceled(true);
			final float distance = Nolijium.config.fogOverride * 16;
			
			if (event.getMode() != FogRenderer.FogMode.FOG_SKY)
				event.setNearPlaneDistance(distance - (float) MathHelper.clamp(distance * 0.1D, 4D, 64D));
			event.setFarPlaneDistance(distance);
		} else if (Nolijium.config.fogMultiplier != 1F) {
			event.setCanceled(true);
			
			event.scaleNearPlaneDistance(Nolijium.config.fogMultiplier);
			event.scaleFarPlaneDistance(Nolijium.config.fogMultiplier);
		}
		
		if (event.getMode() != FogRenderer.FogMode.FOG_SKY &&
			Nolijium.config.fogStartMultiplier != 1F) {
			event.setCanceled(true);
			
			event.scaleNearPlaneDistance(Nolijium.config.fogStartMultiplier);
		}
	}
	
	private void renderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
			if (NolijiumCommon.focusedBlockPosition == null || NolijiumCommon.focusedBlockShape == null)
				return;
			
			final double timestamp = System.nanoTime() * 1E-9D;
			
			ChromaShapeRenderer.render(
				event.getPoseStack(),
				NolijiumCommon.focusedBlockShape,
				NolijiumCommon.focusedBlockPosition.x,
				NolijiumCommon.focusedBlockPosition.y,
				NolijiumCommon.focusedBlockPosition.z,
				(float) RGBHelper.chromaRed(timestamp, Nolijium.config.chromaSpeed, 0),
				(float) RGBHelper.chromaGreen(timestamp, Nolijium.config.chromaSpeed, 0),
				(float) RGBHelper.chromaBlue(timestamp, Nolijium.config.chromaSpeed, 0),
				Nolijium.config.chromaBlockShapeOverlay);
			
			NolijiumCommon.focusedBlockPosition = null;
			NolijiumCommon.focusedBlockShape = null;
		}
	}
	
	@Override
	public String getClickActionName(ClickEvent.Action action) {
		return action.getName();
	}
	
	@Override
	public ComponentContents getEmptyComponentContents() {
		return ComponentContents.EMPTY;
	}
	
	@Override
	public boolean supportsLightLevelOverlay() {
		return false;
	}
	
	@Override
	public void addLineVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, int color, float nx, float ny, float nz) {
		consumer.vertex(pose.pose(), x, y, z).color(color).normal(pose.normal(), nx, ny, nz).endVertex();
	}
	
}
