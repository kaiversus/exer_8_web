package vn.iotstar.model;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {

	private List<?> content;
	private int page;          
	private int size;          
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

	public static PageResult of(Page<?> p, List<?> content) {
		return new PageResult(content, p.getNumber(), p.getSize(), p.getTotalElements(),
				p.getTotalPages(), p.isFirst(), p.isLast());
	}

	public static PageResult of(Page<?> p) {
		return of(p, p.getContent());
	}
}

