package vn.iotstar.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public <S extends Product> S save(S entity) {
		if (entity.getProductId() == null) {
			return productRepository.save(entity);
		} else {
			Optional<Product> opt = findById(entity.getProductId());
			if (opt.isPresent()) {

				if (!StringUtils.hasText(entity.getImages())) {
					entity.setImages(opt.get().getImages());
				}

				if (entity.getCreateDate() == null) {
					entity.setCreateDate(opt.get().getCreateDate());
				}
			}
			return productRepository.save(entity);
		}
	}

	@Override
	public List<Product> findAll() {
		return productRepository.findAll();
	}

	@Override
	public Page<Product> findAll(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	@Override
	public List<Product> findAll(Sort sort) {
		return productRepository.findAll(sort);
	}

	@Override
	public List<Product> findAllById(Iterable<Long> ids) {
		return productRepository.findAllById(ids);
	}

	@Override
	public Optional<Product> findById(Long id) {
		return productRepository.findById(id);
	}

	@Override
	public <S extends Product> Optional<S> findOne(Example<S> example) {
		return productRepository.findOne(example);
	}

	@Override
	public long count() {
		return productRepository.count();
	}

	@Override
	public void deleteById(Long id) {
		productRepository.deleteById(id);
	}

	@Override
	public void delete(Product entity) {
		productRepository.delete(entity);
	}

	@Override
	public Optional<Product> findByProductName(String name) {
		return productRepository.findByProductName(name);
	}

	@Override
	public Optional<Product> findByCreateDate(Date createAt) {
		return productRepository.findByCreateDate(createAt);
	}

	@Override
	public List<Product> findByProductNameContaining(String name) {
		return productRepository.findByProductNameContaining(name);
	}

	@Override
	public Page<Product> findByProductNameContaining(String name, Pageable pageable) {
		return productRepository.findByProductNameContaining(name, pageable);
	}

	@Override
	public List<Product> findByCategoryId(Long categoryId) {
		return productRepository.findByCategory_CategoryId(categoryId);
	}

	@Override
	public Page<Product> findByCategoryId(Long categoryId, Pageable pageable) {
		return productRepository.findByCategory_CategoryId(categoryId, pageable);
	}

	
	@Override
	public Page<Product> search(Long categoryId, String keyword, Pageable pageable) {
		String kw = StringUtils.hasText(keyword) ? keyword.trim() : "";
		if (categoryId != null) {
			return productRepository.findByCategory_CategoryIdAndProductNameContaining(categoryId, kw, pageable);
		}
		return productRepository.findByProductNameContaining(kw, pageable);
	}
}

