package vn.iotstar.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.iotstar.entity.Product;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {

	private Long productId;
	private String productName;
	private int quantity;
	private double unitPrice;
	private String images;
	private String description;
	private double discount;
	private Date createDate;
	private short status;
	private Long categoryId;
	private String categoryName;

	
	public static ProductModel from(Product p) {
		ProductModel m = new ProductModel();
		m.setProductId(p.getProductId());
		m.setProductName(p.getProductName());
		m.setQuantity(p.getQuantity());
		m.setUnitPrice(p.getUnitPrice());
		m.setImages(p.getImages());
		m.setDescription(p.getDescription());
		m.setDiscount(p.getDiscount());
		m.setCreateDate(p.getCreateDate());
		m.setStatus(p.getStatus());
		if (p.getCategory() != null) {
			m.setCategoryId(p.getCategory().getCategoryId());
			m.setCategoryName(p.getCategory().getCategoryName());
		}
		return m;
	}
}

