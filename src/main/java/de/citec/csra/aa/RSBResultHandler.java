/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa;

import java.util.logging.Level;
import java.util.logging.Logger;
import rsb.Event;
import rsb.Handler;
import rst.classification.ClassificationResultMapType.ClassificationResultMap;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class RSBResultHandler implements Handler {

	private final static Logger LOG = Logger.getLogger(RSBResultHandler.class.getName());
	private final ResultHandler actor;

	public RSBResultHandler(ResultHandler actor) {
		this.actor = actor;
	}

	@Override
	public void internalNotify(Event event) {
		if (event.getData() instanceof ClassificationResultMap) {
			try {
				actor.handle((ClassificationResultMap) event.getData());
			} catch (InterruptedException ex) {
				LOG.log(Level.SEVERE, "Interrupted, aborting", ex);
				Thread.currentThread().interrupt();
			}
		} else {
			LOG.log(Level.FINE, "Ignoring irrelevant data type of class ''{0}''", event.getType().getName());
		}
	}
}
