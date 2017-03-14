package example;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class SingletonDetector implements PatternDetector {

	private String color;
	
	public SingletonDetector() {
		Properties props = DesignParser.getProperties();
		this.color = props.getProperty("SingletonDetectorColor");
	}
	
	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		for(JClass cls: classes) {
			
			//represents boolean values that will be set when iterating through
			//the field and method loop
			boolean isStaticInstance = false;	
			boolean hasAStaticMethod = false;
			
			//get the fields & methods from the class
			List<FieldNode> fields = cls.getFields();
			List<MethodNode> methods = cls.getMethods();
			
			//CHECKS FOR A STATIC INSTANCE FIELD
			for(FieldNode f : fields){
				//booleans that represent if the field is static and is an instance, respectively
				boolean isStatic = ((f.access & Opcodes.ACC_STATIC) > 0);
				boolean isSameClass = JClass.getFieldNodeTypeFQNames(f)[0].replaceAll("\\/", ".").equals(cls.getName());
				if(isSameClass && isStatic){
					isStaticInstance = true;
				}
			}
			
			//CHECKS FOR A STATIC METHOD THAT RETURNS THE THE CLASS TYPE
			for(MethodNode m : methods){
				//TODO: DID I DO THIS RIGHT?!?!
				//booleans that represent if a method is static and has a return type of class, respectively
				boolean isStatic = ((m.access & Opcodes.ACC_STATIC) > 0);
				boolean isSameClass = JClass.getMethodReturnTypeFQNames(m)[0].replaceAll("\\/", "\\.").equals(cls.getName());
				if(isStatic && isSameClass){
					hasAStaticMethod = true;
				}
			}
			
			//SETS THE COLOR HERE
			if(isStaticInstance && hasAStaticMethod){
				cls.addArchetypeClassTag("Singleton");
				cls.setAttribute("color", color);
			}
		}

	}

}
