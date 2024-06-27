package com.mohamedrejeb.calf.picker.platform.mac.foundation

import com.sun.jna.NativeLong

/**
 * Could be an address in memory (if pointer to a class or method) or a value (like 0 or 1)
 */
internal class ID : NativeLong {
	constructor()

	constructor(peer: Long) : super(peer)

	fun booleanValue(): Boolean {
		return toInt() != 0
	}

	override fun toByte(): Byte {
		return toLong().toByte()
	}

	override fun toShort(): Short {
		return toLong().toShort()
	}

	companion object {
		val NIL: ID = ID(0L)
	}
}
