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
 * under the License.
 */
package no.priv.bang.fildele.web.api.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import no.priv.bang.fildele.services.FildeleService;
import no.priv.bang.fildele.services.beans.LocaleBean;

class LocalizationResourceTest {
    private static final Locale NB_NO = Locale.forLanguageTag("nb-no");

    @Test
    void testDefaultLocale() {
        var fildele = mock(FildeleService.class);
        when(fildele.defaultLocale()).thenReturn(NB_NO);
        var resource = new LocalizationResource();
        resource.fildele = fildele;
        var defaultLocale = resource.defaultLocale();
        assertEquals(NB_NO, defaultLocale);
    }

    @Test
    void testAvailableLocales() {
        var fildele = mock(FildeleService.class);
        when(fildele.defaultLocale()).thenReturn(NB_NO);
        when(fildele.availableLocales()).thenReturn(Arrays.asList(Locale.forLanguageTag("nb-NO"), Locale.UK).stream().map(l -> LocaleBean.with().locale(l).build()).toList());
        var resource = new LocalizationResource();
        resource.fildele = fildele;
        var availableLocales = resource.availableLocales();
        assertThat(availableLocales).isNotEmpty().contains(LocaleBean.with().locale(fildele.defaultLocale()).build());
    }

    @Test
    void testDisplayTextsForDefaultLocale() {
        var fildele = mock(FildeleService.class);
        when(fildele.defaultLocale()).thenReturn(NB_NO);
        var texts = new HashMap<String, String>();
        texts.put("date", "Dato");
        when(fildele.displayTexts(any())).thenReturn(texts);
        var resource = new LocalizationResource();
        resource.fildele = fildele;
        @SuppressWarnings("unchecked")
        var displayTexts = (Map<String, String>)resource.displayTexts(fildele.defaultLocale().toString()).getEntity();
        assertThat(displayTexts).isNotEmpty();
    }

    @Test
    void testDisplayTextsWithUnknownLocale() {
        var fildele = mock(FildeleService.class);
        when(fildele.displayTexts(any())).thenThrow(MissingResourceException.class);
        var resource = new LocalizationResource();
        var logservice = new MockLogService();
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        var response = resource.displayTexts("en_UK");
        assertThat(response).hasFieldOrPropertyWithValue("status", 500);
    }

}
