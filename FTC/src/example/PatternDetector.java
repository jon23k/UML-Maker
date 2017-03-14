package example;

import java.util.List;
import java.util.Set;

public interface PatternDetector {

	public void analyze(List<JClass> classes, Set<Arrow> arrow) throws ClassNotFoundException;

}
