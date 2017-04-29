package nl.ordina.jtech.ws.jerseyjaxrs;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See https://jersey.java.net/documentation/latest/sse.html
 */
@Path("/event")
public class EventResource {
	private static final Logger LOG = LoggerFactory.getLogger(EventResource.class);
	private static final ExecutorService TASK_EXECUTOR = Executors.newCachedThreadPool();

	// inner class DTO to contain events (message and id)
	class EventModel {
		private String message;
		private int id;
		
		public EventModel() {
			// nothing
		}
		
		public EventModel(String message, int id) {
			this.message = message;
			this.id = id;
		}

		public String getMessage() {
			return message;
		}
		
		public int getId() {
			return id;
		}
	}
	
	// RESTful endpoint that should Produce Server Sent Events; see 
	// https://jersey.java.net/apidocs/latest/jersey/org/glassfish/jersey/media/sse/SseFeature.html
	@GET
	// TODO add something here
	public EventOutput getServerSentEvents() {
		final EventOutput eventOutput = new EventOutput();
		TASK_EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				try {
					for (int i = 0; i < 10; i++) {
						LOG.info("sleeping");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// ignore
						}
						LOG.info("building outbound event #{}", i);
						try {
							/* complete the code below to:
							 * - Build an Outboundevent
							 * - with some name that you like
							 * - mediatype text/plain
							 * - String data '"msg " + i'
							 * - an id of i as String
							 */
							eventOutput.write(...);

							/* here the same, but then:
							 * - nameless and id-less
							 * - mediatype application/json
							 * - EventModel data with values "msg" and i
							 */
							eventOutput.write(...);
						} catch (IOException e) {
							LOG.error("ioexc:", e);
						}
					}
				} finally {
					LOG.info("done sending 10 items - connection will close from server side but browser reopens");
					try {
						eventOutput.close();
					} catch (IOException e) {
						// ignore...
					}
				}
			}
		});
		// return immediately
		return eventOutput;
	}

}
