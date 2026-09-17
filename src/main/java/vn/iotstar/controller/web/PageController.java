package vn.iotstar.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PageController {

	@GetMapping("/")
	public String home() {
		return "index";
	}

	@GetMapping("/admin/categories")
	public String categories() {
		return "admin/categories/ajax";
	}

	@GetMapping("/admin/products")
	public String products() {
		return "admin/products/ajax";
	}
}

