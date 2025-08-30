package co.fineants.api.global.common.paging.slice;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.common.paging.page.CustomPageable;
import co.fineants.api.global.common.paging.page.CustomSort;
import lombok.Getter;

@Getter
public class CustomSliceDto<T, R> {
	@JsonProperty("pageable")
	private final CustomPageable pageable;
	@JsonProperty("first")
	private final boolean first;
	@JsonProperty("last")
	private final boolean last;
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

	public CustomSliceDto(
		CustomPageable pageable,
		Slice<T> slice,
		List<R> content) {
		this(
			pageable,
			slice.isFirst(),
			slice.isLast(),
			slice.getSize(),
			slice.getNumber(),
			CustomSort.from(slice.getSort()),
			slice.getNumberOfElements(),
			slice.isEmpty(),
			content
		);
	}

	@JsonCreator
	private CustomSliceDto(
		@JsonProperty("pageable") CustomPageable pageable,
		@JsonProperty("first") boolean first,
		@JsonProperty("last") boolean last,
		@JsonProperty("size") int size,
		@JsonProperty("number") int number,
		@JsonProperty("sort") CustomSort sort,
		@JsonProperty("numberOfElements") int numberOfElements,
		@JsonProperty("empty") boolean empty,
		@JsonProperty("content") List<R> content) {
		this.pageable = pageable;
		this.first = first;
		this.last = last;
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
