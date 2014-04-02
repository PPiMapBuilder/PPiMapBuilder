package tk.nomis_tech.ppimapbuilder.webservice.psicquic.miql;

/**
 * Representation of a MiQL parameter<br/>
 * Example result:
 * <ul>
 * <li>"name:value"</li>
 * <li>"value"</li>
 * </ul>
 */
public class MiQLParameterBuilder extends AbstractMiQLQueryElement {

	public MiQLParameterBuilder(AbstractMiQLQueryElement value) {
		super(value);
	}
	
	public MiQLParameterBuilder(String value) {
		super(value);
	}
	
	public MiQLParameterBuilder(String name, String value) {
		this(name, (Object)value);
	}
	
	public MiQLParameterBuilder(String name, AbstractMiQLQueryElement value) {
		this(name, (Object)value);
	}
	
	public MiQLParameterBuilder(String name, Object value) {
		super(name+":");
		
		if(value instanceof AbstractMiQLQueryElement) add((AbstractMiQLQueryElement) value);
		else add(value.toString());
	}	
}