package com.tompinn23.hephaestus.config;

import com.tompinn23.hephaestus.Hephaestus;
import com.tompinn23.hephaestus.config.annotations.IntRange;
import com.tompinn23.hephaestus.config.annotations.LongRange;
import com.tompinn23.hephaestus.config.type.MachineConfigs;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import static com.tompinn23.hephaestus.config.Defaults.*;

@me.shedaniel.autoconfig.annotation.Config(name = Hephaestus.MODID)
public class Config implements ConfigData {
    @Comment("Machine configurations options")
    public final Machines machines = new Machines();

    public static class Machines {
        public final MachineConfigs crusher = new MachineConfigs(defaultVoltages(), defaultAmps(), defaultCapacities());
    }

    @Override
    public void validatePostLoad() throws ValidationException {
        try {
            validateObject(this);
        } catch (ReflectiveOperationException roe) {
            throw new ValidationException("Failed to validate Heph config", roe);
        }
    }

    private static void validateObject(Object object) throws ValidationException, ReflectiveOperationException {
        for (var field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            var value = field.get(object);

            if (value instanceof Number number) {
//                var doubleRange = field.getAnnotation(DoubleRange.class);
//                if (doubleRange != null) {
//                    if (number.doubleValue() > doubleRange.max() || number.doubleValue() < doubleRange.min()) {
//                        throw new ValidationException("Expected double entry %s = %f to be in range [%f, %f]".formatted(
//                                field.getName(),
//                                number.doubleValue(),
//                                doubleRange.min(),
//                                doubleRange.max()
//                        ));
//                    }
//                }
                var intRange = field.getAnnotation(IntRange.class);
                if (intRange != null) {
                    if (number.intValue() > intRange.max() || number.intValue() < intRange.min()) {
                        throw new ValidationException("Expected integer entry %s = %d to be in range [%d, %d]".formatted(
                                field.getName(),
                                number.intValue(),
                                intRange.min(),
                                intRange.max()
                        ));
                    }
                }
                var longRange = field.getAnnotation(LongRange.class);
                if (longRange != null) {
                    if (number.longValue() > longRange.max() || number.longValue() < longRange.min()) {
                        throw new ValidationException("Expected long entry %s = %d to be in range [%d, %d]".formatted(
                                field.getName(),
                                number.longValue(),
                                longRange.min(),
                                longRange.max()
                        ));
                    }
                }
            } else if (value != null && value.getClass().getPackageName().startsWith("com.tompinn23.hephaestus.config")) {
                validateObject(value);
            }
        }
    }

    public static ConfigHolder<Config> register() {
        return AutoConfig.register(Config.class, (cfg, cfgClass) -> {
            var janksonBuilder = Jankson.builder();
            // Resource Location
            janksonBuilder.registerDeserializer(String.class, ResourceLocation.class, (string, marshaller) -> {
                try {
                    return new ResourceLocation(string);
                } catch (ResourceLocationException exception) {
                    throw new DeserializationException("Not a valid resource location: " + string, exception);
                }
            });
            janksonBuilder.registerSerializer(ResourceLocation.class, (resLoc, marshaller) -> {
                return new JsonPrimitive(resLoc.toString());
            });

            return new JanksonConfigSerializer<>(cfg, cfgClass, janksonBuilder.build());
        });
    }
}
