import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChartBuilder;
import javafx.stage.Stage;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jdk.nashorn.internal.objects.NativeMath.round;

public class Main {

    //interval
    private static Integer LOWER_BOUND = -3;
    private static Integer HIGHER_BOUND = 3;

    //preciznost
    private static Integer PRECISION_P = 2;

    //funkcija koja pomocu x, y racuna z(x,y)
    public Double projectFunction(Double x, Double y) {
        return null;
    }

    //generisanje pocetne populacije
    private static List<Double> generateInitialPopulation(Integer sizeOfPopulation) {
        return new Random().doubles(LOWER_BOUND, HIGHER_BOUND).limit(sizeOfPopulation).boxed().collect(Collectors.toList());
    }

    private static List<Double> generateRandomNumbers(Integer sizeOfPopulation) {
        return new Random().doubles(0, 1).limit(sizeOfPopulation).boxed().collect(Collectors.toList());
    }

    //broj bita za kodovanje
    private static Integer computeSizeOfBitsForCoding() {
        return (int)Math.ceil(log2((HIGHER_BOUND - LOWER_BOUND) * Math.pow(10, PRECISION_P) + 1)); //n = 𝑙𝑜𝑔2[(𝐺𝑔 − 𝐺𝑑)10𝑝 + 1]
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
    private static void printingInitialPopulation(List<Double> cooridantesX, List<Double> cooridantesY, List<Integer> decimalCodedCoordinatesX, List<Integer> decimalCodedCoordinatesY, List<String> binaryCodedCoordinatesX, List<String> binaryCodedCoordinatesY) {
        System.out.println("i x      DEC  KOD" );
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate +  " " + decimalCodedCoordinatesX.get(i[0]) + " " + binaryCodedCoordinatesX.get(i[0]++));
        });

        System.out.println();
        System.out.println("i y      DEC  KOD" );
        i[0] = 0;
        cooridantesY.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate +  " " + decimalCodedCoordinatesY.get(i[0]) + " " + binaryCodedCoordinatesY.get(i[0]++));
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
         z = 3*Math.pow((1-x),2)*Math.exp(-Math.pow(x,2)-Math.pow((y+1),2))-10*(x/5.0-Math.pow(x, 3)-Math.pow(y,5))*Math.exp(-Math.pow(x, 2)-Math.pow(y, 2))-(1/3.0)*Math.exp(-Math.pow(x+1, 2)-Math.pow(y, 2));
         z =   new BigDecimal(z).round(new MathContext(5)).doubleValue();
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
    private static List<Double> computeIndividualsProbaibility(List<Double> fitnessMaxComputedFunction, Double rateOfPopulation) {
        List<Double> computedProbaibility = new ArrayList<Double>(fitnessMaxComputedFunction.size());
        fitnessMaxComputedFunction.stream().forEach(ffx ->{
            computedProbaibility.add(ffx / rateOfPopulation); //𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
        });
        return  computedProbaibility;
    }

    //racunanje 𝑞[𝑖] = Σ 𝑝[𝑗]
    private static List<Double> computeCumulativeProbaibility(List<Double> computedProbaibility) {
        List<Double> computedCumulativeProbaibility = new ArrayList<Double>(computedProbaibility.size());
        computedCumulativeProbaibility.add(computedProbaibility.get(0));
        int i[] = {0};
        computedProbaibility.stream().forEach(pi ->{
            Double qi = computedCumulativeProbaibility.get(i[0]++) + pi;
            computedCumulativeProbaibility.add(qi); //kumulativna vjerovatnoca = donja granica + sirina "parceta pite"
        });
        return  computedCumulativeProbaibility;
    }

    private static void roundCoordinates(List<Double> cooridantes) {
        int i = 0;
        double rounded;
        for(Double coordinate : cooridantes) {
            rounded = (double) Math.round(coordinate * 10000.0) / 10000.0;
            cooridantes.set(i++, rounded);
        }
    }

    private static void setChartProperties(PieChart chart, List<Double> individualsSelectionProbaibilityMax) throws IOException {
        // Customize Chart
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        chart.getStyler().setAnnotationDistance(1.15);
        chart.getStyler().setPlotContentSize(.7);
        chart.getStyler().setStartAngleInDegrees(90);

        // Series
        int i = 0;
        for(Double individual : individualsSelectionProbaibilityMax)
            chart.addSeries("p[" + i++ + "] = " + individual, individual);

        // Show it
        new SwingWrapper(chart).displayChart();

        // Save it
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);
    }

    private static void turnRoulette(List<Double> randomNumbers, List<Double> individualsCumulativeProbaibilityMax) {
    //provjeriti koja se jedinka(hromozom) nalazi u kojoj kumulativnoj vjerovatnoci
    }

    public static void main(String[] args) throws IOException {

        Scanner scInt = new Scanner(System.in);

        //broj jedinki
        System.out.println("Unesite broj jedinki ");
        Integer sizeOfPopulation = scInt.nextInt();

        //generisanje pocetne populacije (xi, yi)
        List<Double> cooridantesX = generateInitialPopulation(sizeOfPopulation);
        roundCoordinates(cooridantesX); //zaokuruzivanje jedinki na 4 decimale
        List<Double> cooridantesY = generateInitialPopulation(sizeOfPopulation);
        roundCoordinates(cooridantesY); //zaokuruzivanje jedinki na 4 decimale

        //broj bita potrebnih za kodovanje (n)
        Integer sizeOfBitsForCoding = computeSizeOfBitsForCoding();

        //decimalno kodovanje (bd)
        List<Integer> decimalCodedCoordinatesX = codeCoordinatesToDecimal(cooridantesX, sizeOfBitsForCoding);
        List<Integer> decimalCodedCoordinatesY = codeCoordinatesToDecimal(cooridantesY, sizeOfBitsForCoding);

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - pocetna populacija
        List<String> binaryCodedCoordinatesX = codeCoordinatesToBinary(decimalCodedCoordinatesX, sizeOfBitsForCoding);
        List<String> binaryCodedCoordinatesY = codeCoordinatesToBinary(decimalCodedCoordinatesY, sizeOfBitsForCoding);

        printingInitialPopulation(cooridantesX, cooridantesY, decimalCodedCoordinatesX, decimalCodedCoordinatesY, binaryCodedCoordinatesX, binaryCodedCoordinatesY);

        /*do ovoga je sve u redu*/

        //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije
        List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);
        computedFunctionZ.stream().forEach(System.out::println);

        //racunanje ff(x) za minimum funkcije
        Collections.sort(computedFunctionZ); //uzlazno sortiranje, za minimum
        List<Double> fitnessMinComputedFunction = computeFitnessFunction(computedFunctionZ, true);

        //racunanje ff(x) za maximum funkcije
        Collections.sort(computedFunctionZ, Collections.reverseOrder()); //opadajuce soritranje, za maximum
        List<Double> fitnessMaxComputedFunction = computeFitnessFunction(computedFunctionZ, false);

        //racunanje ocjene cijele pocetne populacije (F)
        Double rateOfPopulation = computeRateOfPopulation(fitnessMinComputedFunction);

        //racunanje vjerovatnoce izbora jedinke (p)
        List<Double> individualsSelectionProbaibilityMax = computeIndividualsProbaibility(fitnessMaxComputedFunction, rateOfPopulation); // za maksimum

        List<Double> individualsSelectionProbaibilityMin = computeIndividualsProbaibility(fitnessMinComputedFunction, rateOfPopulation); // za minimum

        //racunanje kumulativne vjerovatnoce (q)

        List<Double> individualsCumulativeProbaibilityMax = computeCumulativeProbaibility(individualsSelectionProbaibilityMax); // za maksimum

        List<Double> individualsCumulativeProbaibilityMin = computeCumulativeProbaibility(individualsSelectionProbaibilityMin); // za minimum

        //ruletska selekcija (ispis kao Tabela 5.)

        //generisanje slucajnih brojeva (ri)
        List<Double> randomNumbers = generateRandomNumbers(sizeOfPopulation);
        roundCoordinates(randomNumbers);    //zaokuruzivanje slucajnih brojeva na 4 decimale

     //   List<Double> newMinGeneration = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin);
    //    List<Double> newMaxGeneration = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax);
        PieChart minChart = new org.knowm.xchart.PieChartBuilder().width(900).height(700).title("Simuliran tocak ruleta (za minimum)").theme(Styler.ChartTheme.GGPlot2).build();
        setChartProperties(minChart, individualsSelectionProbaibilityMax);

        PieChart maxChart = new org.knowm.xchart.PieChartBuilder().width(900).height(7500).title("Simulirani tocak ruleta (za maximum)").theme(Styler.ChartTheme.GGPlot2).build();
        setChartProperties(maxChart, individualsSelectionProbaibilityMax);



        //ispis medjugeneracije (ispis kao Tabela 6.)

        //mijesanje (ispis kao Tabela 7.)

        //ispis medjugeneracije (ispis kao Tabela 8.)

        //odluka o ukrstanju

        //izbor tacke ili tacaka za rekombinaciju

        //odluka o mutaciji i izbor tacke za mutaciju

        //ispis naredne generacije
    }

}
