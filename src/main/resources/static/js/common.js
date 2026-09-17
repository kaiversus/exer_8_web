/* Cac ham dung chung cho moi trang AJAX */

/** Hien thong bao o dau trang */
function showAlert(message, ok) {
	var box = $('#alertBox');
	box.removeClass('d-none alert-success alert-danger')
		.addClass(ok ? 'alert-success' : 'alert-danger')
		.text(message);
	clearTimeout(window.__alertTimer);
	window.__alertTimer = setTimeout(function () {
		box.addClass('d-none');
	}, 4000);
}

/** Lay message tu phan hoi loi cua API (kieu Response{status,message,body}) */
function errorMessage(xhr, fallback) {
	try {
		var res = JSON.parse(xhr.responseText);
		if (res && res.message) {
			return res.message;
		}
	} catch (e) { /* phan hoi khong phai JSON */ }
	return fallback || 'Có lỗi xảy ra, vui lòng thử lại';
}

/** Dinh dang so tien VND */
function formatMoney(value) {
	if (value === null || value === undefined) {
		return '';
	}
	return Number(value).toLocaleString('vi-VN') + ' đ';
}

/** Chong loi XSS khi noi chuoi HTML tu du lieu nguoi dung nhap */
function escapeHtml(text) {
	if (text === null || text === undefined) {
		return '';
	}
	return $('<span>').text(text).html();
}

/** The <img> cho anh da upload, hoac chu "khong co anh" */
function imageTag(filename, width) {
	if (!filename) {
		return '<span class="text-muted small">Không có ảnh</span>';
	}
	return '<img src="' + contextPath + '/images/' + encodeURIComponent(filename)
		+ '" style="width:' + (width || 70) + 'px" class="img-fluid rounded" alt="">';
}

/**
 * Ve thanh phan trang.
 * data: PageResult tra ve tu API. onGo: ham nhan so trang can chuyen den.
 */
function renderPagination(container, data, onGo) {
	var ul = $(container).empty();
	if (!data || data.totalPages <= 1) {
		return;
	}

	function item(label, targetPage, disabled, active) {
		var li = $('<li class="page-item"></li>');
		if (disabled) { li.addClass('disabled'); }
		if (active) { li.addClass('active'); }
		var a = $('<a class="page-link" href="#"></a>').html(label);
		a.on('click', function (e) {
			e.preventDefault();
			if (!disabled && !active) { onGo(targetPage); }
		});
		return li.append(a);
	}

	ul.append(item('&laquo;', data.page - 1, data.first, false));
	for (var i = 0; i < data.totalPages; i++) {
		ul.append(item(String(i + 1), i, false, i === data.page));
	}
	ul.append(item('&raquo;', data.page + 1, data.last, false));
}
