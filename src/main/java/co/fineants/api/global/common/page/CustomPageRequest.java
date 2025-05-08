package co.fineants.api.global.common.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomPageRequest {
	private int page;
	private int size;
	private Sort.Direction direction;

	public CustomPageRequest(Integer page, Integer size, Sort.Direction direction) {
		setPage(page);
		setSize(size);
		setDirection(direction);
	}

	private void setPage(Integer page) {
		this.page = page == null || page <= 0 ? 1 : page;
	}

	private void setSize(Integer size) {
		int defaultSize = 10;
		int maxSize = 50;
		this.size = size == null || size > maxSize || size <= 0 ? defaultSize : size;
	}

	private void setDirection(Sort.Direction direction) {
		this.direction = direction == null ? Sort.Direction.DESC : direction;
	}

	public Pageable of() {
		Sort sort = Sort.by(direction, "createAt");
		return PageRequest.of(page - 1, size, sort);
	}

	@Override
	public String toString() {
		return String.format("CustomPageRequest [page=%s, size=%s, direction=%s]", page, size, direction);
	}
}
