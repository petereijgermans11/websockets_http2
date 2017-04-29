package nl.ordina.jtech.ws.servlet30;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// taken from http://www.javaworld.com/article/2077995/java-concurrency/asynchronous-processing-support-in-servlet-3-0.html
@javax.servlet.annotation.WebServlet(
// servlet name
name = "servlet3-async",
// servlet url pattern
value = { "/servlet3-async" }, //
asyncSupported = true, //
initParams = { @WebInitParam(name = "threadpoolsize", value = "3") })
public class AsyncServlet extends HttpServlet {
	private static final long serialVersionUID = -176002629847268205L;
	private static final Logger LOG = LoggerFactory.getLogger(AsyncServlet.class);

	public static final AtomicInteger counter = new AtomicInteger(0);
	public static final int CALLBACK_TIMEOUT_MS = 6000;
	public static final int MAX_SIMULATED_TASK_LENGTH_MS = 5000;

	private ExecutorService executorService;

	@Override
	public void init() throws ServletException {
		final int size = Integer.parseInt(getInitParameter("threadpoolsize"));
		executorService = Executors.newFixedThreadPool(size);
	}

	@Override
	public void destroy() {
		executorService.shutdown();
	}

	@Override
	// doGet or doPost would also work fine
	public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {

		// TODO start an asynchronous context based on the servlet request

		ctx.setTimeout(CALLBACK_TIMEOUT_MS); // default is 30s
		ctx.addListener(new AsyncListener() {
			@Override
			public void onComplete(final AsyncEvent event) throws IOException {
				LOG.info("onComplete has already been called on the async context, nothing to do");
			}

			@Override
			public void onTimeout(final AsyncEvent event) throws IOException {
				LOG.info("timeout has occurred in async task; event {}", event);
				ctx.getResponse().getWriter().write(String.format("TIMEOUT occurred after {}ms", CALLBACK_TIMEOUT_MS));
				ctx.complete();
			}

			/**
			 * THIS NEVER GETS CALLED - error has occurred in async task... handle it
			 */
			@Override
			public void onError(final AsyncEvent event) throws IOException {
				LOG.info("onError called with event {}", event);
				ctx.getResponse().getWriter().write("ERROR");
				ctx.complete();
			}

			@Override
			public void onStartAsync(final AsyncEvent event) throws IOException {
				LOG.info("async context has started");
			}
		});

		if (counter.addAndGet(1) < 5) {
			// note - gives timeout on GlassFish, but immediate HTTP 500 in
			// Tomcat & Jetty
			throw new IndexOutOfBoundsException(
					String.format(
							"Simulated error (does not cause onError - causes network error in browser) - press F5 %d more times please",
							(5 - counter.get())));
		} else {
			// spawn some task to be run in executor
			enqueLongRunningTask(ctx);
		}
	}

	/**
	 * If something goes wrong in the task, it causes a timeout condition
	 * that causes the async context listener to be invoked.
	 */
	private void enqueLongRunningTask(final AsyncContext ctx) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LOG.info("async request processing starts");

					// TODO retrieve the original servlet request from the async context
					// .. and then
					// TODO retrieve the 'someParam' request parameter

					final int delay = new Random().nextInt(MAX_SIMULATED_TASK_LENGTH_MS);
					Thread.sleep(delay);

					final ServletResponse response = ctx.getResponse();
					if (response != null) {
						
						// this writes data directly to the HTTP stream
						response.getWriter().write("Direct from async task");

						// dispatch processing runs in container thread - adds
						// output to what was already there
						request.setAttribute("threadId", Thread.currentThread().getId());
						request.setAttribute("delay", delay);
						request.setAttribute("inputParam", someParam);
						ctx.dispatch("/async-response.ftl");
					} else {
						throw new IllegalStateException(
								"Response object from context is null - context has timed out. "
										+ "App server has called the listener already.");
					}
				} catch (final Exception e) {
					LOG.info("Problem processing task", e);
					e.printStackTrace();
				} finally {
					LOG.info("async request processing done");
				}
			}
		});
	}
}
