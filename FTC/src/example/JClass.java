package example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

public class JClass {
	private ClassNode node;
	private List<FieldNode> fields;
	private List<MethodNode> methods;
	private List<String> interfaces;
	private String superclass;
	private Map<String, String> attributes;
	private Map<String, String> primitiveTypeIDs;
	private List<String> jClassArchetypeTags;
	private final String graphVizNewLine = "\\l";
	private static final String actualNewLine = System.lineSeparator();

	public JClass(ClassNode node) {
		this.node = node;
		this.fields = (List<FieldNode>) this.node.fields;
		this.methods = (List<MethodNode>) this.node.methods;
		this.interfaces = this.node.interfaces;
		this.superclass = this.node.superName;
		attributes = new HashMap<String, String>();
		attributes.put("color", "black");

		this.primitiveTypeIDs = new HashMap<String, String>();
		this.primitiveTypeIDs.put("B", "byte");
		this.primitiveTypeIDs.put("C", "char");
		this.primitiveTypeIDs.put("D", "double");
		this.primitiveTypeIDs.put("F", "float");
		this.primitiveTypeIDs.put("I", "int");
		this.primitiveTypeIDs.put("J", "long");
		this.primitiveTypeIDs.put("S", "short");
		this.primitiveTypeIDs.put("Z", "boolean");

		this.jClassArchetypeTags = new ArrayList<String>();
	}

	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	public boolean hasSuper() {
		return !(this.superclass == null);
	}

	public boolean hasInterfaces() {
		return !(this.interfaces == null);
	}

	public boolean hasAssociations() {
		return !(this.fields == null);
	}

	public boolean hasDependencies() {
		for (MethodNode mn : this.methods) {
			if (mn.desc != null || mn.parameters != null) {
				return true;
			}
		}
		return false;
	}

	public static String buildDummyClassString(String hashName, String FQName) {
		return hashName + " [label = \"{" + FQName + "}\"];" + actualNewLine;
	}

	public String buildString() {
		String result = "";
		// String className = Type.getObjectType(node.name).getClassName();

		result += this.getHashName() + " [";

		// Iterates through our attributes and sets them here
		for (String key : attributes.keySet()) {
			result += key + " = " + attributes.get(key) + ", ";
		}

		if ((node.access & Opcodes.ACC_INTERFACE) != 0) {
			this.jClassArchetypeTags.add("interface");
		}
		
		result += "label = \"{";
		for (String classTag : this.jClassArchetypeTags) {
			result += "\\<\\<" + classTag + "\\>\\>" + graphVizNewLine;
		}
		result += this.getName() + "|";
		result += buildFields(node);
		result += buildMethods(node);
		result += "}\"];" + actualNewLine;
		return result;
	}

	public String buildMethods(ClassNode classNode) {
		String result = "";
		for (MethodNode method : methods) {
			if (method.name.contains("<")) {
				continue;
			}
			Properties props = DesignParser.getProperties();
			boolean showSynthetic = Boolean.parseBoolean(props.getProperty("showSynthetic"));
			if ((method.access & Opcodes.ACC_SYNTHETIC) > 0 && !showSynthetic) {
				continue;
			} else {
				// get the access modifier (public, private, protected)
				result += buildMethodAccessModifier(method);
				// method return type
				System.out.println(Type.getReturnType(method.desc).getClassName());
				String[] returnTypeParts = Type.getReturnType(method.desc).getClassName().split("\\.");
				result += returnTypeParts[returnTypeParts.length - 1];
				// method name
				result += " " + method.name;

				// this part should get a whole list of the types of each
				// argument
				List<String> parameterTypes = new ArrayList<String>();
				for (Type argType : Type.getArgumentTypes(method.desc)) {
					parameterTypes.add(argType.getClassName());
				}
				// look at helper method
				result += buildMethodArguments(method, parameterTypes);
				result += graphVizNewLine;
			}
		}
		return result;
	}

	// this runs the helper method that will get the names of the arguments
	// and then it will combine it with the list of types we just got
	private String buildMethodArguments(MethodNode method, List<String> parameterTypes) {
		String result = "( ";
		if (method.parameters == null) {
			result += ") ";
			return result;
		} else {
			List<ParameterNode> parameters = (List<ParameterNode>) method.parameters;
			for (int i = 0; i < parameters.size(); i++) {
				if (i != parameters.size() - 1) {
					result += parameters.get(i).name + " : " + parameterTypes.get(i) + ")";
				} else {
					result += parameters.get(i).name + " : " + parameterTypes.get(i) + ", ";
				}
			}
			return result;
		}
	}

	// this handles the public, private, protected part.
	// used as a helper method for buildMethods
	private String buildMethodAccessModifier(MethodNode method) {
		if ((method.access & Opcodes.ACC_PUBLIC) > 0) {
			return "+ ";
		} else if ((method.access & Opcodes.ACC_PRIVATE) > 0) {
			return "- ";
		} else {
			return "# ";
		}
	}
	
	public ClassNode getClassNode() {
		return this.node;
	}

	public String buildFields(ClassNode classNode) {
		String result = "";
		for (FieldNode field : fields) {
			result += buildFieldAccessModifier(field);
			String tempFieldTypeName = Type.getObjectType(field.desc).getClassName().replaceAll(";", "");
			String[] tempFieldTypeNameSplit = tempFieldTypeName.split("\\.");
			String fieldTypeName = tempFieldTypeNameSplit[tempFieldTypeNameSplit.length - 1];
			if (!(this.primitiveTypeIDs.get(fieldTypeName) == null)) {
				fieldTypeName = this.primitiveTypeIDs.get(fieldTypeName);
			}
			result += fieldTypeName + " : ";
			result += field.name + graphVizNewLine;
		}

		// i have a newline - newline here so it will separate the fields from
		// the methods
		return result + /* graphVizNewLine + */ "|" /* + graphVizNewLine */;
	}

	private String buildFieldAccessModifier(FieldNode field) {
		if ((field.access & Opcodes.ACC_PUBLIC) > 0) {
			return "+ ";
		} else if ((field.access & Opcodes.ACC_PRIVATE) > 0) {
			return "- ";
		} else {
			return "# ";
		}
	}

	public String getName() {
		// Return FQ name with periods, no '/'
		return this.node.name.replaceAll("\\/", ".");
	}

	public String getHashName() {
		String FQName = this.getName();
		return Integer.toString(FQName.hashCode());
	}

	/**
	 * Returns String of fully qualified name for the superclass (if it exists)
	 */
	public String getSuperName() {
		String toReturn = node.superName;
		if (toReturn != null) {
			toReturn = toReturn.replaceAll("\\/", ".");
		}
		return toReturn;
	}

	/**
	 * returns Fully qualified names of all interfaces.
	 */
	public List<String> getInterfaces() {
		List<String> FQInterfaceNameList = new ArrayList<String>();
		for (String interfaceName : this.interfaces) {
			// Get the easy name and add it to our list
			String FQInterfaceName = interfaceName.replaceAll("\\/", ".");
			FQInterfaceNameList.add(FQInterfaceName);
		}
		return FQInterfaceNameList;
	}

	public List<MethodNode> getMethods() {
		return this.methods;
	}

	public List<FieldNode> getFields() {
		return this.fields;
	}

	public static String getClassNodeFQName(ClassNode cn) {
		return cn.name;
	}

	/**
	 * Returns an array with the java bytecode versions of fully qualified names
	 * for the field types. Returns an array since it gets each piece of a
	 * parameterized field (if that exists)
	 * 
	 * Parameters: fn - the FieldNode to get the FQ name for.
	 */
	public static String[] getFieldNodeTypeFQNames(FieldNode fn) {
		// If signature is null, then the field is not parameterized.
		// Use FieldNode's descriptor instead
		if (fn.signature != null) {
			return JClass.FQNameHelper(fn.signature);
		} else {
			return JClass.FQNameHelper(fn.desc);
		}
	}

	/**
	 * Returns an array with the java bytecode versions of fully qualified names
	 * for the method return type(s). Returns an array since it gets each piece
	 * of a parameterized type (if that exists)
	 * 
	 * Parameters: mn - the MethodNode to get the FQ name for.
	 */
	public static String[] getMethodReturnTypeFQNames(MethodNode mn) {
		// If signature is null, then the method is not parameterized.
		// Use MethodNode's descriptor instead
		if (mn.signature != null) {
			System.out.println(mn.signature);
			String[] returnType = mn.signature.split("\\)");
			return JClass.FQNameHelper(returnType[returnType.length - 1]);
		} else {
			String returnType = Type.getReturnType(mn.desc).getClassName();
			String[] arrayReturnType = new String[1];
			arrayReturnType[0] = returnType;
			return arrayReturnType;
		}
	}

	/**
	 * Returns an array with the java bytecode versions of fully qualified names
	 * for the method arg type(s). Returns an array since it gets each piece of
	 * a parameterized type (if that exists)
	 * 
	 * Parameters: mn - the MethodNode to get the FQ name for.
	 */
	public static String[] getMethodArgumentTypeFQNames(MethodNode mn) {
		// If signature is null, then the method is not parameterized.
		// Use MethodNode's descriptor instead
		if (mn.signature != null) {
			String[] argTypes = mn.signature.split("\\)");
			String argType = argTypes[0].replaceAll("\\(", "");
			if(argType.startsWith("II")) {
				argType = argType.substring(2);
			}
			return JClass.FQNameHelper(argType);
		} else {
			List<String> argTypesList = new ArrayList<String>();
//			System.out.println(mn.desc);
			for (Type argType : Type.getArgumentTypes(mn.desc)) {
				argTypesList.add(argType.getClassName());
//				System.out.println(argType.getClassName());
			}
			return argTypesList.toArray(new String[argTypesList.size()]);
		}
	}

	/**
	 * This is a helper method that takes in a String of whatever the type
	 * signature is and then returns an Array of String that has all the
	 * corresponding internal names in it.
	 */
	private static String[] FQNameHelper(String s) {
		List<String> toReturn = new ArrayList<String>();
		// Remove all '>' characters from signature
		if(s.contains("TT;")) {
			return new String[] {""};
		}
		String noCloseAngleBrackets = s.replaceAll("\\>", "");
		// All classes are divided by either '<' or ';'
		String[] typeParts = noCloseAngleBrackets.split(";");
		for (String part : typeParts) {
			String[] smallerTypes = part.split("<");
			// Add the parts to the "To Return" arrayList
			for (String t : smallerTypes) {
				// We can't draw wildcard classes that we don't know at runtime
				if (!t.contains("*")) {
					// All Class and Interface types have a prefix of L
					t = t.replaceFirst("L", "");
					// Inner classes are denoted by "$"
					String[] tMainClassOnly = t.split("$");
					// Discard the inner class part and replace / with . for
					// formatting.
					toReturn.add(tMainClassOnly[0].replaceAll("\\/", "\\."));
				}
			}
		}
		return toReturn.toArray(new String[toReturn.size()]);
	}

	public void setAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}
	
	public void addArchetypeClassTag(String classTag) {
		this.jClassArchetypeTags.add(classTag);
	}
}
