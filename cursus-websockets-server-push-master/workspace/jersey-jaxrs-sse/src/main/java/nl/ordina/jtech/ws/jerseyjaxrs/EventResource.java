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
	
	@GET
	@Produces(SseFeature.SERVER_SENT_EVENTS)
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
							// text/plain
							eventOutput.write(new OutboundEvent.Builder()
								.name("text-version")
								.mediaType(MediaType.TEXT_PLAIN_TYPE)
								.data(String.class, "msg " + i)
								.id(Integer.toString(i))
								.build());

							// application/json
							eventOutput.write(new OutboundEvent.Builder()
							.mediaType(MediaType.APPLICATION_JSON_TYPE) //
							.data(EventModel.class, new EventModel("msg", i)) //
							.build());
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
		return eventOutput;
	}

}
