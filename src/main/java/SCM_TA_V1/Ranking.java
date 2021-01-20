package main.java.SCM_TA_V1;

public class Ranking<T,U> {

	public final T entity;
	public final U metric;
	
	public Ranking(T entity, U metric){
		this.entity=entity;
		this.metric=metric;
	}
	
	public T getEntity(){
		return entity;
	}
	public U getMetric(){
		return metric;
	}
	
	
}
