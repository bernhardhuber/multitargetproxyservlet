/*
 * Copyright 2024 pi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.proxyservlet;

import java.net.URI;
import java.util.BitSet;
import java.util.Formatter;

/**
 * Encodes characters in the query or fragment part of the URI.
 *
 * @author pi
 */
class Encoding {

    private static final BitSet asciiQueryChars;

    static {
        /*
         * Plus alphanum
         * Plus punct.  
         * Exclude '?'; RFC-2616 3.2.2. 
         * Exclude '[', ']'; https://www.ietf.org/rfc/rfc1738.txt, 
         * unsafe characters
         */
        char[] cUnreserved = "_-!.~'()*".toCharArray();
        char[] cPunct = ",;:$&+=".toCharArray();
        char[] cReserved = "/@".toCharArray();

        asciiQueryChars = new BitSet(128);
        for (char c = 'a'; c <= 'z'; c++) {
            asciiQueryChars.set(c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            asciiQueryChars.set(c);
        }
        for (char c = '0'; c <= '9'; c++) {
            asciiQueryChars.set(c);
        }
        for (char c : cUnreserved) {
            asciiQueryChars.set(c);
        }
        for (char c : cPunct) {
            asciiQueryChars.set(c);
        }
        for (char c : cReserved) {
            asciiQueryChars.set(c);
        }
        asciiQueryChars.set('%'); //leave existing percent escapes in place
    }

    private Encoding() {
    }

    /**
     * Encodes characters in the query or fragment part of the URI.
     *
     * <p>
     * Unfortunately, an incoming URI sometimes has characters disallowed by the
     * spec. HttpClient insists that the outgoing proxied request has a valid
     * URI because it uses Java's {@link URI}. To be more forgiving, we must
     * escape the problematic characters. See the URI class for the spec.
     *
     * @param in example: name=value&amp;foo=bar#fragment
     * @param encodePercent determine whether percent characters need to be
     * encoded
     * @return
     */
    static CharSequence encodeUriQuery(CharSequence in, boolean encodePercent) {
        //Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get(c) && !(encodePercent && c == '%')) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {
                //not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null) {
                    outBuf.append(c);
                }
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5 * 3);
                    outBuf.append(in, 0, i);
                    formatter = new Formatter(outBuf);
                }
                //leading %, 0 padded, width 2, capital hex
                formatter.format("%%%02X", (int) c); //TODO
            }
        }
        return outBuf != null ? outBuf : in;
    }

}
