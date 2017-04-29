package nl.ordina.jtech.http2.java8.client.jetty;

import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.http2.frames.PushPromiseFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

/**
 * export set MAVEN_OPTS=-Xbootclasspath/p:alpn-boot-8.1.2.v20141202.jar; mvn package exec:java -Dexec.mainClass="nl.ordina.jtech.http2.java8.client.jetty.JettyLoLvlHttp2Client"
 */
public class JettyLoLvlHttp2Client {

    public static void main(String[] args) throws Exception {
        HTTP2Client client = new HTTP2Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true);
        client.addBean(sslContextFactory);
        client.start();

        final String host;
        final int port;
        String path = "";
        //host = "webtide.com"; port=443; // works - but only with trustAll=true?
        //host = "http2.golang.org"; port=443; path="gophertiles";// ends with NPE on decrypt..
        //host = "192.168.99.100"; port=4430; path="gophertiles";// ends with NPE on decrypt..
        //host = "localhost"; port=8443; path="examples/servlets/serverpush/simpleimage"; // works - WITH push!
        host = "localhost"; port=8443; path="http2-java8-example-1.0/push"; // works - WITH push!

        // plaintext (http:// !) HTTP/1.x connection to Tomcat fails server side with:
        //   org.apache.coyote.http11.Http11Processor.service Error parsing HTTP request header
        //   java.lang.IllegalArgumentException: Invalid character found in method name. HTTP method names must be tokens
        // client side Exc is simply java.util.concurrent.TimeoutException
        //host = "localhost"; port=8080; path="http2-java8-example-1.0/push";

        final String uri = "https://" + host + ":" + port + "/" + path;

        FuturePromise<Session> sessionPromise = new FuturePromise<>();
        client.connect(sslContextFactory, new InetSocketAddress(host, port), new ServerSessionListener.Adapter(), sessionPromise);
        Session session = sessionPromise.get(5, TimeUnit.SECONDS);

        HttpFields requestFields = new HttpFields();
        // both these headers are optional
        requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);
        requestFields.put("Cookie", "JSESSIONID=E8616856671FE07B7098BE5FEA105DE6;path=/http2-java8-example-1.0;Secure;HttpOnly");

        MetaData.Request metaData = new MetaData.Request("GET", new HttpURI(uri), HttpVersion.HTTP_2, requestFields);
        HeadersFrame headersFrame = new HeadersFrame(metaData, null, true);
        final Phaser phaser = new Phaser(2);
        session.newStream(headersFrame, new Promise.Adapter<>(), new Stream.Listener.Adapter() {
            @Override
            public void onHeaders(Stream stream, HeadersFrame frame) {
                System.err.println("header: " + frame);
                reportFrameMetaData(frame.getMetaData());
                if (frame.isEndStream())
                    phaser.arrive();
            }

            @Override
            public void onData(Stream stream, DataFrame frame, Callback callback) {
                System.err.println(frame);
                callback.succeeded();
                if (frame.isEndStream())
                    phaser.arrive();
            }

            @Override
            public Stream.Listener onPush(Stream stream, PushPromiseFrame frame) {
                System.err.println(frame);
                final MetaData metaData1 = frame.getMetaData();
                reportFrameMetaData(metaData1);
                System.err.println("  push frame content length: " + metaData1.getContentLength());
                phaser.register();
                return this;
            }

            private void reportFrameMetaData(final MetaData metaData) {
                if (metaData instanceof MetaData.Request) {
                    final MetaData.Request metaDataRequest = (MetaData.Request) metaData;
                    System.err.println("  request: " + metaDataRequest.getMethod() + " " + metaDataRequest.getURI());
                } else if (metaData instanceof MetaData.Response) {
                    final MetaData.Response metaDataRequest = (MetaData.Response) metaData;
                    System.err.println("  response: " + metaDataRequest.getStatus() + " " + metaDataRequest.getReason());
                }
                for (final HttpField httpField : metaData.getFields()) {
                    System.err.println("  header field: " + httpField.getName() + "=" + Arrays.toString(httpField.getValues()));
                    if (httpField.getHeader() != null) {
                        System.err.println("    parsed header: " + httpField.getHeader());
                    }
                }
            }
        });

        phaser.awaitAdvanceInterruptibly(phaser.arrive(), 5, TimeUnit.SECONDS);

        client.stop();
    }
}
