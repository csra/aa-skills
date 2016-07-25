/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa.light;

import de.citec.csra.aa.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class LightExecutor implements Executor {

	private final Informer informer;
	
	public LightExecutor() throws InitializeException, RSBException {
		this.informer = Factory.getInstance().createInformer("/home/locationlight/power");
		this.informer.activate();
	}

	@Override
	public void on(String location) {
		try {
			new OnExecutable(location, this.informer).schedule(0, 30000);
		} catch (RSBException ex) {
			Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule on", ex);
		}
	}

	@Override
	public void off(String location) {
		try {
			new OffExecutable(location, this.informer).schedule(0, 1000);
		} catch (RSBException ex) {
			Logger.getLogger(LightExecutor.class.getName()).log(Level.SEVERE, "failed to schedule off", ex);
		}
	}
	
}
