package vn.iotstar.controller.api;

import java.util.Optional;
import java.util.UUID;

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
import vn.iotstar.model.PageResult;
import vn.iotstar.model.Response;
import vn.iotstar.service.ICategoryService;
import vn.iotstar.service.IStorageService;

@Tag(name = "Category API", description = "CRUD, tim kiem va phan trang bang Categories")
@RestController
@RequestMapping(path = "/api/category")
public class CategoryAPIController {

	@Autowired
	private ICategoryService categoryService;

	@Autowired
	IStorageService storageService;

	@Operation(summary = "Lay tat ca loai san pham")
	@GetMapping
	public ResponseEntity<?> getAllCategory() {

		return new ResponseEntity<Response>(
				new Response(true, "Thanh cong", categoryService.findAll()), HttpStatus.OK);
	}

	@Operation(summary = "Tim kiem theo ten + phan trang",
			description = "keyword de trong thi lay tat ca. sort: categoryId | categoryName")
	@GetMapping(path = "/search")
	public ResponseEntity<?> search(
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "5") int size,
			@RequestParam(value = "sort", required = false, defaultValue = "categoryId") String sort,
			@RequestParam(value = "direction", required = false, defaultValue = "asc") String direction) {

		Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(Math.max(page, 0), size <= 0 ? 5 : size, Sort.by(dir, sort));

		Page<Category> pageData = categoryService.findByCategoryNameContaining(keyword.trim(), pageable);
		return new ResponseEntity<Response>(
				new Response(true, "Thanh cong", PageResult.of(pageData)), HttpStatus.OK);
	}

	@Operation(summary = "Lay 01 loai san pham theo id (kieu REST)")
	@GetMapping(path = "/{id}")
	public ResponseEntity<?> getCategoryById(@PathVariable("id") Long id) {
		return getCategory(id);
	}

	@Operation(summary = "Lay 01 loai san pham theo id")
	@PostMapping(path = "/getCategory")
	public ResponseEntity<?> getCategory(@Validated @RequestParam("id") Long id) {
		Optional<Category> category = categoryService.findById(id);
		if (category.isPresent()) {
			return new ResponseEntity<Response>(
					new Response(true, "Thanh cong", category.get()), HttpStatus.OK);
		} else {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay Category", null), HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Them moi loai san pham (multipart/form-data)")
	@PostMapping(path = "/addCategory")
	public ResponseEntity<?> addCategory(
			@Validated @RequestParam("categoryName") String categoryName,
			@RequestParam(value = "icon", required = false) MultipartFile icon) {

		if (!StringUtils.hasText(categoryName)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten loai san pham khong duoc de trong", null), HttpStatus.BAD_REQUEST);
		}

		Optional<Category> optCategory = categoryService.findByCategoryName(categoryName.trim());
		if (optCategory.isPresent()) {
			return new ResponseEntity<Response>(
					new Response(false, "Loai san pham nay da ton tai trong he thong", optCategory.get()),
					HttpStatus.BAD_REQUEST);
		} else {
			Category category = new Category();

			if (icon != null && !icon.isEmpty()) {
				UUID uuid = UUID.randomUUID();
				String uuString = uuid.toString();

				category.setIcon(storageService.getSorageFilename(icon, uuString));
				storageService.store(icon, category.getIcon());
			}
			category.setCategoryName(categoryName.trim());
			categoryService.save(category);
			return new ResponseEntity<Response>(
					new Response(true, "Them thanh cong", category), HttpStatus.OK);
		}
	}

	@Operation(summary = "Cap nhat loai san pham (multipart/form-data)")
	@PutMapping(path = "/updateCategory")
	public ResponseEntity<?> updateCategory(
			@Validated @RequestParam("categoryId") Long categoryId,
			@Validated @RequestParam("categoryName") String categoryName,
			@RequestParam(value = "icon", required = false) MultipartFile icon) {

		Optional<Category> optCategory = categoryService.findById(categoryId);
		if (optCategory.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay Category", null), HttpStatus.BAD_REQUEST);
		}

		if (!StringUtils.hasText(categoryName)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten loai san pham khong duoc de trong", null), HttpStatus.BAD_REQUEST);
		}

		Optional<Category> sameName = categoryService.findByCategoryName(categoryName.trim());
		if (sameName.isPresent() && !sameName.get().getCategoryId().equals(categoryId)) {
			return new ResponseEntity<Response>(
					new Response(false, "Ten loai san pham nay da ton tai", null), HttpStatus.BAD_REQUEST);
		}

		Category category = optCategory.get();

		if (icon != null && !icon.isEmpty()) {
			UUID uuid = UUID.randomUUID();
			String uuString = uuid.toString();
			category.setIcon(storageService.getSorageFilename(icon, uuString));
			storageService.store(icon, category.getIcon());
		}
		category.setCategoryName(categoryName.trim());
		categoryService.save(category);

		return new ResponseEntity<Response>(
				new Response(true, "Cap nhat thanh cong", category), HttpStatus.OK);
	}

	@Operation(summary = "Xoa loai san pham theo id")
	@DeleteMapping(path = "/deleteCategory")
	public ResponseEntity<?> deleteCategory(@Validated @RequestParam("categoryId") Long categoryId) {
		Optional<Category> optCategory = categoryService.findById(categoryId);
		if (optCategory.isEmpty()) {
			return new ResponseEntity<Response>(
					new Response(false, "Khong tim thay Category", null), HttpStatus.BAD_REQUEST);
		}
		categoryService.delete(optCategory.get());
		return new ResponseEntity<Response>(
				new Response(true, "Xoa thanh cong", optCategory.get()), HttpStatus.OK);
	}
}

