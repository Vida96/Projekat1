import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.style.PieStyler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    //interval
    private static Integer LOWER_BOUND = -2;
    private static Integer HIGHER_BOUND = 2;

    //preciznost
    private static Integer PRECISION_P = 2;

    //broj jedinki
    private static Integer SIZE_OF_POPULATION = 6;

    //vjerovatnoca rekombinacije
    private static Double RECOMBINATION_PROBAIBILITY = 0.15;

    //vjerovatnoca mutacije
    private static Double MUTATION_PROBAIBILITY = 0.05;

    //generisanje pocetne populacije
    private static List<Double> generateInitialPopulation(Integer SIZE_OF_POPULATION) {
        return new Random().doubles(LOWER_BOUND, HIGHER_BOUND).limit(SIZE_OF_POPULATION).boxed().collect(Collectors.toList());
    }

    private static List<Double> generateRandomNumbers(Integer SIZE_OF_POPULATION) {
        return new Random().doubles(0, 1).limit(SIZE_OF_POPULATION).boxed().collect(Collectors.toList());
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
        System.out.println("i x         DEC   KOD" );
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate +  "   " + decimalCodedCoordinatesX.get(i[0]) + "   " + binaryCodedCoordinatesX.get(i[0]++));
        });

        System.out.println();
        System.out.println("i y         DEC   KOD" );
        i[0] = 0;
        cooridantesY.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate +  "   " + decimalCodedCoordinatesY.get(i[0]) + "   " + binaryCodedCoordinatesY.get(i[0]++));
        });
    }

    //racunanje funkcije z = f(x, y)
    private static List<Double> computeFunctionOfCoordinates(List<Double> cooridantesX, List<Double> cooridantesY) {
     List<Double> computedFunctionZ = new ArrayList<>(cooridantesX.size());
     double x, y, z;
     for(int i = 0; i < cooridantesX.size(); i++)
     {
         x = cooridantesX.get(i);
         y = cooridantesY.get(i);
         z = 3*Math.pow((1-x),2)*Math.exp(-Math.pow(x,2)-Math.pow((y+1),2))-10*(x/5.0-Math.pow(x, 3)-Math.pow(y,5))*Math.exp(-Math.pow(x, 2)-Math.pow(y, 2))-(1/3.0)*Math.exp(-Math.pow(x+1, 2)-Math.pow(y, 2));
         z =  new BigDecimal(z).round(new MathContext(5)).doubleValue();
         computedFunctionZ.add(z);
     }
     return computedFunctionZ;
    }

    private static List<Double> computeFitnessFunction(List<Double> computedFunctionZ, boolean isMinimum) {
        List<Double> computedFitnesFunction = new ArrayList<Double>(computedFunctionZ.size());
        Double referenceValue;
        if (isMinimum)  //ako trazimo minimum
        {
            referenceValue = computedFunctionZ
                    .stream()
                    .mapToDouble(z -> z)
                    .max().orElseThrow(NoSuchElementException::new);  //pronalzanje najvece vrijednosti koja ce biti referentna
        }
        else    //ako trazimo maksimum
        {
            referenceValue = computedFunctionZ
                    .stream()
                    .mapToDouble(z -> z)
                    .min().orElseThrow(NoSuchElementException::new); //pronalzanje najmanje vrijednosti koja ce biti referentna
        }

        if(isMinimum) //za minimum
        {
            Double finalReferenceValue = Math.abs(referenceValue); //pretvaramo u pozitivnu vrijednost za racunanje
            computedFunctionZ.stream().forEach(fx ->{
                Double ffx = new BigDecimal(finalReferenceValue - fx).round(new MathContext(5)).doubleValue();
                computedFitnesFunction.add(ffx); //𝑓𝑓(𝑥) = max [𝑥𝑖] 𝑓(𝑥) − 𝑓(𝑥)
            });
        }
        else{
          Double finalReferenceValue1 = referenceValue;
          computedFunctionZ.stream().forEach(fx ->{
                Double ffx = new BigDecimal(fx - finalReferenceValue1).round(new MathContext(5)).doubleValue();
                computedFitnesFunction.add(ffx); //𝑓𝑓(𝑥) =  𝑓(𝑥) - min [𝑥𝑖] 𝑓(𝑥)
            });
        }
        return  computedFitnesFunction;
    }

    private static void printingInitialPopulationRating(List<Double> cooridantesX, List<Double> cooridantesY, List<Double> computedFunctionZ, List<Double> fitnessMinComputedFunction, boolean isMinimum) {
        System.out.println();
        if(isMinimum)
            System.out.println("Ocjena populacije za minimum");
        else
            System.out.println("Ocjena populacije za maximum");
        System.out.println("i   x          y          f(x,y)       ff(x,y)" );
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(i[0] + "   " + cooridantesX.get(i[0]) + "    " + cooridantesY.get(i[0]) + "    " + computedFunctionZ.get(i[0])+ "     " + fitnessMinComputedFunction.get(i[0]++));
        });
    }

    private static Double computeRateOfPopulation(List<Double> fitnessMinComputedFunction) {
        return  fitnessMinComputedFunction.stream().mapToDouble(f -> f.doubleValue()).sum();
    }

    //racunanje 𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
    private static List<Double> computeIndividualsProbaibility(List<Double> fitnessMaxComputedFunction, Double rateOfPopulation) {
        List<Double> computedProbaibility = new ArrayList<Double>(fitnessMaxComputedFunction.size());
        fitnessMaxComputedFunction.stream().forEach(ffx ->{
            Double probaibility =  ffx / rateOfPopulation;  //𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
            probaibility = new BigDecimal(probaibility).round(new MathContext(5)).doubleValue();
            computedProbaibility.add(probaibility);
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
            rounded = (double) Math.round(coordinate * 100000.0) / 100000.0;
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
  //      new SwingWrapper(chart).displayChart();

        // Save it
        BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);
    }

    //ruletska selekcija (ispis kao Tabela 5.)
    private static List<Double> turnRoulette(List<Double> randomNumbers, List<Double> individualsCumulativeProbaibilityMax, List<Double> computedFunctionZ, boolean isMin) {
        Double boundary1, boundary2, selectedIndividual;
        int i;
        Boolean condition = false;
        List<Double> selectedIndividuals = new ArrayList<>(randomNumbers.size());

        if(isMin)
            System.out.println("Ispis izabranih jedinki za minimum");
        else
            System.out.println("Ispis izabranih jedinki za maximum");
        for(Double randomNumber : randomNumbers)
        {
            condition = false;
            i = 0;
            while(!condition) { //sve dok se ne pronadje hromozom koji je izabran u ruletu
                
                boundary1 = individualsCumulativeProbaibilityMax.get(i); //donja granica
                boundary2 = individualsCumulativeProbaibilityMax.get(i + 1); //gornja granica

                if ((boundary1 < randomNumber) && (boundary2 > randomNumber)) {
                    selectedIndividual = computedFunctionZ.get(i);
                    System.out.println("Izabran je hromozomm " + i + " [" + selectedIndividual + " ] pri cemu je generisan slucajan broj " + randomNumber);
                    selectedIndividuals.add(selectedIndividual); //dodajemo izabrani hromozom
                    condition = true;
                }
                i++;

                if(i == (SIZE_OF_POPULATION-1)) //ako dodjemo do kraja pite
                {
                    selectedIndividual = computedFunctionZ.get(i);
                    System.out.println("Izabran je hromozomm " + i + " [" + selectedIndividual + " ] pri cemu je generisan slucajan broj " + randomNumber);
                    condition = true;
                    selectedIndividuals.add(computedFunctionZ.get(i)); //dodajemo izabrani hromozom
                }
            }
        }
        return selectedIndividuals;
    }

    private static void printingIndividualsProbaibility(List<Double> individualsSelectionProbaibility, List<Double> individualsCumulativeProbaibility, boolean isMin) {
        if(isMin)
            System.out.println("Ispis vjerovatnoce izbora jedinki za minimum");
        else
            System.out.println("Ispis vjerovatnoce izbora jedinki za maximum");

        int i[] = {0};
        System.out.println("i p[i]      q[i]" );
        individualsSelectionProbaibility.forEach(p -> {
            System.out.println(i[0] + " " + p +  "   " + individualsCumulativeProbaibility.get(i[0]++));
        });
    }

    private static void printingIntergeneration(List<Double> selectedIndividualsMax, boolean isMin) {
    System.out.println();
    if(isMin)
        System.out.println("Ispis međugeneracije za minimum");
    else
        System.out.println("Ispis međugeneracije za maximum");

    int i = 0;
    System.out.println("x           Redni broj u populaciji" );
    for(Double individual : selectedIndividualsMax)
    {
        System.out.println(individual + "      " + i++);
    }}


    private static List<Double> getRecombinationPairs(List<Double> selectedPairs, boolean isMin) {
        Integer index, numberOfPairs = SIZE_OF_POPULATION / 2;
        List<Double> randomNumbers = generateRandomNumbers(numberOfPairs); //slucajni brojevi za rekombinaciju, generisani za svaki par
        List<Double> pairs = new ArrayList<>();
        String str = isMin ? "minimum" : "maximum";

        for(Double randomNumber : randomNumbers)
            if(randomNumber < RECOMBINATION_PROBAIBILITY) //ako važi  𝑟 < 𝑝𝑟 dolazi do rekombinacije
            {
                if(pairs.size() == 0)
                    System.out.println("Ispis parova za rekombinaciju za " + str);
                index = randomNumbers.indexOf(randomNumber); //uzimamo index onog para kod kojeg je zadovoljen uslov rekombinacije (r < pr)
                System.out.println("Rekombinuje se par broj " + (index + 1));
                pairs.add(selectedPairs.get(index)); //dodajemo par kod kojeg dolazi do rekombinacije
                pairs.add(selectedPairs.get(index + 1)); //dodajemo par kod kojeg dolazi do rekombinacije
            }

        if(pairs.size() == 0 )
            System.out.println("Nema parova za rekombinaciju za " + str);
        return pairs;
    }

    private static List<Integer> getMutationPoints(Integer numberOfRecombinationPairs, boolean isMin) {
        List<Double> randomNumbers = generateRandomNumbers(numberOfRecombinationPairs); //generisanje slucajnih brojeva, koliko imamo parova za mutaciju toliko slucajnih brojeva generisemo
        List<Integer> mutationPoints = new ArrayList<Integer>(numberOfRecombinationPairs);
        Integer mutationPoint;
        for(Double randomNumber : randomNumbers)
        {
            mutationPoint = (int) Math.ceil(randomNumber * 10); //tacka se dobija iz formule  t = 10∙𝑟, pri cemu uzimam gornji cio dio, kao i u dosadasnjem dijelu zadatka
            mutationPoints.add(mutationPoint);
        }

        if(mutationPoints.size() > 0) //ako postoji tacka za mutaciju
        {
            String str = isMin ? "minimum" : "maximum";
            System.out.println("Ispis tacaka rekombinacije za " + str);
            mutationPoints.stream().forEach(t -> {
                System.out.print(t + " ");
            });
            System.out.println();
        }
        return mutationPoints;
    }

    private static List<Double> getIndividualForMutation(List<Double> selectedIndividuals, boolean b) {
        List<Double> randomNumbers = generateRandomNumbers(SIZE_OF_POPULATION); //generisanje slucajnih brojeva, koliko imamo jedinki toliko slucajnih brojeva generisemo
        List<Double> mutationInvididuals = new ArrayList<>();
        Integer index;
        for(Double randomNumber : randomNumbers)
            if(randomNumber < MUTATION_PROBAIBILITY)
            {
                index = randomNumbers.indexOf(randomNumber);
                mutationInvididuals.add(selectedIndividuals.get(index)); //uzimamo jedinku sa tog indexa
            }
        return  mutationInvididuals; //vracamo jedinke koje treba mutirati
    }

    public static void main(String[] args) throws IOException {

        //generisanje pocetne populacije (xi, yi)
        List<Double> cooridantesX = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesX); //zaokuruzivanje jedinki na 5 decimale
        List<Double> cooridantesY = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesY); //zaokuruzivanje jedinki na 5 decimale

        //broj bita potrebnih za kodovanje (n)
        Integer sizeOfBitsForCoding = computeSizeOfBitsForCoding();

        //decimalno kodovanje (bd)
        List<Integer> decimalCodedCoordinatesX = codeCoordinatesToDecimal(cooridantesX, sizeOfBitsForCoding);
        List<Integer> decimalCodedCoordinatesY = codeCoordinatesToDecimal(cooridantesY, sizeOfBitsForCoding);

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - pocetna populacija
        List<String> binaryCodedCoordinatesX = codeCoordinatesToBinary(decimalCodedCoordinatesX, sizeOfBitsForCoding);
        List<String> binaryCodedCoordinatesY = codeCoordinatesToBinary(decimalCodedCoordinatesY, sizeOfBitsForCoding);
        printingInitialPopulation(cooridantesX, cooridantesY, decimalCodedCoordinatesX, decimalCodedCoordinatesY, binaryCodedCoordinatesX, binaryCodedCoordinatesY);

        //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije
        List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

        //racunanje ff(x) za minimum funkcije
        List<Double> fitnessMinComputedFunction = computeFitnessFunction(computedFunctionZ, true);
        printingInitialPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMinComputedFunction, true);

        //racunanje ff(x) za maximum funkcije
        List<Double> fitnessMaxComputedFunction = computeFitnessFunction(computedFunctionZ, false);
        printingInitialPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMaxComputedFunction, false);

        //racunanje ocjene cijele pocetne populacije (F)
        System.out.println();
        Double rateOfPopulation = computeRateOfPopulation(fitnessMinComputedFunction);
        System.out.println("Ocjena populacije iznosi: " + rateOfPopulation);

        //racunanje vjerovatnoce izbora jedinke (p)
        List<Double> individualsSelectionProbaibilityMin = computeIndividualsProbaibility(fitnessMinComputedFunction, rateOfPopulation); // za minimum
        List<Double> individualsSelectionProbaibilityMax = computeIndividualsProbaibility(fitnessMaxComputedFunction, rateOfPopulation); // za maksimum

        //racunanje kumulativne vjerovatnoce (q)
        List<Double> individualsCumulativeProbaibilityMin = computeCumulativeProbaibility(individualsSelectionProbaibilityMin); // za minimum
        List<Double> individualsCumulativeProbaibilityMax = computeCumulativeProbaibility(individualsSelectionProbaibilityMax); // za maksimum

        //Tabela 4. Vjerovatnoce izbora hromozoma pocetne populacije
        printingIndividualsProbaibility(individualsSelectionProbaibilityMin, individualsCumulativeProbaibilityMin, true);
        printingIndividualsProbaibility(individualsSelectionProbaibilityMax, individualsCumulativeProbaibilityMax, false);

        //generisanje slucajnih brojeva (ri)
        List<Double> randomNumbers = generateRandomNumbers(SIZE_OF_POPULATION);
        roundCoordinates(randomNumbers);    //zaokuruzivanje slucajnih brojeva na 5 decimale

        List<Double> selectedIndividualsMin  = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin, computedFunctionZ, true); //ispis koji hromozomi su izabrani za min
        List<Double> selectedIndividualsMax  = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax, computedFunctionZ,false); //ispis koji hromozomi su izabrani za max


//        PieChart minChart = new org.knowm.xchart.PieChartBuilder().width(900).height(700).title("Simuliran tocak ruleta (za minimum)").theme(Styler.ChartTheme.GGPlot2).build();
  //      setChartProperties(minChart, individualsSelectionProbaibilityMax);

    //    PieChart maxChart = new org.knowm.xchart.PieChartBuilder().width(900).height(7500).title("Simulirani tocak ruleta (za maximum)").theme(Styler.ChartTheme.GGPlot2).build();
      //  setChartProperties(maxChart, individualsSelectionProbaibilityMax);


        //ispis medjugeneracije (ispis kao Tabela 6.)
        printingIntergeneration(selectedIndividualsMin, true); //međugeneracija za minimum
        printingIntergeneration(selectedIndividualsMax, false); //međugeneracija za minimum

        //parovi se formiraju uzimanjem po redu 2 hromozoma iz tabele (0,1 hromozom cine par, zatim 2,3 i tako dalje)

        //odluka o ukrstanju, dobijanje liste parova za ukrstanje
        List<Double> recombinationPairsMin = getRecombinationPairs(selectedIndividualsMin, true); //redne brojeve parova kod kojih dolazi do rekombinacije (za minimum)
        List<Double> recombinationPairsMax = getRecombinationPairs(selectedIndividualsMax, false); //redne brojeve parova kod kojih dolazi do rekombinacije (za maximum)

        //izbor tacke za rekombinaciju
        Integer numberOfRecombinationPairs = recombinationPairsMin.size() / 2; //broj parova za rekombinaciju
        List<Integer> mutationPointsMin = getMutationPoints(numberOfRecombinationPairs, true); //za minimum (za jedan ili vise parova)

        numberOfRecombinationPairs = recombinationPairsMax.size(); //broj parova za rekombinaciju
        List<Integer> mutationPointsMax = getMutationPoints(numberOfRecombinationPairs, false); //za maximum (za jedan ili vise parova)

        //odluka o mutaciji i izbor tacke za mutaciju
        List<Double> mutationMin = getIndividualForMutation(selectedIndividualsMin, true); //redne brojeve parova kod kojih dolazi do rekombinacije (za minimum)
        List<Double> mutationMax = getIndividualForMutation(selectedIndividualsMax, false); //redne brojeve parova kod kojih dolazi do rekombinacije (za maximum)

        //ispis naredne generacije
    }




}
