package vn.iotstar.controller.web;

import java.net.URLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import vn.iotstar.service.IStorageService;


@RestController
public class ImageController {

	@Autowired
	IStorageService storageService;

	@GetMapping("/images/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		try {
			Resource file = storageService.loadAsResource(filename);
			String contentType = URLConnection.guessContentTypeFromName(filename);
			MediaType mediaType = (contentType == null)
					? MediaType.APPLICATION_OCTET_STREAM
					: MediaType.parseMediaType(contentType);

			return ResponseEntity.ok()
					.contentType(mediaType)
					.header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
					.body(file);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}

