package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest
    public void simpleSuccess() {
        System.out.print("\tRunning simple logic");
    }

    @MyTest
    public void simpleError() {
        throw new RuntimeException("Something went wrong");
    }

    @MyTest({
            @TestCase(params = {"2", "3"}, expected = "5"),
            @TestCase(params = {"10", "5"}, expected = "15"),
            @TestCase(params = {"2", "2"}, expected = "5")
    })
    public int add(int a, int b) {
        return a + b;
    }

    @MyTest({
            @TestCase(params = {"10", "2"}, expected = "5"),
            @TestCase(params = {"10", "0"}, expected = "0")
    })
    public int divide(int a, int b) {
        return a / b;
    }

    @MyTest({
            @TestCase(params = {"Hello", "World"}, expected = "Hello World")
    })
    public String concat(String a, String b) {
        return a + " " + b;
    }
}