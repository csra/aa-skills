/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.aa;

import de.citec.csra.aa.light.LightExecutor;
import rsb.Factory;
import rsb.InitializeException;
import rsb.Listener;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rst.classification.ClassificationResultMapType;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class AutoExecution {
	
	static {
		DefaultConverterRepository.getDefaultConverterRepository()
				.addConverter(new ProtocolBufferConverter<>(ClassificationResultMapType.ClassificationResultMap.getDefaultInstance()));
	}
	
	public static final String SITUTATION = "/home/situation/result";
	
	public static void main(String[] args) throws InitializeException, RSBException, InterruptedException {
		ResultHandler actor = new ResultHandler();
		actor.addExecutor(new LightExecutor());
		
		Listener l = Factory.getInstance().createListener(SITUTATION);
		l.addHandler(new RSBResultHandler(actor), true);
		
		l.activate();
		new Thread(new PeriodicalSwitch(actor, 15000, false)).start();
		
	}
}
