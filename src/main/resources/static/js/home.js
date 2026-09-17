/* Trang chu: hien danh sach san pham bang AJAX, co loc theo category va sap xep theo gia */

var homeState = { page: 0 };

function loadHomeCategories() {
	return $.getJSON(contextPath + '/api/category', function (res) {
		var opts = ['<option value="">-- Tất cả --</option>'];
		for (var i = 0; i < res.body.length; i++) {
			opts.push('<option value="' + res.body[i].categoryId + '">'
				+ escapeHtml(res.body[i].categoryName) + '</option>');
		}
		$('#filterCategoryId').html(opts.join(''));
	});
}

function loadHomeProducts(page) {
	homeState.page = (page === undefined) ? homeState.page : page;

	var sortValue = $('#sort').val().split(',');

	$.getJSON(contextPath + '/api/product/search', {
		keyword: $('#keyword').val(),
		categoryId: $('#filterCategoryId').val(),
		page: homeState.page,
		size: $('#size').val(),
		sort: sortValue[0],
		direction: sortValue[1]
	}, function (res) {
		var data = res.body;
		var html = [];

		if (data.content.length === 0) {
			html.push('<div class="col-12 text-center text-muted py-5">'
				+ 'Không có sản phẩm nào</div>');
		}

		for (var i = 0; i < data.content.length; i++) {
			var p = data.content[i];
			var media = p.images
				? '<img src="' + contextPath + '/images/' + encodeURIComponent(p.images)
					+ '" class="card-img-top" alt="">'
				: '<div class="no-image"><i class="fa-regular fa-image fa-2x"></i></div>';

			html.push('<div class="col-6 col-md-4 col-lg-3">'
				+ '<div class="card h-100 card-product">'
				+ media
				+ '<div class="card-body d-flex flex-column">'
				+ '<span class="badge text-bg-light align-self-start mb-2">'
				+ escapeHtml(p.categoryName) + '</span>'
				+ '<h6 class="card-title">' + escapeHtml(p.productName) + '</h6>'
				+ '<p class="card-text text-muted small flex-grow-1">'
				+ escapeHtml(p.description) + '</p>'
				+ '<div class="fw-bold text-danger">' + formatMoney(p.unitPrice) + '</div>'
				+ (p.discount > 0
					? '<div class="small text-success">Giảm ' + p.discount + '%</div>'
					: '')
				+ '<div class="small text-muted">Còn ' + p.quantity + ' sản phẩm</div>'
				+ '</div></div></div>');
		}

		$('#productGrid').html(html.join(''));
		$('#homeInfo').text('Tổng ' + data.totalElements + ' sản phẩm · trang '
			+ (data.page + 1) + '/' + (data.totalPages === 0 ? 1 : data.totalPages));
		renderPagination('#homePagination', data, loadHomeProducts);
	}).fail(function (xhr) {
		showAlert(errorMessage(xhr, 'Không tải được danh sách sản phẩm'), false);
	});
}

$(document).ready(function () {
	loadHomeCategories().always(function () {
		loadHomeProducts(0);
	});

	$('#keyword').on('input', function () {
		clearTimeout(window.__homeSearchTimer);
		window.__homeSearchTimer = setTimeout(function () {
			loadHomeProducts(0);
		}, 400);
	});

	$('#filterCategoryId, #sort, #size').on('change', function () {
		loadHomeProducts(0);
	});
});
