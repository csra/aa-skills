/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.aa.Executor;
import de.citec.csra.rst.util.IntervalUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import rsb.RSBException;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy.MAXIMUM;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority.LOW;
import static rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority.NORMAL;
import rst.domotic.state.PowerStateType.PowerState.State;
import static rst.domotic.state.PowerStateType.PowerState.State.*;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import static rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType.COLORABLE_LIGHT;
import static rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType.DIMMABLE_LIGHT;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class LightExecutor implements Executor {

	private final static Logger LOG = Logger.getLogger(LightExecutor.class.getName());

	private final Map<String, AllocatedLightSwitch> execs = new HashMap<>();
	private final Map<String, List<UnitConfig>> units = new HashMap<>();
	private final long TIMEOUT = 5000;

	public LightExecutor() throws InterruptedException {
		try {
//			broken wait for data workaround
			Registries.getUnitRegistry().waitForData(TIMEOUT, TimeUnit.MILLISECONDS);
			Registries.getLocationRegistry().waitForData(TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (CouldNotPerformException ex) {
			LOG.log(Level.SEVERE, "Registries not ready yet", ex);
		}
	}

	public List<UnitConfig> units(String location) throws InterruptedException {
		UnitType t;
		switch (location.toLowerCase()) {
			case "sports":
				t = DIMMABLE_LIGHT;
				break;
			default:
				t = COLORABLE_LIGHT;
				break;
		}
		if (!units.containsKey(location)) {
			try {
				List<UnitConfig> remoteUnits = Registries.getLocationRegistry().getUnitConfigsByLocationLabel(t, location);
				remoteUnits.removeIf(u -> u.getLabel().contains("50"));
				units.put(location, remoteUnits);
			} catch (CouldNotPerformException ex) {
				LOG.log(Level.SEVERE, "Could not read location registry", ex);
			}
		}
		return units.get(location);
	}

	@Override
	public void on(String location) throws InterruptedException {
		exec(location, ON, MAXIMUM, NORMAL, 45, 10, SECONDS);
	}

	@Override
	public void off(String location) throws InterruptedException {
		exec(location, OFF, MAXIMUM, LOW, 30, 10, SECONDS);
	}

	private void exec(String location, State state, Policy policy, Priority priority, long duration, long interval, TimeUnit timeUnit) throws InterruptedException {
		List<UnitConfig> us = units(location);
		if (us != null) {
			for (UnitConfig unit : us) {
				try {
					String id = ScopeGenerator.generateStringRep(unit.getScope()) + ": " + state.name();
					if (execs.containsKey(id) && execs.get(id).getRemote().getRemainingTime() > 0) {
						long now = IntervalUtils.currentTimeInMicros();
						execs.get(id).getRemote().extendTo(timeUnit.convert(now, MICROSECONDS) + duration, timeUnit);
					} else {
						AllocatedLightSwitch exec = new AllocatedLightSwitch(unit, state, policy, priority, duration, interval, timeUnit);
						exec.startup();
						execs.put(id, exec);
					}
				} catch (RSBException | CouldNotPerformException ex) {
					LOG.log(Level.SEVERE, "failed to schedule executable '" + state.name() + "'", ex);
				}
				break;
			}
		}
	}
}
