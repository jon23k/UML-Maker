package example;

public class DependencyArrow extends Arrow {
	
	private String dependencyFormatting;
	
	public DependencyArrow(String pointsFrom, String pointsTo) {
		super();
		this.pointsFrom = pointsFrom;
		this.pointsTo = pointsTo;		
		this.attributes.put("arrowhead", "\"open\"");
		this.attributes.put("style", "\"dashed\"");
		this.attributes.put("color", "black");
		
		this.arrowType = "dependency";
		
		//build arrow with attributes
		dependencyFormatting = "[";
	}

	@Override
	public String convert() {
		dependencyFormatting += this.generateAttributeTags() + "];";
		return pointsFrom + "->" + pointsTo + dependencyFormatting + "\n";
	}
	
	public void setOneToMany(boolean b) {
		isOneToMany = b;
	}
}
