package mail.util;

import mail.MimeMessageHeader;
import mail.util.AddressParser.AddressParserStates;

/**
 * Class responsible for any action like validations in 
 * mail message header
 * @author zbychu
 *
 */

public class HeadersUtils {
	
	private final static String DESTINATION_HEADERS = "to resent-tom cc resent-cc bcc resent-bcc";
	private final static String FROM_AND_SENDER_HEADERS = "from sender";
	
	public static void validateHeader(MimeMessageHeader header) {
		
		
		if (FROM_AND_SENDER_HEADERS.contains(header.getName().toLowerCase())) // from header 
			validateFromHeader(header);
		else if (DESTINATION_HEADERS.contains(header.getName().toLowerCase())) //destination header 
			validateDestinationHeader(header);
		
		
	}
	
	/**
	 * "From"       ":"   mailbox  ; Single author
	 *  / ( "Sender"     ":"   mailbox  ; Actual submittor
	 * @param header
	 */
	public static void validateFromHeader(MimeMessageHeader header) {
		AddressParser.validateAddress(header.getValue());
	}
	
	/**
	 * Destination header is
	 * to, resent-tom cc, resent-cc, bcc, resent-bcc
	 * it should countain one or more addresses ac to RFC822
	 * 
	 * destination =  "To"          ":" 1#address  ; Primary
                 /  "Resent-To"   ":" 1#address
                 /  "cc"          ":" 1#address  ; Secondary
                 /  "Resent-cc"   ":" 1#address
                 /  "bcc"         ":"  #address  ; Blind carbon
                 /  "Resent-bcc"  ":"  #address
	 * 
	 * addresses are seperated by commas
	 * @param header
	 */
	public static void validateDestinationHeader(MimeMessageHeader header) {
		AddressParser.validateAddress(header.getValue());
	}
	
	/**
	 * "Resent-From"      ":"   mailbox
	 * 	/ ( "Resent-Sender"    ":"   mailbox
	 * 
	 */
	public static void validateReplytoHeader(MimeMessageHeader header) {
	}
	
	/**
	 * 
	 * "Received"    ":"            ; one per relay
                       ["from" domain]           ; sending host
                       ["by"   domain]           ; receiving host
                       ["via"  atom]             ; physical path
                      *("with" atom)             ; link/mail protocol
                       ["id"   msg-id]           ; receiver msg id
                       ["for"  addr-spec]        ; initial form
	 * 
	 * @param header
	 */
	public static void validateReceivedHeader(MimeMessageHeader header) {
	}
		
}
