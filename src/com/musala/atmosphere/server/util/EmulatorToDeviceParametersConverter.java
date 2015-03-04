package com.musala.atmosphere.server.util;

import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.sa.EmulatorParameters;
import com.musala.atmosphere.commons.util.Pair;

/**
 * Converts {@link DeviceParameters} to object that is used for emulator creation.
 * 
 * @author yordan.petrov
 * 
 */
public class EmulatorToDeviceParametersConverter {
    /**
     * Converts a {@link DeviceParameters} object to object that is used for emulator creation.
     * 
     * @param deviceParameters
     *        - the {@link DeviceParameters} object that will be converted.
     * @return an {@link EmulatorParameters} object that is used for emulator creation.
     */
    public static EmulatorParameters convert(DeviceParameters deviceParameters) {
        EmulatorParameters emulatorParameters = new EmulatorParameters();
        int deviceApiLevel = deviceParameters.getTargetApiLevel();
        int deviceResolutionWidth = deviceParameters.getResolutionWidth();
        int deviceResolutionHeight = deviceParameters.getResolutionHeight();
        int deviceDpi = deviceParameters.getDpi();
        int deviceRam = deviceParameters.getRam();

        if (deviceApiLevel != DeviceParameters.TARGET_API_LEVEL_NO_PREFERENCE) {
            emulatorParameters.setApiLevel(deviceApiLevel);
        }

        if (deviceResolutionHeight != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE
                && deviceResolutionWidth != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE) {
            emulatorParameters.setResolution(new Pair<Integer, Integer>(deviceResolutionWidth, deviceResolutionHeight));
        }

        if (deviceDpi != DeviceParameters.DPI_NO_PREFERENCE) {
            emulatorParameters.setDpi(deviceDpi);
        }

        if (deviceRam != DeviceParameters.RAM_NO_PREFERENCE) {
            emulatorParameters.setRam((long) deviceRam);
        }

        return emulatorParameters;
    }
}
