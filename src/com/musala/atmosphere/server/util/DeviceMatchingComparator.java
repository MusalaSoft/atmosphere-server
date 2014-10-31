package com.musala.atmosphere.server.util;

import java.util.Comparator;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;
import com.musala.atmosphere.server.data.model.IDevice;

public class DeviceMatchingComparator implements Comparator<DeviceInformation> {
    private DeviceParameters neededDeviceParameters;

    public DeviceMatchingComparator(DeviceParameters matchingBase) {
        this.neededDeviceParameters = matchingBase;
    }

    @Override
    public int compare(DeviceInformation availDeviceFirst, DeviceInformation availDeviceSecond) {
        int matchingScoreFirst = matchingScore(availDeviceFirst);
        int matchingScoreSecond = matchingScore(availDeviceSecond);

        return matchingScoreFirst - matchingScoreSecond;
    }

    /**
     * Calculates a required/present device matching score. Each device parameter should be taken into consideration
     * with different weight. This method will be changed when more complex selection mechanism is discussed. At the
     * moment it is a simple match check.
     * 
     * @param matchDeviceInformation
     *        - information for a present device
     * @return the best matching result
     */
    private int matchingScore(DeviceInformation matchDeviceInformation) {
        int result = 1;
        DeviceType requiredDeviceType = neededDeviceParameters.getDeviceType();
        int requiredDeviceDpi = neededDeviceParameters.getDpi();
        DeviceOs requiredDeviceOs = neededDeviceParameters.getOs();
        String requiredDeviceModel = neededDeviceParameters.getModel();
        String requiredDeviceSerialNumber = neededDeviceParameters.getSerialNumber();
        int requiredDeviceRam = neededDeviceParameters.getRam();
        int requiredDeviceResH = neededDeviceParameters.getResolutionHeight();
        int requiredDeviceResW = neededDeviceParameters.getResolutionWidth();
        Boolean requiredDeviceCameraPresence = neededDeviceParameters.hasCameraPresent();
        int requiredDeviceApiVersion = neededDeviceParameters.getApiLevel();

        if (requiredDeviceType != DeviceParameters.DEVICE_TYPE_NO_PREFERENCE) {
            if (requiredDeviceType == DeviceType.DEVICE_ONLY) {
                if (matchDeviceInformation.isEmulator()) {
                    return 0;
                }
            } else if (requiredDeviceType == DeviceType.EMULATOR_ONLY) {
                if (!matchDeviceInformation.isEmulator()) {
                    return 0;
                }
            }
        }

        if (requiredDeviceOs != DeviceParameters.DEVICE_OS_NO_PREFERENCE) {
            if (!requiredDeviceOs.toString().equals(matchDeviceInformation.getOS())) {
                return 0;
            }
        }

        if (requiredDeviceDpi != DeviceParameters.DPI_NO_PREFERENCE) {
            if (requiredDeviceDpi != matchDeviceInformation.getDpi()) {
                return 0;
            }
        }

        if (requiredDeviceRam != DeviceParameters.RAM_NO_PREFERENCE) {
            if (requiredDeviceRam != matchDeviceInformation.getRam()) {
                return 0;
            }
        }

        if (requiredDeviceResH != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE) {
            if (requiredDeviceResH != matchDeviceInformation.getResolution().getKey()) {
                return 0;
            }
        }

        if (requiredDeviceResW != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE) {
            if (requiredDeviceResW != matchDeviceInformation.getResolution().getValue()) {
                return 0;
            }
        }

        if (!requiredDeviceSerialNumber.equals(DeviceParameters.SERIALNUMBER_NO_PREFERENCE)) {
            if (!requiredDeviceSerialNumber.equals(matchDeviceInformation.getSerialNumber())) {
                return 0;
            }
        }

        if (!requiredDeviceModel.equals(DeviceParameters.MODEL_NO_PREFERENCE)) {
            if (!requiredDeviceModel.equals(matchDeviceInformation.getModel())) {
                return 0;
            }
        }

        if (requiredDeviceCameraPresence != DeviceParameters.HAS_CAMERA_NO_PREFERENCE) {
            if (requiredDeviceCameraPresence != matchDeviceInformation.hasCamera()) {
                return 0;
            }
        }
        if (requiredDeviceApiVersion != DeviceParameters.API_LEVEL_NO_PREFERENCE) {
            if (requiredDeviceApiVersion != matchDeviceInformation.getApiLevel()) {
                return 0;
            }
        }
        return result;
    }

    /**
     * Checks whether the {@link DeviceInformation} of given {@link IDevice} is a valid match for the given
     * {@link DeviceParameters} in the constructor.
     * 
     * @param device
     *        - {@link IDevice} which {@link DeviceInformation} will be compared to the given {@link DeviceParameters}
     * @return - True if the {@link DeviceInformation} is a valid match to the {@link DeviceParameters}, false otherwise
     */
    public boolean isValidMatch(IDevice device) {
        DeviceInformation availableDeviceInformation = device.getInformation();

        int matchScore = matchingScore(availableDeviceInformation);
        boolean isMatch = matchScore > 0;
        return isMatch;
    }

}
