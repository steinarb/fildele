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

import java.util.Base64;

import javax.ws.rs.InternalServerErrorException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.util.WebUtils;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;

import no.priv.bang.fildele.backend.fildeleServiceProvider;
import no.priv.bang.fildele.services.FildeleService;
import no.priv.bang.fildele.services.beans.Credentials;
import no.priv.bang.fildele.web.api.ShiroTestBase;
import no.priv.bang.authservice.definitions.AuthserviceException;
import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import no.priv.bang.osgiservice.users.User;
import no.priv.bang.osgiservice.users.UserManagementService;

class LoginResourceTest extends ShiroTestBase {

    @Test
    void testLogin() {
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("johnnyBoi".getBytes());
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        when(useradmin.getUser(anyString())).thenReturn(User.with().username(username).build());
        var logservice = new MockLogService();
        var httpRequest = new MockHttpServletRequest().setRequestURI("/fildele/");
        var webcontext = new MockServletContext();
        webcontext.setContextPath("/fildele");
        var resource = new LoginResource();
        resource.webcontext = webcontext;
        resource.request = httpRequest;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        createSubjectAndBindItToThread();
        WebUtils.saveRequest(httpRequest);
        var locale = "nb_NO";
        var credentials = Credentials.with().username(username).password(password).build();
        var resultat = resource.login(locale, credentials);
        assertTrue(resultat.success());
        assertEquals(username, resultat.user().username());
        assertEquals("/", resultat.originalRequestUrl());
    }

    @Test
    void testLoginDifferentPath() {
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("johnnyBoi".getBytes());
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        when(useradmin.getUser(anyString())).thenReturn(User.with().username(username).build());
        var logservice = new MockLogService();
        var httpRequest = new MockHttpServletRequest().setRequestURI("/fildele/counter");
        var webcontext = new MockServletContext();
        webcontext.setContextPath("/fildele");
        var resource = new LoginResource();
        resource.webcontext = webcontext;
        resource.request = httpRequest;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        createSubjectAndBindItToThread();
        WebUtils.saveRequest(httpRequest);
        var locale = "nb_NO";
        var credentials = Credentials.with().username(username).password(password).build();
        var resultat = resource.login(locale, credentials);
        assertTrue(resultat.success());
        assertEquals(username, resultat.user().username());
        assertEquals("/counter", resultat.originalRequestUrl());
    }

    @Test
    void testLoginWithEmptyWebContextPath() {
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("johnnyBoi".getBytes());
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        when(useradmin.getUser(anyString())).thenReturn(User.with().username(username).build());
        var logservice = new MockLogService();
        var httpRequest = new MockHttpServletRequest().setRequestURI("/");
        var webcontext = new MockServletContext();
        webcontext.setContextPath("");
        var resource = new LoginResource();
        resource.webcontext = webcontext;
        resource.request = httpRequest;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        createSubjectAndBindItToThread();
        WebUtils.saveRequest(httpRequest);
        var locale = "nb_NO";
        var credentials = Credentials.with().username(username).password(password).build();
        var resultat = resource.login(locale, credentials);
        assertTrue(resultat.success());
        assertEquals(username, resultat.user().username());
        assertEquals("/", resultat.originalRequestUrl());
    }

    @Test
    void testLoginNoWebContext() {
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("johnnyBoi".getBytes());
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        when(useradmin.getUser(anyString())).thenReturn(User.with().username(username).build());
        var logservice = new MockLogService();
        var httpRequest = new MockHttpServletRequest().setRequestURI("/fildele/");
        var resource = new LoginResource();
        resource.request = httpRequest;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        createSubjectAndBindItToThread();
        WebUtils.saveRequest(httpRequest);
        var locale = "nb_NO";
        var credentials = Credentials.with().username(username).password(password).build();
        assertThrows(InternalServerErrorException.class, () -> resource.login(locale, credentials));
        assertThat(logservice.getLogmessages()).hasSize(1);
        assertThat(logservice.getLogmessages().get(0)).contains("NullPointerException");
    }

    @Test
    void testLoginByUserWithoutRole() {
        var request = new MockHttpServletRequest();
        var logservice = new MockLogService();
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        var webcontext = new MockServletContext();
        webcontext.setContextPath("/fildele");
        var resource = new LoginResource();
        resource.webcontext = webcontext;
        resource.request = request;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("johnnyBoi".getBytes());
        createSubjectAndBindItToThread();
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertTrue(result.success());
        assertFalse(result.authorized());
    }

    @Test
    void testLoginWithOriginalRequestUrl() {
        var request = new MockHttpServletRequest()
            .setContextPath("/fildele");
        var logservice = new MockLogService();
        var fildele = mock(FildeleService.class);
        var useradmin = mock(UserManagementService.class);
        var webcontext = new MockServletContext();
        webcontext.setContextPath("/fildele");
        var resource = new LoginResource();
        resource.webcontext = webcontext;
        resource.request = request;
        resource.setLogservice(logservice);
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        var username = "jad";
        var password = Base64.getEncoder().encodeToString("1ad".getBytes());
        var originalRequest = new MockHttpServletRequest();
        originalRequest.setRequestURI("/fildele/");
        createSubjectFromOriginalRequestAndBindItToThread(originalRequest);
        WebUtils.saveRequest(originalRequest);
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertTrue(result.success());
        assertTrue(result.authorized());
        assertEquals("/", result.originalRequestUrl());
    }

    @Test
    void testLoginWrongPassword() {
        var fildele = new fildeleServiceProvider();
        var logservice = new MockLogService();
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.setLogservice(logservice);
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("feil".getBytes());
        createSubjectAndBindItToThread();
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertFalse(result.success());
        assertThat(result.errormessage()).startsWith("Feil passord");
    }

    @Test
    void testLoginWhenMaxFailedLoginLimitReached() {
        var fildele = new fildeleServiceProvider();
        var logservice = new MockLogService();
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.setLogservice(logservice);
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("feil".getBytes());
        createSubjectThrowingExceptionAndBindItToThread(ExcessiveAttemptsException.class);
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertFalse(result.success());
        assertThat(result.errormessage()).startsWith("Maks antall feilede innlogginger passert. Kontoen vil bli låst. Kontakt systemadministrator");
    }

    @Test
    void testLoginLockedAccount() {
        var fildele = new fildeleServiceProvider();
        var logservice = new MockLogService();
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.setLogservice(logservice);
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("feil".getBytes());
        createSubjectThrowingExceptionAndBindItToThread(LockedAccountException.class);
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertFalse(result.success());
        assertThat(result.errormessage()).startsWith("Låst konto");
    }

    @Test
    void testLoginOtherAuthenticationError() {
        var fildele = new fildeleServiceProvider();
        var logservice = new MockLogService();
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.setLogservice(logservice);
        var username = "jd";
        var password = Base64.getEncoder().encodeToString("feil".getBytes());
        createSubjectThrowingExceptionAndBindItToThread(AuthenticationException.class);
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertFalse(result.success());
        assertThat(result.errormessage()).startsWith("Ukjent feil");
    }

    @Test
    void testLoginUnkownUsername() {
        var fildele = new fildeleServiceProvider();
        var logservice = new MockLogService();
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.setLogservice(logservice);
        var username = "jdd";
        var password = Base64.getEncoder().encodeToString("feil".getBytes());
        createSubjectAndBindItToThread();
        var credentials = Credentials.with().username(username).password(password).build();
        var locale = "nb_NO";
        var result = resource.login(locale, credentials);
        assertThat(result.errormessage()).startsWith("Ukjent konto");
    }

    @Test
    void testFindUserSafelyWithUnknownUsername() {
        var useradmin = mock(UserManagementService.class);
        when(useradmin.getUser(anyString())).thenThrow(AuthserviceException.class);
        var resource = new LoginResource();
        resource.useradmin = useradmin;
        var user = resource.findUserSafely("null");
        assertNull(user.username());
    }

    @Test
    void testLogout() {
        var locale = "nb_NO";
        var fildele = new fildeleServiceProvider();
        var resource = new LoginResource();
        resource.fildele = fildele;
        var username = "jd";
        var password = "johnnyBoi";
        var subject = createSubjectAndBindItToThread();
        var token = new UsernamePasswordToken(username, password.toCharArray(), true);
        subject.login(token);
        assertTrue(subject.isAuthenticated()); // Verify precondition user logged in

        var loginresult = resource.logout(locale);
        assertFalse(loginresult.success());
        assertEquals("Logget ut", loginresult.errormessage());
        assertFalse(loginresult.authorized());
        assertFalse(subject.isAuthenticated()); // Verify user has been logged out
    }

    @Test
    void testGetLoginstateWhenLoggedIn() {
        var locale = "nb_NO";
        var fildele = new fildeleServiceProvider();
        var useradmin = mock(UserManagementService.class);
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        var username = "jad";
        var password = "1ad";
        var subject = createSubjectAndBindItToThread();
        var token = new UsernamePasswordToken(username, password.toCharArray(), true);
        subject.login(token);

        var loginresult = resource.loginstate(locale);
        assertTrue(loginresult.success());
        assertEquals("Bruker er logget inn og har tilgang", loginresult.errormessage());
        assertTrue(loginresult.authorized());
    }

    @Test
    void testGetLoginstateWhenLoggedInButUserDoesntHaveRoleFildeleuser() {
        var locale = "nb_NO";
        var fildele = new fildeleServiceProvider();
        var useradmin = mock(UserManagementService.class);
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        var username = "jd";
        var password = "johnnyBoi";
        var subject = createSubjectAndBindItToThread();
        var token = new UsernamePasswordToken(username, password.toCharArray(), true);
        subject.login(token);

        var loginresult = resource.loginstate(locale);
        assertTrue(loginresult.success());
        assertEquals("Bruker er logget inn men mangler tilgang", loginresult.errormessage());
        assertFalse(loginresult.authorized());
    }

    @Test
    void testGetLoginstateWhenNotLoggedIn() {
        var locale = "nb_NO";
        var fildele = new fildeleServiceProvider();
        var useradmin = mock(UserManagementService.class);
        var resource = new LoginResource();
        resource.fildele = fildele;
        resource.useradmin = useradmin;
        createSubjectAndBindItToThread();

        var loginresult = resource.loginstate(locale);
        assertFalse(loginresult.success());
        assertEquals("Bruker er ikke logget inn", loginresult.errormessage());
        assertFalse(loginresult.authorized());
    }

}
