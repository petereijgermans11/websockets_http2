package nl.ordina.jtech.http2.java8.client.jetty;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * mvn package exec:java -Dexec.mainClass="nl.ordina.jtech.http2.java8.client.jetty.JettyHiLvlHttp1Client"
 */
public class JettyHiLvlHttp1Client {
    public static void main(String[] args) throws Exception {
        HttpClient client = new HttpClient(new SslContextFactory(true));
        client.start();

        final String uri = args.length > 0 ? args[0] :
                //"https://http2.akamai.com" // reports correctly that we use HTTP/1
                //"https://192.168.99.100:4430/gophertiles" // works - "you're <b>not</b> using HTTP/2 right now"
                //"https://localhost:8443/examples/servlets/serverpush/simpleimage" // works - "Server push requests are not supported by this protocol"
                "http://localhost:8080/http2-java8-example-1.0/push" // works - "image was NOT provided via a push request!"
                ;

        ContentResponse response = client.GET(uri);
        System.out.println("response: " + response);
        System.out.println("response content: " + response.getContentAsString());
        client.stop();
    }
}
