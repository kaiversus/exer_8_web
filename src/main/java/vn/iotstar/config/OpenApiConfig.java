package vn.iotstar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;


@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI webApiAjaxOpenAPI() {
		return new OpenAPI().info(new Info()
				.title("WEB_API_AJAX - RESTful API Category & Product")
				.version("1.0.0")
				.description("API CRUD cho bang Category va bang Product, render bang AJAX (jQuery).")
				.contact(new Contact().name("Nguyen Minh Quan")));
	}
}

