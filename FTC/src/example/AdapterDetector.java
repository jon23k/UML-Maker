package example;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AdapterDetector implements PatternDetector{
	
	private String color;
	
	public AdapterDetector() {
		Properties props = DesignParser.getProperties();
		this.color = props.getProperty("AdapterDetectorColor");
	}

	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		String adapteeName = null;
		String targetName = null;
		
		//THERE ARE TWO FOR LOOPS, this one find the adapter class and gets the names of the adaptee and target
		for(JClass cls: classes) {
			//if this gets set to true then we set the color to that of an adapter
			for(Arrow a : arrow){
				
				//If an extend arrow points from our class, then we want to look at those
				if(a.pointsFrom.equals(cls.getHashName()) && a.getArrowType().equals("extends")){
					
					for(FieldNode field : cls.getFields()){
						
						//gets all the types of the fields
						String[] fieldTypes = JClass.getFieldNodeTypeFQNames(field);
						
						//if any of the field types equals the class that the extend arrow points to, look there
						if(Arrays.asList(fieldTypes).contains(a.pointsTo)) {
							for(MethodNode method : cls.getMethods()){
								List<String> methodReturnTypes = Arrays.asList(JClass.getMethodReturnTypeFQNames(method));
								List<String> methodArgumentTypes = Arrays.asList(JClass.getMethodArgumentTypeFQNames(method));
								
								//VERY IMPORTANT
								//if the class that the extend arrow points to is also in a return type for a method
								//AND if that same method shares a parameter type with an interface that the class implements
								if(methodReturnTypes.contains(a.pointsTo) && methodArgumentTypes.retainAll(cls.getInterfaces())){
									
									//set the color, label an adapter and then pass a.points as the adapteeName
									cls.setAttribute("color", color);
									cls.addArchetypeClassTag("Adapter");
									adapteeName = a.pointsTo;
									
									//retainAll alters methodArgumentTypes by removing all the elements that are not shared with
									//the interfaces that the class implements, so if there is a shared element then it mus be
									//the target!!
									targetName = methodArgumentTypes.get(0);
								}
							}
						}
					}
					
				}
			}
		}
		
		//THE SECOND LOOP
		for(JClass cls2: classes){
			
			//takes our new adaptee and target names and finds the classes that correspond with it
			//then sets its class tag and color
			if(cls2.getHashName().equals(targetName)){
				cls2.setAttribute("color", color);
				cls2.addArchetypeClassTag("Target");
			}
			else if(cls2.getHashName().equals(adapteeName)){
				cls2.setAttribute("color", color);
				cls2.addArchetypeClassTag("Adaptee");
			}
		}
	}

}
