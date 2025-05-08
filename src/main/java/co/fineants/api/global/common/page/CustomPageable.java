package co.fineants.api.global.common.page;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomPageable {
	@JsonProperty("pageNumber")
	private final int pageNumber;
	@JsonProperty("pageSize")
	private final int pageSize;
	@JsonProperty("sort")
	private final CustomSort sort;
	@JsonProperty("offset")
	private final long offset;
	@JsonProperty("paged")
	private final boolean paged;
	@JsonProperty("unpaged")
	private final boolean unpaged;

	@JsonCreator
	private CustomPageable(
		@JsonProperty("pageNumber") int pageNumber,
		@JsonProperty("pageSize") int pageSize,
		@JsonProperty("sort") CustomSort sort,
		@JsonProperty("offset") long offset,
		@JsonProperty("paged") boolean paged,
		@JsonProperty("unpaged") boolean unpaged) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.sort = sort;
		this.offset = offset;
		this.paged = paged;
		this.unpaged = unpaged;
	}

	public static CustomPageable from(Pageable pageable) {
		return new CustomPageable(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			CustomSort.from(pageable.getSort()),
			pageable.getOffset(),
			pageable.isPaged(),
			pageable.isUnpaged()
		);
	}

	@Override
	public String toString() {
		return String.format(
			"CustomPageable [pageNumber=%s, pageSize=%s, sort=%s, offset=%s, paged=%s, unpaged=%s]",
			pageNumber, pageSize, sort, offset, paged, unpaged);
	}
}
