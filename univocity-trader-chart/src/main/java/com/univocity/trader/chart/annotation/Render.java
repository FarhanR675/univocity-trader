package com.univocity.trader.chart.annotation;

import com.univocity.trader.chart.charts.painter.renderer.*;
import com.univocity.trader.chart.dynamic.*;

import java.lang.annotation.*;

@Repeatable(Render.List.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface Render {
	String value() default "getValue";

	double constant() default Double.MIN_VALUE;

	String description() default "";

	Class<? extends Renderer<?>> renderer() default LineRenderer.class;

	Class<? extends Theme> theme() default Theme.class;

	boolean displayValue() default true;

	String[] args() default "";

	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	@interface List {
		Render[] value();
	}
}
