package pt.floraon.entities;

import pt.floraon.driver.DatabaseException;

public abstract class NamedDBNode extends GeneralDBNode {
	protected String name;

	public NamedDBNode(String name) throws DatabaseException {
		super();
		if(name != null && name.trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = name != null ? name.trim() : null;
	}

	public NamedDBNode(Object name) throws DatabaseException {
		super();
		if(name != null && name.toString().trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = name != null ? name.toString().trim() : null;
	}

	public NamedDBNode(NamedDBNode n) throws DatabaseException {
		super(n);
		if(n.name != null && n.name.trim().length() == 0) throw new DatabaseException("Node must have a name");
		this.name = n.name != null ? n.name.trim() : null;
	}
	
	public NamedDBNode() {
		super();
	}

	public String getName() {
		return this.name;
	}
}
