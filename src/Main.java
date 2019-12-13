import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {

    //broj iteracija
    private static Integer NUMBER_OF_ITERATIONS = 100;

    //interval
    private static Integer LOWER_BOUND = -3;
    private static Integer HIGHER_BOUND = 3;

    //preciznost
    private static Integer PRECISION_P = 2;

    //broj jedinki
    private static Integer SIZE_OF_POPULATION = 10;

    //vjerovatnoća rekombinacije
    private static Double RECOMBINATION_PROBAIBILITY = 0.00;

    //vjerovatnoća mutacije
    private static Double MUTATION_PROBAIBILITY = 0.00;

    //broj bita za kodovanje
    private static Integer LENGTH_OF_BITS_FOR_CODING;

    //generisanje početne populacije
    private static List<Double> generateInitialPopulation(Integer SIZE_OF_POPULATION) {
        return new Random().doubles(LOWER_BOUND, HIGHER_BOUND).limit(SIZE_OF_POPULATION).boxed().collect(Collectors.toList());
    }

    //generisanje slučajnih brojeva za ruletsku selekciju
    private static List<Double> generateRandomNumbers(Integer SIZE_OF_POPULATION) {
        return new Random().doubles(0, 1).limit(SIZE_OF_POPULATION).boxed().collect(Collectors.toList());
    }

    //broj bita za kodovanje
    private static Integer computeLENGTH_OF_BITS_FOR_CODING() {
        return (int) Math.ceil(log2((HIGHER_BOUND - LOWER_BOUND) * Math.pow(10, PRECISION_P) + 1)); //n = 𝑙𝑜𝑔2[(𝐺𝑔 − 𝐺𝑑)10𝑝 + 1]
    }

    public static double log2(double n) {
        return (Math.log(n) / Math.log(2));
    }

    //kodovanje koordinata x, y u bd
    private static List<Integer> codeCoordinatesToInteger(List<Double> cooridantes) {
        List<Integer> decimalCodedCoordinates = new ArrayList<>(cooridantes.size());
        for (Double coordinate : cooridantes)
            decimalCodedCoordinates.add(codeCoordinateToInteger(coordinate));  // bd = [(𝑥 − 𝐺𝑑 / Gg - Gd) * (2^n - 1)]
        return decimalCodedCoordinates;
    }

    //kodovanje po formuli za svaku koordinatu posebno
    private static Integer codeCoordinateToInteger(Double coordinate) {
        return (int) Math.ceil((coordinate - LOWER_BOUND) / (HIGHER_BOUND - LOWER_BOUND) * (Math.pow(2, LENGTH_OF_BITS_FOR_CODING) - 1)); // bd = [(𝑥 − 𝐺𝑑 / Gg - Gd) * (2^n - 1)]
    }

    //binarno kodovanje koordinata x, y
    private static List<String> codeCoordinatesToBinary(List<Integer> decimalCooridantes) {
        List<String> binaryCodedCoordinates = new ArrayList<>(decimalCooridantes.size());
        String binaryRepresentation;
        for (Integer coordinate : decimalCooridantes) {
            binaryRepresentation = codeToBinary(coordinate);
            binaryCodedCoordinates.add(binaryRepresentation);
        }
        return binaryCodedCoordinates;
    }

    //binarno kodovanje koordinate
    private static String codeToBinary(Integer coordinate) {
        return String.format("%" + LENGTH_OF_BITS_FOR_CODING + "s", Integer.toBinaryString(coordinate)).replaceAll(" ", "0");
    }

    //ispis početne populacije, x[i], y[i] (decimalno, cjelobrojno i binarno)
    private static void printingPopulation(List<Double> cooridantesX, List<Double> cooridantesY, boolean isMin) {
        String str = isMin ? "minimum" : "maximum";
        System.out.println("Ispis naredne generacije za " + str);
        System.out.println("===============================================");

        //decimalno kodovanje (bd)
        List<Integer> decimalCodedCoordinatesX = codeCoordinatesToInteger(cooridantesX);
        List<Integer> decimalCodedCoordinatesY = codeCoordinatesToInteger(cooridantesY);

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - početna populacija
        List<String> binaryCodedCoordinatesX = codeCoordinatesToBinary(decimalCodedCoordinatesX);
        List<String> binaryCodedCoordinatesY = codeCoordinatesToBinary(decimalCodedCoordinatesY);

        System.out.println("i x         DEC   KOD");
        System.out.println("===============================================");
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate + "   " + decimalCodedCoordinatesX.get(i[0]) + "   " + binaryCodedCoordinatesX.get(i[0]++));
        });

        System.out.println("===============================================");
        System.out.println("i y         DEC   KOD");
        System.out.println("===============================================");
        i[0] = 0;
        cooridantesY.forEach(coordinate -> {
            System.out.println(i[0] + " " + coordinate + "   " + decimalCodedCoordinatesY.get(i[0]) + "   " + binaryCodedCoordinatesY.get(i[0]++));
        });
        System.out.println("===============================================");
    }

    //računanje funkcije z = f(x, y)
    private static List<Double> computeFunctionOfCoordinates(List<Double> cooridantesX, List<Double> cooridantesY) {
        List<Double> computedFunctionZ = new ArrayList<>(cooridantesX.size());
        double x, y, z;
        MathContext precision = new MathContext(5);
        for (int i = 0; i < cooridantesX.size(); i++) {
            x = cooridantesX.get(i);
            y = cooridantesY.get(i);
            z = 3 * Math.pow((1 - x), 2) * Math.exp(-Math.pow(x, 2) - Math.pow((y + 1), 2)) - 10 * (x / 5.0 - Math.pow(x, 3) - Math.pow(y, 5)) * Math.exp(-Math.pow(x, 2) - Math.pow(y, 2)) - (1 / 3.0) * Math.exp(-Math.pow(x + 1, 2) - Math.pow(y, 2));
            z = new BigDecimal(z).round(precision).doubleValue();
            computedFunctionZ.add(z);
        }
        return computedFunctionZ;
    }

    //računanje fitnes funkcije
    private static List<Double> computeFitnessFunction(List<Double> computedFunctionZ, boolean isMinimum) {
        List<Double> computedFitnesFunction = new ArrayList<>(computedFunctionZ.size());
        Double referenceValue;
        MathContext precision = new MathContext(5);

        if (isMinimum)  //ako trazimo minimum
        {
            referenceValue = computedFunctionZ
                    .stream()
                    .mapToDouble(z -> z)
                    .max().orElseThrow(NoSuchElementException::new);  //pronalzanje najvece vrijednosti koja ce biti referentna
        } else    //ako trazimo maksimum
        {
            referenceValue = computedFunctionZ
                    .stream()
                    .mapToDouble(z -> z)
                    .min().orElseThrow(NoSuchElementException::new); //pronalzanje najmanje vrijednosti koja ce biti referentna
        }

        if (isMinimum) //za minimum
        {
            Double finalReferenceValue = Math.abs(referenceValue); //pretvaramo u pozitivnu vrijednost za racunanje
            computedFunctionZ.stream().forEach(fx -> {
                Double ffx = new BigDecimal(finalReferenceValue - fx).round(precision).doubleValue();
                computedFitnesFunction.add(ffx); //𝑓𝑓(𝑥) = max [𝑥𝑖] 𝑓(𝑥) − 𝑓(𝑥)
            });
        } else { //za maximum
            Double finalReferenceValue1 = referenceValue;
            computedFunctionZ.stream().forEach(fx -> {
                Double ffx = new BigDecimal(fx - finalReferenceValue1).round(precision).doubleValue();
                computedFitnesFunction.add(ffx); //𝑓𝑓(𝑥) =  𝑓(𝑥) - min [𝑥𝑖] 𝑓(𝑥)
            });
        }
        return computedFitnesFunction;
    }

    //ispis ocjene populacije
    private static void printingPopulationRating(List<Double> cooridantesX, List<Double> cooridantesY, List<Double> computedFunctionZ, List<Double> fitnessMinComputedFunction, boolean isMinimum) {
        if (isMinimum)
            System.out.println("Ocjena populacije za minimum");
        else
            System.out.println("Ocjena populacije za maximum");
        System.out.println("===============================================");
        System.out.println("i   x          y          f(x,y)       ff(x,y)");
        System.out.println("===============================================");
        int[] i = {0};
        cooridantesX.forEach(coordinate -> {
            System.out.println(i[0] + "   " + cooridantesX.get(i[0]) + "    " + cooridantesY.get(i[0]) + "    " + computedFunctionZ.get(i[0]) + "     " + fitnessMinComputedFunction.get(i[0]++));
        });
        System.out.println("===============================================");
    }

    //računanje ocjene populacije
    private static Double computeRateOfPopulation(List<Double> fitnessComputedFunction) {
        Double rate = fitnessComputedFunction.stream().mapToDouble(f -> f.doubleValue()).sum();
        MathContext precision = new MathContext(5);
        rate = new BigDecimal(rate).round(precision).doubleValue();  //zaokruzivanje na 5 decimala
        return rate;
    }

    //računanje 𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
    private static List<Double> computeIndividualsProbaibility(List<Double> fitnessMaxComputedFunction, Double rateOfPopulation) {
        List<Double> computedProbaibility = new ArrayList<Double>(fitnessMaxComputedFunction.size());
        MathContext precision = new MathContext(5);

        fitnessMaxComputedFunction.stream().forEach(ffx -> {
            Double probaibility = ffx / rateOfPopulation;  //𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
            probaibility = new BigDecimal(probaibility).round(precision).doubleValue();
            computedProbaibility.add(probaibility);
        });
        return computedProbaibility;
    }

    //računanje 𝑞[𝑖] = Σ 𝑝[𝑗]
    private static List<Double> computeCumulativeProbaibility(List<Double> computedProbaibility) {
        List<Double> computedCumulativeProbaibility = new ArrayList<Double>(computedProbaibility.size());
        computedCumulativeProbaibility.add(computedProbaibility.get(0));
        int i[] = {0};
        MathContext precision = new MathContext(5);
        computedProbaibility.stream().forEach(pi -> {
            Double qi = computedCumulativeProbaibility.get(i[0]++) + pi;
            qi = new BigDecimal(qi).round(precision).doubleValue();
            computedCumulativeProbaibility.add(qi); //kumulativna vjerovatnoća = donja granica + sirina "parčeta pite"
        });
        return computedCumulativeProbaibility;
    }

    //zaokruživanje koordinata na 5 decimala
    private static void roundCoordinates(List<Double> cooridantes) {
        int i = 0;
        double rounded;
        for (Double coordinate : cooridantes) {
            rounded = (double) Math.round(coordinate * 100000.0) / 100000.0;
            cooridantes.set(i++, rounded);
        }
    }

    //generisanje prikaza Pie chart-a
    private static void setChartProperties(PieChart chart, List<Double> individualsSelectionProbaibilityMax) throws IOException {
        // Customize Chart
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndPercentage);
        chart.getStyler().setAnnotationDistance(1.15);
        chart.getStyler().setPlotContentSize(.7);
        chart.getStyler().setStartAngleInDegrees(90);

        // Series
        int i = 0;
        for (Double individual : individualsSelectionProbaibilityMax)
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

        if (isMin)
            System.out.println("Ispis izabranih jedinki za minimum");
        else
            System.out.println("Ispis izabranih jedinki za maximum");
        System.out.println("===============================================");
        for (Double randomNumber : randomNumbers) {
            condition = false;
            i = 0;
            while (!condition) { //sve dok se ne pronadje hromozom koji je izabran u ruletu

                boundary1 = individualsCumulativeProbaibilityMax.get(i); //donja granica
                boundary2 = individualsCumulativeProbaibilityMax.get(i + 1); //gornja granica

                if ((boundary1 < randomNumber) && (boundary2 > randomNumber)) {
                    selectedIndividual = computedFunctionZ.get(i);
                    System.out.println("Izabran je hromozomm " + i + " [" + selectedIndividual + " ] pri cemu je generisan slucajan broj " + randomNumber);
                    selectedIndividuals.add(selectedIndividual); //dodajemo izabrani hromozom
                    condition = true;
                }
                i++;

                if (i == (SIZE_OF_POPULATION - 1)) //ako dodjemo do kraja pite
                {
                    selectedIndividual = computedFunctionZ.get(i);
                    System.out.println("Izabran je hromozomm " + i + " [" + selectedIndividual + " ] pri cemu je generisan slucajan broj " + randomNumber);
                    condition = true;
                    selectedIndividuals.add(computedFunctionZ.get(i)); //dodajemo izabrani hromozom
                }
            }
        }
        System.out.println("===============================================");
        return selectedIndividuals;
    }

    //ispis vjerovatnoće izbora jedinki
    private static void printingIndividualsProbaibility(List<Double> individualsSelectionProbaibility, List<Double> individualsCumulativeProbaibility, boolean isMin) {
        if (isMin)
            System.out.println("Ispis vjerovatnoce izbora jedinki za minimum");
        else
            System.out.println("Ispis vjerovatnoce izbora jedinki za maximum");

        int i[] = {0};
        System.out.println("===============================================");
        System.out.println("i p[i]      q[i]");
        System.out.println("===============================================");
        individualsSelectionProbaibility.forEach(p -> {
            System.out.println(i[0] + " " + p + "   " + individualsCumulativeProbaibility.get(i[0]++));
        });
        System.out.println("===============================================");
    }

    //ispis međugeneracije
    private static void printingIntergeneration(List<Double> selectedIndividualsXMin, List<Double> selectedIndividualsXMax, boolean isMin) {
        if (isMin)
            System.out.println("Ispis međugeneracije za minimum");
        else
            System.out.println("Ispis međugeneracije za maximum");

        System.out.println("===============================================");
        int i = 0;
        System.out.println("x           Redni broj u populaciji");
        System.out.println("===============================================");
        for (Double individual : selectedIndividualsXMax) {
            System.out.println(individual + "      " + i++);
        }
        System.out.println("===============================================");
    }

    //rekombinacija parova
    private static List<Double> getRecombinationPairs(List<Double> selectedIndividuals, List<Double> randomNumbersForRecombination, boolean isMin, boolean isXCoordinate) {
        Integer index;
        List<Double> pairs = new ArrayList<>();
        String str = isMin ? "minimum" : "maximum";
        String coordinate = isXCoordinate ? "X" : "Y";
        System.out.println("Ispis parova za rekombinaciju za " + str + " za " + coordinate + " koordinatu.");
        System.out.println("===============================================");

        for (Double randomNumber : randomNumbersForRecombination)
            if (randomNumber < RECOMBINATION_PROBAIBILITY) //ako važi  𝑟 < 𝑝𝑟 dolazi do rekombinacije
            {
                index = randomNumbersForRecombination.indexOf(randomNumber); //uzimamo index onog para kod kojeg je zadovoljen uslov rekombinacije (r < pr)
                System.out.println("Rekombinuje se par broj " + (index + 1));

                //izbor tacke za rekombinaciju
                Integer mutationPoint = getPoint(); //za minimum (za jedan ili vise parova)

                // rekombinacija
                List<Double> recombinedPair = recombinePair(selectedIndividuals.get(index), selectedIndividuals.get(index + 1), mutationPoint);
                pairs.add(recombinedPair.get(0)); //dodajemo prvu rekombinovanu jedinku iz para
                pairs.add(recombinedPair.get(1)); //dodajemo drugu rekombinovanu jedinku iz para
            } else {
                index = randomNumbersForRecombination.indexOf(randomNumber);
                pairs.add(selectedIndividuals.get(index)); //dodajemo par kod kojeg ne dolazi do rekombinacije
                pairs.add(selectedIndividuals.get(index + 1)); //dodajemo par kod kojeg ne dolazi do rekombinacije
                System.out.println("Par " + (index + 1) + " prolazi bez ukrštanja");
            }
        System.out.println("===============================================");
        return pairs;
    }

    //pomoćna funkcija koja poziva funkciju doRecombination u kojoj se vrši rekombinacija
    private static List<Double> recombinePair(Double firstIndividual, Double secondIndividual, Integer mutationPoint) {
        List<Double> recombinedPair = new ArrayList<>();
        recombinedPair.add(firstIndividual);
        recombinedPair.add(secondIndividual);
        List<Integer> decimalCodedPair = codeCoordinatesToInteger(recombinedPair);
        List<String> binaryCodedPair = codeCoordinatesToBinary(decimalCodedPair);
        binaryCodedPair = doRecombination(binaryCodedPair, mutationPoint);
        decimalCodedPair = binaryToInteger(binaryCodedPair);
        recombinedPair = integerToDouble(decimalCodedPair);
        return recombinedPair;
    }

    //konvertovanje cjelobrojne vrijednost u decimalnu vrijednost uz pomoć naredne funkcije
    private static List<Double> integerToDouble(List<Integer> decimalCodedPair) {
        List<Double> doubleCodedPair = new ArrayList<>();
        for (Integer decimal : decimalCodedPair) {
            doubleCodedPair.add(convertToDouble(decimal));
        }
        return doubleCodedPair;
    }

    //računanje x = 𝐺𝑑 + (𝐺𝑔 −𝐺𝑑) / (2𝑛−1) ∙𝑏𝑑
    private static Double convertToDouble(Integer decimal) {
        Double x = LOWER_BOUND + (HIGHER_BOUND - LOWER_BOUND) / (Math.pow(2, LENGTH_OF_BITS_FOR_CODING) - 1) * decimal;
        MathContext precision = new MathContext(5);
        x = new BigDecimal(x).round(precision).doubleValue();
        return x;
    }

    //iz binarne u cjeloborojnu vrijednost
    private static List<Integer> binaryToInteger(List<String> binaryCodedPair) {
        List<Integer> decimalCodedPair = new ArrayList<>();
        binaryCodedPair.stream().forEach(i -> {
            Integer decimal = Integer.parseInt(i, 2);
            decimalCodedPair.add(decimal);
        });
        return decimalCodedPair;
    }

    //izvršavanje rekombinacije
    private static List<String> doRecombination(List<String> binaryCodedPair, Integer mutationPoint) {
        String pom = binaryCodedPair.get(0); //cuvamo prvi string da ga mozemo iskoristi za rekombinaciju sa drugim stringom
        List<String> recombinedPair = new ArrayList<String>(2);
        String firstIndividual = changeIndividual(binaryCodedPair.get(0), binaryCodedPair.get(1), mutationPoint);
        String secondIndividual = changeIndividual(binaryCodedPair.get(1), pom, mutationPoint);
        recombinedPair.add(firstIndividual);
        recombinedPair.add(secondIndividual);
        return recombinedPair;
    }

    //promjena bita jedinki
    private static String changeIndividual(String s, String s1, Integer recombinationPoint) {
        StringBuilder str = new StringBuilder(s);
        Integer lowerBoundary = s.length() - recombinationPoint - 1;
        if (lowerBoundary < 0)
            lowerBoundary = 0;
        Integer higherBoundary;

        if (s.length() > s1.length()) //u slucaju da je u pitanju negativan broj on ce imati vise bita pa ce iskakati iz opsega
            higherBoundary = s1.length();
        else
            higherBoundary = s.length();
        str.replace(lowerBoundary, higherBoundary, s1.substring(lowerBoundary, higherBoundary)); //izlazi iz granica duzine stringa
        return str.toString();
    }

    //dobijanje tačke mutacije
    private static Integer getPoint() {
        Double randomNumber = new Random().doubles(0, 1).limit(1).boxed().collect(Collectors.toList()).get(0);
        Integer point = (int) Math.ceil(randomNumber * 10);
        return point;
    }

    //pomoćna funkcija za mutaciju individue, ako je zadovoljen uslov mutacije
    private static List<Double> mutateIndividuals(List<Double> selectedIndividuals, List<Double> randomNumbers, boolean b) {
        List<Double> mutatedInvididuals = new ArrayList<>();
        Double mutatedIndvidual;
        Integer mutationPoint, i = 0;
        for (Double randomNumber : randomNumbers) {
            if (randomNumber < MUTATION_PROBAIBILITY) {
                mutationPoint = getPoint(); //dobijanje tacke gdje se vrsi mutacija
                Integer decimalCodedIndividual = codeCoordinateToInteger(selectedIndividuals.get(i)); //kodiranje invdividue u cjelobrojnu vrijednost
                String binaryCodedIndividual = codeToBinary(decimalCodedIndividual); //kodiranje invdividue u binarnu vrijednost
                binaryCodedIndividual = doMutation(binaryCodedIndividual, mutationPoint); //mutacija jedinke u odredjenoj tacki
                decimalCodedIndividual = Integer.parseInt(binaryCodedIndividual, 2); //vracanje u cjelobrojnu vrijednost
                mutatedIndvidual = convertToDouble(decimalCodedIndividual); //vracanje u decimalni zapis
                mutatedInvididuals.add(mutatedIndvidual);   //dodavanje jedinke koja je mutirana
            } else {
                mutatedInvididuals.add(selectedIndividuals.get(i)); //dodavanje jedinke koja nije mutirana
            }
            i++;
        }
        return mutatedInvididuals; //vraćamo mutirane jedinke
    }

    //mutacija individue
    private static String doMutation(String binaryCodedStr, Integer mutationPoint) {
        StringBuilder str = new StringBuilder(binaryCodedStr);
        mutationPoint = binaryCodedStr.length() - mutationPoint - 1; //dobijamo mjesto gdje trebamo odraditi mutaciju
        if (mutationPoint < 0) //ako se generiše broj koji je veći od dužine broja bita dobijenih brojeva, npr. generiše se broj 8, a duzina bita brojeva je 8
            mutationPoint = 0;
        char bit = str.charAt(mutationPoint) == '0' ? '1' : '0';
        str.setCharAt(mutationPoint, bit); //promjena bita na mjestu, mutiranje hromozoma na prethodno generisanoj poziciji (mutation point)
        return str.toString();
    }

    public static void main(String[] args) throws IOException {
        //promjenljive za minimum
        List<Double> mutatedIndividualsXMin = null;
        List<Double> mutatedIndividualsYMin = null;
        List<Double> fitnessMinComputedFunction = null;
        List<Double> individualsSelectionProbaibilityMin;
        List<Double> individualsCumulativeProbaibilityMin;
        List<Double> selectedIndividualsXMin;
        List<Double> selectedIndividualsYMin;
        List<Double> recombinationPairsXMin;
        List<Double> recombinationPairsYMin;

        //promjenljive za maximum
        List<Double> mutatedIndividualsXMax = null;
        List<Double> mutatedIndividualsYMax = null;
        List<Double> fitnessMaxComputedFunction = null;
        List<Double> individualsSelectionProbaibilityMax;
        List<Double> individualsCumulativeProbaibilityMax;
        List<Double> selectedIndividualsXMax;
        List<Double> selectedIndividualsYMax;
        List<Double> recombinationPairsXMax;
        List<Double> recombinationPairsYMax;

        //slučajni brojevi (za rekombinaciju i mutaciju, respektivno)
        List<Double> randomNumbersForRecombination;
        List<Double> randomNumbersForMutation;

        Boolean isMin = true; //ako je false onda racunamo maksimum, u suprotnom minimum
        Integer numberOfPairs = SIZE_OF_POPULATION / 2;

        //generisanje početne populacije (xi, yi)
        List<Double> cooridantesX = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesX); //zaokuruživanje jedinki na 5 decimala
        List<Double> cooridantesY = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesY); //zaokuruživanje jedinki na 5 decimala

        //broj bita potrebnih za kodovanje (n)
        LENGTH_OF_BITS_FOR_CODING = computeLENGTH_OF_BITS_FOR_CODING();

        System.out.println("===============================================");
        System.out.println("INTERVAL: [" + LOWER_BOUND + "," + HIGHER_BOUND + "]");
        System.out.println("VELIČINA POPULACIJE: " + SIZE_OF_POPULATION);
        System.out.println("VJEROVATNOĆA REKOMBINACIJE: " + RECOMBINATION_PROBAIBILITY);
        System.out.println("VJEROVATNOĆA MUTACIJE: " + MUTATION_PROBAIBILITY);
        System.out.println("BROJ BITA POTREBNIH ZA KODOVANJE: " + LENGTH_OF_BITS_FOR_CODING);
        System.out.println("===============================================");

        printingPopulation(cooridantesX, cooridantesY, isMin);


        if (isMin) {
            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                System.out.println("ITERACIJA BROJ " + (i + 1));
                //računanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena početne populacije
                List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

                //računanje ff(x) za minimum funkcije
                fitnessMinComputedFunction = computeFitnessFunction(computedFunctionZ, true);
                printingPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMinComputedFunction, true);

                //računanje ocjene cijele pocetne populacije (F)
                Double rateOfPopulation = computeRateOfPopulation(fitnessMinComputedFunction);
                System.out.println("Ocjena populacije za minimum iznosi: " + rateOfPopulation);
                System.out.println("===============================================");

                //računanje vjerovatnoce izbora jedinke (p)
                individualsSelectionProbaibilityMin = computeIndividualsProbaibility(fitnessMinComputedFunction, rateOfPopulation); // za minimum

                //računanje kumulativne vjerovatnoce (q)
                individualsCumulativeProbaibilityMin = computeCumulativeProbaibility(individualsSelectionProbaibilityMin); // za minimum

                //Tabela 4. Vjerovatnoće izbora hromozoma početne populacije
                printingIndividualsProbaibility(individualsSelectionProbaibilityMin, individualsCumulativeProbaibilityMin, true);

                //generisanje slučajnih brojeva (ri)
                List<Double> randomNumbers = generateRandomNumbers(SIZE_OF_POPULATION);
                roundCoordinates(randomNumbers);    //zaokuruživanje slučajnih brojeva na 5 decimale

                selectedIndividualsXMin = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin, cooridantesX, true); //ispis koji hromozomi su izabrani za min
                selectedIndividualsYMin = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin, cooridantesY, true); //ispis koji hromozomi su izabrani za min

                PieChart minChart = new org.knowm.xchart.PieChartBuilder().width(900).height(700).title("Simuliran tocak ruleta (za minimum)").theme(Styler.ChartTheme.GGPlot2).build();
                setChartProperties(minChart, individualsSelectionProbaibilityMin);

                //ispis međugeneracije (ispis kao Tabela 6.)
                printingPopulation(selectedIndividualsXMin, selectedIndividualsYMin, true); //međugeneracija za minimum

                //parovi se formiraju uzimanjem po redu 2 hromozoma iz tabele (1,2 hromozom cine par, zatim 3,4 i tako dalje)

                //odluka o ukrštanju, dobijanje liste parova za ukrštanje
                randomNumbersForRecombination = generateRandomNumbers(numberOfPairs); //slučajni brojevi za rekombinaciju, generisani za svaki par
                recombinationPairsXMin = getRecombinationPairs(selectedIndividualsXMin, randomNumbersForRecombination, true, true); //izbor parova za rekombinaciju i rekombinacija unutar metoda(za minimum)
                recombinationPairsYMin = getRecombinationPairs(selectedIndividualsYMin, randomNumbersForRecombination, true, false); //izbor parova za rekombinaciju i rekombinacija unutar metoda(za minimum)

                //odluka o mutaciji i izbor tačke za mutaciju
                randomNumbersForMutation = generateRandomNumbers(SIZE_OF_POPULATION); //generisanje slučajnih brojeva, koliko imamo jedinki toliko slučajnih brojeva generišemo
                mutatedIndividualsXMin = mutateIndividuals(recombinationPairsXMin, randomNumbersForMutation, true); //mutiranje individua kod kojih je ispunjen uslov mutacije (za minimum)
                mutatedIndividualsYMin = mutateIndividuals(recombinationPairsYMin, randomNumbersForMutation, true); //mutiranje individua kod kojih je ispunjen uslov mutacije (za minimum)

                //ispis naredne generacije
                printingPopulation(mutatedIndividualsXMin, mutatedIndividualsYMin, true); //ispis naredne generacije (za minimum)

                cooridantesX = mutatedIndividualsXMin;
                cooridantesY = mutatedIndividualsYMin;
            }
        } else {
            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                System.out.println("ITERACIJA BROJ " + (i + 1));
                //računanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena početne populacije
                List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

                //računanje ff(x) za maximum funkcije
                fitnessMaxComputedFunction = computeFitnessFunction(computedFunctionZ, false);
                printingPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMaxComputedFunction, false);

                //računanje ocjene cijele početne populacije (F)
                Double rateOfPopulation = computeRateOfPopulation(fitnessMaxComputedFunction);
                System.out.println("Ocjena populacije za maximum iznosi: " + rateOfPopulation);
                System.out.println("===============================================");

                //racunanje vjerovatnoće izbora jedinke (p)
                individualsSelectionProbaibilityMax = computeIndividualsProbaibility(fitnessMaxComputedFunction, rateOfPopulation); // za maksimum

                //racunanje kumulativne vjerovatnoće (q)
                individualsCumulativeProbaibilityMax = computeCumulativeProbaibility(individualsSelectionProbaibilityMax); // za maksimum

                //Tabela 4. Vjerovatnće izbora hromozoma početne populacije
                printingIndividualsProbaibility(individualsSelectionProbaibilityMax, individualsCumulativeProbaibilityMax, false);

                //generisanje slučajnih brojeva (ri)
                List<Double> randomNumbers = generateRandomNumbers(SIZE_OF_POPULATION);
                roundCoordinates(randomNumbers);    //zaokuruživanje slučajnih brojeva na 5 decimala

                selectedIndividualsXMax = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax, cooridantesX, false); //ispis koji hromozomi su izabrani za max
                selectedIndividualsYMax = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax, cooridantesY, false); //ispis koji hromozomi su izabrani za max

                PieChart maxChart = new org.knowm.xchart.PieChartBuilder().width(900).height(700).title("Simuliran tocak ruleta (za minimum)").theme(Styler.ChartTheme.GGPlot2).build();
                setChartProperties(maxChart, individualsSelectionProbaibilityMax);

                //ispis međugeneracije (ispis kao Tabela 6.)
                printingPopulation(selectedIndividualsXMax, selectedIndividualsYMax, false); //međugeneracija za maximum

                //parovi se formiraju uzimanjem po redu 2 hromozoma iz tabele (1,2 hromozom čine par, zatim 3,4 i tako dalje)

                //odluka o ukrštanju, dobijanje liste parova za ukrštanje
                randomNumbersForRecombination = generateRandomNumbers(numberOfPairs); //slučajni brojevi za rekombinaciju, generisani za svaki par
                recombinationPairsXMax = getRecombinationPairs(selectedIndividualsXMax, randomNumbersForRecombination, false, true); //izbor parova za rekombinaciju i rekombinacija unutar metoda (za maximum)
                recombinationPairsYMax = getRecombinationPairs(selectedIndividualsYMax, randomNumbersForRecombination, false, false); //izbor parova za rekombinaciju i rekombinacija unutar metoda (za maximum)

                //odluka o mutaciji i izbor tačke za mutaciju
                randomNumbersForMutation = generateRandomNumbers(SIZE_OF_POPULATION); //generisanje slučajnih brojeva, koliko imamo jedinki toliko slučajnih brojeva generisemo
                mutatedIndividualsXMax = mutateIndividuals(recombinationPairsXMax, randomNumbersForMutation, false); //mutiranje individua kod kojih je ispunjen uslov mutacije (za maximum)
                mutatedIndividualsYMax = mutateIndividuals(recombinationPairsYMax, randomNumbersForMutation, false); //mutiranje individua kod kojih je ispunjen uslov mutacije (za maximum)

                //ispis naredne generacije
                printingPopulation(mutatedIndividualsXMax, mutatedIndividualsYMax, false); //ispis naredne generacije (za maximum)

                cooridantesX = mutatedIndividualsXMax;
                cooridantesY = mutatedIndividualsYMax;
            }
        }
        List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);
        //ispis populacije nakon svih iteracija
        printingPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, isMin ? fitnessMinComputedFunction : fitnessMaxComputedFunction, false);
    }
}
