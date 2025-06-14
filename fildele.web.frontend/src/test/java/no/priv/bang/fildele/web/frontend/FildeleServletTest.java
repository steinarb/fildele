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
package no.priv.bang.fildele.web.frontend;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mockrunner.mock.web.MockHttpServletResponse;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

class FildeleServletTest {

    @Test
    void testGet() throws Exception {
        var logservice = new MockLogService();
        var servlet = new FildeleServlet();
        var servletConfig = mock(ServletConfig.class);
        when(servletConfig.getInitParameter("from")).thenReturn("to");
        servlet.init(servletConfig);
        servlet.setLogService(logservice);
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/fildele/useradmin/");
        when(request.getPathInfo()).thenReturn("/");
        var response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertEquals("text/html", response.getContentType());
        assertEquals(200, response.getStatus());
        assertThat(response.getBufferSize()).isPositive();
    }


    @Test
    void testDoGetAddTrailingSlash() throws Exception {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8181/fildele/useradmin"));
        when(request.getServletPath()).thenReturn("/frontend-karaf-demo");
        var response = new MockHttpServletResponse();

        var servlet = new FildeleServlet();
        servlet.setLogService(logservice);

        servlet.service(request, response);

        assertEquals(302, response.getStatus());
    }

    @Test
    void testDoGetResponseThrowsIOException() throws Exception {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/fildele/useradmin/");
        when(request.getPathInfo()).thenReturn("/");
        var response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);
        response.resetAll();
        var streamThrowingIOException = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(streamThrowingIOException).write(anyInt());
        when(response.getOutputStream()).thenReturn(streamThrowingIOException);

        var servlet = new FildeleServlet();
        servlet.setLogService(logservice);

        servlet.service(request, response);

        assertEquals(500, response.getStatus());
    }

    @Test
    void testDoGetResponseStreamMethodThrowsIOException() throws Exception {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/fildele/useradmin/");
        when(request.getPathInfo()).thenReturn("/");
        var response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);
        response.resetAll();
        when(response.getOutputStream()).thenThrow(IOException.class);

        var servlet = new FildeleServlet();
        servlet.setLogService(logservice);

        servlet.service(request, response);

        assertEquals(500, response.getStatus());
    }

    @Test
    void testDoGetResourceNotFound() throws Exception {
        var logservice = new MockLogService();
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("http://localhost:8181/fildele/useradmin/static/nosuchname.png");
        when(request.getPathInfo()).thenReturn("/static/nosuchname.png");
        var response = new MockHttpServletResponse();

        var servlet = new FildeleServlet();
        servlet.setLogService(logservice);

        servlet.service(request, response);

        assertEquals(404, response.getErrorCode());
    }

}
