package dev.nolij.nolijium.impl.config;

import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.util.DetailLevel;
import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonField;
import dev.nolij.zumegradle.proguard.ProGuardKeep;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Consumer;

public class NolijiumConfigImpl implements Cloneable {
	
	//region Options
	@ZsonField(comment = """
		Show an overlay on blocks if the block light level above them is below 8.
		Exclusive to NeoForge 21+.
		DEFAULT: `false`""")
	public boolean enableLightLevelOverlay = false;
	
	@ZsonField(comment = """
		Changes the number of messages kept in chat history (100 in vanilla).
		DEFAULT: `100`""")
	public int maxChatHistory = 100;
	
	@ZsonField(comment = """
		If enabled, the chat history will not be cleared when you leave a server, and instead only be cleared when Minecraft is closed.
		DEFAULT: `false`""")
	public boolean keepChatHistory = false;
	
	@ZsonField(comment = """
		If enabled, the server will be ignored if it instructs the client to close the chat bar (for example, if the player is teleported).
		DEFAULT: `false`""")
	public boolean keepChatBarOpen = false;
	
	@ProGuardKeep.Enum
	public enum RememberChatBarContents {
		UNTIL_SENT,
		UNTIL_USER_CLOSED,
		NEVER,
	}
	
	@ZsonField(comment = """
		Controls what happens to the unsent contents of the chat bar if it is closed by clicking escape or by the server.
		OPTIONS:
			`UNTIL_SENT`: Will always remember the unsent message/command unless the message was sent.
			`UNTIL_USER_CLOSED`: Will only remember the unsent message/command if the chat bar was closed by the server.
			`NEVER`: Will never remember the unsent message/command (same as vanilla).
		DEFAULT: `UNTIL_ESCAPED`""")
	public RememberChatBarContents rememberChatBarContents = RememberChatBarContents.UNTIL_USER_CLOSED;
	
	@ZsonField(comment = """
		If enabled, useful information will be shown in the tooltips of links and clickable text in chat and other UIs.
		DEFAULT: `false`""")
	public boolean enableToolTipInfo = false;
	
	@ZsonField(comment = """
		Overrides the number of stars rendered.
		DEFAULT: `1500`""")
	public int starCount = 1500;
	
	@ZsonField(comment = """
		Overrides the scale of stars.
		DEFAULT: `0.1`""")
	public float starScale = 0.1F;
	
	@ZsonField(comment = """
		Overrides the brightness of stars.
		DEFAULT: `0.5`""")
	public float starBrightness = 0.5F;
	
	@ZsonField(comment = """
		If enabled, terrain fog will not be rendered.
		DEFAULT: `false`""")
	public boolean disableFog = false;
	
	@ZsonField(comment = """
		If set, this option explicitly overrides the fog distance (in chunks).
		DEFAULT: `0`""")
	public int fogOverride = 0;
	
	@ZsonField(comment = """
		If set, fog distance is multiplied by this option.
		DEFAULT: `1.0`""")
	public float fogMultiplier = 1F;
	
	@ZsonField(comment = """
		If set, fog start distance is multiplied by this option.
		DEFAULT: `1.0`""")
	public float fogStartMultiplier = 1F;
	
	@ZsonField(comment = """
		If enabled, light level 0 will render as pitch black.
		DEFAULT: `false`""")
	public boolean enablePureDarkness = false;
	
	@ZsonField(comment = """
		Determines the minimum sky light level (0.2 in vanilla).
		DEFAULT: `0.2`""")
	public float minimumSkyLightLevel = 0.2F;
	
	@ZsonField(comment = """
		If enabled, a HUD will be drawn on the screen showing useful performance statistics.
		DEFAULT: `false`""")
	public boolean hudEnabled = false;
	
	@ZsonField(comment = """
		The horizontal alignment of the HUD.
		OPTIONS: `LEFT`, `RIGHT`
		DEFAULT: `LEFT`""")
	public Alignment.X hudAlignmentX = Alignment.X.LEFT;
	
	@ZsonField(comment = """
		The vertical alignment of the HUD.
		OPTIONS: `TOP`, `BOTTOM`
		DEFAULT: `TOP`""")
	public Alignment.Y hudAlignmentY = Alignment.Y.TOP;
	
	@ZsonField(comment = """
		The HUD will be offset this many pixels horizontally from the screen edge.
		DEFAULT: `5`""")
	public int hudMarginX = 5;
	
	@ZsonField(comment = """
		The HUD will be offset this many pixels vertically from the screen edge.
		DEFAULT: `5`""")
	public int hudMarginY = 5;
	
	@ZsonField(comment = """
		If enabled, a background will be drawn behind HUD text.
		DEFAULT: `true`""")
	public boolean hudBackground = true;
	
	@ZsonField(comment = """
		If enabled, HUD text will be drawn with a shadow.
		DEFAULT: `true`""")
	public boolean hudShadow = true;
	
	@ZsonField(comment = """
		The HUD refresh rate (in ticks). `0` will refresh it every frame.
		Lower values may reduce performance.
		DEFAULT: `1`""")
	public int hudRefreshRateTicks = 1;
	
	@ZsonField(comment = """
		Determines much information about FPS should be displayed in the HUD.
		OPTIONS:
			`NONE`: No FPS information will be displayed in the HUD.
			`SIMPLE`: HUD will display a simple, accurate FPS value.
			`EXTENDED`: HUD will display FPS, as well as a MIN (0.1% low), MAX (high), and AVG (average) FPS value.
		DEFAULT: `SIMPLE`""")
	public DetailLevel hudShowFPS = DetailLevel.SIMPLE;
	
	@ZsonField(comment = """
		The amount of frame time history to keep (in seconds).
		Larger values will result in smoother average values, at the cost of higher memory usage if average FPS is very high.
		Only applicable if `hudShowFPS` is set to `EXTENDED`.
		DEFAULT: `10.0`""")
	public double hudFrameTimeBufferSize = 10D;
	
	@ZsonField(comment = """
		If enabled, the HUD will show CPU usage.
		DEFAULT: `false`""")
	public boolean hudShowCPU = false;
	
	@ZsonField(comment = """
		If enabled, the HUD will show Memory usage.
		DEFAULT: `false`""")
	public boolean hudShowMemory = false;
	
	@ZsonField(comment = """
		If enabled, the HUD will show the player's coordinates.
		DEFAULT: `false`""")
	public boolean hudShowCoordinates = false;
	
	@ZsonField(comment = """
		Reverts the fix for MC-26678.
		DEFAULT: `false`""")
	public boolean revertDamageCameraTilt = false;
	
	@ZsonField(comment = """
		Restores potions effects to their original form.
		Re-joining the server in multiplayer or closing and opening the world in singleplayer may be necessary to affect particles.
		DEFAULT: `false`""")
	public boolean revertPotions = false;
	
	@ZsonField(comment = """
		Removes transparency from block outlines.
		DEFAULT: `false`""")
	public boolean enableOpaqueBlockOutlines = false;
	
	@ZsonField(comment = """
		Disables block light flickering.
		DEFAULT: `false`""")
	public boolean disableBlockLightFlicker = false;
	
	@ZsonField(comment = """
		Disables animations for water, lava, fire, etc.
		DEFAULT: `false`""")
	public boolean disableTextureAnimations = false;
	
	@ZsonField(comment = """
		Disables sky rendering.
		DEFAULT: `false`""")
	public boolean disableSky = false;
	
	@ZsonField(comment = """
		Disables weather rendering.
		DEFAULT: `false`""")
	public boolean disableWeatherRendering = false;
	
	@ZsonField(comment = """
		Disables weather entirely.
		DEFAULT: `false`""")
	public boolean disableWeatherTicking = false;
	
	@ZsonField(comment = """
		Disables font shadows.
		DEFAULT: `false`""")
	public boolean disableFontShadows = false;
	
	@ZsonField(comment = """
		Disables all toast messages.
		DEFAULT: `false`""")
	public boolean hideAllToasts = false;
	
	@ZsonField(comment = """
		Disables toast messages for receiving Advancements.
		DEFAULT: `false`""")
	public boolean hideAdvancementToasts = false;
	
	@ZsonField(comment = """
		Disables toast messages for unlocking Recipes.
		DEFAULT: `false`""")
	public boolean hideRecipeToasts = false;
	
	@ZsonField(comment = """
		Disables toasts for System messages.
		DEFAULT: `false`""")
	public boolean hideSystemToasts = false;
	
	@ZsonField(comment = """
		Disables the Tutorial.
		DEFAULT: `false`""")
	public boolean hideTutorialToasts = false;
	
	@ZsonField(comment = """
		Prevents all particles from rendering.
		DEFAULT: `false`""")
	public boolean hideParticles = false;
	
	@ZsonField(comment = """
		Prevents specific particles from rendering based on their ID. For example, `minecraft:block`.
		Also supports modded particles.
		DEFAULT: `[ ]`""")
	public ArrayList<String> hideParticlesByID = new ArrayList<>();
	
	@ZsonField(comment = """
		Higher values cycle faster. Lower values cycle slower.
		DEFAULT: `0.5`""")
	public double chromaSpeed = 0.5D;
	
	@ZsonField(comment = """
		Enable Chroma on block outlines.
		DEFAULT: `false`""")
	public boolean enableChromaBlockOutlines = false;
	
	@ZsonField(comment = """
		If set, a chroma overlay will be drawn on block surfaces with the specified value used for transparency.
		May look buggy on some blocks when `Fabulous!` rendering is enabled.
		Set to `0` to disable.
		DEFAULT: `0`""")
	public float chromaBlockShapeOverlay = 0F;
	
	@ZsonField(comment = """
		Enable Chroma on tooltip outlines.
		DEFAULT: `false`""")
	public boolean enableChromaToolTips = false;
	
	@ZsonField(comment = """
		Enable Chroma on HUD text.
		DEFAULT: `false`""")
	public boolean enableChromaHUD = false;
	
	@ZsonField(comment = """
		Removes darkness and all client-side lighting calculations, resulting in a decent performance boost on some systems.
		May cause issues with shaders, dynamic lighting, and light level overlays (the light level overlay in Nolijium has some mitigations for this, but still expect some weird behaviour).
		DEFAULT: `false`""")
	public boolean enableGamma = false;
	
	@ZsonField(comment = """
		Intended for advanced users and developers only. Colour format is 0xAARRGGBB.
		DEFAULT: `false`""")
	public boolean tooltipColourOverride = false;
	
	@ZsonField
	public int tooltipBorderStart = 0;
	
	@ZsonField
	public int tooltipBorderEnd = 0;
	
	@ZsonField
	public int tooltipBackgroundStart = 0;
	
	@ZsonField
	public int tooltipBackgroundEnd = 0;
	
	@ZsonField(comment = """
		Intended for advanced users and developers only. Colour format is 0xAARRGGBB.
		Alpha will only be applied to block faces. Outline will always be fully opaque when enabled.
		A value of `0` disables this setting.
		DEFAULT: `0`""")
	public int blockShapeOverlayOverride = 0;
	//endregion
	
	private static final int EXPECTED_VERSION = 1;
	
	@ZsonField(comment = "Used internally. Don't modify this.")
	public int configVersion = EXPECTED_VERSION;
	
	@Override
	public NolijiumConfigImpl clone() throws CloneNotSupportedException {
		return (NolijiumConfigImpl) super.clone();
	}
	
	private static final int MAX_RETRIES = 5;
	private static final Zson ZSON = new Zson();
	
	private static NolijiumConfigImpl readFromFile(final File configFile) {
		if (configFile == null || !configFile.exists())
			return null;
		
		int i = 0;
		while (true) {
			try {
				//noinspection DataFlowIssue
				return Zson.map2Obj(Zson.parse(new FileReader(configFile)), NolijiumConfigImpl.class);
            } catch (IllegalArgumentException | AssertionError e) {
				if (++i < MAX_RETRIES) {
                    try {
	                    //noinspection BusyWait
	                    Thread.sleep(i * 200L);
						continue;
                    } catch (InterruptedException ignored) {
                        return null;
                    }
                }
				Nolijium.LOGGER.error("Error parsing config after {} retries: ", i, e);
				return null;
			} catch (IOException e) {
				Nolijium.LOGGER.error("Error reading config: ", e);
				return null;
            }
        }
	}
	
	private static NolijiumConfigImpl readConfigFile() {
		NolijiumConfigImpl result = readFromFile(getConfigFile());
		
		if (result == null)
			result = new NolijiumConfigImpl();
		
		return result;
	}
	
	private void writeToFile(final File configFile) {
		this.configVersion = EXPECTED_VERSION;
		try (final FileWriter configWriter = new FileWriter(configFile)) {
			ZSON.write(Zson.obj2Map(this), configWriter);
			configWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException("Failed to write config file", e);
		}
	}
	
	private static Consumer<NolijiumConfigImpl> consumer;
	private static IFileWatcher instanceWatcher;
	private static IFileWatcher globalWatcher;
	private static File instanceFile = null;
	private static File globalFile = null;
	
	public static void replace(final NolijiumConfigImpl newConfig) throws InterruptedException {
		try {
			instanceWatcher.lock();
			try {
				globalWatcher.lock();
				
				newConfig.writeToFile(getConfigFile());
				consumer.accept(newConfig);
			} finally {
				globalWatcher.unlock();
			}
		} finally {
			instanceWatcher.unlock();
		}
	}
	
	public void modify(Consumer<NolijiumConfigImpl> modifier) {
		final NolijiumConfigImpl newConfig = this.clone();
		modifier.accept(newConfig);
		replace(newConfig);
	}
	
	
	private static final HostPlatform HOST_PLATFORM;
	static {
		final String OS_NAME = System.getProperty("os.name").toLowerCase();
		
		if (OS_NAME.contains("linux"))
			HOST_PLATFORM = HostPlatform.LINUX;
		else if (OS_NAME.contains("mac"))
			HOST_PLATFORM = HostPlatform.MAC_OS;
		else if (OS_NAME.contains("win"))
			HOST_PLATFORM = HostPlatform.WINDOWS;
		else
			HOST_PLATFORM = HostPlatform.UNKNOWN;
	}
	private static final String CONFIG_PATH_OVERRIDE = System.getProperty("nolijium.configPathOverride");
	private static final Path GLOBAL_CONFIG_PATH;
	
	static {
		final Path dotMinecraft;
		if (HOST_PLATFORM == HostPlatform.LINUX || HOST_PLATFORM == HostPlatform.UNKNOWN)
			dotMinecraft = Paths.get(System.getProperty("user.home"), ".minecraft");
		else if (HOST_PLATFORM == HostPlatform.WINDOWS)
			dotMinecraft = Paths.get(System.getenv("APPDATA"), ".minecraft");
		else
			dotMinecraft = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
		
		GLOBAL_CONFIG_PATH = dotMinecraft.resolve("global");
		if (Files.notExists(GLOBAL_CONFIG_PATH)) {
            try {
                Files.createDirectories(GLOBAL_CONFIG_PATH);
            } catch (IOException e) {
                Nolijium.LOGGER.error("Failed to create global config path: ", e);
            }
        }
	}
	
	public static File getConfigFile() {
		if (CONFIG_PATH_OVERRIDE != null) {
			return new File(CONFIG_PATH_OVERRIDE);
		}
		
		if (instanceFile != null && instanceFile.exists()) {
			return instanceFile;
		}
		
		return globalFile;
	}
	
	public static void reloadConfig() {
		Nolijium.LOGGER.info("Reloading config...");
		
		final NolijiumConfigImpl newConfig = readConfigFile();
		
		consumer.accept(newConfig);
	}
	
	public static void openConfigFile() {
		final File configFile = getConfigFile();
		try {
			final String CONFIG_PATH = configFile.getCanonicalPath();
			
			final ProcessBuilder builder = new ProcessBuilder().inheritIO();
			
			switch (HOST_PLATFORM) {
				case LINUX, UNKNOWN -> builder.command("xdg-open", CONFIG_PATH);
				case WINDOWS -> builder.command("rundll32", "url.dll,FileProtocolHandler", CONFIG_PATH);
				case MAC_OS -> builder.command("open", "-t", CONFIG_PATH);
			}
			
			builder.start();
		} catch (IOException e) {
			Nolijium.LOGGER.error("Error opening config file: ", e);
		}
	}
	
	public static void init(final Path instanceConfigPath, final String fileName, final Consumer<NolijiumConfigImpl> configConsumer) {
		if (consumer != null)
			throw new AssertionError("Config already initialized!");
		
		consumer = configConsumer;
		if (CONFIG_PATH_OVERRIDE == null) {
			instanceFile = instanceConfigPath.resolve(fileName).toFile();
			globalFile = GLOBAL_CONFIG_PATH.resolve(fileName).toFile();
		}
		
		NolijiumConfigImpl config = readConfigFile();
		
		// write new options and comment updates to disk
		config.writeToFile(getConfigFile());
		
		consumer.accept(config);
		
		try {
			final IFileWatcher nullWatcher = new NullFileWatcher();
			
			if (CONFIG_PATH_OVERRIDE == null) {
				instanceWatcher = FileWatcher.onFileChange(instanceFile.toPath(), NolijiumConfigImpl::reloadConfig);
				globalWatcher = FileWatcher.onFileChange(globalFile.toPath(), NolijiumConfigImpl::reloadConfig);
			} else {
				instanceWatcher = nullWatcher;
				globalWatcher = FileWatcher.onFileChange(getConfigFile().toPath(), NolijiumConfigImpl::reloadConfig);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to create file watcher", e);
		}
	}
	
}
