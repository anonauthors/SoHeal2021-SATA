package main.java.mainPipeline;

public enum Response {
	CLASS_1,
	CLASS_2,
	CLASS_3;
	
	public double getWeight(Response response) {
		switch (response) {
			case CLASS_1:
				return 0.3;
			case CLASS_2:
				return 0.2;
			case CLASS_3:
				return 0.1;
			default:
				return 0.1;
			}
	}
}
