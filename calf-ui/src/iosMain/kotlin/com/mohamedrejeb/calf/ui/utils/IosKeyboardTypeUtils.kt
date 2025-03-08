package com.mohamedrejeb.calf.ui.utils

import com.mohamedrejeb.calf.ui.uikit.IosKeyboardType
import platform.UIKit.UIKeyboardTypeASCIICapable
import platform.UIKit.UIKeyboardTypeASCIICapableNumberPad
import platform.UIKit.UIKeyboardTypeDecimalPad
import platform.UIKit.UIKeyboardTypeDefault
import platform.UIKit.UIKeyboardTypeEmailAddress
import platform.UIKit.UIKeyboardTypeNamePhonePad
import platform.UIKit.UIKeyboardTypeNumberPad
import platform.UIKit.UIKeyboardTypeNumbersAndPunctuation
import platform.UIKit.UIKeyboardTypePhonePad
import platform.UIKit.UIKeyboardTypeTwitter
import platform.UIKit.UIKeyboardTypeURL
import platform.UIKit.UIKeyboardTypeWebSearch

/**
 * Converts an [IosKeyboardType] to a [platform.UIKit.UIKeyboardType].
 *
 * @return The [platform.UIKit.UIKeyboardType] equivalent of the [IosKeyboardType].
 */
fun IosKeyboardType.toUIKeyboardType(): Long {
    return when (this) {
        IosKeyboardType.Default -> UIKeyboardTypeDefault
        IosKeyboardType.AsciiCapable -> UIKeyboardTypeASCIICapable
        IosKeyboardType.NumbersAndPunctuation -> UIKeyboardTypeNumbersAndPunctuation
        IosKeyboardType.URL -> UIKeyboardTypeURL
        IosKeyboardType.NumberPad -> UIKeyboardTypeNumberPad
        IosKeyboardType.PhonePad -> UIKeyboardTypePhonePad
        IosKeyboardType.NamePhonePad -> UIKeyboardTypeNamePhonePad
        IosKeyboardType.EmailAddress -> UIKeyboardTypeEmailAddress
        IosKeyboardType.DecimalPad -> UIKeyboardTypeDecimalPad
        IosKeyboardType.Twitter -> UIKeyboardTypeTwitter
        IosKeyboardType.WebSearch -> UIKeyboardTypeWebSearch
        IosKeyboardType.AsciiCapableNumberPad -> UIKeyboardTypeASCIICapableNumberPad
    }
}