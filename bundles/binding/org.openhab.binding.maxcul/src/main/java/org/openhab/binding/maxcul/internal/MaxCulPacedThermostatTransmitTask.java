/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.maxcul.internal;

import java.util.Collection;
import java.util.TimerTask;

import org.openhab.binding.maxcul.MaxCulBindingProvider;
import org.openhab.binding.maxcul.internal.messages.SetTemperatureMsg;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

public class MaxCulPacedThermostatTransmitTask extends TimerTask {

    private MaxCulMsgHandler messageHandler;
    private Command command;
    private MaxCulBindingConfig bindingConfig;
    private Collection<MaxCulBindingProvider> providers;

    public MaxCulPacedThermostatTransmitTask(Command cmd, MaxCulBindingConfig cfg, MaxCulMsgHandler msgHandler,
            Collection<MaxCulBindingProvider> providers) {
        command = cmd;
        bindingConfig = cfg;
        messageHandler = msgHandler;
        this.providers = providers;
    }

    private void sendToDevices(double temp) {
        messageHandler.sendSetTemperature(bindingConfig.getDevAddr(), temp);
        /* send temperature to associated devices */
        for (MaxCulBindingProvider provider : providers) {
            for (MaxCulBindingConfig bc : provider.getAssociations(bindingConfig.getSerialNumber())) {
                messageHandler.sendSetTemperature(bc.getDevAddr(), temp);
            }
        }
    }

    @Override
    public void run() {
        if (command instanceof OnOffType) {
            if (((OnOffType) command) == OnOffType.ON) {
                sendToDevices(SetTemperatureMsg.TEMPERATURE_ON);
            } else if (((OnOffType) command) == OnOffType.OFF) {
                sendToDevices(SetTemperatureMsg.TEMPERATURE_OFF);
            }
        } else if (command instanceof DecimalType) {
            sendToDevices(((DecimalType) command).doubleValue());
        }
    }

}
