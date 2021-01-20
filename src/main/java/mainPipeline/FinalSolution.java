package main.java.mainPipeline;

public class FinalSolution<S, C, D> {
	private S solution;
	private C cost;
	private D diffusion;
	
	public FinalSolution(S soultion, C cost, D diffusion) {
		this.cost = cost;
		this.diffusion = diffusion;
		this.solution = soultion;
	}
	
	public C getCost() {
		return this.cost;
	}
	
	public D getDiffusion() {
		return this.diffusion;
	}
	
	public S getSolution() {
		return this.solution;
	}
	
	public void setSolution(S solution) {
		this.solution = solution;
	}
	
	public void setCost(C cost) {
		this.cost = cost;
	}
	
	public void setDiffusion(D diffusion) {
		this.diffusion = diffusion;
	}
	
}
