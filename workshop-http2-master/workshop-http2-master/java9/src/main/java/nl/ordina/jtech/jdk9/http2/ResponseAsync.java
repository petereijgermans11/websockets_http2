package nl.ordina.jtech.jdk9.http2;

import java.io.File;
import java.net.URI;
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpClient.Version;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import jdk.incubator.http.HttpResponse.BodyHandler;
import nl.ordina.jtech.jdk9.http2.util.AbstractResponseStrategy;
import nl.ordina.jtech.jdk9.http2.util.ExampleUtils;
import nl.ordina.jtech.jdk9.http2.util.SSLContextCreator;
import nl.ordina.jtech.jdk9.http2.util.UriProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

/**
 * Uses an http2 request to fetch an html-file from a URI. This request will not receive files pushed by the server.
 * 
 * @author janweinschenker
 */
public class ResponseAsync extends AbstractResponseStrategy {
	private static final Logger LOG = Logger.getLogger(ResponseAsync.class.getName());

	public static void main(String[] args) {
        new ResponseAsync().go();
	}

	public void go() {
	    LOG.fine("fine log");
		List<URI> targets = UriProvider.getInstance().getUriList();
		List<CompletableFuture<File>> futures = ExampleUtils.getCompletableFutures(this, targets);
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		ExampleUtils.printFuturesReport(futures);
	}

	/**
	 * Fetch a list of target URIs asynchronously and store them in Files.
	 * @see HttpRequest
	 * @param targets
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	@Override
	public List<CompletableFuture<File>> getCompletableFutures(List<URI> targets) {
		SSLContext context = SSLContextCreator.getContextInstance();
        HttpClient client = HttpClient
				              .newBuilder().sslContext(context)
				              .build();

		List<CompletableFuture<File>> futures = targets.stream().map(target -> 
			 client.sendAsync(HttpRequest.newBuilder(target)
				.version(Version.HTTP_2)
				.GET().build(), 
					BodyHandler.asFile(Paths.get("target", target.getPath().replace('/', '-'))))
      .thenApply(response -> response.body())
			.whenCompleteAsync((it, err) -> {
				if (it != null) {
					LOG.log(Level.INFO, "saved to disk: " + it.toString());
				} else {
					LOG.log(Level.SEVERE, err.getClass().getSimpleName());
				}
			}).thenApply((Path dest) -> {
				return dest.toFile();
			}))
		.collect(Collectors.toList());
		return futures;
	}
}
