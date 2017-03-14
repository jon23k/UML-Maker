package example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class DesignParser {
	private static Properties props;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		props = new Properties();
		props.load(new FileInputStream("designProperties"));
		for(String arg : args) {
			if(arg.startsWith("file=")) {
				String[] keyAndPath = arg.split("=");
				String path = keyAndPath[keyAndPath.length - 1];
				props.load(new FileInputStream(path));
			}
			else {
				String[] keyAndValue = arg.split("=");
				props.put(keyAndValue[0], keyAndValue[1]);
			}
		}
		List<ClassNode> classNodes = new ArrayList<ClassNode>();
		List<String> recursiveClasses = new ArrayList<String>();
		List<String> toRender = Arrays.asList(props.getProperty("whitelist").split(","));
		recursiveClasses.addAll(toRender);
		// Check for recursive Flag
		List<String> classNames = new ArrayList<String>();
		if (Boolean.parseBoolean(props.getProperty("recursive"))) {
			// Remove recursive flag from list
			recursiveClasses.remove("-r");
			classNames = recursiveAdd(recursiveClasses);
		} else {
			classNames = recursiveClasses;
		}
		Set<String> noDuplicatesPlease = new HashSet<String>();
		noDuplicatesPlease.addAll(classNames);
		classNames.clear();
		classNames.addAll(noDuplicatesPlease);
		for (String className : classNames) {
			System.out.println(className);
			ClassReader reader = new ClassReader(className);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, ClassReader.EXPAND_FRAMES);
			classNodes.add(classNode);
		}

		GraphVizBuilder builder = new GraphVizBuilder(classNodes);
		
		//ADD DETECTORS HERE
		DetectorFramework framework = DetectorFramework.getInstance();
		//framework.addDetector(new FCOIViolationDetector());
		//framework.addDetector(new SingletonDetector());
		List<String> detectors = Arrays.asList(props.getProperty("detectors").split(","));
		for(String d : detectors) {
			try {
				framework.addDetector((PatternDetector) Class.forName(d).newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		for(PatternDetector d : framework.getDetectors()){
			d.analyze(builder.getClasses(), builder.getArrows());
		}
		builder.run();
	}

	/**
	 * TODO: If recursive Flag is not found, we still need to find a way so that the output GraphViz
	 * has arrows pointing to boxes with the correct names.  Currently, the only names being printed
	 * in these boxes are the internal names.
	 * 
	 * TODO: Need to add support for recursively adding "Associated" and "Dependent" Classes
	 * */
	public static List<String> recursiveAdd(List<String> classNames) throws IOException {
		List<String> blacklist = Arrays.asList(props.getProperty("blacklist").split(","));
		List<String> whitelist = Arrays.asList(props.getProperty("whitelist").split(","));
		List<String> toCheck = new ArrayList<String>();
		List<String> current = new ArrayList<String>();
		for (String className : classNames) {
			boolean isBlacklisted = false;
			for(String s: blacklist) {
				if(className.contains(s) && !whitelist.contains(className)) {
					isBlacklisted = true;
					break;
				}
			}
			if(!isBlacklisted) {
				ClassReader reader = new ClassReader(className);
				ClassNode classNode = new ClassNode();
				reader.accept(classNode, ClassReader.EXPAND_FRAMES);
				if (classNode.superName != null) {
					// System.out.println("Supername:" + classNode.superName);
					toCheck.add(classNode.superName);
				}
				for (String s : (List<String>) classNode.interfaces) {
					// System.out.println("Interfaces: " + s);
					toCheck.add(s);
				}
				current.add(classNode.name);
			}
		}
		if (!toCheck.isEmpty()) {
			List<String> back = recursiveAdd(toCheck);
			current.addAll(back);
		}
		return current;
	}
	
	public static Properties getProperties() {
		return props;
	}
}
