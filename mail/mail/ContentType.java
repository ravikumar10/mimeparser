package mail;

import mail.exceptions.ParseException;
import mail.util.HeaderTokenizer;
import mail.util.ParameterList;

public class ContentType {
	
	final String BOUNDARY = "boundary";
	
	private String primaryType;	// primary type like text
    private String subType;	// subtype line plain
    private ParameterList list;	// parameter list

    /**
     * No-arg Constructor.
     */
    public ContentType() { }

    /**
     * Constructor.
     *
     * @param	primaryType	primary type
     * @param	subType	subType
     * @param	list	ParameterList
     */
    public ContentType(String primaryType, String subType) {
    	this.primaryType = primaryType;
    	this.subType = subType;
    }
	
    /**
     * Return the specified parameter value. Returns <code>null</code>
     * if this parameter is absent.
     * @return	parameter value
     */
    public String getParameter(String name) {
    	if (list == null)
    		return null;
    	return list.get(name);
    }
    
    /**
     * creates ContentType object from content of ContentType header
     * @param contentTypeHeader
     */
    public ContentType(String contentTypeHeader) throws ParseException {
    	HeaderTokenizer h = new HeaderTokenizer(contentTypeHeader, HeaderTokenizer.MIME);
    	HeaderTokenizer.Token tk;

    	// First "type" ..
    	tk = h.next();
    	if (tk.getType() != HeaderTokenizer.Token.ATOM)
    	    throw new ParseException();
    	primaryType = tk.getValue();

    	// The '/' separator ..
    	tk = h.next();
    	if ((char)tk.getType() != '/')
    	    throw new ParseException();

    	// Then "subType" ..
    	tk = h.next();
    	if (tk.getType() != HeaderTokenizer.Token.ATOM)
    	    throw new ParseException();
    	subType = tk.getValue();

    	// Finally parameters ..
    	String rem = h.getRemainder();
    	if (rem != null)
    	    list = new ParameterList(rem);
    	
    	
    	
    }
    public ContentType(String primaryType, String subType, ParameterList list) {
    	this.primaryType = primaryType;
    	this.subType = subType;
    	this.list = list;
    }
    
    /**
     * Return the primary type.
     * @return the primary type
     */
    public String getPrimaryType() {
    	return primaryType;
    }

    /**
     * Return the subType.
     * @return the subType
     */
    public String getSubType() {
    	return subType;
    }

    /**
     * Return the MIME type string, without the parameters.
     * The returned value is basically the concatenation of
     * the primaryType, the '/' character and the secondaryType.
     *
     * @return the type
     */
    public String getBaseType() {
    	return primaryType + '/' + subType;
    }
	
    public static void main(String[] args) {
    	

    	String contentType = "multipart/mixed;boundary=\"----=_NextPart_000_0063_01C82582.9D9C6DF0\"";
    	ContentType ct = null;
		try {
			ct = new ContentType(contentType);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(ct.getBaseType());
    	
    	System.out.println(ct.getList());
    	
	}

	public ParameterList getList() {
		return list;
	}

	public void setList(ParameterList list) {
		this.list = list;
	}

	public void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
    
	public String getBoundaryLine() {
		
		String boundary = null;
		boundary = getParameter(this.BOUNDARY);
		if (boundary!=null) {
			//TODO!! nie wszystkie maile sa takie fajne ze maja boundary 
			// z -- dodatkowym
			boundary = "--" + boundary;
		} 
		return boundary;
	}
}
