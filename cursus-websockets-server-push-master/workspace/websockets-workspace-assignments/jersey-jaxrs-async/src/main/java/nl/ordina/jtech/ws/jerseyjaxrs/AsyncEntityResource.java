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

@Path("/entity")
public class AsyncEntityResource {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncEntityResource.class);
	private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

	/**
	 * RESTful resource to create a new 'heavy' object using the supplied name.
	 * The heavy creation - on an external backend - is faked by a 4s wait.
	 * Your task: complete the code to make the response asynchronous, to conserve appserver thread resources.
	 */
	@POST
	public void createNew(/*TODO*/, @FormParam("name") final String name) {
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
				response./*TODO*/(Response.ok().entity(String.format("Created entity with name %s", name)).build());
			}
		});
	}

	/*
	 * RESTful resource to retrieve a heavy entity by its id.
	 * Slow backend retrieval is faked with a 4s wait.
	 */
	@GET
	@Path("{entity_id}")
	public void retrieveEntity(@Suspended final AsyncResponse response, @PathParam("entity_id") final String entityId) {
		LOG.info("GET {}", entityId);
		final Future<Boolean> submittedTask = ... /*TODO*/
				// your task: submit the following inline code to the instance's task executor

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

				// end of inline code
				
		LOG.info("JAX-RS GET call returns, but submittedTask {} will resume response once it has an answer",
				submittedTask);
	}

	/*
	 * Retrieval of a list of heavy objects.
	 * For illustatration purposes, this will always fail with a timeout.
	 * Your task: make the timeout work.
	 */
	@GET
	public void retrieveEntityList(@Suspended final AsyncResponse asyncResponse) {
		LOG.info("AsyncResource.asyncGetWithTimeout()");

		// check the logs: is onComplete() invoked in case of timeout or not?
		asyncResponse.register(new CompletionCallback() {
			@Override
			public void onComplete(final Throwable throwable) {
				// you could do resource cleanup here - see 
				// https://github.com/MousePilots/StockTicker/blob/master/src/main/java/org/mousepilots/demos/stockticker/rest/AexResourceListener.java
				LOG.info("completed");
			}
		});

		// TODO: set a timeout handler on the asynchronous response (and that's a hint!)
		// Handle the timeout by resuming the response with this entity:
		//
		// Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation time out.").build()
		//

		// TODO: set a timeout of 3 secs on the asynchronous response

		// code below is already complete - waits 4s to fake long-running operation
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
