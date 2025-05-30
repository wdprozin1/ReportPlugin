package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.SystemUtils;

public interface Counter {

    DecimalFormat DECIMAL_FORMAT = (DecimalFormat) SystemUtils.make(new DecimalFormat("########0.00"), (decimalformat) -> {
        decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });
    Counter DEFAULT;
    Counter DIVIDE_BY_TEN;
    Counter DISTANCE;
    Counter TIME;

    String format(int i);

    static {
        NumberFormat numberformat = NumberFormat.getIntegerInstance(Locale.US);

        Objects.requireNonNull(numberformat);
        DEFAULT = numberformat::format;
        DIVIDE_BY_TEN = (i) -> {
            return Counter.DECIMAL_FORMAT.format((double) i * 0.1D);
        };
        DISTANCE = (i) -> {
            double d0 = (double) i / 100.0D;
            double d1 = d0 / 1000.0D;

            return d1 > 0.5D ? Counter.DECIMAL_FORMAT.format(d1) + " km" : (d0 > 0.5D ? Counter.DECIMAL_FORMAT.format(d0) + " m" : "" + i + " cm");
        };
        TIME = (i) -> {
            double d0 = (double) i / 20.0D;
            double d1 = d0 / 60.0D;
            double d2 = d1 / 60.0D;
            double d3 = d2 / 24.0D;
            double d4 = d3 / 365.0D;

            return d4 > 0.5D ? Counter.DECIMAL_FORMAT.format(d4) + " y" : (d3 > 0.5D ? Counter.DECIMAL_FORMAT.format(d3) + " d" : (d2 > 0.5D ? Counter.DECIMAL_FORMAT.format(d2) + " h" : (d1 > 0.5D ? Counter.DECIMAL_FORMAT.format(d1) + " min" : "" + d0 + " s")));
        };
    }
}
