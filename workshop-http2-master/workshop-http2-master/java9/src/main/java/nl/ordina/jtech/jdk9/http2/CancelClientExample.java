package nl.ordina.jtech.jdk9.http2;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import nl.ordina.jtech.jdk9.http2.util.SSLContextCreator;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;

//import jdk.incubator.http.HttpClient.Version;
 
public class CancelClientExample {
	private static final Logger LOG = Logger.getLogger(CancelClientExample.class.getName());

	public static void main(String[] args) {
		CancelClientExample client = new CancelClientExample();
		try {
			client.send();
		} catch (InterruptedException | ExecutionException | URISyntaxException e) {
			LOG.log(Level.SEVERE, e.getClass().getSimpleName() + ": " + e.getMessage() );
		} catch (CancellationException e) {
			LOG.log(Level.SEVERE, "The request has been cancelled: " + e.getClass().getSimpleName());
			System.exit(0);
		}
	}

	public HttpResponse send() throws InterruptedException, ExecutionException, URISyntaxException {
		SSLContext context = SSLContextCreator.getContextInstance();
       HttpClient client = HttpClient
				              .newBuilder().sslContext(context)
				              .build();

		HttpRequest request = HttpRequest.newBuilder(new URI("https://localhost:8443"))
				//.body(HttpRequest.noBody())
				//.version(Version.HTTP_2)
				.GET().build();
		CompletableFuture<HttpResponse<Path>> future = client.sendAsync(request,
				BodyHandler.asFile(Paths.get("c:/temp", "response.html")));

		Thread.sleep(1000);
		if (!future.isDone()) {
			future.cancel(true);
			LOG.info("Cancelled request after 'timeout' of " + 10 +"ms");
		}
		LOG.info("Request finished without timeout.");
		HttpResponse<Path> response = future.get();
		LOG.info(response.uri().toASCIIString());
		return response;
	}
}
