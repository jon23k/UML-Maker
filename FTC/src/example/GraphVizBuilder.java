package example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class GraphVizBuilder {
	
	private List<JClass> classes;
	private Map<String, JClass> hashToJClass = new HashMap<String, JClass>();
	private Map<String, String> dummyClassesToRender = new HashMap<String, String>();
	private Set<Arrow> arrows = new HashSet<Arrow>();
	private List<String> primitiveTypes = new ArrayList<String>() {{
	    add("boolean");
	    add("int");
	    add("void");
	    add("long");
	    add("float");
	    add("short");
	    add("char");
	    add("byte");
	    add("double");
	    // Primitive type prefixes from bytecode
		add("B");
		add("C");
		add("V");
		add("D");
		add("F");
		add("I");
		add("J");
		add("S");
		add("Z");
	}};
	
	public GraphVizBuilder(List<ClassNode> classNodes) {
		this.classes = new ArrayList<JClass>();
		for(ClassNode node : classNodes){
			JClass jclass = new JClass(node);
			classes.add(jclass);
			String hashName = jclass.getHashName();
			hashToJClass.put(hashName, jclass);
		}
//		System.out.println(classes.get(0).getSuperName());
		
		// Create all Arrow Objects
		for(String hashName : this.hashToJClass.keySet()) {
			addExtendArrow(hashName);
			addImplementsArrows(hashName);
			addAssociationArrows(hashName);
			addDependencyArrows(hashName);
		}
	}
	
	public List<JClass> getClasses(){
		return this.classes;
	}
	
	public Set<Arrow> getArrows(){
		return this.arrows;
	}

	public void run() {
		// Build Class String Boilerplate
		String finalResult = "digraph example{\n";
		finalResult += "rankdir = BT;\n";
		finalResult += "node [\n\tshape=\"record\"\n]\n";
		
		// Append each class's GV String
		for(JClass jclass : classes){
			finalResult += jclass.buildString();
		}
		
		// Append each dummy Class
		for(String dummyHashName : this.dummyClassesToRender.keySet()) {
			String dummyFQName = this.dummyClassesToRender.get(dummyHashName);
			finalResult += JClass.buildDummyClassString(dummyHashName, dummyFQName);
		}
		
		// Append each Arrow's GV String
		for(Arrow arrow : arrows) {
			finalResult += arrow.convert();
		}
		
		finalResult += "}";
		
		// Write finalResultString to file.
		try {
			FileOutputStream output = new FileOutputStream("./output/output.dot");
			output.write(finalResult.getBytes());
		}
		catch(IOException e) {
			System.err.println("Failed to create output file.");
		}
		
	}
	
	public void addExtendArrow(String hashName) {
		JClass pointsFromClass = this.hashToJClass.get(hashName);
		if(pointsFromClass.hasSuper()) {
			String supername = pointsFromClass.getSuperName();
			String superHash = Integer.toString(supername.hashCode());
			
			// Check to see if we are rendering the superclass
			if(!this.hashToJClass.containsKey(superHash)) {
				// We aren't rendering the superclass by default.
				// Make sure we render a "dummy" class that maps 
				// the hash to the real class's name.
				this.dummyClassesToRender.put(superHash, supername);
			}
			
			Arrow eArrow = new ExtendsArrow(pointsFromClass.getHashName(), superHash);
			arrows.add(eArrow);
		}
	}
	
	public void addImplementsArrows(String hashName) {
		JClass jclass = this.hashToJClass.get(hashName);
		if(jclass.hasInterfaces()) {
			List<String> interfaces = jclass.getInterfaces();
			for(String s : interfaces) {
				String interfaceHash = Integer.toString(s.hashCode());
				if(!this.hashToJClass.containsKey(interfaceHash)) {
					this.dummyClassesToRender.put(interfaceHash, s);
				}
				arrows.add(new ImplementsArrow(hashName, interfaceHash));
			}
		}
	}
	
	//helper method to make sure arrows are not made to a primitive type
	public boolean isPrimitiveType(String type){		
		if(primitiveTypes.contains(type)){
			return true;
		}
		return false;
	}
	
	public void addAssociationArrows(String hashName) {
		JClass jclass = this.hashToJClass.get(hashName);
		if (jclass.hasAssociations()) {
			List<FieldNode> fields = jclass.getFields(); // All fields of JClass
			for (FieldNode field : fields) {
				// Get fully qualified names for all types in the field
				// (including parameterized types)
				String[] fieldTypes = JClass.getFieldNodeTypeFQNames(field);
				// Add AssociationArrows from the field types
				this.createAndAddADArrowsFromType(jclass, fieldTypes, 'a');
			}
		}
	}
	
	public void addDependencyArrows(String hashName) {
		JClass jclass = this.hashToJClass.get(hashName);
		if (jclass.hasDependencies()) {

			// gets all the methods from our class
			List<MethodNode> methods = jclass.getMethods();

			// iterates through every method in the class
			for (MethodNode method : methods) {
				// Get fully qualified names for all return types of method
				// (including parameterized types)
				String[] returnTypes = JClass.getMethodReturnTypeFQNames(method);
				
				// Add dependencyArrows from methodReturn types.
				this.createAndAddADArrowsFromType(jclass, returnTypes, 'd');

				// Get FQ names for all Argument types of this method
				String[] argTypes = JClass.getMethodArgumentTypeFQNames(method);
				// Add DependencyArrows from method argument types
				this.createAndAddADArrowsFromType(jclass, argTypes, 'd');

			}
		}
	}
	
	private void createAndAddADArrowsFromType(JClass jclass, String[] typeArray, char ADKey) {
		// Need to keep track of collections/map occurrences
		boolean lastWasCollection = false;
		int lastMapOccurrence = 0;
		for (int i = 0; i < typeArray.length; i++) {
			String ft = typeArray[i];
			if (this.isPrimitiveType(ft.replaceAll("\\[", "")) || ft.length() == 0) {
				continue;
			}
			if (ft.contains("[")) {
				// This means that we have an array. Toggle 1-to-many flag
				Arrow arrow = this.createADArrow(jclass.getName(), ft.replaceAll("\\[\\]", ""), ADKey, true);
				// Add the arrow to the hashset
				arrows.add(arrow);
			} else if (Collection.class.isAssignableFrom(GraphVizBuilder.getClassFromFullyQualifiedName(ft))) {
				// We have a collection. Set a local flag to toggle 1-to-many
				// flag with next field types.
				lastWasCollection = true;
			} else if (Map.class.isAssignableFrom(GraphVizBuilder.getClassFromFullyQualifiedName(ft))) {
				// We have a Map. Set local flag to toggle 1-to-many flag with
				// next 2 field types.
				lastMapOccurrence = 2;
			} else if (lastWasCollection) {
				// Now that we have the parameterized type for the collection,
				// create the arrow.
				lastWasCollection = false;
				Arrow arrow = this.createADArrow(jclass.getName(), ft, ADKey, true);
				// Add the arrow to the hashset
				arrows.add(arrow);

			} else if (lastMapOccurrence >= 1) {
				// Now that we have the parameterized type for the map, create
				// the arrow.
				lastMapOccurrence--;
				Arrow arrow = this.createADArrow(jclass.getName(), ft, ADKey, true);
				// Add the arrow to the hashset
				arrows.add(arrow);
			} else {
				// No one to many flag needed.
				Arrow arrow = this.createADArrow(jclass.getName(), ft, ADKey, false);
				// Add the arrow to the hashset
				arrows.add(arrow);

			}

			// If they don't parameterize the Collection/Map, we should
			// draw an arrow to the collection/map type.
			// If this happens, it will be the last entry in the fieldTypes
			// array.
			boolean unparameterizedCollectionCheck = (i == typeArray.length - 1) && lastWasCollection;
			boolean unparameterizedMapCheck = (i == typeArray.length - 1) && (lastMapOccurrence == 2);
			if (unparameterizedCollectionCheck || unparameterizedMapCheck) {
				Arrow arrow = this.createADArrow(jclass.getName(), ft, ADKey, false);
				// Add the arrow to the hashset
				arrows.add(arrow);
			}
		}
	}
	
	/**
	 * Creates and returns an Association or Dependency Arrow.  
	 * 
	 * Throws: IllegalArgumentException - if ADKey is not 'a' or 'd'
	 * 
	 * Parameters: pointsTo  - non pretty fully qualified name of the class to be
	 * 						  contained in the Arrow's pointsTo field.
	 * 			   ADKey 	 - 'a' for AssociationArrow, 'd' for DependencyArrow. 
	 * 			   oneToMany - sets value of the oneToMany flag on the arrow created
	 */
	private Arrow createADArrow(String pointsFrom, String pointsTo, char ADKey, boolean oneToMany) {
		String[] ftsNoOpenBracket = pointsTo.split("\\[");
		// If there are 2 dimensional arrays, this still works
		pointsTo = ftsNoOpenBracket[ftsNoOpenBracket.length - 1];
		// Replace all '/' with periods
		pointsTo = pointsTo.replaceAll("\\/", ".");
		
		String pointsToHash = Integer.toString(pointsTo.hashCode());
		String pointsFromHash = Integer.toString(pointsFrom.hashCode());
		
		if(!this.hashToJClass.containsKey(pointsToHash)) {
			this.dummyClassesToRender.put(pointsToHash, pointsTo);
		}

		if (ADKey == 'a') {
			AssociationArrow a = new AssociationArrow(pointsFromHash, pointsToHash);
			a.setOneToMany(oneToMany);
			return a;
		} else if (ADKey == 'd') {
			DependencyArrow a = new DependencyArrow(pointsFromHash, pointsToHash);
			a.setOneToMany(oneToMany);
			return a;
		} else {
			throw new IllegalArgumentException(
					"The provided ADKey was '" + ADKey + "', but only 'a' and 'd' are valid.");
		}
	}
	
	/**
	 * Returns a string of the "pretty" name to use for GraphViz output given a 
	 * fully qualified class name that may still have the bytecode prefixes.
	 * 
	 * Example: getPrettyName("Ljava.lang.String") returns "String"
	 * 
	 * Parameters: s - FQ class name with bytecode prefixes.
	 * */
	private String getPrettyName(String s) {
		String[] ftsNoOpenBracket = s.split("\\[");
		// If there are 2 dimensional arrays, this still works
		s = ftsNoOpenBracket[ftsNoOpenBracket.length - 1];
		// Get Pretty Name
		s.replaceAll("\\/", ".");
		String[] typeNameArray = s.split("\\.");
		
		String typeName = typeNameArray[typeNameArray.length - 1];
		return typeName;
	}
	
	/**
	 * Returns the Class object associated with the class or interface with 
	 * the given fully qualified name.
	 * 
	 * Throws: ClassNotFoundException
	 * 
	 * Parameters: internalName - fully qualified name of class or interface
	 * */
	private static Class<?> getClassFromFullyQualifiedName(String internalName) {
		try {
			return Class.forName(internalName);
		} catch (ClassNotFoundException e) {
			System.err.println("Internal Name \"" + internalName + "\" does not exist.");
			e.printStackTrace();
			return null;
		}
	}
	
}
