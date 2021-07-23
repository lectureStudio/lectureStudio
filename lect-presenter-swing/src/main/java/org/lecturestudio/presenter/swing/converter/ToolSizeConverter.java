package org.lecturestudio.presenter.swing.converter;

import org.lecturestudio.core.beans.Converter;

/**
 * Tool size to slide space and vice-versa converter.
 */
public class ToolSizeConverter implements Converter<Double, Integer> {

    public static final ToolSizeConverter INSTANCE = new ToolSizeConverter();


    @Override
    public Integer to(Double value) {
        return (int) (value * 500);
    }

    @Override
    public Double from(Integer value) {
        return value / 500.d;
    }
}
