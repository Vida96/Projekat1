import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    //interval
    private static Integer LOWER_BOUND = -15;
    private static Integer HIGHER_BOUND = 15;

    //funkcija koja pomocu x, y racuna z(x,y)
    public Double projectFunction(Double x, Double y) {
        return null;
    }

    //generisanje pocetne populacije
    private static List<Double> generateInitialPopulation(Integer sizeOfPopulation) {
        return new Random().doubles(LOWER_BOUND,HIGHER_BOUND).limit(sizeOfPopulation).boxed().collect(Collectors.toList());
    }

    private static List<Double> generateRandomNumbers(Integer sizeOfPopulation) {
        return generateInitialPopulation(sizeOfPopulation);
    }

    //broj bita za kodovanje
    private static Integer computeSizeOfBitsForCoding() {
        Integer p = 2;
        return (int)Math.ceil(log2((HIGHER_BOUND - LOWER_BOUND) * Math.pow(10, p) + 1)); //n = 𝑙𝑜𝑔2[(𝐺𝑔 − 𝐺𝑑)10𝑝 + 1]
    }

    public static double log2(double n)
    {
        return (Math.log(n) / Math.log(2));
    }

    //decimalno kodovanje koordinata x, y u bd
    private static List<Integer> codeCoordinatesToDecimal(List<Double> cooridantes, Integer sizeOfBitsForCoding) {
        List<Integer> decimalCodedCoordinates = new ArrayList<>(cooridantes.size());
        for(Double coordinate : cooridantes)
            decimalCodedCoordinates.add((int)Math.ceil((coordinate - LOWER_BOUND) / (HIGHER_BOUND - LOWER_BOUND) * (Math.pow(2, sizeOfBitsForCoding) - 1)));  // bd = [(𝑥 − 𝐺𝑑 / Gg - Gd) * (2^n - 1)]
        return decimalCodedCoordinates;
    }

    //binarno kodovanje koordinata x, y
    private static List<String> codeCoordinatesToBinary(List<Integer> decimalCooridantes, Integer sizeOfBitsForCoding) {
        List<String> binaryCodedCoordinates = new ArrayList<>(decimalCooridantes.size());
        for(Integer coordinate : decimalCooridantes)
          binaryCodedCoordinates.add(String.format("%" + sizeOfBitsForCoding.toString() + "s", Integer.toBinaryString(coordinate)).replace(' ', '0'));
        return binaryCodedCoordinates;
    }

    //racunanje z = f(x, y)
    private static List<Double> computeFunctionOfCoordinates(List<Double> cooridantesX, List<Double> cooridantesY) {
     List<Double> computedFunction = new ArrayList<Double>(cooridantesX.size());
     Stream.concat(cooridantesX.stream(), cooridantesY.stream()).forEachOrdered(str -> {
            // compute function
        // computedFunction.add(izracunata f-ja)
     });
     return computedFunction;
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
        Integer sizeOfBitsForCoding = computeSizeOfBitsForCoding();

        //decimalno kodovanje (bd)
        List<Integer> decimalCodedCoordinatesX = codeCoordinatesToDecimal(cooridantesX, sizeOfBitsForCoding);
        List<Integer> decimalCodedCoordinatesY = codeCoordinatesToDecimal(cooridantesY, sizeOfBitsForCoding);

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - pocetna populacija
        List<String> binaryCodedCoordinatesX = codeCoordinatesToBinary(decimalCodedCoordinatesX, sizeOfBitsForCoding);
        List<String> binaryCodedCoordinatesY = codeCoordinatesToBinary(decimalCodedCoordinatesY, sizeOfBitsForCoding);

        //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije
        List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

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
