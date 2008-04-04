package mail.util;

import mail.exceptions.MimeMessageHeaderAddressValidationException;

public class AddressParser {
	
	
	enum AddressParserStates { IN_ROUTE_ADDRESS, OUT_ROUTE_ADDRESS, AFTER_ROUTE_ADDRESS
		} //states that tell us about  
	
	/**
	 * Semantic for address
	 * 
	 address     =  mailbox                      ; one addressee
     				/  group                        ; named list

	 mailbox     =  addr-spec                    ; simple address
                 	/  phrase route-addr            ; name & addr-spec
	 
	 route-addr  =  "<" [route] addr-spec ">"

     route       =  1#("@" domain) ":"           ; path-relative
	 
	 addr-spec   =  local-part "@" domain        ; global address
	 
	 local-part  =  word *("." word)             ; uninterpreted
                                                 ; case-preserved
	 
	 
	 domain      =  sub-domain *("." sub-domain) // sub-domain and 0 or more subdomain after dot

     sub-domain  =  domain-ref / domain-literal

     domain-ref  =  atom                         ; symbolic reference
	
	
	 atom        =  1*<any CHAR except specials, SPACE and CTLs>
	 
	 specials    =  "(" / ")" / "<" / ">" / "@"  ; Must be in quoted-
                 /  "," / ";" / ":" / "\" / <">  ;  string, to use
                 /  "." / "[" / "]"  
	 
	 word        =  atom / quoted-string
	 
	 quoted-string = <"> *(qtext/quoted-pair) <">; Regular qtext or
                                                 ;   quoted chars.

     qtext       =  <any CHAR excepting <">,     ; => may be folded
                     "\" & CR, and including
                     linear-white-space>
	 
	 quoted-pair =  "\" CHAR                     ; may quote any char
	 
	 domain-literal =  "[" *(dtext / quoted-pair) "]"
	 
	 
	 dtext       =  <any CHAR excluding "[",     ; => may be folded
                     "]", "\" & CR, & including
                     linear-white-space>
	 
	 
	 phrase      =  1*word                       ; Sequence of words
	 
	 group       =  phrase ":" [#mailbox] ";"
	 
	 */
	
	/**
	 * Validates addresses according to RFC822 - semantic above
	 * no line folded in stringAddress!!!
	 * also validates multipule addresses splited by comma ,
	 * f.e. 1#address
	 * 
	 * eg.
	  -- 
	   "Zbigniew Artemiuk" <zbigniew.artemiuk@gmail.com>
	  -- 
	   Zbigniew Artemiuk <zbychu@astronet.pl>,
        Zbigniew Artemiuk <zbychu@astronet.pl>
        
	 * @param address without line folded
	 */
	public static void validateAddress(String stringAddress) {
		//32 to space
		//46 to .
		//64 to @
		//60 to <
		//62 to >
		//92 to \
		//34 to "
		//91 to [
		//93 to ]
		//44 to ,
		
		AddressParserStates routeAddressState = AddressParserStates.OUT_ROUTE_ADDRESS;
		int spaceCounter = 0;
		int quotedStringCounter=0;
		boolean quotedText=false;
		boolean wasPhrase=false;
		boolean dotEncounter=false;
		boolean atEncounter=false;
		boolean atAppeared=false;
		
		char beforeChar = " ".charAt(0);
		for (int i = 0; i < stringAddress.length(); i++) {
			char c = stringAddress.charAt(i);
			
			if (routeAddressState==AddressParserStates.AFTER_ROUTE_ADDRESS) {
				if (c==44) {// ,
					//resseting all states
					routeAddressState=AddressParserStates.OUT_ROUTE_ADDRESS;
					wasPhrase=false;
					atEncounter=false;
					atAppeared=false;
					spaceCounter=0;
					quotedStringCounter=0;
					continue;
				}
				String reason = "No " + c + " available at this stage";
				throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
			} else if (c == 32) { // space
				if (!quotedText) {
					if (routeAddressState==AddressParserStates.OUT_ROUTE_ADDRESS) {
						if (spaceCounter>1) {
							String reason = "Space outside qouted string - maybe mail from Thunderbird";
							throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
						}
						spaceCounter++;
						wasPhrase=true;
					} else {
						String reason = "Space inside address";
						throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
					}
					spaceCounter++;
				}
			} else if (StringUtils.isSpecialCharacter(c)) {
				if (!quotedText) { // jezeli nie jest w qouted stringu to moze to specjalne przypadki dla 
					//: - na razie nie obslugiwane
 					//; - na razie nie obslugiwane
					//@ - address
					//< - rozpoczecie address route
					//> - koniec address route
					//" - qouted
					//[ - domain ref beg
					//] - domain ref end
					
					if (dotEncounter || atEncounter) { // special sign after @ and .
						if (dotEncounter) {
							String reason = "Special char " + c +" not allowed after .";
							throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
						} else if (atEncounter) {
							if (c == 91) {// [
								
							} else if (c == 91) {// ]
								
							} else {
								String reason = "Special char " + c +" not allowed after @";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							}
						}
					} else { // others
						if (c == 60) { // <
							if (beforeChar!=32) {
								String reason = "" + c + " without space before";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else if (routeAddressState==AddressParserStates.IN_ROUTE_ADDRESS) {
								String reason = c + "not allowed on this stage - in route address";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else {
								routeAddressState=AddressParserStates.IN_ROUTE_ADDRESS;
							}
						} else if (c==62){ // >
							if (routeAddressState==AddressParserStates.IN_ROUTE_ADDRESS){
								if (beforeChar==60) {
									System.out.println("Nothing insde <>");
								}
								if (!atAppeared) {
									String reason = "@ not in address - should be instead of " + c;
									throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
								}
								routeAddressState=AddressParserStates.AFTER_ROUTE_ADDRESS;
							} else {
								String reason = "" + c + " without < before";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							}
						} else if (c == 64) { //@
							if (StringUtils.isSpecialCharacter(beforeChar) ||
								StringUtils.isCTLCharacter(beforeChar) ||
								beforeChar==32 ||//space
								beforeChar==46 // .
								) 
							{
								String reason = "Not allowed char " + c +  " before @";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else if (atAppeared){
								String reason = "Second encounter of @";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else if(routeAddressState==AddressParserStates.OUT_ROUTE_ADDRESS && wasPhrase) {
								String reason = "@ cannot encounter on this stage";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else {
								atEncounter=true;
								atAppeared=true;
								beforeChar=c;
								continue;
							}
						} else if (c == 34) {// "
							if (beforeChar!=92) {// not \ before "
								quotedStringCounter++;
								quotedText=!quotedText;
							}
						} else if (c == 46) {// .
							if (StringUtils.isSpecialCharacter(beforeChar) ||
								StringUtils.isCTLCharacter(beforeChar) ||
								beforeChar==32//space
								)
							{
								String reason = "Wrong char " + beforeChar + " before .";
								throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
							} else {
								dotEncounter=true;
								beforeChar=c;
								continue;
							}
						} else {
							String reason = "Special character " + c + " outside qouted string";
							throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
						}
					}
				} else { // special character in qouted string
					if (c == 34) {// " - end of quoted string
						if (beforeChar!=92) {// not \ before "
							quotedStringCounter++;	
							quotedText=!quotedText;
						}
					}
				}
			} else if (StringUtils.isCTLCharacter(c)) {//ctl characters
				 
				if (!quotedText) {
					String reason = "CTL character " + c + " outside qouted string";
					throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
				} else {
					if (dotEncounter) {
						String reason = "CTL char" + c + "not allowed after .";
						throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
					} else if (atEncounter) {
						String reason = "CTL char" + c + "not allowed after @";
						throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
					} else {
						if ( (c==92 || c==34 || c==13) && beforeChar != 92 ) {
							String reason = "No " + c + " available here without escaping by \\";
							throw new MimeMessageHeaderAddressValidationException(stringAddress,reason,i);
						}
					}
				}
			}  else {
				//just pass
			}

			beforeChar=c;
			dotEncounter=false;
			atEncounter=false;
		}
		
		if (routeAddressState!=AddressParserStates.AFTER_ROUTE_ADDRESS && !atAppeared){
			String reason = "No address in mail";
			throw new MimeMessageHeaderAddressValidationException(reason);
		} else if (dotEncounter || atEncounter) {
			String reason = "No @ or . available at the end of email";
			throw new MimeMessageHeaderAddressValidationException(reason);
		}
		
		if (quotedStringCounter%2!=0) {
			String reason = "Number of \" is wrong";
			throw new MimeMessageHeaderAddressValidationException(reason);
		}
		 
	}
	
	public static void main(String[] args) {
		
		String address = "\"Zbigniew \" Artemiuk\" <s@s>";
		String address2 = "zbychu@fds\fds.pl";
		
		try {
			validateAddress(address);
		} catch (MimeMessageHeaderAddressValidationException ex) {
			ex.printStackTrace();
		}
		
		
		
	}
}
