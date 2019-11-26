import java.util.*;
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

    //ispis pocetne populacije, x[i], y[i], decimalno i binarno
    private static void printingInitialPopulation(List<Double> cooridantesX, List<Double> cooridantesY) {
        System.out.println("x                   y" );
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(coordinate +  " " + cooridantesY.get(i[0]++));
        });
    }

    //racunanje funkcije z = f(x, y)
    private static List<Double> computeFunctionOfCoordinates(List<Double> cooridantesX, List<Double> cooridantesY) {
     List<Double> computedFunctionZ = new ArrayList<Double>(cooridantesX.size());
     double x, y, z;
     for(int i = 0; i < cooridantesX.size(); i++)
     {
         x = cooridantesX.get(i);
         y = cooridantesY.get(i);
         z = 3 * Math.pow((1 - x),2) * Math.exp(-Math.pow(x,2) - Math.pow((y + 1),2)) - 10 * (x / 5.0 - Math.pow(x, 3) - Math.pow(y,5)) * Math.exp(-Math.pow(x, 2) - Math.pow(y, 2))- (1/3.0) *Math.exp(-Math.pow(x + 1, 2)-Math.pow(y, 2));
         computedFunctionZ.add(z);
     }
     return computedFunctionZ;
    }

    private static List<Double> computeFitnessFunction(List<Double> computedFunctionZ, boolean isMinimum){
        List<Double> computedFitnesFunction = new ArrayList<Double>(computedFunctionZ.size());
        Double referenceValue = computedFunctionZ.get(0);
        if(isMinimum) //za minimum
            computedFunctionZ.stream().forEach(fx ->{
                computedFitnesFunction.add(referenceValue - fx); //𝑓𝑓(𝑥) = max [𝑥𝑖] 𝑓(𝑥) − 𝑓(𝑥)
            });
        else{
            computedFunctionZ.stream().forEach(fx ->{
                computedFitnesFunction.add(fx - referenceValue); //𝑓𝑓(𝑥) = 𝑓(𝑥) − min [xi] 𝑓(𝑥)
            });
        }
        return  computedFitnesFunction;
    }

    private static Double computeRateOfPopulation(List<Double> fitnessMinComputedFunction) {
        return  fitnessMinComputedFunction.stream().mapToDouble(f -> f.doubleValue()).sum();
    }

    //racunanje 𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
    private static List<Double> computeIndividualsSelectionProbaibility(List<Double> fitnessMaxComputedFunction, Double rateOfPopulation) {
        List<Double> computedProbaibility = new ArrayList<Double>(fitnessMaxComputedFunction.size());
        fitnessMaxComputedFunction.stream().forEach(ffx ->{
            computedProbaibility.add(ffx / rateOfPopulation); //𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
        });
        return  computedProbaibility;
    }

    public static void main(String[] args) {

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

        printingInitialPopulation(cooridantesX, cooridantesY);

        //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije
        List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

        //racunanje ff(x) za minimum funkcije
        Collections.sort(computedFunctionZ); //uzlazno sortiranje, za minimum
        List<Double> fitnessMinComputedFunction = computeFitnessFunction(computedFunctionZ, true);

        //racunanje ff(x) za maximum funkcije
        Collections.sort(computedFunctionZ, Collections.reverseOrder()); //opadajuce soritranje, za maximum
        List<Double> fitnessMaxComputedFunction = computeFitnessFunction(computedFunctionZ, false);

        //racunanje ocjene cijele pocetne populacije (F)
        Double rateOfPopulation = computeRateOfPopulation(fitnessMinComputedFunction);

        //racunanje vjerovatnoce izbora jedinke (p)
        List<Double> individualsSelectionProbaibilityMax = computeIndividualsSelectionProbaibility(fitnessMaxComputedFunction, rateOfPopulation); // za maksimum

        List<Double> individualsSelectionProbaibilityMin = computeIndividualsSelectionProbaibility(fitnessMinComputedFunction, rateOfPopulation); // za minimum

        //racunanje kumulativne vjerovatnoce (q)

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
