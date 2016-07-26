/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.allocation.ExecutableResource;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.Informer;
import rsb.RSBException;
import rst.communicationpatterns.ResourceAllocationType;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class OnExecutable extends ExecutableResource {

	private final static Logger LOG = Logger.getLogger(OnExecutable.class.getName());
	private final Informer informer;
	private final String cmd;

	public OnExecutable(String location, Informer informer) {
		super("auto-on:" + location, Policy.MAXIMUM, Priority.NORMAL, location);
		this.informer = informer;
		this.cmd = "ON:" + location;
	}

	@Override
	public void allocationUpdated(ResourceAllocationType.ResourceAllocation allocation, String cause) {
		super.allocationUpdated(allocation, cause);
		if (allocation.getSlot().getEnd().getTime() - allocation.getSlot().getBegin().getTime() < 10000) {
			try {
				LOG.log(Level.FINE, "cancelling too short allocation");
				shutdown();
			} catch (RSBException ex) {
				Logger.getLogger(OnExecutable.class.getName()).log(Level.SEVERE, "Could not cancel allocation", ex);
			}
		}
	}

	@Override
	public Object execute(long slice) throws ExecutionException {
		try {
			LOG.log(Level.INFO, "switching: {0}", cmd);
			informer.publish(cmd);
			Thread.sleep(slice - 100);
		} catch (RSBException ex) {
			LOG.log(Level.SEVERE, "could not publish", ex);
			throw new ExecutionException(ex);
		} catch (InterruptedException ex) {
			LOG.log(Level.SEVERE, "could not sleep", ex);
			throw new ExecutionException(ex);
		}
		return true;
	}

}
