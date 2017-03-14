package example;
import java.util.HashMap;
import java.util.Map;

public abstract class Arrow {
	
	public String pointsFrom; // Class that the arrow points from
	public String pointsTo; // Class that the arrow points to
	public Map<String, String> attributes = new HashMap<String, String>();
	// Controls if the Arrow should display the one-to-many head annotation
	public boolean isOneToMany = false;
	public String arrowType = "";
	
	/**
	 * Converts the stored arrows into a string that will then be
	 * written to the GraphViz file.  This will use the "User-friendly names"
	 * */
	public abstract String convert();
	
	/**
	 * gets all attributes attached to this arrow
	 * */
	public Map<String, String> getAttributes(){
		return this.attributes;
	}
	
	/**
	 * Generates the formatting tag for this arrow based on its attributes.
	 * */
	public String generateAttributeTags() {
		String delim = "";
		String toReturn = "";
		for(String key : attributes.keySet()){
			toReturn += delim + key +"="+ attributes.get(key);
			delim = ", ";
		}
		return toReturn;
	}
	
	/**
	 * Override equals so we can use a Set of arrows to filter out duplicates
	 * */
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(!Arrow.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		Arrow a = (Arrow) obj;
		if(this.pointsFrom.equals(a.pointsFrom) && this.pointsTo.equals(a.pointsTo)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Must also override the hashcode for the set to filter properly
	 * */
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + pointsFrom.hashCode();
		result = 31 * result + pointsTo.hashCode();
		return result;
	}
	
	/**
	 * Convenience method to determine which "type" of arrow this is
	 * */
	public String getArrowType() {
		return this.arrowType;
	}
}
