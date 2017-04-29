package nl.ordina.jtech.ws.jerseyjaxrs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS client die RESTful requests afvuurt op een server-side AsyncEntityResource.
 * Client is zelf synchroon, maar is vrij eenvoudig asynchroon te maken.
 */
public class AsyncEntityClient {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncEntityClient.class);

	private final Client client;
	private final String entityServiceUrl;

	public AsyncEntityClient(final String url) {
		client = ClientBuilder.newClient();
		entityServiceUrl = url;
	}

	public String getEntity(final int id) throws InterruptedException, ExecutionException, TimeoutException {
		LOG.debug("get entity {}", id);
		final WebTarget target = client.target(entityServiceUrl);

		// response code wrapper
		final Response response = target //
				.path(String.format("entity/%d", id)) //
				.request() //
				.accept("text/html") //
				// TODO bonus: add async variant using simply .async() //
				// ... but is that enough?
				.get(Response.class);

		return extractEntity(id, target, response);
	}

	private String extractEntity(final int id, final WebTarget target, final Response response) {
		switch (response.getStatus()) {
		case 200:
			return response.readEntity(String.class);
		case 404:
			LOG.info("Entity {} not found", id);
			return null;
		case 400:
			LOG.info("illegal Entity id {}", id);
			throw new IllegalArgumentException(Integer.toString(id));
		case 503:
			LOG.info("Service not available - probably timeout");
			throw new ServiceUnavailableException(response);
		default:
			LOG.info("unknown error requesting Entity id {}", id);
			throw new RuntimeException(String.format("unknown error requesting Entity id %s", id));
		}
	}

	public String createEntity(final String entityName) {
		LOG.debug("create entity {}", entityName);
		final WebTarget target = client.target(entityServiceUrl);
		final Response response = target //
				.path(String.format("entity")) //
				.request(MediaType.APPLICATION_FORM_URLENCODED) //
				.post(Entity.form(new Form("name", entityName)));
		return extractEntity(-1, target, response);
	}

	public String getEntityList() {
		final WebTarget target = client.target(entityServiceUrl);

		final Response response = target //
				.path("entity") //
				.request() //
				// TODO bonus: add async variant using simply .async() //
				// ... but is that enough?
				.accept("text/html") //
				.get(Response.class);
		return extractEntity(-1, target, response);
	}

}
