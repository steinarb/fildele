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
package no.priv.bang.fildele.web.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

public class ShiroTestBase {

    private static WebSecurityManager securitymanager;
    private static SimpleAccountRealm realm;

    public ShiroTestBase() {
        super();
    }

    protected void loginUser(String username, String password) {
        var session = mock(HttpSession.class);
        var dummyrequest = new MockHttpServletRequest();
        dummyrequest.setSession(session);
        var dummyresponse = new MockHttpServletResponse();
        loginUser(dummyrequest, dummyresponse, username, password);
    }

    protected void loginUser(HttpServletRequest request, HttpServletResponse response, String username, String password) {
        var subject = createSubjectAndBindItToThread(request, response);
        var token = new UsernamePasswordToken(username, password.toCharArray(), true);
        subject.login(token);
    }

    protected void removeWebSubjectFromThread() {
        ThreadContext.remove(ThreadContext.SUBJECT_KEY);
    }

    protected WebSubject createSubjectWithNullPrincipalAndBindItToThread() {
        var subject = mock(WebSubject.class);
        ThreadContext.bind(subject);
        return subject;
    }

    protected WebSubject createSubjectAndBindItToThread() {
        var originalRequest = new MockHttpServletRequest();
        return createSubjectFromOriginalRequestAndBindItToThread(originalRequest);
    }

    protected WebSubject createSubjectFromOriginalRequestAndBindItToThread(MockHttpServletRequest originalRequest) {
        var session = new MockHttpSession();
        originalRequest.setSession(session);
        var dummyresponse = new MockHttpServletResponse();
        return createSubjectAndBindItToThread(originalRequest, dummyresponse);
    }

    protected WebSubject createSubjectAndBindItToThread(HttpServletRequest request, HttpServletResponse response) {
        var subject = new WebSubject.Builder(getSecurityManager(), request, response).buildWebSubject();
        ThreadContext.bind(subject);
        return subject;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected WebSubject createSubjectThrowingExceptionAndBindItToThread(Class exceptionClass) {
        var subject = mock(WebSubject.class);
        doThrow(exceptionClass).when(subject).login(any());
        ThreadContext.bind(subject);
        return subject;
    }

    public static WebSecurityManager getSecurityManager() {
        if (securitymanager == null) {
            var env = new IniWebEnvironment();
            env.setConfigLocations("classpath:test.shiro.ini");
            env.init();
            securitymanager = env.getWebSecurityManager();
            realm = findRealmFromSecurityManager(securitymanager);
        }

        return securitymanager;
    }

    private static SimpleAccountRealm findRealmFromSecurityManager(WebSecurityManager securitymanager) {
        var realmSecurityManager = (RealmSecurityManager) securitymanager;
        var realms = realmSecurityManager.getRealms();
        return (SimpleAccountRealm) realms.iterator().next();
    }

    public static SimpleAccount getShiroAccountFromRealm(String username) {
        if (realm == null) {
            getSecurityManager();
        }

        return findUserFromRealm(realm, username);
    }

    private static SimpleAccount findUserFromRealm(SimpleAccountRealm realm, String username) {
        try {
            var getUserMethod = SimpleAccountRealm.class.getDeclaredMethod("getUser", String.class);
            getUserMethod.setAccessible(true);
            return (SimpleAccount) getUserMethod.invoke(realm, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
