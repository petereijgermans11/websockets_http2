package nl.ordina.jtech.http2.java8.client.jetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * export set MAVEN_OPTS=-Xbootclasspath/p:alpn-boot-8.1.2.v20141202.jar; mvn package exec:java -Dexec.mainClass="nl.ordina.jtech.http2.java8.client.jetty.JettyHiLvlHttp2Client"
 */
public class JettyHiLvlHttp2Client {
    public static void main(String[] args) throws Exception {
        HttpClientTransportOverHTTP2 clientTransport = new HttpClientTransportOverHTTP2(new HTTP2Client());
        HttpClient client = new HttpClient(clientTransport, new SslContextFactory(true));
        client.start();

        final String uri =
                //"https://http2.akamai.com" // works - "You are using HTTP/2 right now!"
                //"https://192.168.99.100:4430/gophertiles" // fails with EofException
                //"https://localhost:8443/examples/servlets/serverpush/simpleimage" // works but no push callback in this API
                "https://localhost:8443/http2-java8-example-1.0/push" // works but no push callback in this API
                //"http://localhost:8080/http2-java8-example-1.0/push" // fails with java.nio.channels.ClosedChannelException
                ;

        ContentResponse response = client.GET(uri);
        System.out.println("response: " + response);
        System.out.println("response content: " + response.getContentAsString());
    }
}
