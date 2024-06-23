// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
package jodd.util

/**
 * Checks whether a string or path matches a given wildcard pattern.
 * Possible patterns allow to match single characters ('?') or any count of
 * characters ('*'). Wildcard characters can be escaped (by an '\').
 * When matching path, deep tree wildcard also can be used ('**').
 *
 *
 * This method uses recursive matching, as in linux or windows. regexp works the same.
 * This method is very fast, comparing to similar implementations.
 */
object Wildcard {
    /**
     * Checks whether a string matches a given wildcard pattern.
     *
     * @param string    input string
     * @param pattern    pattern to match
     * @return            `true` if string matches the pattern, otherwise `false`
     */
    fun match(string: CharSequence, pattern: CharSequence): Boolean {
        return match(string, pattern, 0, 0)
    }

    /**
     * Internal matching recursive function.
     */
    private fun match(string: CharSequence, pattern: CharSequence, sNdx: Int, pNdx: Int): Boolean {
        var sNdx = sNdx
        var pNdx = pNdx
        val pLen = pattern.length
        if (pLen == 1) {
            if (pattern[0] == '*') {     // speed-up
                return true
            }
        }
        val sLen = string.length
        var nextIsNotWildcard = false

        while (true) {
            // check if end of string and/or pattern occurred

            if ((sNdx >= sLen)) {        // end of string still may have pending '*' in pattern
                while ((pNdx < pLen) && (pattern[pNdx] == '*')) {
                    pNdx++
                }
                return pNdx >= pLen
            }
            if (pNdx >= pLen) {                    // end of pattern, but not end of the string
                return false
            }
            val p = pattern[pNdx] // pattern char

            // perform logic
            if (!nextIsNotWildcard) {
                if (p == '\\') {
                    pNdx++
                    nextIsNotWildcard = true
                    continue
                }
                if (p == '?') {
                    sNdx++
                    pNdx++
                    continue
                }
                if (p == '*') {
                    var pNext = 0.toChar() // next pattern char
                    if (pNdx + 1 < pLen) {
                        pNext = pattern[pNdx + 1]
                    }
                    if (pNext == '*') {                    // double '*' have the same effect as one '*'
                        pNdx++
                        continue
                    }
                    pNdx++

                    // find recursively if there is any substring from the end of the
                    // line that matches the rest of the pattern !!!
                    var i = string.length
                    while (i >= sNdx) {
                        if (match(string, pattern, i, pNdx)) {
                            return true
                        }
                        i--
                    }
                    return false
                }
            } else {
                nextIsNotWildcard = false
            }

            // check if pattern char and string char are equals
            if (p != string[sNdx]) {
                return false
            }

            // everything matches for now, continue
            sNdx++
            pNdx++
        }
    }


    // ---------------------------------------------------------------- utilities
    /**
     * Matches string to at least one pattern.
     * Returns index of matched pattern, or `-1` otherwise.
     * @see .match
     */
    fun matchOne(src: String, vararg patterns: String): Int {
        for (i in patterns.indices) {
            if (match(src, patterns[i])) {
                return i
            }
        }
        return -1
    }
}