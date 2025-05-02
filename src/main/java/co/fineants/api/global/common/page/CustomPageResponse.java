package co.fineants.api.global.common.page;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomPageResponse<T> {
	@JsonAnyGetter
	private final Map<String, List<T>> content;
	@JsonProperty("pageable")
	private final Pageable pageable;
	@JsonProperty("first")
	private final boolean first;
	@JsonProperty("last")
	private final boolean last;
	@JsonProperty("totalElements")
	private final long totalElements;
	@JsonProperty("totalPages")
	private final int totalPages;
	@JsonProperty("size")
	private final int size;
	@JsonProperty("number")
	private final int number;
	@JsonProperty("sort")
	private final Sort sort;
	@JsonProperty("numberOfElements")
	private final int numberOfElements;
	@JsonProperty("empty")
	private final boolean empty;

	public CustomPageResponse(Page<T> page, String contentKeyName) {
		this.content = Map.of(contentKeyName, page.getContent());
		this.pageable = page.getPageable();
		this.first = page.isFirst();
		this.last = page.isLast();
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.size = page.getSize();
		this.number = page.getNumber();
		this.sort = page.getSort();
		this.numberOfElements = page.getNumberOfElements();
		this.empty = page.isEmpty();
	}

	@Override
	public String toString() {
		return String.format(
			"CustomPageResponse [content=%s, pageable=%s, first=%s, last=%s, totalElements=%s, totalPages=%s, size=%s, number=%s, sort=%s, numberOfElements=%s, empty=%s]",
			content, pageable, first, last, totalElements, totalPages, size, number, sort,
			numberOfElements, empty);
	}
}
