package example;

import java.util.ArrayList;
import java.util.List;

public class DetectorFramework {
	private List<PatternDetector> detectors;
	private volatile static DetectorFramework instance;

	private DetectorFramework() {
		detectors = new ArrayList<PatternDetector>();
	}

	public void addDetector(PatternDetector d) {
		detectors.add(d);
	}
	
	public List<PatternDetector> getDetectors() {
		return this.detectors;
	}

	public static DetectorFramework getInstance() {
		// Double checked synchronized locking
		if (instance == null) {
			synchronized (DetectorFramework.class) {
				if (instance == null) {
					instance = new DetectorFramework();
				}
			}
		}
		return instance;
	}
}
