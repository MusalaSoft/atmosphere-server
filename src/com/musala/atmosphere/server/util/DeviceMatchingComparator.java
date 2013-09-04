package com.musala.atmosphere.server.util;

import java.util.Comparator;

import com.musala.atmosphere.commons.DeviceInformation;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceOs;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceParameters;
import com.musala.atmosphere.commons.cs.clientbuilder.DeviceType;

public class DeviceMatchingComparator implements Comparator<DeviceInformation>
{
	private DeviceParameters requiredDeviceParameters;

	public DeviceMatchingComparator(DeviceParameters matchingBase)
	{
		this.requiredDeviceParameters = matchingBase;
	}

	@Override
	public int compare(DeviceInformation availDeviceFirst, DeviceInformation availDeviceSecond)
	{
		int matchingScoreFirst = matchingScore(requiredDeviceParameters, availDeviceFirst);
		int matchingScoreSecond = matchingScore(requiredDeviceParameters, availDeviceSecond);

		return matchingScoreFirst - matchingScoreSecond;
	}

	/**
	 * Calculates a required/present device matching score. Each device parameter should be taken into consideration
	 * with different weight. This method will be changed when more complex selection mechanism is discussed. At the
	 * moment it is a simple match check.
	 * 
	 * @param requiredDevice
	 *        information for a device that is needed.
	 * @param matchDevice
	 *        information for a present device.
	 * @return
	 */
	private static int matchingScore(DeviceParameters neededDeviceParameters, DeviceInformation matchDeviceInformation)
	{
		int result = 1;
		DeviceType requiredDeviceType = neededDeviceParameters.getDeviceType();
		int requiredDeviceDpi = neededDeviceParameters.getDpi();
		DeviceOs requiredDeviceOs = neededDeviceParameters.getOs();
		String requiredDeviceModel = neededDeviceParameters.getModel();
		String requiredDeviceSerialNumber = neededDeviceParameters.getSerialNumber();
		int requiredDeviceRam = neededDeviceParameters.getRam();
		int requiredDeviceResH = neededDeviceParameters.getResolutionHeight();
		int requiredDeviceResW = neededDeviceParameters.getResolutionWidth();

		if (requiredDeviceType != DeviceParameters.DEVICE_TYPE_NO_PREFERENCE)
		{
			if (requiredDeviceType == DeviceType.DEVICE_ONLY)
			{
				if (matchDeviceInformation.isEmulator())
				{
					return 0;
				}
			}
			else if (requiredDeviceType == DeviceType.EMULATOR_ONLY)
			{
				if (!matchDeviceInformation.isEmulator())
				{
					return 0;
				}
			}
		}

		if (requiredDeviceOs != DeviceParameters.DEVICE_OS_NO_PREFERENCE)
		{
			if (!requiredDeviceOs.toString().equals(matchDeviceInformation.getOS()))
			{
				return 0;
			}
		}

		if (requiredDeviceDpi != DeviceParameters.DPI_NO_PREFERENCE)
		{
			if (requiredDeviceDpi != matchDeviceInformation.getDpi())
			{
				return 0;
			}
		}

		if (requiredDeviceRam != DeviceParameters.RAM_NO_PREFERENCE)
		{
			if (requiredDeviceRam != matchDeviceInformation.getRam())
			{
				return 0;
			}
		}

		if (requiredDeviceResH != DeviceParameters.RESOLUTION_HEIGHT_NO_PREFERENCE)
		{
			if (requiredDeviceResH != matchDeviceInformation.getResolution().getKey())
			{
				return 0;
			}
		}

		if (requiredDeviceResW != DeviceParameters.RESOLUTION_WIDTH_NO_PREFERENCE)
		{
			if (requiredDeviceResW != matchDeviceInformation.getResolution().getValue())
			{
				return 0;
			}
		}

		if (!requiredDeviceSerialNumber.equals(DeviceParameters.SERIALNUMBER_NO_PREFERENCE))
		{
			if (!requiredDeviceSerialNumber.equals(matchDeviceInformation.getSerialNumber()))
			{
				return 0;
			}
		}

		if (!requiredDeviceModel.equals(DeviceParameters.MODEL_NO_PREFERENCE))
		{
			if (!requiredDeviceModel.equals(matchDeviceInformation.getModel()))
			{
				return 0;
			}
		}

		return result;
	}

	/**
	 * Checks whether a given {@link DeviceInformation} is a valid match for given {@link DeviceParameters}.
	 * 
	 * @param neededDeviceParameters
	 *        - {@link DeviceParameters} that need to be matched.
	 * @param availableDeviceInformation
	 *        - {@link DeviceInformation} that is compared to the {@link DeviceParameters}.
	 * @return - True if the {@link DeviceInformation} is a valid match to the {@link DeviceParameters}. False
	 *         otherwise.
	 */
	public static boolean isValidMatch(	DeviceParameters neededDeviceParameters,
										DeviceInformation availableDeviceInformation)
	{
		int matchScore = matchingScore(neededDeviceParameters, availableDeviceInformation);
		boolean isMatch = matchScore > 0;
		return isMatch;
	}

}
