package co.fineants.api.global.common.page;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomPageResponse<T> {
	@JsonAnyGetter
	private final Map<String, List<T>> content;
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

	public CustomPageResponse(
		Map<String, List<T>> content,
		CustomPageable pageable,
		boolean first,
		boolean last,
		long totalElements,
		int totalPages,
		int size,
		int number,
		CustomSort sort,
		int numberOfElements,
		boolean empty
	) {
		this.content = content;
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
	}

	public static <T> CustomPageResponse<T> of(CustomPageDto customPageDto,
		Map<String, List<T>> content) {
		return new CustomPageResponse<>(
			content,
			customPageDto.getPageable(),
			customPageDto.isFirst(),
			customPageDto.isLast(),
			customPageDto.getTotalElements(),
			customPageDto.getTotalPages(),
			customPageDto.getSize(),
			customPageDto.getNumber(),
			customPageDto.getSort(),
			customPageDto.getNumberOfElements(),
			customPageDto.isEmpty()
		);
	}

	@Override
	public String toString() {
		return String.format(
			"CustomPageResponse [content=%s, pageable=%s, first=%s, last=%s, totalElements=%s, totalPages=%s, size=%s, number=%s, sort=%s, numberOfElements=%s, empty=%s]",
			content, pageable, first, last, totalElements, totalPages, size, number, sort,
			numberOfElements, empty);
	}
}
