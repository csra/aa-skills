/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.Factory;
import rsb.RSBException;
import rsb.patterns.RemoteServer;
import rst.classification.ClassificationResultMapType.ClassificationResultMap;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
@Deprecated
public class PeriodicalSwitch implements Runnable {

	private final RemoteServer serv;
	private final long interval;

	private final static Logger LOG = Logger.getLogger(PeriodicalSwitch.class.getName());
	private final ResultHandler actor;
	private final boolean handle;

	public PeriodicalSwitch(ResultHandler actor, long interval, boolean handle) throws RSBException {
		this.serv = Factory.getInstance().createRemoteServer("/home/situation/result");
		this.serv.activate();
		this.interval = interval;
		this.handle = handle;
		this.actor = actor;
	}

	@Override
	public void run() {
		while (!Thread.interrupted() && serv.isActive()) {
			try {
				Object result = serv.call("get", "", 1.0);
				if (handle) {
					if (result instanceof ClassificationResultMap) {
						actor.handle((ClassificationResultMap) result);
					} else {
						LOG.log(Level.FINE, "Ignoring irrelevant return type of class ''{0}''", result.getClass().getName());
					}
				}
				Thread.sleep(interval);
			} catch (InterruptedException ex) {
				LOG.log(Level.SEVERE, "Interrupted, shutting down", ex);
				Thread.currentThread().interrupt();
			} catch (RSBException | ExecutionException | TimeoutException ex) {
				LOG.log(Level.SEVERE, "Error querying situation, retrying next time", ex);
			}
		}
	}
}
