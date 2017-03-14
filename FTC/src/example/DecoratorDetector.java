package example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DecoratorDetector implements PatternDetector {
	
	private String color;
	
	public DecoratorDetector() {
		Properties properties = DesignParser.getProperties();
		this.color = properties.getProperty("DecoratorDetectorColor");
	}

	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		// TODO: Detect Decorator Classes
		for (JClass cls : classes) {
			// Decorators extend a class.  Ignore anything that doesn't.
			if(cls.hasSuper() == false) {
				// Only Object should hit this check
				continue;
			} else if (cls.getSuperName().equals(Object.class.getName())) {
				// Are there decorators for Object?
				continue;
			} else if (!cls.hasAssociations()) {
				// All decorators have association arrows
				continue;
			}
			// We have a potential Decorator class.
			// This class has a non-Object superclass and association arrow(s)
			
			// Find our extendsArrow and AssociationArrows
			Arrow ourExtendsArrow = null;
			List<Arrow> ourAssocArrows = new ArrayList<Arrow>();
			for(Arrow a : arrow) {
				if(!a.pointsFrom.equals(cls.getHashName())) {
					// This is an arrow we don't care about since
					// it doesn't point from our JClass.
					continue;
					
					// All arrows from here on out will be pointing from our JClass
				} else if (a.getArrowType().equals("extends")) {
					// We now have the ExtendsArrow pointing from our class
					ourExtendsArrow = a;
				} else if (a.getArrowType().equals("association")) {
					ourAssocArrows.add(a);
				}
			}
			
			// Loop through our AssociationArrows and see if there are any that
			// point to our superclass.  If so, Decorator is detected.
			String superHashName = ourExtendsArrow.pointsTo;
			for(Arrow assocArrow : ourAssocArrows) {
				if(assocArrow.pointsTo.equals(superHashName)) {
					// We detected a Decorator!  Flag it.
					// TODO: Fill Classes with color
					cls.setAttribute("bgcolor", this.color);
					// TODO: Add <<decorator>> tags to Abstract and Concrete Decorators
					cls.addArchetypeClassTag("<<decorator>>");
					// TODO: Add <<Component>> tag to decorated interface or class.
					this.tagDecorated(classes, superHashName);
					// TODO: Add <<decorates>> tag to association arrow from decorator to component.
					Map<String, String> attr = assocArrow.getAttributes();
					attr.put("headlabel", "<<decorates>>");
				}
			}
			
		}
		

	}
	
	private void tagDecorated(List<JClass> classes, String hashSuperName) {
		for(JClass cls : classes) {
			if(cls.getHashName().equals(hashSuperName)) {
				cls.addArchetypeClassTag("<<Component>>");
			}
		}
	}
}
