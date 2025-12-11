package uj.wmii.pwj.anns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MyTestEngine {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) {
        String className;

        if (args.length >= 1) {
            className = args[0].trim();
        }
        else {
            className = "uj.wmii.pwj.anns.MyBeautifulTestSuite";
        }

        printBanner();

        System.out.printf("Testing class: %s%s%s\n\n", ANSI_CYAN, className, ANSI_RESET);

        MyTestEngine engine = new MyTestEngine();
        engine.runTests(className);
    }

    private static void printBanner() {
        System.out.println(ANSI_CYAN);
        System.out.println("######## ########  ######  ########    ######## ##    ##  ######   #### ##    ## ######## ");
        System.out.println("   ##    ##       ##    ##    ##       ##       ###   ## ##    ##   ##  ###   ## ##       ");
        System.out.println("   ##    ##       ##          ##       ##       ####  ## ##         ##  ####  ## ##       ");
        System.out.println("   ##    ######    ######     ##       ######   ## ## ## ##   ####  ##  ## ## ## ######   ");
        System.out.println("   ##    ##             ##    ##       ##       ##  #### ##    ##   ##  ##  #### ##       ");
        System.out.println("   ##    ##       ##    ##    ##       ##       ##   ### ##    ##   ##  ##   ### ##       ");
        System.out.println("   ##    ########  ######     ##       ######## ##    ##  ######   #### ##    ## ######## ");
        System.out.println(ANSI_RESET);
    }

    public void runTests(String className) {
        Object unit = getObject(className);
        if (unit == null) return;

        int totalTests = 0;
        int passed = 0;
        int failed = 0;
        int errors = 0;

        for (Method m : unit.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(MyTest.class)) {
                MyTest testAnnotation = m.getAnnotation(MyTest.class);
                TestCase[] cases = testAnnotation.value();

                if (cases.length == 0) {
                    totalTests++;
                    TestResult result = executeSingleTest(m, unit, new Object[]{}, null);
                    printResult(m.getName(), "No Params", result);
                    if (result == TestResult.PASS) passed++;
                    else if (result == TestResult.FAIL) failed++;
                    else errors++;
                } else {
                    for (TestCase testCase : cases) {
                        totalTests++;
                        Object[] args = parseParameters(m.getParameterTypes(), testCase.params());
                        TestResult result = executeSingleTest(m, unit, args, testCase.expected());

                        String caseDesc = String.join(", ", testCase.params());
                        printResult(m.getName(), caseDesc, result);

                        if (result == TestResult.PASS) passed++;
                        else if (result == TestResult.FAIL) failed++;
                        else errors++;
                    }
                }
            }
        }

        System.out.println("\n" + "-".repeat(50));
        System.out.printf("SUMMARY: Total: %d | %sPass: %d%s | %sFail: %d%s | %sError: %d%s\n",
                totalTests,
                ANSI_GREEN, passed, ANSI_RESET,
                ANSI_RED, failed, ANSI_RESET,
                ANSI_YELLOW, errors, ANSI_RESET);
    }

    private TestResult executeSingleTest(Method m, Object unit, Object[] args, String expectedStr) {
        try {
            Object actualResult = m.invoke(unit, args);

            if (expectedStr == null) {
                return TestResult.PASS;
            }

            Object expectedValue = parseValue(m.getReturnType(), expectedStr);

            if (expectedValue.equals(actualResult)) {
                return TestResult.PASS;
            } else {
                System.out.printf("\t%s[Expected: %s, Got: %s]%s\n", ANSI_RED, expectedValue, actualResult, ANSI_RESET);
                return TestResult.FAIL;
            }

        } catch (InvocationTargetException e) {
            System.out.printf("\t%s[Exception: %s]%s\n", ANSI_YELLOW, e.getCause().getClass().getSimpleName(), ANSI_RESET);
            return TestResult.ERROR;
        } catch (Exception e) {
            e.printStackTrace();
            return TestResult.ERROR;
        }
    }

    private void printResult(String method, String args, TestResult result) {
        String color;
        switch (result) {
            case PASS: color = ANSI_GREEN; break;
            case FAIL: color = ANSI_RED; break;
            case ERROR: color = ANSI_YELLOW; break;
            default: color = ANSI_RESET;
        }
        System.out.printf("[%s%s%s] Method: %-15s Params: (%s)\n", color, result, ANSI_RESET, method, args);
    }

    private Object[] parseParameters(Class<?>[] types, String[] params) {
        if (params.length != types.length) {
            throw new IllegalArgumentException("Parameter count mismatch!");
        }
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = parseValue(types[i], params[i]);
        }
        return args;
    }

    private Object parseValue(Class<?> type, String value) {
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private static Object getObject(String className) {
        try {
            Class<?> unitClass = Class.forName(className);
            return unitClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            System.out.println("Could not instantiate class: " + className);
            return null;
        }
    }
}