package nl.ordina.jtech.ws.servlet30;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Illustrates two Spring async features:
 * 1. handling request processing in the background, in this case file upload 
 * 2. using DeferredResult for handling, well, deferred results
 * 
 * In the first case, Spring handles the task processing thread pool. (By default: SimpleAsyncTaskExecutor)
 * The second approach enables a custom thread pool.
 */
@Controller
public class AsyncSpringMVCController {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncSpringMVCController.class);

	@RequestMapping(value = "/uploadFile", method = RequestMethod.GET)
	public String getUploadFileForm() {
		return "uploadFileForm";
	}

	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public ... uploadFile(@RequestParam("file") final MultipartFile file, final ModelMap model) {
		LOG.info("file uploaded: {}", file.getOriginalFilename());
		// TODO return the following inner code to be callable by Spring. 
		// It will return the name of the view to render.

		...
				if (!file.isEmpty()) {
					LOG.info("streaming uploaded file of size {} to disk", file.getSize());
					try (InputStream input = file.getInputStream()) {
						Files.copy(
								input, 
								Paths.get("c:\\temp\\" + file.getOriginalFilename()),
								StandardCopyOption.REPLACE_EXISTING);
					}
					LOG.info("streaming done");
				}
				model.addAttribute("user", "<<Your name here>>");
				model.addAttribute("filename", file.getOriginalFilename());
				model.addAttribute("filekB", file.getSize() / 1024);
				return "got-file";
		// end of inner code
				
	}

	@Autowired
	DeferredResultContainer deferredResultContainer;

	@RequestMapping(value = "/getQuote", method = RequestMethod.GET)
	// @ResponseBody indicates: quote itself is HTTP response data - no view rendering required
	@ResponseBody
	public DeferredResult<String> getQuote() {
		final DeferredResult<String> deferredResult = new DeferredResult<String>();
		deferredResultContainer.put(deferredResult);
		
		// TODO: remove the deferred result from the container on timeout as well as completion
		...
		
		// don't forget to fix DeferredResultContainer!
		
		// return immediately
		return deferredResult;
	}
}
