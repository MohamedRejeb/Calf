package io.github.vinceglb.filekit.core.platform.mac.foundation

import com.sun.jna.Callback
import com.sun.jna.Function
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import org.jetbrains.annotations.NonNls
import java.io.File
import java.lang.reflect.Proxy
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.Collections
import java.util.UUID

/**
 * see [Documentation](http://developer.apple.com/documentation/Cocoa/Reference/ObjCRuntimeRef/Reference/reference.html)
 */
@NonNls
internal object Foundation {
	private val myFoundationLibrary: FoundationLibrary = Native.load(
		"Foundation",
		FoundationLibrary::class.java, Collections.singletonMap("jna.encoding", "UTF8")
	)

	private val myObjcMsgSend: Function by lazy {
		val nativeLibrary = (Proxy.getInvocationHandler(myFoundationLibrary) as Library.Handler).nativeLibrary
		nativeLibrary.getFunction("objc_msgSend")
	}

	/**
	 * Get the ID of the NSClass with className
	 */
	fun getObjcClass(className: String?): ID? {
		return myFoundationLibrary.objc_getClass(className)
	}

	fun getProtocol(name: String?): ID? {
		return myFoundationLibrary.objc_getProtocol(name)
	}

	fun createSelector(s: String?): Pointer? {
		return myFoundationLibrary.sel_registerName(s)
	}

	private fun prepInvoke(id: ID?, selector: Pointer?, args: Array<out Any?>): Array<Any?> {
		val invokArgs = arrayOfNulls<Any>(args.size + 2)
		invokArgs[0] = id
		invokArgs[1] = selector
		System.arraycopy(args, 0, invokArgs, 2, args.size)
		return invokArgs
	}

	fun invoke(id: ID?, selector: Pointer?, vararg args: Any?): ID {
		// objc_msgSend is called with the calling convention of the target method
		// on x86_64 this does not make a difference, but arm64 uses a different calling convention for varargs
		// it is therefore important to not call objc_msgSend as a vararg function
		return ID(myObjcMsgSend.invokeLong(prepInvoke(id, selector, args)))
	}

	/**
	 * Invokes the given vararg selector.
	 * Expects `NSArray arrayWithObjects:(id), ...` like signature, i.e. exactly one fixed argument, followed by varargs.
	 */
	fun invokeVarArg(id: ID?, selector: Pointer?, vararg args: Any?): ID? {
		// c functions and objc methods have at least 1 fixed argument, we therefore need to separate out the first argument
		return myFoundationLibrary.objc_msgSend(
			id, selector,
			args[0], *Arrays.copyOfRange(args, 1, args.size)
		)
	}

	fun invoke(cls: String?, selector: String?, vararg args: Any?): ID {
		return invoke(getObjcClass(cls), createSelector(selector), *args)
	}

	fun invokeVarArg(cls: String?, selector: String?, vararg args: Any?): ID? {
		return invokeVarArg(getObjcClass(cls), createSelector(selector), *args)
	}

	fun safeInvoke(stringCls: String?, stringSelector: String?, vararg args: Any): ID {
		val cls = getObjcClass(stringCls)
		val selector = createSelector(stringSelector)
		if (!invoke(cls, "respondsToSelector:", selector).booleanValue()) {
			throw RuntimeException(
				String.format(
					"Missing selector %s for %s",
					stringSelector,
					stringCls
				)
			)
		}
		return invoke(cls, selector, *args)
	}

	fun invoke(id: ID?, selector: String?, vararg args: Any?): ID {
		return invoke(id, createSelector(selector), *args)
	}

	fun invoke_fpret(receiver: ID?, selector: Pointer?, vararg args: Any?): Double {
		return myObjcMsgSend.invokeDouble(prepInvoke(receiver, selector, args))
	}

	fun invoke_fpret(receiver: ID?, selector: String?, vararg args: Any?): Double {
		return invoke_fpret(receiver, createSelector(selector), *args)
	}

	fun isNil(id: ID?): Boolean {
		return id == null || ID.NIL == id
	}

	fun safeInvoke(id: ID, stringSelector: String?, vararg args: Any): ID {
		val selector = createSelector(stringSelector)
		if (id != ID.NIL && !invoke(id, "respondsToSelector:", selector).booleanValue()) {
			throw RuntimeException(
				String.format(
					"Missing selector %s for %s", stringSelector, toStringViaUTF8(
						invoke(id, "description")
					)
				)
			)
		}
		return invoke(id, selector, *args)
	}

	fun allocateObjcClassPair(superCls: ID?, name: String?): ID? {
		return myFoundationLibrary.objc_allocateClassPair(superCls, name, 0)
	}

	fun registerObjcClassPair(cls: ID?) {
		myFoundationLibrary.objc_registerClassPair(cls)
	}

	fun isClassRespondsToSelector(cls: ID?, selectorName: Pointer?): Boolean {
		return myFoundationLibrary.class_respondsToSelector(cls, selectorName)
	}

	/**
	 * @param cls          The class to which to add a method.
	 * @param selectorName A selector that specifies the name of the method being added.
	 * @param impl         A function which is the implementation of the new method. The function must take at least two arguments-self and _cmd.
	 * @param types        An array of characters that describe the types of the arguments to the method.
	 * See [](https://developer.apple.com/library/IOs/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100)
	 * @return true if the method was added successfully, otherwise false (for example, the class already contains a method implementation with that name).
	 */
	fun addMethod(cls: ID?, selectorName: Pointer?, impl: Callback?, types: String?): Boolean {
		return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types)
	}

	fun addProtocol(aClass: ID?, protocol: ID?): Boolean {
		return myFoundationLibrary.class_addProtocol(aClass, protocol)
	}

	fun addMethodByID(cls: ID?, selectorName: Pointer?, impl: ID?, types: String?): Boolean {
		return myFoundationLibrary.class_addMethod(cls, selectorName, impl, types)
	}

	fun isMetaClass(cls: ID?): Boolean {
		return myFoundationLibrary.class_isMetaClass(cls)
	}

	fun stringFromSelector(selector: Pointer?): String? {
		val id = myFoundationLibrary.NSStringFromSelector(selector)
		return if (ID.NIL == id) null else toStringViaUTF8(id)
	}

	fun stringFromClass(aClass: ID?): String? {
		val id = myFoundationLibrary.NSStringFromClass(aClass)
		return if (ID.NIL == id) null else toStringViaUTF8(id)
	}

	fun getClass(clazz: Pointer?): Pointer? {
		return myFoundationLibrary.objc_getClass(clazz)
	}

	fun fullUserName(): String? {
		return toStringViaUTF8(myFoundationLibrary.NSFullUserName())
	}

	fun class_replaceMethod(cls: ID?, selector: Pointer?, impl: Callback?, types: String?): ID? {
		return myFoundationLibrary.class_replaceMethod(cls, selector, impl, types)
	}

	fun getMetaClass(className: String?): ID? {
		return myFoundationLibrary.objc_getMetaClass(className)
	}

	fun isPackageAtPath(path: String): Boolean {
		val workspace = invoke("NSWorkspace", "sharedWorkspace")
		val result = invoke(workspace, createSelector("isFilePackageAtPath:"), nsString(path))

		return result.booleanValue()
	}

	fun isPackageAtPath(file: File): Boolean {
		if (!file.isDirectory) return false
		return isPackageAtPath(file.path)
	}

	fun nsString(s: String?): ID {
		return if (s == null) ID.NIL else NSString.create(s)
	}

	fun nsString(s: CharSequence?): ID {
		return if (s == null) ID.NIL else NSString.create(s)
	}

	fun nsUUID(uuid: UUID): ID {
		return nsUUID(uuid.toString())
	}

	fun nsUUID(uuid: String): ID {
		return invoke(
			invoke(invoke("NSUUID", "alloc"), "initWithUUIDString:", nsString(uuid)),
			"autorelease"
		)
	}

	fun nsURL(path: String): ID {
		return invoke("NSURL", "fileURLWithPath:", nsString(path))
	}

	fun toStringViaUTF8(cfString: ID?): String? {
		if (ID.NIL == cfString) return null

		val lengthInChars = myFoundationLibrary.CFStringGetLength(cfString)
		val potentialLengthInBytes =
			3 * lengthInChars + 1 // UTF8 fully escaped 16 bit chars, plus nul

		val buffer = ByteArray(potentialLengthInBytes)
		val ok = myFoundationLibrary.CFStringGetCString(
			cfString,
			buffer,
			buffer.size,
			FoundationLibrary.kCFStringEncodingUTF8
		)
		if (ok.toInt() == 0) throw RuntimeException("Could not convert string")
		return Native.toString(buffer)
	}

	// @NlsSafe
//	fun getNSErrorText(error: ID?): String? {
//		if (error == null || error.toInt() == 0) return null
//
//		var description = toStringViaUTF8(invoke(error, "localizedDescription"))
//		val recovery = toStringViaUTF8(invoke(error, "localizedRecoverySuggestion"))
//		if (recovery != null) description += """
//
// 	$recovery
// 	""".trimIndent()
//		return StringUtil.notNullize(description)
//	}

	fun getEncodingName(nsStringEncoding: Long): String? {
		val cfEncoding =
			myFoundationLibrary.CFStringConvertNSStringEncodingToEncoding(nsStringEncoding)
		val pointer = myFoundationLibrary.CFStringConvertEncodingToIANACharSetName(cfEncoding)
		var name = toStringViaUTF8(pointer)
		if ("macintosh" == name) name =
			"MacRoman" // JDK8 does not recognize IANA's "macintosh" alias

		return name
	}

//	fun getEncodingCode(encodingName: String?): Long {
//		if (StringUtil.isEmptyOrSpaces(encodingName)) return -1
//
//		val converted = nsString(encodingName)
//		val cfEncoding = myFoundationLibrary.CFStringConvertIANACharSetNameToEncoding(converted)
//
//		val restored = myFoundationLibrary.CFStringConvertEncodingToIANACharSetName(cfEncoding)
//		if (ID.NIL == restored) return -1
//
//		return convertCFEncodingToNS(cfEncoding)
//	}

	private fun convertCFEncodingToNS(cfEncoding: Long): Long {
		return myFoundationLibrary.CFStringConvertEncodingToNSStringEncoding(cfEncoding) and 0xffffffffffL // trim to C-type limits
	}

	fun cfRetain(id: ID?) {
		myFoundationLibrary.CFRetain(id)
	}

	fun cfRelease(vararg ids: ID?) {
		for (id in ids) {
			if (id != null) {
				myFoundationLibrary.CFRelease(id)
			}
		}
	}

	fun autorelease(id: ID?): ID {
		return invoke(id, "autorelease")
	}

	val isMainThread: Boolean
		get() = invoke("NSThread", "isMainThread").booleanValue()

	private var ourRunnableCallback: Callback? = null
	private val ourMainThreadRunnables: MutableMap<String?, RunnableInfo> = HashMap()
	private var ourCurrentRunnableCount: Long = 0
	private val RUNNABLE_LOCK = Any()

	fun executeOnMainThread(
		withAutoreleasePool: Boolean,
		waitUntilDone: Boolean,
		runnable: Runnable
	) {
		var runnableCountString: String?
		synchronized(RUNNABLE_LOCK) {
			initRunnableSupport()
			runnableCountString = (++ourCurrentRunnableCount).toString()
			ourMainThreadRunnables.put(
				runnableCountString,
				RunnableInfo(runnable, withAutoreleasePool)
			)
		}

		// fixme: Use Grand Central Dispatch instead?
		val ideaRunnable = getObjcClass("IdeaRunnable")
		val runnableObject = invoke(invoke(ideaRunnable, "alloc"), "init")
		val keyObject = invoke(nsString(runnableCountString), "retain")
		invoke(
			runnableObject,
			"performSelectorOnMainThread:withObject:waitUntilDone:",
			createSelector("run:"),
			keyObject,
			waitUntilDone
		)
		invoke(runnableObject, "release")
	}

	/**
	 * Registers idea runnable adapter class in ObjC runtime, if not registered yet.
	 *
	 *
	 * Warning: NOT THREAD-SAFE! Must be called under lock. Danger of segmentation fault.
	 */
	private fun initRunnableSupport() {
		if (ourRunnableCallback == null) {
			val runnableClass = allocateObjcClassPair(getObjcClass("NSObject"), "IdeaRunnable")
			registerObjcClassPair(runnableClass)

			val callback: Callback = object : Callback {
				fun callback(self: ID?, selector: String?, keyObject: ID?) {
					val key = toStringViaUTF8(keyObject)
					invoke(keyObject, "release")

					var info: RunnableInfo?
					synchronized(RUNNABLE_LOCK) {
						info = ourMainThreadRunnables.remove(key)
					}

					if (info == null) {
						return
					}

					var pool: ID? = null
					try {
						if (info!!.myUseAutoreleasePool) {
							pool = invoke("NSAutoreleasePool", "new")
						}

						info!!.myRunnable.run()
					} finally {
						if (pool != null) {
							invoke(pool, "release")
						}
					}
				}
			}
			if (!addMethod(runnableClass, createSelector("run:"), callback, "v@:*")) {
				throw RuntimeException("Unable to add method to objective-c runnableClass class!")
			}
			ourRunnableCallback = callback
		}
	}

	fun fillArray(a: Array<Any>): ID {
		val result = invoke("NSMutableArray", "array")
		for (s in a) {
			invoke(result, "addObject:", convertType(s))
		}

		return result
	}

	fun createDict(keys: Array<String>, values: Array<Any>): ID {
		val nsKeys = invokeVarArg("NSArray", "arrayWithObjects:", *convertTypes(keys.map { it }.toTypedArray()))
		val nsData = invokeVarArg("NSArray", "arrayWithObjects:", *convertTypes(values))
		return invoke("NSDictionary", "dictionaryWithObjects:forKeys:", nsData, nsKeys)
	}

	fun createPointerReference(): PointerType {
		val reference: PointerType = PointerByReference(Memory(Native.POINTER_SIZE.toLong()))
		reference.pointer.clear(Native.POINTER_SIZE.toLong())
		return reference
	}

	fun castPointerToNSError(pointerType: PointerType): ID {
		return ID(pointerType.pointer.getLong(0))
	}

	fun convertTypes(v: Array<Any>): Array<Any?> {
		val result = arrayOfNulls<Any>(v.size + 1)
		for (i in v.indices) {
			result[i] = convertType(v[i])
		}
		result[v.size] = ID.NIL
		return result
	}

	private fun convertType(o: Any): Any {
		return if (o is Pointer || o is ID) {
			o
		} else if (o is String) {
			nsString(o)
		} else {
			throw IllegalArgumentException("Unsupported type! " + o.javaClass)
		}
	}

	private object NSString {
		private val nsStringCls = getObjcClass("NSString")
		private val stringSel = createSelector("string")
		private val allocSel = createSelector("alloc")
		private val autoreleaseSel = createSelector("autorelease")
		private val initWithBytesLengthEncodingSel =
			createSelector("initWithBytes:length:encoding:")
		private val nsEncodingUTF16LE =
			convertCFEncodingToNS(FoundationLibrary.kCFStringEncodingUTF16LE.toLong())

		fun create(s: String): ID {
			// Use a byte[] rather than letting jna do the String -> char* marshalling itself.
			// Turns out about 10% quicker for long strings.
			if (s.isEmpty()) {
				return invoke(nsStringCls, stringSel)
			}

			val utf16Bytes = s.toByteArray(StandardCharsets.UTF_16LE)
			return create(utf16Bytes)
		}

		fun create(cs: CharSequence): ID {
			if (cs is String) {
				return create(cs)
			}
			if (cs.isEmpty()) {
				return invoke(nsStringCls, stringSel)
			}

			val utf16Bytes = StandardCharsets.UTF_16LE.encode(CharBuffer.wrap(cs)).array()
			return create(utf16Bytes)
		}

		private fun create(utf16Bytes: ByteArray): ID {
			val emptyNsString = invoke(nsStringCls, allocSel)
			val initializedNsString = invoke(
				emptyNsString,
				initWithBytesLengthEncodingSel,
				utf16Bytes,
				utf16Bytes.size,
				nsEncodingUTF16LE
			)
			return invoke(initializedNsString, autoreleaseSel)
		}
	}

	internal class RunnableInfo(var myRunnable: Runnable, var myUseAutoreleasePool: Boolean)

	class NSDictionary(private val myDelegate: ID?) {
		fun get(key: ID?): ID {
			return invoke(myDelegate, "objectForKey:", key)
		}

		fun get(key: String?): ID {
			return get(nsString(key))
		}

		fun count(): Int {
			return invoke(myDelegate, "count").toInt()
		}

		fun keys(): NSArray {
			return NSArray(invoke(myDelegate, "allKeys"))
		}

		companion object {
			fun toStringMap(delegate: ID?): Map<String?, String?> {
				val result: MutableMap<String?, String?> = HashMap()
				if (isNil(delegate)) {
					return result
				}

				val dict = NSDictionary(delegate)
				val keys = dict.keys()

				for (i in 0 until keys.count()) {
					val key = toStringViaUTF8(keys.at(i))
					val `val` = toStringViaUTF8(dict.get(key))
					result[key] = `val`
				}

				return result
			}

			fun toStringDictionary(map: Map<String?, String?>): ID {
				val dict = invoke("NSMutableDictionary", "dictionaryWithCapacity:", map.size)
				for ((key, value) in map) {
					invoke(
						dict, "setObject:forKey:", nsString(value), nsString(
							key
						)
					)
				}
				return dict
			}
		}
	}

	class NSArray(private val myDelegate: ID) {
		fun count(): Int {
			return invoke(myDelegate, "count").toInt()
		}

		fun at(index: Int): ID {
			return invoke(myDelegate, "objectAtIndex:", index)
		}

		val list: List<ID>
			get() {
				val result: MutableList<ID> = ArrayList()
				for (i in 0 until count()) {
					result.add(at(i))
				}
				return result
			}
	}

//	class NSData // delegate should not be nil
//		(private val myDelegate: ID) {
//		fun length(): Int {
//			return invoke(myDelegate, "length").toInt()
//		}
//
//		fun bytes(): ByteArray {
//			val data = Pointer(invoke(myDelegate, "bytes").toLong())
//			return data.getByteArray(0, length())
//		}
//
//		fun createImageFromBytes(): Image {
//			return ImageLoader.loadFromBytes(bytes())
//		}
//	}

	class NSAutoreleasePool {
		private val myDelegate =
			invoke(invoke("NSAutoreleasePool", "alloc"), "init")

		fun drain() {
			invoke(myDelegate, "drain")
		}
	}

//	@Structure.FieldOrder("origin", "size")
//	class NSRect(x: Double, y: Double, w: Double, h: Double) : Structure(),
//		Structure.ByValue {
//		var origin: NSPoint = NSPoint(x, y)
//		var size: NSSize = NSSize(w, h)
//	}
//
//	@Structure.FieldOrder("x", "y")
//	class NSPoint @JvmOverloads constructor(x: Double = 0.0, y: Double = 0.0) :
//		Structure(), Structure.ByValue {
//		var x: CoreGraphics.CGFloat = CGFloat(x)
//		var y: CoreGraphics.CGFloat = CGFloat(y)
//	}
//
//	@Structure.FieldOrder("width", "height")
//	class NSSize @JvmOverloads constructor(width: Double = 0.0, height: Double = 0.0) :
//		Structure(), Structure.ByValue {
//		var width: CoreGraphics.CGFloat = CGFloat(width)
//		var height: CoreGraphics.CGFloat = CGFloat(height)
//	}
}
