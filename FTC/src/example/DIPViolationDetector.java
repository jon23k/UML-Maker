package example;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

public class DIPViolationDetector implements PatternDetector {
	private String color;
	private double COSINE_CUTOFF = 0.5;
	
	public DIPViolationDetector() {
		Properties props = DesignParser.getProperties();
		this.color = props.getProperty("DIPViolationDetectorColor");
	}

	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		for(JClass c: classes) {
			String supername = c.getSuperName();
			JClass superClass = null;
			if(supername != null) {
				for(JClass d: classes) {
					if(d.getName().equals(supername)) {
						superClass = d;
					}
				}
				if(!supername.equals("java.lang.Object")) {
					List<MethodNode> scmethods = superClass.getMethods();
					List<MethodNode> cmethods = c.getMethods();
					Vector<Integer> scvector = new Vector<Integer>();
					Vector<Integer> cvector = new Vector<Integer>();
					Map<String, Integer> classLocations = new HashMap<String, Integer>();
					int i = 0;
					if(!((superClass.getClassNode().access & Opcodes.ACC_INTERFACE) > 0)  && !((superClass.getClassNode().access & Opcodes.ACC_ABSTRACT) > 0)) {
						System.out.println("Now you not fucking up");
						for(MethodNode m: scmethods) {
							String returnTypeName= Type.getReturnType(m.desc).getClassName();
							System.out.println(returnTypeName);
							if(!classLocations.containsKey(returnTypeName)) {
								classLocations.put(returnTypeName, i);
								scvector.addElement(1);
								cvector.addElement(0);
								i++;
							}
							else {
								int index = classLocations.get(returnTypeName);
								scvector.set(index, scvector.elementAt(index) + 1);
							}
							if(m.parameters != null) {
								List<ParameterNode> parameters = m.parameters;
								for(ParameterNode mc : parameters) {
									String parameterTypeName = mc.name;
									if(!classLocations.containsKey(parameterTypeName)) {
										classLocations.put(parameterTypeName, i);
										scvector.addElement(1);
										cvector.addElement(0);
										i++;
									}
									else {
										int index = classLocations.get(parameterTypeName);
										scvector.set(index, scvector.elementAt(index) + 1);
									}
								}
							}
						}
						for(MethodNode m: cmethods) {
							String returnTypeName = Type.getReturnType(m.desc).getClassName();
							//System.out.println(returnTypeNameA[returnTypeNameA.length - 1]);
							if(!classLocations.containsKey(returnTypeName)) {
								classLocations.put(returnTypeName, i);
								cvector.addElement(1);
								scvector.addElement(0);
								i++;
							}
							else {
								int index = classLocations.get(returnTypeName);
								cvector.set(index, scvector.elementAt(index) + 1);
							}
							List<ParameterNode> parameters = m.parameters;
							if(parameters != null) {
								for(ParameterNode n: parameters) {
									i++;
									String parameterTypeName = n.name.replaceAll("\\/", ".");
									if(!classLocations.containsKey(parameterTypeName)) {
										classLocations.put(parameterTypeName, i);
										cvector.addElement(1);
										scvector.addElement(0);
										i++;
									}
									else {
										int index = classLocations.get(parameterTypeName);
										cvector.set(index, scvector.elementAt(index) + 1);
									}
								}
							}
						}
						double dotProduct = 0;
						for(int j = 0; j < scvector.size(); j++) {
							dotProduct += (scvector.elementAt(j) * cvector.elementAt(j));
						}
						double c_magnitude = 0;
						double sc_magnitude = 0;
						for(int j: cvector) {
							c_magnitude += (j*j);
						}
						c_magnitude = Math.sqrt(c_magnitude);
						for(int j: scvector) {
							sc_magnitude += (j*j);
						}
						sc_magnitude = Math.sqrt(sc_magnitude);
						double cosine = dotProduct/(c_magnitude * sc_magnitude);
						System.out.println(classLocations.keySet());
						
						System.out.println(cvector.toString());
						System.out.println(scvector.toString());
						System.out.println("scvector size: " + scvector.size());
						System.out.println("cvector size: " + cvector.size());
						System.out.println("cosine: " + cosine);
						if(cosine > COSINE_CUTOFF) {
							c.setAttribute("style", "filled");
							c.setAttribute("fillcolor", color);
						}
					}
				}
			}
		}
	}

}
