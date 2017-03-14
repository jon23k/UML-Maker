package example;

public class AssociationArrow extends Arrow {
	
	private String assocFormatting;
	
	public AssociationArrow(String pointsFrom, String pointsTo) {
		super();
		this.pointsFrom = pointsFrom;
		this.pointsTo = pointsTo;
		
		this.arrowType = "association";
		
		//add attributes here
		this.attributes.put("arrowhead", "\"open\"");
		this.attributes.put("color", "\"black\"");
		assocFormatting = "[";
	}

	@Override
	public String convert() {
		if(isOneToMany) {
			this.attributes.put("headlabel", "\"1..*\"");
		}
		assocFormatting += this.generateAttributeTags() + "];";
		return pointsFrom + "->" + pointsTo + assocFormatting + "\n";
	}
	
	public void setOneToMany(boolean b) {
		isOneToMany = b;
	}
}
