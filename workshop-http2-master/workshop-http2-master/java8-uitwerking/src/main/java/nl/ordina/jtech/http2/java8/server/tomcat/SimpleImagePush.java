/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package nl.ordina.jtech.http2.java8.server.tomcat;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimpleImagePush extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("request path: " + req.getContextPath() +
                " >> " + req.getServletPath() +
                " >> " + req.getPathTranslated());

        if (req.getPathTranslated() != null && req.getPathTranslated().contains("dynamic-image")) {
            handleDynamicImage(req, resp);
        }

        final HttpSession session = req.getSession(true);
        System.out.println(
                " (possibly new) sessionid: " + session.getId() +
                ", requested sessionid: " + req.getRequestedSessionId() +
                ", from cookie: " + req.isRequestedSessionIdFromCookie() +
                ", valid: " + req.isRequestedSessionIdValid());

        /*
         * Result:
         * GET https://localhost:8443/http2-java8-example-1.0/return.gif?answer=42
         *  header: x-my-header=[bar]
         *  header: x-my-header-1=[foo]
         *  header: x-my-header-1=[zaphod]
         */
        // Tomcat impl: http://svn.apache.org/viewvc/tomcat/tc9.0.x/branches/gsoc-jaspic/java/org/apache/catalina/core/ApplicationPushBuilder.java?view=markup
        PushBuilder pb = req.getPushBuilder()
                .path("return.gif") // path is the only required value

                // note: the browser does not show these headers - only the ones delivered in the pushed resource itself
                .setHeader("x-my-header", "overwritten by subsequent setHeader")
                .setHeader("x-my-header", "bar")
                .addHeader("x-my-header-1", "foo")
                .addHeader("x-my-header-1", "zaphod") // note: had expected this to be reported as x-my-header-1=[foo,zaphod] ?

                // GET is default
                // ?! "IllegalArgumentException - if the method set expects a request body (eg POST)"; does not happen; Tomcat does not enforce it!
                .method("POST")

                .queryString("answer=42")

                //.sessionId("some-session-id") // dropped?! "pushed request will include the session ID either as a Cookie or as a URI parameter"
                .sessionId(session.getId())

                ;
        final boolean pushResult;
        try {
            //pb.push(); // results in 'java.lang.NoSuchMethodError: javax.servlet.http.PushBuilder.push()V'
            // - Tomcat's Servlet 4.0 API version return type is boolean, not void!
            final Method push = pb.getClass().getMethod("push");
            pushResult = (boolean) push.invoke(pb);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                respondWith(resp, "<p>The following image was NOT provided via a push request! " +
                        "Cannot push over plain HTTP/1.x.</p>" +
                        "<img src=\"" + req.getContextPath() + "/return.gif\"/>");
                return;
            }
            respondWith(resp, e.getClass().getName() + ": " + e.getMessage() + ", cause: " + e.getCause());
            return;
        }

        simplePush(req, "Chrome Pony.png");
        simplePush(req, "second.html");

        respondWith(resp,
                "<p>The following static image was provided via a push request with result "+pushResult+"</p>" +
                    "<img src=\"" + req.getContextPath() + "/return.gif\"/><br/>" +
                "<p>Dynamic push request: </p><img src=\"push/dynamic-image\"/><br/>" +
                "<p><a href=\"second.html\">Link naar gepushte pagina</a></p>");
    }

    private void simplePush(final HttpServletRequest req, final String path) {
        final PushBuilder pb = req.getPushBuilder().path(path);
        try {
            pb.getClass().getMethod("push").invoke(pb);
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // ignore
        }
    }

    private void handleDynamicImage(final HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("first we wait 5s");
        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            // ignore
        }
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Firefox Pony.png");
        IOUtils.copy(inputStream,resp.getOutputStream());
    }

    private void respondWith(final HttpServletResponse resp, final String body) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html");
        PrintWriter pw = resp.getWriter();
        pw.println("<html>");
        pw.println("<body>");
        pw.println(body);
        pw.println("</body>");
        pw.println("</html>");
        pw.flush();
    }
}
