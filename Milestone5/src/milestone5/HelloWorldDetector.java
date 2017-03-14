package milestone5;

import java.util.List;
import java.util.Set;

import example.Arrow;
import example.JClass;
import example.PatternDetector;

public class HelloWorldDetector implements PatternDetector {

	@Override
	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		for(JClass c: classes) {
			c.addArchetypeClassTag("Hello World");
		}
	}

}
