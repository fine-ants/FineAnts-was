package co.study.money;

import java.util.Objects;

public class Pair {
	private final String from;
	private final String to;

	public Pair(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Pair pair = (Pair)object;
		return from == pair.from && to == pair.to;
	}
}
