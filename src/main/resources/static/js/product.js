/* CRUD Product bang RESTful API + AJAX */

var productState = { page: 0, mode: 'add' };

/* Nap danh sach category vao cac o select (bo loc va form them/sua) */
function loadCategoryOptions() {
	return $.getJSON(contextPath + '/api/category', function (res) {
		var categories = res.body;
		var filterOpts = ['<option value="">-- Tất cả --</option>'];
		var formOpts = [];

		for (var i = 0; i < categories.length; i++) {
			var c = categories[i];
			var opt = '<option value="' + c.categoryId + '">'
				+ escapeHtml(c.categoryName) + '</option>';
			filterOpts.push(opt);
			formOpts.push(opt);
		}

		$('#filterCategoryId').html(filterOpts.join(''));
		$('#categoryId').html(formOpts.join(''));
	});
}

/* ================= READ: hien danh sach product ================= */
function loadProducts(page) {
	productState.page = (page === undefined) ? productState.page : page;

	var sortValue = $('#sort').val().split(',');

	$.getJSON(contextPath + '/api/product/search', {
		keyword: $('#keyword').val(),
		categoryId: $('#filterCategoryId').val(),
		page: productState.page,
		size: $('#size').val(),
		sort: sortValue[0],
		direction: sortValue[1]
	}, function (res) {
		var data = res.body;
		var tr = [];

		if (data.content.length === 0) {
			tr.push('<tr><td colspan="10" class="text-center text-muted py-4">'
				+ 'Không tìm thấy dữ liệu</td></tr>');
		}

		for (var i = 0; i < data.content.length; i++) {
			var p = data.content[i];
			tr.push('<tr>');
			tr.push('<td>' + p.productId + '</td>');
			tr.push('<td>' + imageTag(p.images, 70) + '</td>');
			tr.push('<td>' + escapeHtml(p.productName) + '</td>');
			tr.push('<td>' + escapeHtml(p.categoryName) + '</td>');
			tr.push('<td class="text-end">' + formatMoney(p.unitPrice) + '</td>');
			tr.push('<td class="text-end">' + p.discount + '%</td>');
			tr.push('<td class="text-end">' + p.quantity + '</td>');
			tr.push('<td class="desc-cell" title="' + escapeHtml(p.description) + '">'
				+ escapeHtml(p.description) + '</td>');
			tr.push('<td>' + (p.status === 1
				? '<span class="badge text-bg-success">Đang bán</span>'
				: '<span class="badge text-bg-secondary">Ngừng bán</span>') + '</td>');
			tr.push('<td class="text-end table-actions">'
				+ '<a href="#" class="btn btn-sm btn-outline-warning me-1 btn-edit-product"'
				+ ' data-id="' + p.productId + '"><i class="fa-solid fa-pen-to-square"></i></a>'
				+ '<a href="#" class="btn btn-sm btn-outline-danger btn-delete-product"'
				+ ' data-id="' + p.productId + '"><i class="fa-solid fa-trash"></i></a>'
				+ '</td>');
			tr.push('</tr>');
		}

		$('#productTableBody').html(tr.join(''));
		$('#productInfo').text('Tổng ' + data.totalElements + ' sản phẩm · trang '
			+ (data.page + 1) + '/' + (data.totalPages === 0 ? 1 : data.totalPages));
		renderPagination('#productPagination', data, loadProducts);
	}).fail(function (xhr) {
		showAlert(errorMessage(xhr, 'Không tải được danh sách Product'), false);
	});
}

/* ================= CREATE ================= */
function showCreateProductModal() {
	productState.mode = 'add';
	$('#productForm')[0].reset();
	$('#productId').val('');
	$('#productModalTitle').text('Thêm Product');
	$('#productSubmitBtn').text('Thêm');
	$('#currentImageBox').empty();
	$('#imageHint').text('Chọn ảnh cho sản phẩm (không bắt buộc).');
	new bootstrap.Modal('#productModal').show();
}

/* ================= UPDATE: nap du lieu vao modal ================= */
function showEditProductModal(productId) {
	$.ajax({
		url: contextPath + '/api/product/getProduct',
		type: 'POST',
		data: { id: productId },
		dataType: 'json',
		success: function (res) {
			var p = res.body;
			productState.mode = 'edit';
			$('#productForm')[0].reset();
			$('#productId').val(p.productId);
			$('#productName').val(p.productName);
			$('#categoryId').val(p.categoryId);
			$('#unitPrice').val(p.unitPrice);
			$('#discount').val(p.discount);
			$('#quantity').val(p.quantity);
			$('#description').val(p.description);
			$('#status').val(p.status);
			$('#productModalTitle').text('Cập nhật Product #' + p.productId);
			$('#productSubmitBtn').text('Cập nhật');
			$('#imageHint').text('Để trống nếu muốn giữ ảnh cũ.');
			$('#currentImageBox').html('<label class="form-label">Ảnh hiện tại</label>'
				+ '<div>' + imageTag(p.images, 90) + '</div>');
			new bootstrap.Modal('#productModal').show();
		},
		error: function (xhr) {
			showAlert(errorMessage(xhr, 'Không lấy được thông tin sản phẩm'), false);
		}
	});
}

$(document).ready(function () {

	// nap category truoc, roi moi nap product de bo loc hien dung
	loadCategoryOptions().always(function () {
		loadProducts(0);
	});

	$('#searchProductForm').on('submit', function (e) {
		e.preventDefault();
		loadProducts(0);
	});
	$('#filterCategoryId, #sort, #size').on('change', function () {
		loadProducts(0);
	});

	/* CREATE hoac UPDATE: cung 1 form, khac url va http method */
	$('form#productForm').on('submit', function (e) {
		e.preventDefault();
		var isEdit = productState.mode === 'edit';
		var formData = new FormData(this);

		$.ajax({
			url: contextPath + (isEdit ? '/api/product/updateProduct' : '/api/product/addProduct'),
			type: isEdit ? 'PUT' : 'POST',
			dataType: 'json',
			data: formData,
			success: function (res) {
				bootstrap.Modal.getInstance('#productModal').hide();
				showAlert(res.message, true);
				isEdit ? loadProducts() : loadProducts(0);
			},
			error: function (xhr) {
				showAlert(errorMessage(xhr,
					isEdit ? 'Cập nhật sản phẩm thất bại' : 'Thêm sản phẩm thất bại'), false);
			},
			cache: false,
			contentType: false,
			processData: false
		});
	});

	$(document).on('click', '.btn-edit-product', function (e) {
		e.preventDefault();
		showEditProductModal($(this).data('id'));
	});

	/* DELETE */
	$(document).on('click', '.btn-delete-product', function (e) {
		e.preventDefault();
		var id = $(this).data('id');
		if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
			return;
		}
		$.ajax({
			type: 'DELETE',
			url: contextPath + '/api/product/deleteProduct?productId=' + id,
			dataType: 'json',
			success: function (res) {
				showAlert(res.message, true);
				loadProducts();
			},
			error: function (xhr) {
				showAlert(errorMessage(xhr, 'Xóa sản phẩm thất bại'), false);
			}
		});
	});
});
