package com.metarhia.jstp.compiler.annotations;

import com.metarhia.jstp.core.JSTypes.JSValue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by lundibundi on 8/10/16.
 */
@Target({ElementType.METHOD})
public @interface Typed {

  Class value() default JSValue.class;
}
