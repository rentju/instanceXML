

public class PairValue<V1, V2> {
	public V1 first;
	public V2 second;

	public PairValue() {
	}

	public PairValue(V1 v1, V2 v2) {
		this.first = v1;
		this.second = v2;
	}

	public V1 getV1() {
		return first;
	}

	public void setV1(V1 v1) {
		this.first = v1;
	}

	public V2 getV2() {
		return second;
	}

	public void setV2(V2 v2) {
		this.second = v2;
	}

	@Override
	public String toString() {
		return "[v1=" + String.valueOf(first) + ",v2=" + String.valueOf(second) + "]";
	}

	public boolean equals(PairValue<V1, V2> o) {
		if (o == null) {
			return false;
		}
		return toString().equals(o.toString());
	}
}
