/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.allocation.cli.ExecutableResource;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.Informer;
import rsb.RSBException;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Initiator;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class OffExecutable extends ExecutableResource {

	private final static Logger LOG = Logger.getLogger(OffExecutable.class.getName());
	private final Informer informer;
	private final String cmd;

	public OffExecutable(String location, Informer informer) {
		super("auto-off:" + location, Policy.PRESERVE, Priority.LOW, Initiator.SYSTEM, location);
		this.informer = informer;
		this.cmd = "OFF:" + location;
	}

	@Override
	public Object execute(long slice) throws ExecutionException {
		try {
			LOG.log(Level.INFO, "switching: {0}", cmd);
			informer.publish(cmd);
		} catch (RSBException ex) {
			LOG.log(Level.SEVERE, "could not publish", ex);
			throw new ExecutionException(ex);
		}
		return true;
	}

}
