/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import rst.classification.ClassificationResultMapType;
import rst.classification.ClassificationResultMapType.ClassificationResultMap;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class ResultHandler {

	private final static Logger LOG = Logger.getLogger(ResultHandler.class.getName());
	private final List<Executor> execs = new LinkedList<>();

	public void addExecutor(Executor exec) {
		this.execs.add(exec);
	}

	public void removeExecutor(Executor exec) {
		this.execs.remove(exec);
	}

	public void handle(ClassificationResultMap m) {
		Set<String> movement = filter(m, "some");
		Set<String> noMovement = filter(m, "none");
		noMovement.removeAll(movement);
		
		movement.remove("Sports");
		noMovement.remove("Sports");
		
		LOG.log(Level.INFO, "movement: {0}, no movement: {1}", new Object[]{movement, noMovement});

		for (Executor e : this.execs) {
			for (String s : movement) {
				e.on(s);
			}
			for (String s : noMovement) {
				e.off(s);
			}
		}
	}

	public Set<String> filter(ClassificationResultMapType.ClassificationResultMap sit, String decided) {
		Set<String> matching = sit.getAspectsList().stream()
				.filter(a -> a.getName().startsWith("Movement_"))
				.filter(a -> a.getResult().getDecidedClass().toStringUtf8().equalsIgnoreCase(decided))
				.map(a -> a.getName().replaceAll("Movement_", "").split("_")[0])
				.collect(Collectors.toSet());
		return matching;
	}
}
