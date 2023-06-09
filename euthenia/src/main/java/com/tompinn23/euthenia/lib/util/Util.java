package com.tompinn23.euthenia.lib.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tompinn23.euthenia.lib.logistics.fluid.Tank;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.openjdk.nashorn.internal.runtime.options.Option;

import java.text.NumberFormat;
import java.util.*;

public class Util {

    public static final int BUCKET_AMOUNT = 1000;

    public static boolean anyMatch(int[] arr, int value) {
        return Arrays.stream(arr).anyMatch(i -> i == value);
    }
    public static boolean anyMatch(long[] arr, long value) {
        return Arrays.stream(arr).anyMatch(i -> i == value);
    }

    public static int safeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public static int safeInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    public static long safeLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    static final NavigableMap<Long, String> SUFFIXES = new TreeMap<>();

    static {
        SUFFIXES.put(1_000L, "k");
        SUFFIXES.put(1_000_000L, "M");
        SUFFIXES.put(1_000_000_000L, "B");
        SUFFIXES.put(1_000_000_000_000L, "T");
        SUFFIXES.put(1_000_000_000_000_000L, "P");
        SUFFIXES.put(1_000_000_000_000_000_000L, "E");
    }

    public static Component formatTankContent(Tank tank) {
        return formatTankContent(tank.getFluidAmount(), tank.getCapacity());
    }

    public static Component formatTankContent(long amount, long capacity) {
        return Component.translatable("info.balnor.mb.stored",
                Util.addCommas(amountToMillibuckets(amount)),
                Util.numFormat(amountToMillibuckets(capacity))
        ).withStyle(ChatFormatting.DARK_GRAY);
    }

    /**
     * Amount of fluid in one millibucket.
     * 1 on Forge.
     */
    public static int millibucketAmount() {
        return (int) (BUCKET_AMOUNT / 1000);
    }

    public static long amountToMillibuckets(long amount) {
        var result = amount * 1000 / BUCKET_AMOUNT;
        if (result == 0 && amount != 0) {
            return amount >= 0 ? 1 : -1;
        }
        return result;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public static String numFormat(long value) {
        if (value == Long.MIN_VALUE) return numFormat(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + numFormat(-value);
        if (value < 1000) return Long.toString(value);

        Map.Entry<Long, String> e = SUFFIXES.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String addCommas(long value) {
        return NumberFormat.getInstance(Locale.ROOT).format(value);
    }

    public static JsonElement getElement(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return json.get(memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + "");
        }
    }

    public static ResourceLocation getResourceLocation(JsonObject json, String key) {
        String text = GsonHelper.getAsString(json, key);
        ResourceLocation location = ResourceLocation.tryParse(text);
        if (location == null) {
            throw new JsonSyntaxException("Expected " + key + " to be a Resource location, was '" + text + "'");
        }
        return location;
    }

    public static <T> Optional<T> getBlockEntityAt(Class<T> clazz, LevelReader level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if(clazz.isInstance(entity)) {
            return Optional.of((T)entity);
        }
        return Optional.empty();
    }
}
