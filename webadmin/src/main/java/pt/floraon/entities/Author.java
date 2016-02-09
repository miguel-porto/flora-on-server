package pt.floraon.entities;

public class Author extends GeneralDBNode {
	protected Integer idAut;
	protected String name;
	protected String email;
	protected String acronym;
	protected Integer level;
	protected String username;
	
	
	public Author(Integer idAut,String name,String email,String acronym,String username,Integer level) {
		this.idAut=idAut;
		this.name=name;
		this.email=email;
		this.acronym=acronym;
		this.username=username;
		this.level=level;
	}
	
	public Author(Author aut) {
		super(aut);
		this.idAut=aut.idAut;
		this.name=aut.name;
		this.email=aut.email;
		this.acronym=aut.acronym;
		this.username=aut.username;
		this.level=aut.level;
	}
	
	public Integer getIdAut() {
		return this.idAut;
	}
}
