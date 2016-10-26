/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.aa.Executor;
import de.citec.csra.util.Remotes;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.jul.exception.CouldNotPerformException;
import rsb.RSBException;
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
		return Remotes.get().getLocationRegistry().getUnitConfigsByLocationLabel(t, location);
	}

	@Override
	public void on(String location) {
		try {
			for (UnitConfig unit : units(location)) {
				try {
					new OnExecutable(unit).schedule(0, 30000);
				} catch (RSBException ex) {
					Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule on", ex);
				}
			}
		} catch (InstantiationException | InterruptedException | CouldNotPerformException ex) {
			Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule on", ex);
		}
	}

	@Override
	public void off(String location) {
		try {
			for (UnitConfig unit : units(location)) {
				try {
					new OffExecutable(unit).schedule(0, 1000);
				} catch (RSBException ex) {
					Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule on", ex);
				}
			}
		} catch (InstantiationException | InterruptedException | CouldNotPerformException ex) {
			Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule on", ex);
		}
	}
}
