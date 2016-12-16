/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.aa.Executor;
import de.citec.csra.util.Remotes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

	private final Map<String, AllocatedLightSwitch> execs = new HashMap<>();
	private final Map<String, List<UnitConfig>> units = new HashMap<>();

	public List<UnitConfig> units(String location) throws InstantiationException, InterruptedException, CouldNotPerformException {
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
			List<UnitConfig> remoteUnits = Remotes.get().getLocationRegistry().getUnitConfigsByLocationLabel(t, location);
			remoteUnits.removeIf(u -> u.getLabel().contains("50"));
			units.put(location, remoteUnits);
		}
		return units.get(location);
	}

	@Override
	public void on(String location) {
		exec(location, ON, MAXIMUM, NORMAL, 30000, 10000);
	}

	@Override
	public void off(String location) {
		exec(location, OFF, MAXIMUM, LOW, 30000, 10000);
	}

	private void exec(String location, State state, Policy policy, Priority priority, long duration, long interval) {
		try {
			for (UnitConfig unit : units(location)) {
				try {
					String id = ScopeGenerator.generateStringRep(unit.getScope()) + ": " + state.name();
					if (execs.containsKey(id) && execs.get(id).remaining() > 0) {
						long now = System.currentTimeMillis();
						execs.get(id).extendTo(now + duration);
					} else {
						AllocatedLightSwitch exec = new AllocatedLightSwitch(unit, state, policy, priority, duration, interval);
						exec.startup();
						execs.put(id, exec);
					}
				} catch (RSBException ex) {
					Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule executable", ex);
				}
			}
		} catch (InstantiationException | InterruptedException | CouldNotPerformException ex) {
			Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule", ex);
		}
	}
}
