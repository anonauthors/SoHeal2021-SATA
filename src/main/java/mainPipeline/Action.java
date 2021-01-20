package main.java.mainPipeline;

public enum Action {
	COST,
	DIFFUSION;
	
	/**
	 * the method is intended to return the opposite action of the caller action when
	 * needed
	 * @return the opposite action of the caller
	 */
	public Action getOpposite() {
		switch (this) {
			case COST:
				return DIFFUSION;
			case DIFFUSION:
				return COST;
			default:
				return null;
		}
	}
}
