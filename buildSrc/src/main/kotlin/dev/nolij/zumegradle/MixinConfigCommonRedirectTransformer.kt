package dev.nolij.zumegradle

import com.github.jengelman.gradle.plugins.shadow.transformers.Transformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.FileTreeElement
import org.gradle.api.tasks.Input
import org.apache.tools.zip.ZipOutputStream
import org.apache.tools.zip.ZipEntry

class MixinConfigCommonRedirectTransformer : Transformer {
	
	private val JSON = JsonSlurper()

	@Input lateinit var modId: String
	@Input lateinit var implName: String
	@Input lateinit var commonImplName: String
	@Input lateinit var packageName: String

	override fun getName(): String {
		return "MixinConfigCommonRedirectTransformer"
	}

	override fun canTransformResource(element: FileTreeElement?): Boolean {
		return element != null && element.name.endsWith(".mixins.json")
	}

	private var transformed = false

	private var mixins = ArrayList<String>()

	override fun transform(context: TransformerContext?) {
		if (context == null)
			return

		this.transformed = true

		val parsed = JSON.parse(context.`is`) as Map<*, *>
		if (parsed.contains("client")) {
			@Suppress("UNCHECKED_CAST")
			mixins.addAll(parsed["client"] as List<String>)
		}
	}

	override fun hasTransformedResource(): Boolean {
		return transformed
	}

	override fun modifyOutputStream(os: ZipOutputStream?, preserveFileTimestamps: Boolean) {
		val mixinConfigEntry = ZipEntry("${modId}-${implName}.mixins.json")
		os!!.putNextEntry(mixinConfigEntry)
		
		val mixinConfigJson = mutableMapOf(
			"required" to true,
			"minVersion" to "0.8",
			"package" to packageName,
			"compatibilityLevel" to "JAVA_8",
			"client" to mixins
				.map { it.replaceFirst("${commonImplName}.", "${implName}.") }
				.toList(),
			"injectors" to mapOf(
				"defaultRequire" to 1,
			)
		)

		os.write(JsonOutput.prettyPrint(JsonOutput.toJson(mixinConfigJson)).toByteArray())

		transformed = false
		mixins.clear()
	}
	
}