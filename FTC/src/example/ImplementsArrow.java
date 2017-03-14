package example;


public class ImplementsArrow extends Arrow {

	// Key will be user-friendly name of implementor,
	// value is a list of the user-friendly class names that the implementor
	// implements

	// Standard GraphViz boilerplate for an implements Arrow.
	private String implementsFormatting;
	
	public ImplementsArrow(String pointsFrom, String pointsTo) {
		this.pointsFrom = pointsFrom;
		this.pointsTo = pointsTo;
		this.attributes.put("arrowhead", "\"onormal\"");
		this.attributes.put("style", "\"dashed\"");
		this.attributes.put("color", "black");
		
		this.arrowType = "implements";
		
		//build arrow with attributes
		implementsFormatting = "[";
	}

	@Override
	public String convert() {
		// Extends Arrow GraphViz code example: 
		// WeatherData -> Subject [arrowhead="onormal", style="dashed"];
		// Comes from UML Example on CSSE374 Moodle page.
		implementsFormatting += this.generateAttributeTags() + "];";
		return pointsFrom + "->" + pointsTo + implementsFormatting + "\n";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!ImplementsArrow.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		ImplementsArrow a = (ImplementsArrow) obj;
		if(this.pointsFrom.equals(a.pointsFrom) && this.pointsTo.equals(a.pointsTo)) {
			return true;
		}
		return false;
	}
}
