package dev.ahmedmourad.nocopy.sample;

public class JavaMain {
    public static void main(String[] args) {
        System.out.println(
                PhoneNumber.Companion.of("0145347534").copy("zzzz")
        );
    }
}
