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
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Initiator;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;
import rst.domotic.state.PowerStateType;
import static rst.domotic.state.PowerStateType.PowerState.State.OFF;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class OffExecutable extends ExecutableResource {

	private final static Logger LOG = Logger.getLogger(OffExecutable.class.getName());
	private final UnitConfig unit;

	public OffExecutable(UnitConfig unit) {
		super("auto-on:" + unit.getLabel(),
				Policy.PRESERVE,
				Priority.LOW,
				Initiator.SYSTEM,
				ScopeGenerator.generateStringRep(unit.getScope()));
		this.unit = unit;
	}

	@Override
	public Object execute(long slice) throws ExecutionException {
		try {
			long start = System.currentTimeMillis();
			ColorableLightRemote light = Remotes.get().getColorableLight(unit);
			LOG.log(Level.FINE, "execute setPower async with parameters ''{0}'' at ''{1}''", new Object[]{OFF, unit.getLabel()});
			light.setPowerState(PowerStateType.PowerState.newBuilder().setValue(OFF).build());
			long end = System.currentTimeMillis();
			Thread.sleep(slice - 10 - (end - start));
			return true;
		} catch (InterruptedException ex) {
			LOG.log(Level.SEVERE, "could not sleep", ex);
			throw new ExecutionException(ex);
		} catch (CouldNotPerformException ex) {
			LOG.log(Level.SEVERE, "could not perform ^^", ex);
			throw new ExecutionException(ex);
		}
	}
}
