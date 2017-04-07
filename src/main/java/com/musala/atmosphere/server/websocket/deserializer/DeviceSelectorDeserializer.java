package com.musala.atmosphere.server.websocket.deserializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceParameter;
import com.musala.atmosphere.commons.cs.deviceselection.DeviceSelector;

public class DeviceSelectorDeserializer implements JsonDeserializer<DeviceSelector> {

    @Override
    public DeviceSelector deserialize(JsonElement element, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        Gson gson = new Gson();
        JsonArray rootArray = element.getAsJsonArray();
        Map<Class<? extends DeviceParameter>, DeviceParameter> parameters = new HashMap<>();
        for (JsonElement parameterArrayElement : rootArray) {
            try {
                JsonArray parameterArray = parameterArrayElement.getAsJsonArray();

                Class<? extends DeviceParameter> parameterClass =
                        (Class<? extends DeviceParameter>) Class.forName(parameterArray.get(0).getAsString());
                DeviceParameter parameter = gson.fromJson(parameterArray.get(1).getAsString(), parameterClass);

                parameters.put(parameterClass, parameter);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        DeviceSelector selector = new DeviceSelector(parameters);

        return selector;
    }
}
