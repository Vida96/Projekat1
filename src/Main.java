import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    //interval
    private static Integer LOWER_BOUND = -15;
    private static Integer HIGHER_BOUND = 15;

    //funkcija koja pomocu x, y racuna z(x,y)
    public Double projectFunction(Double x, Double y) {
        return null;
    }

    private static List<Double> generateInitialPopulation(Integer sizeOfPopulation) {
        return new Random().doubles(LOWER_BOUND,HIGHER_BOUND).limit(sizeOfPopulation).boxed().collect(Collectors.toList());
    }

    private static List<Double> generateRandomNumbers(Integer sizeOfPopulation) {
        return generateInitialPopulation(sizeOfPopulation);
    }

    public static void main(String[] args){

        Scanner scInt = new Scanner(System.in);
        Scanner scDouble = new Scanner(System.in);

        //broj jedinki
        System.out.println("Unesite broj jedinki ");
        Integer sizeOfPopulation = scInt.nextInt();

        //generisanje pocetne populacije (xi, yi)
        List<Double> cooridantesX = generateInitialPopulation(sizeOfPopulation);
        List<Double> cooridantesY = generateInitialPopulation(sizeOfPopulation);

        //generisanje slucajnih brojeva (ri)
        List<Double> randomNumbers = generateRandomNumbers(sizeOfPopulation);

        //broj bita potrebnih za kodovanje (n)

        //decimalno kodovanje (bd)

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - pocetna populacija

        //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije

        //racunanje ocjene cijele pocetne populacije (F)

        //racunanje vjerovatnoce izbora jedinke (p)

        //racunanje kumulativne vjerovatnoce (q)

        //ruletska selekcija (ispis kao Tabela 5.)

        //ispis medjugeneracije (ispis kao Tabela 6.)

        //mijesanje (ispis kao Tabela 7.)

        //ispis medjugeneracije (ispis kao Tabela 8.)

        //odluka o ukrstanju

        //izbor tacke ili tacaka za rekombinaciju

        //odluka o mutaciji i izbor tacke za mutaciju

        //ispis naredne generacije
    }

}
