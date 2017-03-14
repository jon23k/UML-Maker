package example;

public class ExtendsArrow extends Arrow {
	
	// Key will be user-friendly name of Child, 
	// value is user-friendly name of parent (if applicable)
	private String extendsFormatting;
	
	public ExtendsArrow(String pointsFrom, String pointsTo) {
		super();
		this.pointsFrom = pointsFrom;
		this.pointsTo = pointsTo;
		this.attributes.put("arrowhead", "\"onormal\"");
		this.attributes.put("color", "black");
		this.arrowType = "extends";
		
		//build arrow with attributes
		extendsFormatting = "[";
		
	}

	@Override
	public String convert() {
		// Extends Arrow GraphViz code example: Dog -> Animal
		// Comes from http://www.ffnn.nl/pages/articles/media/uml-diagrams-using-graphviz-dot.php
		extendsFormatting += this.generateAttributeTags() + "];";
		return pointsFrom + "->" + pointsTo + extendsFormatting + "\n";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!ExtendsArrow.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		ExtendsArrow a = (ExtendsArrow) obj;
		if(this.pointsFrom.equals(a.pointsFrom) && this.pointsTo.equals(a.pointsTo)) {
			return true;
		}
		return false;
	}
}
