package co.fineants.api.global.common.paging.slice;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.common.paging.page.CustomPageable;
import co.fineants.api.global.common.paging.page.CustomSort;

public class CustomSliceResponse<T> {
	@JsonAnyGetter
	private final Map<String, List<T>> content;
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

	public CustomSliceResponse(
		Map<String, List<T>> content,
		CustomPageable pageable,
		boolean first,
		boolean last,
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
		this.size = size;
		this.number = number;
		this.sort = sort;
		this.numberOfElements = numberOfElements;
		this.empty = empty;
	}

	public static <T> CustomSliceResponse<T> of(CustomSliceDto customSliceDto,
		Map<String, List<T>> content) {
		return new CustomSliceResponse<>(
			content,
			customSliceDto.getPageable(),
			customSliceDto.isFirst(),
			customSliceDto.isLast(),
			customSliceDto.getSize(),
			customSliceDto.getNumber(),
			customSliceDto.getSort(),
			customSliceDto.getNumberOfElements(),
			customSliceDto.isEmpty()
		);
	}

	@Override
	public String toString() {
		return String.format(
			"CustomSliceResponse [content=%s, pageable=%s, first=%s, last=%s, size=%s, number=%s, sort=%s, numberOfElements=%s, empty=%s]",
			content, pageable, first, last, size, number, sort, numberOfElements, empty);
	}
}
