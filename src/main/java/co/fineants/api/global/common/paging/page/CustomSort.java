package co.fineants.api.global.common.paging.page;

import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomSort {
	@JsonProperty("empty")
	private final boolean empty;
	@JsonProperty("unsorted")
	private final boolean unsorted;
	@JsonProperty("sorted")
	private final boolean sorted;

	@JsonCreator
	public CustomSort(
		@JsonProperty("empty") boolean empty,
		@JsonProperty("unsorted") boolean unsorted,
		@JsonProperty("sorted") boolean sorted) {
		this.empty = empty;
		this.unsorted = unsorted;
		this.sorted = sorted;
	}

	public static CustomSort from(Sort sort) {
		return new CustomSort(
			sort.isEmpty(),
			sort.isUnsorted(),
			sort.isSorted()
		);
	}
}
