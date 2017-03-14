package example;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.tree.FieldNode;

public class FCOIViolationDetector implements PatternDetector {
	
	private String color;
	
	public FCOIViolationDetector() {
		Properties props = DesignParser.getProperties();
		this.color = props.getProperty("FCOIDetectorColor");
	}

	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		for(JClass cls: classes) {
			String supername = cls.getSuperName();
			// Check because Object doesn't have a superclass
			if(supername != null) {
				supername = supername.replaceAll("\\/", ".");
				// Don't want to color classes that only extend Object.
				if(!supername.equals("java.lang.Object")) {
					Class<?> superClass = Class.forName(supername);
					if(!superClass.isInterface() && !Modifier.isAbstract(superClass.getModifiers())) {
						for(FieldNode fn: cls.getFields()) {
							if(JClass.getFieldNodeTypeFQNames(fn)[0].equals(supername)) {
								break;
							}
						}
						// Color this class
						cls.setAttribute("color", color);
						// Color the extends Arrow pointing from our class.
						for(Arrow a: arrow) {
							if(a.pointsFrom.equals(cls.getHashName()) && a.getArrowType().equals("extends")) {
								a.getAttributes().put("color", color);
							}
						}
					}
				}
			}
		}
	}
}
