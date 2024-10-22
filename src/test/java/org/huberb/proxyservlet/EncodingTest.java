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

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author pi
 */
public class EncodingTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "ABCDEFGHIJKLMNOPQRSTUVWYZ",
        "abcdefghijklmnopqrstuvwyz",
        "0123456789"
    })
    /*default*/ void testEncodeUriQuery_non_escaping(String s) {
        assertEquals(s, Encoding.encodeUriQuery(s, false).toString());
        assertEquals(s, Encoding.encodeUriQuery(s, true).toString());
    }

    @ParameterizedTest
    @MethodSource()
    /*default*/ void testEncodeUriQuery_escape(String expected, String arg) {
        assertEquals(expected, Encoding.encodeUriQuery(arg, false).toString());
    }

    static Stream<Arguments> testEncodeUriQuery_escape() {
        return Stream.of(
                Arguments.of("%200%201%202%203%204%205%206%207%208%209%20", " 0 1 2 3 4 5 6 7 8 9 "),
                Arguments.of("ABCDEFGHIJKLMNOPQRSTUVWXYZ%20abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz"),
                Arguments.of("AAA%20BBB", "AAA BBB"),
                Arguments.of("AAA!%22$%&/()=%5B%5DBBB", "AAA!\"$%&/()=[]BBB"),
                Arguments.of("AAA%3C%3E%7C,.-_:;%23'+*+'%60BBB", "AAA<>|,.-_:;#'+*+'`BBB"),
                Arguments.of("AAA/BBB", "AAA/BBB"),
                Arguments.of("AAA-BBB", "AAA-BBB")
        );
    }

}
