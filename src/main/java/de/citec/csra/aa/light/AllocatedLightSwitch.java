/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.allocation.cli.ExecutableResource;
import de.citec.csra.util.Remotes;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import rsb.RSBException;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Initiator;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;
import rst.domotic.state.PowerStateType.PowerState.State;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class AllocatedLightSwitch extends ExecutableResource {

	private final static Logger LOG = Logger.getLogger(AllocatedLightSwitch.class.getName());
	private final UnitConfig unit;
	private final State state;
	private final long interval;

	public AllocatedLightSwitch(UnitConfig unit, State state, Policy pol, Priority prio, long duration, long interval) {
		super("switch:" + unit.getLabel() + " -> " + state.name(),
				pol,
				prio,
				Initiator.SYSTEM,
				0,
				duration,
				ScopeGenerator.generateStringRep(unit.getScope()));
		this.unit = unit;
		this.interval = interval;
		this.state = state;
	}

	@Override
	public Object execute() throws ExecutionException, InterruptedException {
		if (interval > 0) {
			while (interval < remaining()) {
				exec();
				Thread.sleep(interval);
			}
		} else {
			exec();
		}
		Thread.sleep(Math.max(0, remaining()));
		return true;
	}

	@Override
	public boolean updated(ResourceAllocation allocation) {
		if (remaining() < interval) {
			LOG.log(Level.FINE, "cancelling too short allocation");
			try {
				shutdown();
			} catch (RSBException ex) {
				LOG.log(Level.SEVERE, "Could not cancel allocation", ex);
			}
		}
		return true;
	}

	private void exec() throws ExecutionException, InterruptedException {
		try {
			ColorableLightRemote light = Remotes.get().getColorableLight(unit);
			LOG.log(Level.FINE, "execute setPower async with parameters ''{0}'' at ''{1}''", new Object[]{state, unit.getLabel() + " [" + ScopeGenerator.generateStringRep(unit.getScope()) + "]"});
			light.setPowerState(rst.domotic.state.PowerStateType.PowerState.newBuilder().setValue(state).build());
		} catch (CouldNotPerformException ex) {
			LOG.log(Level.SEVERE, "could not perform ^^", ex);
			throw new ExecutionException(ex);
		}
	}
}
