package dev.nolij.nolijium.impl.util;

import dev.nolij.zumegradle.proguard.ProGuardKeep;

public final class Alignment {
	
	@ProGuardKeep.Enum
	public enum X {
		LEFT, RIGHT
	}
	
	@ProGuardKeep.Enum
	public enum Y {
		TOP, BOTTOM
	}
	
}
