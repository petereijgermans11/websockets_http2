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

@Controller
public class AsyncSpringMVCController {
	private static final Logger LOG = LoggerFactory.getLogger(AsyncSpringMVCController.class);

	@RequestMapping(value = "/uploadFile", method = RequestMethod.GET)
	public String getUploadFileForm() {
		return "uploadFileForm";
	}

	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public Callable<String> uploadFile(@RequestParam("file") final MultipartFile file, final ModelMap model) {
		LOG.info("file uploaded: {}", file.getOriginalFilename());
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
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
			}
		};
	}

	@Autowired
	DeferredResultContainer deferredResultContainer;

	@RequestMapping(value = "/getQuote", method = RequestMethod.GET)
	// quote itself is HTTP response data
	@ResponseBody
	public DeferredResult<String> getQuote() {
		final long start = System.currentTimeMillis();
		final DeferredResult<String> deferredResult = new DeferredResult<String>();
		deferredResultContainer.put(deferredResult);
		deferredResult.onTimeout(new Runnable() {
			@Override
			public void run() {
				LOG.info("timeout after {}ms", System.currentTimeMillis() - start);
				deferredResultContainer.remove(deferredResult);
			}
		});
		deferredResult.onCompletion(new Runnable() {
			@Override
			public void run() {
				LOG.info("completed after {}ms", System.currentTimeMillis() - start);
				deferredResultContainer.remove(deferredResult);
			}
		});
		return deferredResult;
	}
}
