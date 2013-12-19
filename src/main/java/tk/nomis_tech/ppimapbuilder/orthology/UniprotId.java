package tk.nomis_tech.ppimapbuilder.orthology;

/**
 *
 * @author Kevin Gravouil
 */
class UniprotId {

	/**
	 * Exception throwed when Uniprot ID is invalid.
	 */
	public class BadUniprotIdFormatException extends Exception {

		/**
		 * Exception throwed if String does not match Uniprot ID pattern.
		 */
		public BadUniprotIdFormatException() {
		}

		/**
		 * Exception throwed if String does not match Uniprot ID pattern.
		 *
		 * @param message
		 */
		public BadUniprotIdFormatException(String message) {
			super(message);
		}

	}

	/**
	 * Uniprot ID pattern. According to
	 * http://www.ebi.ac.uk/miriam/main/export/xml/
	 */
	public final static String pattern = "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\.\\d+)?$";
	/**
	 * The Uniprot ID.
	 */
	private String uniprotId;

	/**
	 * Construc a Uniprot ID object if the given string is valid or throw an
	 * BadUniprotIdFormatException.
	 *
	 * @param uniprotId
	 * @throws
	 * tk.nomis_tech.ppimapbuilder.orthology.UniprotId.BadUniprotIdFormatException
	 */
	public UniprotId(String uniprotId) throws BadUniprotIdFormatException {
		if (uniprotId.matches(pattern)) {
			this.uniprotId = uniprotId;
		} else {
			throw new BadUniprotIdFormatException("Id is not Uniprot-like");
		}
	}

	/**
	 * Get the Uniprot ID.
	 *
	 * @return
	 */
	public String getUniprotId() {
		return uniprotId;
	}

	/**
	 * Set the Uniprot ID.
	 *
	 * @param uniprotId
	 */
	public void setUniprotId(String uniprotId) throws BadUniprotIdFormatException {
		if (uniprotId.matches(pattern)) {
			this.uniprotId = uniprotId;
		} else {
			throw new BadUniprotIdFormatException("Id is not Uniprot-like");
		}
	}

	/**
	 * Test if a given string matches the Uniprot ID pattern.
	 *
	 * @param uniprotId
	 * @return boolean
	 */
	public static boolean isValid(String uniprotId) {
		return uniprotId.matches(pattern);
	}

}
