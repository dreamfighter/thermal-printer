package id.dreamfighter.android.thermalprinter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TableColumn{
    String key() default "";
    String name() default "";
    String align() default "left";
}
