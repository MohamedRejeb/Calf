package com.mohamedrejeb.calf.navigation

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * AdaptiveNavType denotes the type that can be used in a [NavArgument].
 *
 * There are built-in AdaptiveNavTypes for primitive types, such as int, long, boolean, float, and strings,
 * parcelable, and serializable classes (including Enums), as well as arrays of each supported type.
 *
 * You should only use one of the static AdaptiveNavType instances and subclasses defined in this class.
 *
 * @param T the type of the data that is supported by this AdaptiveNavType
 */
abstract class AdaptiveNavType<T>(
    /**
     * Check if an argument with this type can hold a null value.
     * @return Returns true if this type allows null values, false otherwise.
     */
    open val isNullableAllowed: Boolean
) {

    /**
     * Put a value of this type in the `bundle`
     *
     * @param bundle bundle to put value in
     * @param key    bundle key
     * @param value  value of this type
     */
    abstract fun put(bundle: AdaptiveBundle, key: String, value: T)

    /**
     * Get a value of this type from the `bundle`
     *
     * @param bundle bundle to get value from
     * @param key    bundle key
     * @return value of this type
     */
    abstract operator fun get(bundle: AdaptiveBundle, key: String): T?

    /**
     * Parse a value of this type from a String.
     *
     * @param value string representation of a value of this type
     * @return parsed value of the type represented by this AdaptiveNavType
     * @throws IllegalArgumentException if value cannot be parsed into this type
     */
    abstract fun parseValue(value: String): T

    /**
     * Parse a value of this type from a String and then combine that
     * parsed value with the given previousValue of the same type to
     * provide a new value that contains both the new and previous value.
     *
     * By default, the given value will replace the previousValue.
     *
     * @param value string representation of a value of this type
     * @param previousValue previously parsed value of this type
     * @return combined parsed value of the type represented by this AdaptiveNavType
     * @throws IllegalArgumentException if value cannot be parsed into this type
     */
    open fun parseValue(value: String, previousValue: T) = parseValue(value)

    /**
     * Parse a value of this type from a String and put it in a `bundle`
     *
     * @param bundle bundle to put value in
     * @param key    bundle key under which to put the value
     * @param value  string representation of a value of this type
     * @return parsed value of the type represented by this AdaptiveNavType
     * @suppress
     */
    fun parseAndPut(bundle: AdaptiveBundle, key: String, value: String): T {
        val parsedValue = parseValue(value)
        put(bundle, key, parsedValue)
        return parsedValue
    }

    /**
     * Parse a value of this type from a String, combine that parsed value
     * with the given previousValue, and then put that combined parsed
     * value in a `bundle`.
     *
     * @param bundle bundle to put value in
     * @param key    bundle key under which to put the value
     * @param value  string representation of a value of this type
     * @param previousValue previously parsed value of this type
     * @return combined parsed value of the type represented by this AdaptiveNavType
     * @suppress
     */
    fun parseAndPut(bundle: AdaptiveBundle, key: String, value: String?, previousValue: T): T {
        if (!bundle.containsKey(key)) {
            throw IllegalArgumentException("There is no previous value in this bundle.")
        }
        if (value != null) {
            val parsedCombinedValue = parseValue(value, previousValue)
            put(bundle, key, parsedCombinedValue)
            return parsedCombinedValue
        }
        return previousValue
    }

    /**
     * Serialize a value of this AdaptiveNavType into a String.
     *
     * By default it returns value of [kotlin.toString] or null if value passed in is null.
     *
     * This method can be override for custom serialization implementation on types such
     * custom AdaptiveNavType classes.
     *
     * @param value a value representing this AdaptiveNavType to be serialized into a String
     * @return serialized String value of [value]
     */
    open fun serializeAsValue(value: T): String {
        return value.toString()
    }

    /**
     * The name of this type.
     *
     * This is the same value that is used in Navigation XML `argType` attribute.
     *
     * @return name of this type
     */
    open val name: String = "nav_type"

    override fun toString(): String {
        return name
    }

    companion object {
        /**
         * Parse an argType string into a AdaptiveNavType.
         *
         * @param type        argType string, usually parsed from the Navigation XML file
         * @param packageName package name of the R file,
         * used for parsing relative class names starting with a dot.
         * @return a AdaptiveNavType representing the type indicated by the argType string.
         * Defaults to StringType for null.
         * @throws IllegalArgumentException if there is no valid argType
         * @throws RuntimeException if the type class name cannot be found
         */
        @Suppress("NON_FINAL_MEMBER_IN_OBJECT") // this needs to be open to
        // maintain api compatibility and type cast are unchecked
        @JvmStatic
        open fun fromArgType(type: String?, packageName: String?): AdaptiveNavType<*> {
            when {
                IntType.name == type -> return IntType
                IntArrayType.name == type -> return IntArrayType
                LongType.name == type -> return LongType
                LongArrayType.name == type -> return LongArrayType
                BoolType.name == type -> return BoolType
                BoolArrayType.name == type -> return BoolArrayType
                StringType.name == type -> return StringType
                StringArrayType.name == type -> return StringArrayType
                FloatType.name == type -> return FloatType
                FloatArrayType.name == type -> return FloatArrayType
                ReferenceType.name == type -> return ReferenceType
                !type.isNullOrEmpty() -> {
//                    try {
                        var className: String
                        className = if (type.startsWith(".") && packageName != null) {
                            packageName + type
                        } else {
                            type
                        }
//                        if (type.endsWith("[]")) {
//                            className = className.substring(0, className.length - 2)
//                            val clazz = Class.forName(className)
//                            when {
//                                Parcelable::class.java.isAssignableFrom(clazz) -> {
//                                    return ParcelableArrayType(clazz as Class<Parcelable>)
//                                }
//                                Serializable::class.java.isAssignableFrom(clazz) -> {
//                                    return SerializableArrayType(clazz as Class<Serializable>)
//                                }
//                            }
//                        } else {
//                            val clazz = Class.forName(className)
//                            when {
//                                Parcelable::class.java.isAssignableFrom(clazz) -> {
//                                    return ParcelableType(clazz as Class<Any?>)
//                                }
//                                Enum::class.java.isAssignableFrom(clazz) -> {
//                                    return EnumType(clazz as Class<Enum<*>>)
//                                }
//                                Serializable::class.java.isAssignableFrom(clazz) -> {
//                                    return SerializableType(clazz as Class<Serializable>)
//                                }
//                            }
//                        }
                        throw IllegalArgumentException(
                            "$className is not Serializable or Parcelable."
                        )
//                    } catch (e: ClassNotFoundException) {
//                        throw RuntimeException(e)
//                    }
                }
            }
            return StringType
        }

        /** @suppress */
        @Suppress("UNCHECKED_CAST") // needed for cast to AdaptiveNavType<Any>
        @JvmStatic
        fun inferFromValue(value: String): AdaptiveNavType<Any> {
            // because we allow Long literals without the L suffix at runtime,
            // the order of IntType and LongType parsing has to be reversed compared to Safe Args
            try {
                IntType.parseValue(value)
                return IntType as AdaptiveNavType<Any>
            } catch (e: IllegalArgumentException) {
                // ignored, proceed to check next type
            }
            try {
                LongType.parseValue(value)
                return LongType as AdaptiveNavType<Any>
            } catch (e: IllegalArgumentException) {
                // ignored, proceed to check next type
            }
            try {
                FloatType.parseValue(value)
                return FloatType as AdaptiveNavType<Any>
            } catch (e: IllegalArgumentException) {
                // ignored, proceed to check next type
            }
            try {
                BoolType.parseValue(value)
                return BoolType as AdaptiveNavType<Any>
            } catch (e: IllegalArgumentException) {
                // ignored, proceed to check next type
            }
            return StringType as AdaptiveNavType<Any>
        }

        /**
         * @param value nothing
         * @throws IllegalArgumentException not real
         * @suppress
         */
        @Suppress("UNCHECKED_CAST") // needed for cast to AdaptiveNavType<Any>
        @JvmStatic
        fun inferFromValueType(value: Any): AdaptiveNavType<Any> {
            return when {
                value is Int -> IntType as AdaptiveNavType<Any>
                value is IntArray -> IntArrayType as AdaptiveNavType<Any>
                value is Long -> LongType as AdaptiveNavType<Any>
                value is LongArray -> LongArrayType as AdaptiveNavType<Any>
                value is Float -> FloatType as AdaptiveNavType<Any>
                value is FloatArray -> FloatArrayType as AdaptiveNavType<Any>
                value is Boolean -> BoolType as AdaptiveNavType<Any>
                value is BooleanArray -> BoolArrayType as AdaptiveNavType<Any>
                value is String -> StringType as AdaptiveNavType<Any>
//                value is Array<*> && value.isArrayOf<String>() -> StringArrayType as AdaptiveNavType<Any>
//                value.javaClass.isArray &&
//                        Parcelable::class.java.isAssignableFrom(value.javaClass.componentType!!) -> {
//                    ParcelableArrayType(
//                        value.javaClass.componentType as Class<Parcelable>
//                    ) as AdaptiveNavType<Any>
//                }
//                value.javaClass.isArray &&
//                        Serializable::class.java.isAssignableFrom(value.javaClass.componentType!!) -> {
//                    SerializableArrayType(
//                        value.javaClass.componentType as Class<Serializable>
//                    ) as AdaptiveNavType<Any>
//                }
//                value is Parcelable -> ParcelableType(value.javaClass) as AdaptiveNavType<Any>
//                value is Enum<*> -> EnumType(value::class) as AdaptiveNavType<Any>
//                value is Serializable -> SerializableType(value.javaClass) as AdaptiveNavType<Any>
                else -> {
                    throw IllegalArgumentException(
                        "Object of type ${value::class.simpleName} is not supported for navigation " +
                                "arguments."
                    )
                }
            }
        }

        /**
         * AdaptiveNavType for storing integer values,
         * corresponding with the "integer" type in a Navigation XML file.
         *
         * Null values are not supported.
         */
        @JvmField
        val IntType: AdaptiveNavType<Int> = object : AdaptiveNavType<Int>(false) {
            override val name: String
                get() = "integer"

            override fun put(bundle: AdaptiveBundle, key: String, value: Int) {
                bundle.putInt(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Int? {
                return bundle[key]
            }

            override fun parseValue(value: String): Int {
                return if (value.startsWith("0x")) {
                    value.substring(2).toInt(16)
                } else {
                    value.toInt()
                }
            }
        }

        /**
         * AdaptiveNavType for storing integer values representing resource ids,
         * corresponding with the "reference" type in a Navigation XML file.
         *
         * Null values are not supported.
         */
        @JvmField
        val ReferenceType: AdaptiveNavType<Int> = object : AdaptiveNavType<Int>(false) {
            override val name: String
                get() = "reference"

            override fun put(bundle: AdaptiveBundle, key: String, value: Int) {
                bundle.putInt(key, value)
            }
            
            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Int? {
                return bundle[key]
            }

            override fun parseValue(value: String): Int {
                return if (value.startsWith("0x")) {
                    value.substring(2).toInt(16)
                } else {
                    value.toInt()
                }
            }
        }

        /**
         * AdaptiveNavType for storing integer arrays,
         * corresponding with the "integer[]" type in a Navigation XML file.
         *
         * Null values are supported.
         * Default values in Navigation XML files are not supported.
         */
        @JvmField
        val IntArrayType: AdaptiveNavType<IntArray?> = object : AdaptiveNavType<IntArray?>(true) {
            override val name: String
                get() = "integer[]"

            override fun put(bundle: AdaptiveBundle, key: String, value: IntArray?) {
                bundle.putIntArray(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): IntArray? {
                return bundle[key]
            }

            override fun parseValue(value: String): IntArray {
                return intArrayOf(IntType.parseValue(value))
            }

            override fun parseValue(value: String, previousValue: IntArray?): IntArray {
                return previousValue?.plus(parseValue(value)) ?: parseValue(value)
            }
        }

        /**
         * AdaptiveNavType for storing long values,
         * corresponding with the "long" type in a Navigation XML file.
         *
         * Null values are not supported.
         * Default values for this type in Navigation XML files must always end with an 'L' suffix, e.g.
         * `app:defaultValue="123L"`.
         */
        @JvmField
        val LongType: AdaptiveNavType<Long> = object : AdaptiveNavType<Long>(false) {
            override val name: String
                get() = "long"

            override fun put(bundle: AdaptiveBundle, key: String, value: Long) {
                bundle.putLong(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Long? {
                return bundle[key]
            }

            override fun parseValue(value: String): Long {
                // At runtime the L suffix is optional, contrary to the Safe Args plugin.
                // This is in order to be able to parse long numbers passed as deep link URL
                // parameters
                var localValue = value
                if (value.endsWith("L")) {
                    localValue = localValue.substring(0, value.length - 1)
                }
                return if (value.startsWith("0x")) {
                    localValue.substring(2).toLong(16)
                } else {
                    localValue.toLong()
                }
            }
        }

        /**
         * AdaptiveNavType for storing long arrays,
         * corresponding with the "long[]" type in a Navigation XML file.
         *
         * Null values are supported.
         * Default values in Navigation XML files are not supported.
         */
        @JvmField
        val LongArrayType: AdaptiveNavType<LongArray?> = object : AdaptiveNavType<LongArray?>(true) {
            override val name: String
                get() = "long[]"

            override fun put(bundle: AdaptiveBundle, key: String, value: LongArray?) {
                bundle.putLongArray(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): LongArray? {
                return bundle[key]
            }

            override fun parseValue(value: String): LongArray {
                return longArrayOf(LongType.parseValue(value))
            }

            override fun parseValue(value: String, previousValue: LongArray?): LongArray? {
                return previousValue?.plus(parseValue(value)) ?: parseValue(value)
            }
        }

        /**
         * AdaptiveNavType for storing float values,
         * corresponding with the "float" type in a Navigation XML file.
         *
         * Null values are not supported.
         */
        @JvmField
        val FloatType: AdaptiveNavType<Float> = object : AdaptiveNavType<Float>(false) {
            override val name: String
                get() = "float"

            override fun put(bundle: AdaptiveBundle, key: String, value: Float) {
                bundle.putFloat(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Float? {
                return bundle[key]
            }

            override fun parseValue(value: String): Float {
                return value.toFloat()
            }
        }

        /**
         * AdaptiveNavType for storing float arrays,
         * corresponding with the "float[]" type in a Navigation XML file.
         *
         * Null values are supported.
         * Default values in Navigation XML files are not supported.
         */
        @JvmField
        val FloatArrayType: AdaptiveNavType<FloatArray?> = object : AdaptiveNavType<FloatArray?>(true) {
            override val name: String
                get() = "float[]"

            override fun put(bundle: AdaptiveBundle, key: String, value: FloatArray?) {
                bundle.putFloatArray(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): FloatArray? {
                return bundle[key]
            }

            override fun parseValue(value: String): FloatArray {
                return floatArrayOf(FloatType.parseValue(value))
            }

            override fun parseValue(value: String, previousValue: FloatArray?): FloatArray? {
                return previousValue?.plus(parseValue(value)) ?: parseValue(value)
            }
        }

        /**
         * AdaptiveNavType for storing boolean values,
         * corresponding with the "boolean" type in a Navigation XML file.
         *
         * Null values are not supported.
         */
        @JvmField
        val BoolType: AdaptiveNavType<Boolean> = object : AdaptiveNavType<Boolean>(false) {
            override val name: String
                get() = "boolean"

            override fun put(bundle: AdaptiveBundle, key: String, value: Boolean) {
                bundle.putBoolean(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Boolean? {
                return bundle[key]
            }

            override fun parseValue(value: String): Boolean {
                return when (value) {
                    "true" -> true
                    "false" -> false
                    else -> {
                        throw IllegalArgumentException(
                            "A boolean AdaptiveNavType only accepts \"true\" or \"false\" values."
                        )
                    }
                }
            }
        }

        /**
         * AdaptiveNavType for storing boolean arrays,
         * corresponding with the "boolean[]" type in a Navigation XML file.
         *
         * Null values are supported.
         * Default values in Navigation XML files are not supported.
         */
        @JvmField
        val BoolArrayType: AdaptiveNavType<BooleanArray?> = object : AdaptiveNavType<BooleanArray?>(true) {
            override val name: String
                get() = "boolean[]"

            override fun put(bundle: AdaptiveBundle, key: String, value: BooleanArray?) {
                bundle.putBooleanArray(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): BooleanArray? {
                return bundle[key]
            }

            override fun parseValue(value: String): BooleanArray {
                return booleanArrayOf(BoolType.parseValue(value))
            }

            override fun parseValue(value: String, previousValue: BooleanArray?): BooleanArray? {
                return previousValue?.plus(parseValue(value)) ?: parseValue(value)
            }
        }

        /**
         * AdaptiveNavType for storing String values,
         * corresponding with the "string" type in a Navigation XML file.
         *
         * Null values are supported.
         */
        @JvmField
        val StringType: AdaptiveNavType<String?> = object : AdaptiveNavType<String?>(true) {
            override val name: String
                get() = "string"

            override fun put(bundle: AdaptiveBundle, key: String, value: String?) {
                bundle.putString(key, value)
            }

            @Suppress("DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): String? {
                return bundle[key]
            }

            /**
             * Returns input value by default.
             *
             * If input value is "null", returns null as the reversion of Kotlin standard library
             * serializing null receivers of [kotlin.toString] into "null".
             */
            override fun parseValue(value: String): String? {
                return if (value == "null") null else value
            }

            /**
             * Returns default value of Uri.encode(value).
             *
             * If input value is null, returns "null" in compliance with Kotlin standard library
             * parsing null receivers of [kotlin.toString] into "null".
             *
             */
            override fun serializeAsValue(value: String?): String {
//                return value?.let { Uri.encode(value) } ?: "null"
                return value?.let { value } ?: "null"
            }
        }

        /**
         * AdaptiveNavType for storing String arrays,
         * corresponding with the "string[]" type in a Navigation XML file.
         *
         * Null values are supported.
         * Default values in Navigation XML files are not supported.
         */
        @JvmField
        val StringArrayType: AdaptiveNavType<Array<String>?> = object : AdaptiveNavType<Array<String>?>(
            true
        ) {
            override val name: String
                get() = "string[]"

            override fun put(bundle: AdaptiveBundle, key: String, value: Array<String>?) {
                bundle.putStringArray(key, value)
            }

            @Suppress("UNCHECKED_CAST", "DEPRECATION")
            override fun get(bundle: AdaptiveBundle, key: String): Array<String>? {
                return bundle[key]
            }

            override fun parseValue(value: String): Array<String> {
                return arrayOf(value)
            }

            override fun parseValue(value: String, previousValue: Array<String>?): Array<String>? {
                return previousValue?.plus(parseValue(value)) ?: parseValue(value)
            }
        }
    }
}
