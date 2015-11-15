package pt.floraon.entities;

public class AuthorVertex extends GeneralDBNode {
	protected Integer idAut;
	protected String name;
	protected String email;
	protected String acronym;
	protected Integer level;
	protected String username;
	
	
	public AuthorVertex(Integer idAut,String name,String email,String acronym,String username,Integer level) {
		this.idAut=idAut;
		this.name=name;
		this.email=email;
		this.acronym=acronym;
		this.username=username;
		this.level=level;
	}
	
	public AuthorVertex(Author aut) {
		super(aut);
		this.idAut=aut.idAut;
		this.name=aut.name;
		this.email=aut.email;
		this.acronym=aut.acronym;
		this.username=aut.username;
		this.level=aut.level;
	}

	public AuthorVertex(AuthorVertex aut) {
		super(aut);
		this.idAut=aut.idAut;
		this.name=aut.name;
		this.email=aut.email;
		this.acronym=aut.acronym;
		this.username=aut.username;
		this.level=aut.level;

	}
}
