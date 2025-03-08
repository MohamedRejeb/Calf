package com.mohamedrejeb.calf.ui.uikit

/**
 * Represents the type of the keyboard of a text field in an iOS alert dialog.
 *
 * See [UIKeyboardType](https://developer.apple.com/documentation/uikit/uikeyboardtype?language=objc)
 */
enum class IosKeyboardType {
    /**
     * Specifies the default keyboard for the current input method.
     */
    Default,

    /**
     * Specifies a keyboard that displays standard ASCII characters.
     */
    AsciiCapable,

    /**
     * Specifies a keyboard that displays numbers and punctuation.
     */
    NumbersAndPunctuation,

    /**
     * Specifies a keyboard for URL entry.
     */
    URL,

    /**
     * Specifies a numeric keypad for PIN entry.
     */
    NumberPad,

    /**
     * Specifies a keypad for entering telephone numbers.
     */
    PhonePad,

    /**
     * Specifies a keypad for entering a person’s name or phone number.
     */
    NamePhonePad,

    /**
     * Specifies a keyboard for entering email addresses.
     */
    EmailAddress,

    /**
     * Specifies a keyboard with numbers and a decimal point.
     */
    DecimalPad,

    /**
     * Specifies a keyboard for Twitter text entry, with easy access to the at (”@”) and hash (”#”) characters.
     */
    Twitter,

    /**
     * Specifies a keyboard for web search terms and URL entry.
     */
    WebSearch,

    /**
     * Specifies a number pad that outputs only ASCII digits.
     */
    AsciiCapableNumberPad,
    ;
}