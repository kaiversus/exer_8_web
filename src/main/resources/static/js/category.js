/* CRUD Category bang RESTful API + AJAX */

var categoryState = { page: 0 };

/* ================= READ: hien danh sach category ================= */
function loadCategories(page) {
	categoryState.page = (page === undefined) ? categoryState.page : page;

	var sortValue = $('#sort').val().split(',');

	$.getJSON(contextPath + '/api/category/search', {
		keyword: $('#keyword').val(),
		page: categoryState.page,
		size: $('#size').val(),
		sort: sortValue[0],
		direction: sortValue[1]
	}, function (res) {
		var data = res.body;
		var tr = [];

		if (data.content.length === 0) {
			tr.push('<tr><td colspan="4" class="text-center text-muted py-4">'
				+ 'Không tìm thấy dữ liệu</td></tr>');
		}

		for (var i = 0; i < data.content.length; i++) {
			var c = data.content[i];
			tr.push('<tr>');
			tr.push('<td>' + c.categoryId + '</td>');
			tr.push('<td>' + imageTag(c.icon, 70) + '</td>');
			tr.push('<td>' + escapeHtml(c.categoryName) + '</td>');
			tr.push('<td class="text-end table-actions">'
				+ '<a href="#" class="btn btn-sm btn-outline-warning me-1 btn-edit-category"'
				+ ' data-id="' + c.categoryId + '"><i class="fa-solid fa-pen-to-square"></i></a>'
				+ '<a href="#" class="btn btn-sm btn-outline-danger btn-delete-category"'
				+ ' data-id="' + c.categoryId + '"><i class="fa-solid fa-trash"></i></a>'
				+ '</td>');
			tr.push('</tr>');
		}

		$('#categoryTableBody').html(tr.join(''));
		$('#categoryInfo').text('Tổng ' + data.totalElements + ' loại sản phẩm · trang '
			+ (data.page + 1) + '/' + (data.totalPages === 0 ? 1 : data.totalPages));
		renderPagination('#categoryPagination', data, loadCategories);
	}).fail(function (xhr) {
		showAlert(errorMessage(xhr, 'Không tải được danh sách Category'), false);
	});
}

/* ================= CREATE ================= */
function showCreateCategoryModal() {
	$('#addCategory')[0].reset();
	new bootstrap.Modal('#createCategoryModal').show();
}

/* ================= UPDATE: nap du lieu vao modal ================= */
function showEditCategoryModal(categoryId) {
	// goi API lay lai du lieu moi nhat cua 1 dong
	$.ajax({
		url: contextPath + '/api/category/getCategory',
		type: 'POST',
		data: { id: categoryId },
		dataType: 'json',
		success: function (res) {
			var c = res.body;
			$('#updateCategoryInfoModalId').text('Category ID: ' + c.categoryId);
			$('#updateCategoryInfoModalName').text('Tên: ' + c.categoryName);
			$('#updateCategoryInfoModalIcon').text('Icon: ' + (c.icon || '(chưa có)'));
			$('#categoryName_up').val(c.categoryName);
			$('#categoryId_up').val(c.categoryId);
			$('#icon_up').val('');
			new bootstrap.Modal('#updateCategoryInfoModal').show();
		},
		error: function (xhr) {
			showAlert(errorMessage(xhr, 'Không lấy được thông tin Category'), false);
		}
	});
}

$(document).ready(function () {

	loadCategories(0);

	$('#searchCategoryForm').on('submit', function (e) {
		e.preventDefault();
		loadCategories(0);
	});
	$('#sort, #size').on('change', function () {
		loadCategories(0);
	});

	/* CREATE: gui form multipart bang AJAX */
	$('form#addCategory').on('submit', function (e) {
		e.preventDefault();
		var formData = new FormData(this);
		$.ajax({
			url: contextPath + '/api/category/addCategory',
			type: 'POST',
			dataType: 'json',
			data: formData,
			success: function (res) {
				bootstrap.Modal.getInstance('#createCategoryModal').hide();
				showAlert(res.message, true);
				loadCategories(0);
			},
			error: function (xhr) {
				showAlert(errorMessage(xhr, 'Thêm Category thất bại'), false);
			},
			cache: false,
			contentType: false,
			processData: false
		});
	});

	/* UPDATE */
	$('form#updateCategory').on('submit', function (e) {
		e.preventDefault();
		var formData = new FormData(this);
		$.ajax({
			url: contextPath + '/api/category/updateCategory',
			type: 'PUT',
			dataType: 'json',
			data: formData,
			success: function (res) {
				bootstrap.Modal.getInstance('#updateCategoryInfoModal').hide();
				showAlert(res.message, true);
				loadCategories();
			},
			error: function (xhr) {
				showAlert(errorMessage(xhr, 'Cập nhật Category thất bại'), false);
			},
			cache: false,
			contentType: false,
			processData: false
		});
	});

	/* Bat su kien tren cac dong duoc sinh ra sau khi trang da tai */
	$(document).on('click', '.btn-edit-category', function (e) {
		e.preventDefault();
		showEditCategoryModal($(this).data('id'));
	});

	/* DELETE */
	$(document).on('click', '.btn-delete-category', function (e) {
		e.preventDefault();
		var id = $(this).data('id');
		if (!confirm('Bạn có chắc muốn xóa loại sản phẩm này? '
			+ 'Các sản phẩm thuộc loại này cũng sẽ bị xóa theo.')) {
			return;
		}
		$.ajax({
			type: 'DELETE',
			url: contextPath + '/api/category/deleteCategory?categoryId=' + id,
			dataType: 'json',
			success: function (res) {
				showAlert(res.message, true);
				loadCategories();
			},
			error: function (xhr) {
				showAlert(errorMessage(xhr, 'Xóa Category thất bại'), false);
			}
		});
	});
});
