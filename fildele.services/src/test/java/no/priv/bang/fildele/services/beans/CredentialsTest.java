/*
 * Copyright 2025 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the package.
 */
package no.priv.bang.fildele.services.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CredentialsTest {

    @Test
    void testCreate() {
        var username = "jd";
        var password = "johnniboi";
        var bean = Credentials.with().username(username).password(password).build();
        assertEquals(username, bean.username());
        assertEquals(password, bean.password());
    }

    @Test
    void testNoArgsConstructor() {
        var bean = Credentials.with().build();
        assertNull(bean.username());
        assertNull(bean.password());
    }

    @Test
    void testToString() {
        var bean = Credentials.with().build();
        assertThat(bean.toString()).startsWith("Credentials[");
    }

}
