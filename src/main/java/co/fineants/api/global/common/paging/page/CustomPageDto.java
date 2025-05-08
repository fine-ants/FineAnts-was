package co.fineants.api.global.common.paging.page;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class CustomPageDto<T, R> {
	@JsonProperty("pageable")
	private final CustomPageable pageable;
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
	private final CustomSort sort;
	@JsonProperty("numberOfElements")
	private final int numberOfElements;
	@JsonProperty("empty")
	private final boolean empty;
	@JsonProperty("content")
	private final List<R> content;

	public CustomPageDto(
		CustomPageable pageable,
		Page<T> page,
		List<R> content) {
		this(
			pageable,
			page.isFirst(),
			page.isLast(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.getSize(),
			page.getNumber(),
			CustomSort.from(page.getSort()),
			page.getNumberOfElements(),
			page.isEmpty(),
			content
		);
	}

	@JsonCreator
	private CustomPageDto(
		@JsonProperty("pageable") CustomPageable pageable,
		@JsonProperty("first") boolean first,
		@JsonProperty("last") boolean last,
		@JsonProperty("totalElements") long totalElements,
		@JsonProperty("totalPages") int totalPages,
		@JsonProperty("size") int size,
		@JsonProperty("number") int number,
		@JsonProperty("sort") CustomSort sort,
		@JsonProperty("numberOfElements") int numberOfElements,
		@JsonProperty("empty") boolean empty,
		@JsonProperty("content") List<R> content) {
		this.pageable = pageable;
		this.first = first;
		this.last = last;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.size = size;
		this.number = number;
		this.sort = sort;
		this.numberOfElements = numberOfElements;
		this.empty = empty;
		this.content = content;
	}

	public Map<String, List<R>> newContentMap(String contentKeyName) {
		return Map.of(contentKeyName, content);
	}
}
