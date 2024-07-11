package dev.nolij.nolijium.impl.config;

import dev.nolij.nolijium.impl.util.Alignment;
import dev.nolij.nolijium.impl.Nolijium;
import dev.nolij.nolijium.impl.util.DetailLevel;
import dev.nolij.zson.Zson;
import dev.nolij.zson.ZsonField;

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
		DEFAULT: `false`""")
	public boolean enableGamma = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideAllToasts = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideAdvancementToasts = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideRecipeToasts = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideSystemToasts = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideTutorialToasts = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hideParticles = false;
	
	@ZsonField(comment = """
		DEFAULT: `[ ]`""")
	public ArrayList<String> hideParticlesByID = new ArrayList<>();
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean disableTextureAnimations = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean revertDamageCameraTilt = false;
	
	@ZsonField(comment = """
		DEFAULT: `100`""")
	public int maxChatHistory = 100;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hudEnabled = false;
	
	@ZsonField(comment = """
		DEFAULT: `LEFT`""")
	public Alignment.X hudAlignmentX = Alignment.X.LEFT;
	
	@ZsonField(comment = """
		DEFAULT: `TOP`""")
	public Alignment.Y hudAlignmentY = Alignment.Y.TOP;
	
	@ZsonField(comment = """
		DEFAULT: `5`""")
	public int hudMarginX = 5;
	
	@ZsonField(comment = """
		DEFAULT: `5`""")
	public int hudMarginY = 5;
	
	@ZsonField(comment = """
		DEFAULT: `true`""")
	public boolean hudBackground = true;
	
	@ZsonField(comment = """
		DEFAULT: `true`""")
	public boolean hudShadow = true;
	
	@ZsonField(comment = """
		DEFAULT: `5`""")
	public int hudRefreshRateTicks = 5;
	
	@ZsonField(comment = """
		DEFAULT: `SIMPLE`""")
	public DetailLevel hudShowFPS = DetailLevel.SIMPLE;
	
	@ZsonField(comment = """
		DEFAULT: `10.0`""")
	public double hudFrameTimeBufferSize = 10D;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hudShowCPU = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hudShowMemory = false;
	
	@ZsonField(comment = """
		DEFAULT: `false`""")
	public boolean hudShowCoordinates = false;
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
            } catch (IllegalArgumentException e) {
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
		final Path dotMinecraft = switch (HOST_PLATFORM) {
			case LINUX, UNKNOWN -> Paths.get(System.getProperty("user.home"), ".minecraft");
			case WINDOWS -> Paths.get(System.getenv("APPDATA"), ".minecraft");
			case MAC_OS -> Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft");
		};
		
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
