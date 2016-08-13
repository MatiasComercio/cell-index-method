package ar.edu.itba.ss.cellindexmethod.models;

import org.immutables.builder.Builder;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
				typeAbstract = "*Abs",
				typeImmutable = "*",
				get = ""
)
public abstract class PointAbs {
	@Builder.Parameter
	public abstract double x();
	
	@Builder.Parameter
	public abstract double y();
	
	@Value.Default
	public double radio() {
		return 0;
	}
	
	@Value.Check
	protected void checkRadio() {
		if (radio() < 0) {
			throw new IllegalArgumentException("Radio should be >= 0");
		}
	}
}
