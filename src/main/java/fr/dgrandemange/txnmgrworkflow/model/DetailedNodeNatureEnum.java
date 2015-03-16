package fr.dgrandemange.txnmgrworkflow.model;

/**
 * @author dgrandemange
 * 
 */
public enum DetailedNodeNatureEnum {
	undefGroupNature("G !UNDEF!", "group-undef.png"), subflowNature("S", "subflow.png"), groupNature("G",
			"group.png"), undefParticipantNature("P !UNDEF!", "participant-undef.png"), participantNature("P",
			"participant.png");

	private String alt;
	private String icon;

	DetailedNodeNatureEnum(String alt, String icon) {
		this.alt = alt;
		this.icon = icon;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @return the alt
	 */
	public String getAlt() {
		return alt;
	}
}
