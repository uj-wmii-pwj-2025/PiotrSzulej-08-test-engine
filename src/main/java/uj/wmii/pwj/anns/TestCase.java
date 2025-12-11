package uj.wmii.pwj.anns;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCase {
    String[] params();
    String expected();
}
