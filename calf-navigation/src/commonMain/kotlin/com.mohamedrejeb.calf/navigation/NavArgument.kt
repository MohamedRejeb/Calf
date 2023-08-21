package com.mohamedrejeb.calf.navigation


/**
 * NavArgument denotes an argument that is supported by a [NavDestination].
 *
 * A AdaptiveNavArgument has a type and optionally a default value, that are used to read/write
 * it in a Bundle. It can also be nullable if the type supports it.
 */
class NavArgument internal constructor(
    val type: AdaptiveNavType<Any>,
    val isNullable: Boolean,
    val defaultValue: Any,
    val defaultValuePresent: Boolean
) {

    init {
        require(!(!type.isNullableAllowed && isNullable)) {
            "${type.name} does not allow nullable values"
        }
    }

    fun putDefaultValue(name: String, bundle: AdaptiveBundle) {
        if (defaultValuePresent) {
            type.put(bundle, name, defaultValue)
        }
    }

    fun verify(name: String, bundle: AdaptiveBundle): Boolean {
        if (!isNullable && bundle.containsKey(name) && bundle.getValue(name) == null) {
            return false
        }
        try {
            type[bundle, name]
        } catch (e: ClassCastException) {
            return false
        }
        return true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(this::class.simpleName)
        sb.append(" Type: $type")
        sb.append(" Nullable: $isNullable")
        if (defaultValuePresent) {
            sb.append(" DefaultValue: $defaultValue")
        }
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        val that = other as NavArgument
        if (isNullable != that.isNullable) return false
        if (defaultValuePresent != that.defaultValuePresent) return false
        if (type != that.type) return false
        return defaultValue == that.defaultValue
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + if (isNullable) 1 else 0
        result = 31 * result + if (defaultValuePresent) 1 else 0
        result = 31 * result + (defaultValue?.hashCode() ?: 0)
        return result
    }

    /**
     * A builder for constructing [NavArgument] instances.
     */
    @Suppress("UNCHECKED_CAST")
    class Builder {
        private var type: AdaptiveNavType<Any>? = null
        private var isNullable = false
        private var defaultValue: Any? = null
        private var defaultValuePresent = false

        /**
         * Set the type of the argument.
         * @param type Type of the argument.
         * @return This builder.
         */
        public fun <T> setType(type: AdaptiveNavType<T>): Builder {
            this.type = type as AdaptiveNavType<Any>
            return this
        }

        /**
         * Specify if the argument is nullable.
         * The NavType you set for this argument must allow nullable values.
         * @param isNullable Argument will be nullable if true.
         * @return This builder.
         * @see NavType.isNullableAllowed
         */
        public fun setIsNullable(isNullable: Boolean): Builder {
            this.isNullable = isNullable
            return this
        }

        /**
         * Specify the default value for an argument. Calling this at least once will cause the
         * argument to have a default value, even if it is set to null.
         * @param defaultValue Default value for this argument.
         * Must match NavType if it is specified.
         * @return This builder.
         */
        public fun setDefaultValue(defaultValue: Any?): Builder {
            this.defaultValue = defaultValue
            defaultValuePresent = true
            return this
        }

        /**
         * Build the NavArgument specified by this builder.
         * If the type is not set, the builder will infer the type from the default argument value.
         * If there is no default value, the type will be unspecified.
         * @return the newly constructed NavArgument.
         */
        public fun build(): NavArgument {
            require(defaultValue != null) {
                "Default value must be set if no type is specified"
            }
            val finalType = type ?: AdaptiveNavType.inferFromValueType(defaultValue!!)
            return NavArgument(finalType, isNullable, defaultValue!!, defaultValuePresent)
        }
    }
}

/**
 * Returns a list of NavArgument keys where required NavArguments with that key
 * returns false for the predicate `isArgumentMissing`.
 *
 * @param [isArgumentMissing] predicate that returns true if the key of a required NavArgument
 * is missing from a Bundle that is expected to contain it.
 */
internal fun Map<String, NavArgument?>.missingRequiredArguments(
    isArgumentMissing: (key: String) -> Boolean
): List<String> {
    val requiredArgumentKeys = filterValues {
        if (it != null) {
            !it.isNullable && !it.defaultValuePresent
        } else false
    }.keys
    return requiredArgumentKeys.filter { key -> isArgumentMissing(key) }
}