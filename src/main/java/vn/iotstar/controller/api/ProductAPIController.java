package vn.iotstar.controller.api;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.model.PageResult;
import vn.iotstar.model.ProductModel;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IProductService;
import vn.iotstar.service.IStorageService;

@Tag(name = "Product API", description = "CRUD, tim kiem va phan trang bang Products")
@RestController
@RequestMapping(path = "/api/product")
public class ProductAPIController {

	@Autowired
	IProductService productService;

	@Autowired
	ICategoryService categoryService;

	@Autowired
	IStorageService storageService;

	private List<ProductModel> toModels(List<Product> products) {
		return products.stream().map(ProductModel::from).collect(Collectors.toList());
	}

	@Operation(summary = "Lay tat ca san pham")
	@GetMapping
	public ResponseEntity<?> getAllProduct() {
		return new ResponseEntity<Response>(
				new Response(true, "Thanh cong", toModels(productService.findAll())), HttpStatus.OK);
	}

	@Operation(summary = "Lay tat ca san pham cua 01 loai san pham")
	@GetMapping(path = "/byCategory")
	public ResponseEntity<?> getByCategory(@RequestParam("categoryId") Long categoryId) {
		if (categoryService.findById(categoryId).isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay Category", null), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Response>(
				new Response(true, "Thanh cong", toModels(productService.findByCategoryId(categoryId))),
				HttpStatus.OK);
	}

	@Operation(summary = "Tim kiem theo ten + loc theo loai + phan trang",
			description = "sort: productId | productName | unitPrice | quantity | createDate")
	@GetMapping(path = "/search")
	public ResponseEntity<?> search(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "5") int size,
			@RequestParam(value = "sort", required = false, defaultValue = "productId") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "asc") String direction) {

		Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 5 : size, Sort.by(dir, sort));

		Page<Product> pageData = productService.search(categoryId, keyword, pageable);
		return new ResponseEntity<Response>(
				new Response(true, "Thanh cong", PageResult.of(pageData, toModels(pageData.getContent()))),
				HttpStatus.OK);
	}

	@Operation(summary = "Lay 01 san pham theo id (kieu REST)")
	@GetMapping(path = "/{id}")
	public ResponseEntity<?> getProductById(@PathVariable("id") Long id) {
		return getProduct(id);
	}

	@Operation(summary = "Lay 01 san pham theo id")
	@PostMapping(path = "/getProduct")
	public ResponseEntity<?> getProduct(@Validated @RequestParam("id") Long id) {
		Optional<Product> product = productService.findById(id);
		if (product.isPresent()) {
			return new ResponseEntity<Response>(
					new Response(true, "Thanh cong", ProductModel.from(product.get())), HttpStatus.OK);
		}
		return new ResponseEntity<Response>(
				new Response(false, "Khong tim thay san pham", null), HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Them moi san pham (multipart/form-data)")
	@PostMapping(path = "/addProduct")
	public ResponseEntity<?> addProduct(
			@Validated @RequestParam("productName") String productName,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@Validated @RequestParam("unitPrice") Double unitPrice,
			@RequestParam(value = "discount", required = false, defaultValue = "0") Double discount,
			@RequestParam(value = "description", required = false, defaultValue = "") String description,
			@Validated @RequestParam("categoryId") Long categoryId,
			@Validated @RequestParam("quantity") Integer quantity,
			@RequestParam(value = "status", required = false, defaultValue = "1") Short status) {

		if (!StringUtils.hasText(productName)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten san pham khong duoc de trong", null), HttpStatus.BAD_REQUEST);
		}

		Optional<Product> optProduct = productService.findByProductName(productName.trim());
		if (optProduct.isPresent()) {
			return new ResponseEntity<Response>(
					new Response(false, "San pham nay da ton tai trong he thong",
							ProductModel.from(optProduct.get())),
					HttpStatus.BAD_REQUEST);
		}

		Optional<Category> optCategory = categoryService.findById(categoryId);
		if (optCategory.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Loai san pham khong ton tai", null), HttpStatus.BAD_REQUEST);
		}

		Product product = new Product();
		product.setProductName(productName.trim());
		product.setUnitPrice(unitPrice);
		product.setDiscount(discount == null ? 0 : discount);
		product.setDescription(description);
		product.setQuantity(quantity == null ? 0 : quantity);
		product.setStatus(status == null ? (short) 1 : status);
		product.setCategory(optCategory.get());
		product.setCreateDate(new Timestamp(new Date(System.currentTimeMillis()).getTime()));

		if (imageFile != null && !imageFile.isEmpty()) {
			UUID uuid = UUID.randomUUID();
			String uuString = uuid.toString();
			product.setImages(storageService.getSorageFilename(imageFile, uuString));
			storageService.store(imageFile, product.getImages());
		}

		productService.save(product);
		return new ResponseEntity<Response>(
				new Response(true, "Them thanh cong", ProductModel.from(product)), HttpStatus.OK);
	}

	@Operation(summary = "Cap nhat san pham (multipart/form-data)")
	@PutMapping(path = "/updateProduct")
	public ResponseEntity<?> updateProduct(
			@Validated @RequestParam("productId") Long productId,
			@Validated @RequestParam("productName") String productName,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@Validated @RequestParam("unitPrice") Double unitPrice,
			@RequestParam(value = "discount", required = false, defaultValue = "0") Double discount,
			@RequestParam(value = "description", required = false, defaultValue = "") String description,
			@Validated @RequestParam("categoryId") Long categoryId,
			@Validated @RequestParam("quantity") Integer quantity,
			@RequestParam(value = "status", required = false, defaultValue = "1") Short status) {

		Optional<Product> optProduct = productService.findById(productId);
		if (optProduct.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay san pham", null), HttpStatus.BAD_REQUEST);
		}

		if (!StringUtils.hasText(productName)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten san pham khong duoc de trong", null), HttpStatus.BAD_REQUEST);
		}

		Optional<Product> sameName = productService.findByProductName(productName.trim());
		if (sameName.isPresent() && !sameName.get().getProductId().equals(productId)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten san pham nay da ton tai", null), HttpStatus.BAD_REQUEST);
		}

		Optional<Category> optCategory = categoryService.findById(categoryId);
		if (optCategory.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Loai san pham khong ton tai", null), HttpStatus.BAD_REQUEST);
		}

		Product product = optProduct.get();
		product.setProductName(productName.trim());
		product.setUnitPrice(unitPrice);
		product.setDiscount(discount == null ? 0 : discount);
		product.setDescription(description);
		product.setQuantity(quantity == null ? 0 : quantity);
		product.setStatus(status == null ? (short) 1 : status);
		product.setCategory(optCategory.get());

		if (imageFile != null && !imageFile.isEmpty()) {
			UUID uuid = UUID.randomUUID();
			String uuString = uuid.toString();
			product.setImages(storageService.getSorageFilename(imageFile, uuString));
			storageService.store(imageFile, product.getImages());
		}

		productService.save(product);
		return new ResponseEntity<Response>(
				new Response(true, "Cap nhat thanh cong", ProductModel.from(product)), HttpStatus.OK);
	}

	@Operation(summary = "Xoa san pham theo id")
	@DeleteMapping(path = "/deleteProduct")
	public ResponseEntity<?> deleteProduct(@Validated @RequestParam("productId") Long productId) {
		Optional<Product> optProduct = productService.findById(productId);
		if (optProduct.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay san pham", null), HttpStatus.BAD_REQUEST);
		}
		ProductModel deleted = ProductModel.from(optProduct.get());
		productService.delete(optProduct.get());
		return new ResponseEntity<Response>(
				new Response(true, "Xoa thanh cong", deleted), HttpStatus.OK);
	}
}

