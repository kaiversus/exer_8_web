package vn.iotstar.entity;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.annotations.Nationalized;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Categories")
public class Category implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long categoryId;

	@NotBlank(message = "Ten loai san pham khong duoc de trong")
	@Size(max = 100, message = "Ten loai san pham toi da 100 ky tu")
	@Nationalized 
	@Column(length = 100, nullable = false)
	private String categoryName;

	@Column(length = 255)
	private String icon;


	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	private Set<Product> products;

	public Category(String categoryName, String icon) {
		this.categoryName = categoryName;
		this.icon = icon;
	}
}

