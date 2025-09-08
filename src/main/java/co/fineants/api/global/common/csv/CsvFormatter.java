package co.fineants.api.global.common.csv;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class CsvFormatter<T extends CsvLineConvertible> {

	private final String delimiter;
	private final List<String> headers;

	public CsvFormatter(String delimiter, String[] headers) {
		this(delimiter, Arrays.asList(headers));
	}

	public CsvFormatter(String delimiter, List<String> headers) {
		this.delimiter = delimiter;
		this.headers = headers;
	}

	@SafeVarargs
	public final String format(T... items) {
		String title = String.join(delimiter, headers);
		String lines = createLines(Arrays.asList(items));
		return String.join(Strings.LINE_SEPARATOR, title, lines).trim();
	}

	@NotNull
	private String createLines(List<T> items) {
		return items.stream()
			.map(T::toCsvLine)
			.collect(Collectors.joining(Strings.LINE_SEPARATOR));
	}
}
