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

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;

import no.priv.bang.fildele.services.FildeleService;
import no.priv.bang.fildele.services.beans.LocaleBean;

@Path("")
public class LocalizationResource extends ResourceBase {

    @Inject
    FildeleService fildele;

    Logger logger;

    @Inject
    void setLogservice(LogService logservice) {
        this.logger = logservice.getLogger(getClass());
    }

    @GET
    @Path("defaultlocale")
    @Produces(MediaType.APPLICATION_JSON)
    public Locale defaultLocale() {
        return fildele.defaultLocale();
    }

    @GET
    @Path("availablelocales")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LocaleBean> availableLocales() {
        return fildele.availableLocales();
    }

    @GET
    @Path("displaytexts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response displayTexts(@QueryParam("locale")String locale) {
        try {
            return Response.ok(fildele.displayTexts(Locale.forLanguageTag(locale.replace('_', '-')))).build();
        } catch (MissingResourceException e) {
            var message = String.format("Unknown locale '%s' used when fetching GUI texts", locale);
            logger.error(message);
            return response(500, message);
        }
    }

}
