package fr.dgrandemange.txnmgrworkflow.model;

/**
 * @author dgrandemange
 * 
 */
public class Wrapper {

	private Object wrapped;

	public Wrapper(Object wrapped) {
		super();
		this.wrapped = wrapped;
	}

	public Object getWrapped() {
		return wrapped;
	}

	public void setWrapped(Object wrapped) {
		this.wrapped = wrapped;
	}

}
