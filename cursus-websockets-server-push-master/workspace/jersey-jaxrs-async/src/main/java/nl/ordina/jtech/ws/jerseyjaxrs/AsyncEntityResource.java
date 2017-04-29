package nl.ordina.jtech.ws.jerseyjaxrs;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See http://www.bayes.me/jaxrs-2-and-servlet-3-0-async
 */
@Path("/entity")
public class AsyncEntityResource {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncEntityResource.class);
	private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

	@POST
	public void createNew(@Suspended final AsyncResponse response, @FormParam("name") final String name) {
		LOG.info("POST {}", name);
		TASK_EXECUTOR.submit(new Runnable() {
			@Override
			public void run() {
				LOG.info("sleeping 4s");
				try {
					Thread.sleep(4000);
				} catch (final InterruptedException e) {
				}
				LOG.info("resuming");
				response.resume(Response.ok().entity(String.format("Created entity with name %s", name)).build());
			}
		});
	}

	@GET
	@Path("{entity_id}")
	public void retrieveEntity(@Suspended final AsyncResponse response, @PathParam("entity_id") final String entityId) {
		LOG.info("GET {}", entityId);
		final Future<Boolean> submittedTask = TASK_EXECUTOR.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				LOG.info("sleeping 4s");
				try {
					Thread.sleep(4000);
				} catch (final InterruptedException e) {
				}
				LOG.info("resuming");
				final boolean resumeSucceeded = response.resume(Response.ok()
						.entity(String.format("You sent me entityId %s", entityId)).build());
				LOG.info("resume succeeded: {}", resumeSucceeded);
				return resumeSucceeded;
			}
		});
		LOG.info("JAX-RS GET call returns, but submittedTask {} will resume response once it has an answer",
				submittedTask);
	}

	@GET
	public void retrieveEntityList(@Suspended final AsyncResponse asyncResponse) {
		LOG.info("AsyncResource.asyncGetWithTimeout()");

		asyncResponse.register(new CompletionCallback() {
			@Override
			public void onComplete(final Throwable throwable) {
				LOG.info("completed");
			}
		});

		asyncResponse.setTimeoutHandler(new TimeoutHandler() {
			@Override
			public void handleTimeout(final AsyncResponse asyncResponse) {
				LOG.info("Sending timeout message");
				asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation time out.")
						.build());
			}
		});

		asyncResponse.setTimeout(3, TimeUnit.SECONDS);

		new Thread(new Runnable() {
			@Override
			public void run() {
				final String result = veryExpensiveOperation();
				asyncResponse.resume(result);
			}

			private String veryExpensiveOperation() {
				LOG.info("sleeping 4s - longer than timeout (3s)");
				try {
					Thread.sleep(4000);
				} catch (final InterruptedException e) {
					LOG.info("sleep interrupted - timeout might not occur");
				}
				// will never be returned
				return "Hi";
			}
		}).start();
	}
}
