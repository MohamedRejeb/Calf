package com.mohamedrejeb.calf.navigation

/**
 * A mapping from String keys to values of various types. In most cases, you
 * should work directly with either the [AdaptiveBundle] or
 * [PersistableBundle] subclass.
 */
open class AdaptiveBaseBundle {

    // Invariant - exactly one of mMap / mParcelledData will be null
    // (except inside a call to unparcel)
    var mMap: MutableMap<String, Any?> = mutableMapOf()

    /*
     * Flag indicating if mParcelledData is only referenced in this bundle.
     * mParcelledData could be referenced by other bundles if mMap contains lazy values,
     * and bundle data is copied to another bundle using putAll or the copy constructors.
     */
    var mOwnsLazyValues = true

    /** {@hide}  */
    var mFlags = 0
    
    /**
     * Removes all elements from the mapping of this Bundle.
     */
    open fun clear() {
        mMap.clear()
    }

    /**
     * Returns true if the given key is contained in the mapping
     * of this Bundle.
     *
     * @param key a String key
     * @return true if the key is part of the mapping, false otherwise
     */
    fun containsKey(key: String): Boolean {
        return mMap.containsKey(key)
    }

    /**
     * Returns the object of type `clazz` for the given `key`, or `null` if:
     *
     *  * No mapping of the desired type exists for the given key.
     *  * A `null` value is explicitly associated with the key.
     *  * The object is not of type `clazz`.
     *
     *
     *
     * Use the more specific APIs where possible, especially in the case of containers such as
     * lists, since those APIs allow you to specify the type of the items.
     *
     * @param key String key
     * @param clazz The type of the object expected
     * @return an Object, or null
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? {
        return mMap[key] as? T
    }

    fun getValue(key: String): Any? {
        return mMap[key]
    }

    /**
     * Removes any entry with the given key from the mapping of this Bundle.
     *
     * @param key a String key
     */
    open fun remove(key: String) {
        mMap.remove(key)
    }

    /**
     * Inserts all mappings from the given Map into this BaseBundle.
     *
     * @param map a Map
     */
    fun putAll(map: Map<String, Any>) {
        mMap.putAll(map)
    }

    /**
     * Returns a Set containing the Strings used as keys in this Bundle.
     *
     * @return a Set of String keys
     */
    fun keySet(): Set<String> {
        return mMap.keys
    }

    /** {@hide}  */
    open fun putObject(key: String, value: Any?) {
        when (value) {
            null -> putString(key, null)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Double -> putDouble(key, value)
            is String -> putString(key, value as String?)
            is BooleanArray -> putBooleanArray(key, value as BooleanArray?)
            is IntArray -> putIntArray(key, value as IntArray?)
            is LongArray -> putLongArray(key, value as LongArray?)
            is DoubleArray -> putDoubleArray(key, value as DoubleArray?)
            else -> throw IllegalArgumentException("Unsupported type " + value::class)
        }
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a boolean
     */
    fun putBoolean(key: String, value: Boolean) {
        mMap[key] = value
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a byte
     */
    open fun putByte(key: String, value: Byte) {
        mMap[key] = value
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a char
     */
    open fun putChar(key: String, value: Char) {
        mMap[key] = value
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a short
     */
    open fun putShort(key: String, value: Short) {
        mMap[key] = value
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value an int
     */
    fun putInt(key: String, value: Int) {
        mMap[key] = value
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a long
     */
    fun putLong(key: String, value: Long) {
        mMap[key] = value
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a float
     */
    open fun putFloat(key: String, value: Float) {
        mMap[key] = value
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String
     * @param value a double
     */
    fun putDouble(key: String, value: Double) {
        mMap[key] = value
    }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a String, or null
     */
    fun putString(key: String, value: String?) {
        mMap[key] = value
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a CharSequence, or null
     */
    open fun putCharSequence(key: String, value: CharSequence?) {
        mMap[key] = value
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value an ArrayList<Integer> object, or null
    </Integer></Integer> */
    fun putIntegerArrayList(key: String, value: ArrayList<Int?>?) {
        mMap[key] = value
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value an ArrayList<String> object, or null
    </String></String> */
    fun putStringArrayList(key: String, value: ArrayList<String?>?) {
        mMap[key] = value
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value an ArrayList<CharSequence> object, or null
    </CharSequence></CharSequence> */
    fun putCharSequenceArrayList(key: String, value: ArrayList<CharSequence?>?) {
        mMap[key] = value
    }

    /**
     * Inserts a boolean array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a boolean array object, or null
     */
    fun putBooleanArray(key: String, value: BooleanArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a byte array object, or null
     */
    open fun putByteArray(key: String, value: ByteArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a short array object, or null
     */
    open fun putShortArray(key: String, value: ShortArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a char array object, or null
     */
    open fun putCharArray(key: String, value: CharArray?) {
        mMap[key] = value
    }

    /**
     * Inserts an int array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value an int array object, or null
     */
    fun putIntArray(key: String, value: IntArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a long array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a long array object, or null
     */
    fun putLongArray(key: String, value: LongArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a float array object, or null
     */
    open fun putFloatArray(key: String, value: FloatArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a double array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a double array object, or null
     */
    fun putDoubleArray(key: String, value: DoubleArray?) {
        mMap[key] = value
    }

    /**
     * Inserts a String array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a String array object, or null
     */
    fun putStringArray(key: String, value: Array<String>?) {
        mMap[key] = value
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String
     * @param value a CharSequence array object, or null
     */
    open fun putCharSequenceArray(key: String, value: Array<CharSequence?>?) {
        mMap[key] = value
    }

    /**
     * Returns the value associated with the given key, or false if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a boolean value
     */
    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Boolean ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    open fun getByte(key: String): Byte {
        return getByte(key, 0.toByte())
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a byte value
     */
    open fun getByte(key: String, defaultValue: Byte): Byte {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Byte ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    open fun getChar(key: String): Char {
        return getChar(key, 0.toChar())
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a char value
     */
    open fun getChar(key: String, defaultValue: Char): Char {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Char ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    open fun getShort(key: String): Short {
        return getShort(key, 0.toShort())
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a short value
     */
    open fun getShort(key: String, defaultValue: Short): Short {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Short ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return an int value
     */
    fun getInt(key: String): Int {
        return getInt(key, 0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return an int value
     */
    fun getInt(key: String, defaultValue: Int): Int {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Int ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or 0L if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a long value
     */
    fun getLong(key: String): Long {
        return getLong(key, 0L)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a long value
     */
    fun getLong(key: String, defaultValue: Long): Long {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Long ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or 0.0f if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    open fun getFloat(key: String): Float {
        return getFloat(key, 0.0f)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a float value
     */
    open fun getFloat(key: String, defaultValue: Float): Float {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Float ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or 0.0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a double value
     */
    fun getDouble(key: String): Double {
        return getDouble(key, 0.0)
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a double value
     */
    fun getDouble(key: String, defaultValue: Double): Double {
        val o: Any = mMap[key] ?: return defaultValue
        return o as? Double ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String
     * @return a String value, or null
     */
    fun getString(key: String): String? {
        val o: Any? = mMap[key]
        return o?.toString()
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key or if a null
     * value is explicitly associated with the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist or if a null
     * value is associated with the given key.
     * @return the String value associated with the given key, or defaultValue
     * if no valid String object is currently mapped to that key.
     */
    fun getString(key: String, defaultValue: String): String {
        val s = getString(key)
        return s ?: defaultValue
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String
     * @return a CharSequence value, or null
     */
    open fun getCharSequence(key: String): CharSequence? {
        val o: Any? = mMap[key]
        return o as? CharSequence
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key or if a null
     * value is explicitly associated with the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist or if a null
     * value is associated with the given key.
     * @return the CharSequence value associated with the given key, or defaultValue
     * if no valid CharSequence object is currently mapped to that key.
     */
    open fun getCharSequence(key: String, defaultValue: CharSequence?): CharSequence? {
        val cs = getCharSequence(key)
        return cs ?: defaultValue
    }
}

