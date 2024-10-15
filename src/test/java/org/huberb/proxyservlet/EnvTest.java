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

import org.huberb.proxyservlet.Env.Builder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pi
 */
public class EnvTest {

    @Test
    public void testGetTargetUri() {
        Env instance = new Builder().targetUri("http://localhost:8080/a").build();
        assertEquals("http://localhost:8080/a", instance.getTargetUri());
    }

    @Test
    public void testGetTargetHost() {
        Env instance = new Builder().targetUri("http://localhost:8080/a").build();
        assertEquals("localhost", instance.getTargetHost().getHostName());
        assertEquals(8080, instance.getTargetHost().getPort());
    }

}
