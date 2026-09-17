package vn.iotstar.config;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.CategoryRepository;
import vn.iotstar.repository.ProductRepository;


@Configuration
public class DataSeeder {

	@Value("${app.seed-data:false}")
	private boolean seedData;

	@Bean
	ApplicationRunner seedRunner(CategoryRepository categoryRepository, ProductRepository productRepository) {
		return args -> {
			if (!seedData || categoryRepository.count() > 0) {
				return;
			}

			Category dienThoai = categoryRepository.save(new Category("Điện thoại", null));
			Category laptop = categoryRepository.save(new Category("Laptop", null));
			Category phuKien = categoryRepository.save(new Category("Phụ kiện", null));

			Timestamp now = new Timestamp(System.currentTimeMillis());

			productRepository.saveAll(List.of(
					sample("iPhone 15 Pro Max", 25, 34990000, 5, "Bản 256GB, titan tự nhiên", dienThoai, now),
					sample("Samsung Galaxy S24", 30, 22990000, 10, "Bản 256GB, màu đen", dienThoai, now),
					sample("Xiaomi Redmi Note 13", 50, 4990000, 0, "Bản 8GB/256GB", dienThoai, now),
					sample("MacBook Air M3", 15, 27990000, 3, "13 inch, 8GB RAM, 256GB SSD", laptop, now),
					sample("Dell XPS 13", 10, 32990000, 7, "Core Ultra 7, 16GB RAM", laptop, now),
					sample("Tai nghe AirPods Pro 2", 80, 5990000, 12, "Chống ồn chủ động", phuKien, now),
					sample("Sạc nhanh Anker 65W", 120, 890000, 0, "GaN, 3 cổng", phuKien, now)));

			System.out.println(">>> Da nap du lieu mau: 3 category, 7 product");
		};
	}

	private Product sample(String name, int quantity, double price, double discount,
			String description, Category category, Timestamp createDate) {
		Product p = new Product();
		p.setProductName(name);
		p.setQuantity(quantity);
		p.setUnitPrice(price);
		p.setDiscount(discount);
		p.setDescription(description);
		p.setCategory(category);
		p.setCreateDate(createDate);
		p.setStatus((short) 1);
		return p;
	}
}

