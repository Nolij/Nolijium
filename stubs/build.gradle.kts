operator fun String.invoke(): String = rootProject.properties[this] as? String ?: error("Property $this not found")