import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.style.PieStyler;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
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
    private static Integer SIZE_OF_POPULATION = 50;

    //vjerovatnoca rekombinacije
    private static Double RECOMBINATION_PROBAIBILITY = 0.1;

    //vjerovatnoca mutacije
    private static Double MUTATION_PROBAIBILITY = 0.08;

    //broj bita za kodovanje
    private static Integer LENGTH_OF_BITS_FOR_CODING;

    //generisanje pocetne populacije
    private static List<Double> generateInitialPopulation(Integer SIZE_OF_POPULATION) {
        return new Random().doubles(LOWER_BOUND, HIGHER_BOUND).limit(SIZE_OF_POPULATION).boxed().collect(Collectors.toList());
    }

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

    //decimalno kodovanje koordinata x, y u bd
    private static List<Integer> codeCoordinatesToDecimal(List<Double> cooridantes) {
        List<Integer> decimalCodedCoordinates = new ArrayList<>(cooridantes.size());
        for (Double coordinate : cooridantes)
            decimalCodedCoordinates.add(codeCoordinateToDecimal(coordinate));  // bd = [(𝑥 − 𝐺𝑑 / Gg - Gd) * (2^n - 1)]
        return decimalCodedCoordinates;
    }

    private static Integer codeCoordinateToDecimal(Double coordinate) {
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

    private static String codeToBinary(Integer coordinate) {
        return String.format("%" + LENGTH_OF_BITS_FOR_CODING + "s", Integer.toBinaryString(coordinate)).replaceAll(" ", "0");
    }

    //ispis pocetne populacije, x[i], y[i], decimalno i binarno
    private static void printingInitialPopulation(List<Double> cooridantesX, List<Double> cooridantesY, List<Integer> decimalCodedCoordinatesX, List<Integer> decimalCodedCoordinatesY, List<String> binaryCodedCoordinatesX, List<String> binaryCodedCoordinatesY) {
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

    //racunanje funkcije z = f(x, y)
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

    private static List<Double> computeFitnessFunction(List<Double> computedFunctionZ, boolean isMinimum) {
        List<Double> computedFitnesFunction = new ArrayList<Double>(computedFunctionZ.size());
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
        } else {
            Double finalReferenceValue1 = referenceValue;
            computedFunctionZ.stream().forEach(fx -> {
                Double ffx = new BigDecimal(fx - finalReferenceValue1).round(precision).doubleValue();
                computedFitnesFunction.add(ffx); //𝑓𝑓(𝑥) =  𝑓(𝑥) - min [𝑥𝑖] 𝑓(𝑥)
            });
        }
        return computedFitnesFunction;
    }

    private static void printingInitialPopulationRating(List<Double> cooridantesX, List<Double> cooridantesY, List<Double> computedFunctionZ, List<Double> fitnessMinComputedFunction, boolean isMinimum) {
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

    private static Double computeRateOfPopulation(List<Double> fitnessComputedFunction) {
        Double rate = fitnessComputedFunction.stream().mapToDouble(f -> f.doubleValue()).sum();
        ;
        MathContext precision = new MathContext(5);
        rate = new BigDecimal(rate).round(precision).doubleValue();  //zaokruzivanje na 5 decimala
        return rate;
    }

    //racunanje 𝑝[𝑖] = 𝑓𝑓(𝑥[𝑖]) / 𝐹
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

    //racunanje 𝑞[𝑖] = Σ 𝑝[𝑗]
    private static List<Double> computeCumulativeProbaibility(List<Double> computedProbaibility) {
        List<Double> computedCumulativeProbaibility = new ArrayList<Double>(computedProbaibility.size());
        computedCumulativeProbaibility.add(computedProbaibility.get(0));
        int i[] = {0};
        MathContext precision = new MathContext(5);
        computedProbaibility.stream().forEach(pi -> {
            Double qi = computedCumulativeProbaibility.get(i[0]++) + pi;
            qi = new BigDecimal(qi).round(precision).doubleValue();
            computedCumulativeProbaibility.add(qi); //kumulativna vjerovatnoca = donja granica + sirina "parceta pite"
        });
        return computedCumulativeProbaibility;
    }

    private static void roundCoordinates(List<Double> cooridantes) {
        int i = 0;
        double rounded;
        for (Double coordinate : cooridantes) {
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


    private static List<Double> getRecombinationPairs(List<Double> selectedIndividuals, boolean isMin, boolean isXCoordinate) {
        Integer index, numberOfPairs = SIZE_OF_POPULATION / 2;
        List<Double> randomNumbers = generateRandomNumbers(numberOfPairs); //slucajni brojevi za rekombinaciju, generisani za svaki par
        List<Double> pairs = new ArrayList<>();
        String str = isMin ? "minimum" : "maximum";
        String coordinate = isXCoordinate ? "X" : "Y";
        System.out.println("Ispis parova za rekombinaciju za " + str + " za " + coordinate + " koordinatu.");
        System.out.println("===============================================");

        for (Double randomNumber : randomNumbers)
            if (randomNumber < RECOMBINATION_PROBAIBILITY) //ako važi  𝑟 < 𝑝𝑟 dolazi do rekombinacije
            {
                index = randomNumbers.indexOf(randomNumber); //uzimamo index onog para kod kojeg je zadovoljen uslov rekombinacije (r < pr)
                System.out.println("Rekombinuje se par broj " + (index + 1));

                //izbor tacke za rekombinaciju
                Integer mutationPoint = getPoint(); //za minimum (za jedan ili vise parova)

                // rekombinacija
                List<Double> recombinedPair = recombinePair(selectedIndividuals.get(index), selectedIndividuals.get(index + 1), mutationPoint);
                pairs.add(recombinedPair.get(0)); //dodajemo prvu rekombinovanu jedinku iz para
                pairs.add(recombinedPair.get(1)); //dodajemo drugu rekombinovanu jedinku iz para
            } else {
                index = randomNumbers.indexOf(randomNumber);
                pairs.add(selectedIndividuals.get(index)); //dodajemo par kod kojeg ne dolazi do rekombinacije
                pairs.add(selectedIndividuals.get(index + 1)); //dodajemo par kod kojeg ne dolazi do rekombinacije
                System.out.println("Par " + (index + 1) + " prolazi bez ukrštanja");
            }
        System.out.println("===============================================");
        return pairs;
    }

    private static List<Double> recombinePair(Double firstIndividual, Double secondIndividual, Integer mutationPoint) {
        List<Double> recombinedPair = new ArrayList<>();
        recombinedPair.add(firstIndividual);
        recombinedPair.add(secondIndividual);
        List<Integer> decimalCodedPair = codeCoordinatesToDecimal(recombinedPair);
        List<String> binaryCodedPair = codeCoordinatesToBinary(decimalCodedPair);
        binaryCodedPair = doRecombination(binaryCodedPair, mutationPoint);
        decimalCodedPair = binaryToDecimal(binaryCodedPair);
        recombinedPair = decimalToDouble(decimalCodedPair);
        return recombinedPair;
    }

    private static List<Double> decimalToDouble(List<Integer> decimalCodedPair) {
        List<Double> doubleCodedPair = new ArrayList<>();
        for (Integer decimal : decimalCodedPair) {
            doubleCodedPair.add(convertToDouble(decimal));
        }
        return doubleCodedPair;
    }

    private static Double convertToDouble(Integer decimal) {
        Double x = HIGHER_BOUND + (HIGHER_BOUND - LOWER_BOUND) / (Math.pow(2, LENGTH_OF_BITS_FOR_CODING) - 1) * decimal;     //𝑥 = 𝐺𝑑 + (𝐺𝑔 −𝐺𝑑) / (2𝑛−1) ∙𝑏𝑑
        MathContext precision = new MathContext(5);
        x = new BigDecimal(x).round(precision).doubleValue();
        return x;
    }

    private static List<Integer> binaryToDecimal(List<String> binaryCodedPair) {
        List<Integer> decimalCodedPair = new ArrayList<>();
        for (String binaryCode : binaryCodedPair) {

        }
        binaryCodedPair.stream().forEach(i -> {
            Integer decimal = (int) Long.parseLong(i, 2);           /* EXCEPTION BACA */
            decimalCodedPair.add(decimal);
        });
        return decimalCodedPair;
    }

    private static List<String> doRecombination(List<String> binaryCodedPair, Integer mutationPoint) {
        String pom = binaryCodedPair.get(0); //cuvamo prvi string da ga mozemo iskoristi za rekombinaciju sa drugim stringom
        List<String> recombinedPair = new ArrayList<String>(2);
        String firstIndividual = changeIndividual(binaryCodedPair.get(0), binaryCodedPair.get(1), mutationPoint);
        String secondIndividual = changeIndividual(binaryCodedPair.get(1), pom, mutationPoint);
        recombinedPair.add(firstIndividual);
        recombinedPair.add(secondIndividual);
        return recombinedPair;
    }

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

    private static Integer getPoint() {
        Double randomNumber = new Random().doubles(0, 1).limit(1).boxed().collect(Collectors.toList()).get(0);
        Integer point = (int) Math.ceil(randomNumber * 10);
        return point;
    }

    private static List<Double> mutateIndividuals(List<Double> selectedIndividuals, boolean b) {
        List<Double> randomNumbers = generateRandomNumbers(SIZE_OF_POPULATION); //generisanje slucajnih brojeva, koliko imamo jedinki toliko slucajnih brojeva generisemo
        List<Double> mutatedInvididuals = new ArrayList<>();
        Double mutatedIndvidual;
        Integer index, mutationPoint, i = 0;
        for (Double randomNumber : randomNumbers) {
            if (randomNumber < MUTATION_PROBAIBILITY) {
                mutationPoint = getPoint(); //dobijanje tacke gdje se vrsi mutacija
                Integer decimalCodedIndividual = codeCoordinateToDecimal(selectedIndividuals.get(i)); //kodiranje invdividue u decimalnu vrijednost
                String binaryCodedIndividual = codeToBinary(decimalCodedIndividual); //kodiranje invdividue u binarnu vrijednost
                binaryCodedIndividual = doMutation(binaryCodedIndividual, mutationPoint); //mutacija jedinke u odredjenoj tacki
                decimalCodedIndividual = (int) Long.parseLong(binaryCodedIndividual, 2); //vracanje u decimalnu vrijednost
                mutatedIndvidual = convertToDouble(decimalCodedIndividual); //vracanje u mutiranu vrijednost (konvertovanu u Double)
                mutatedInvididuals.add(mutatedIndvidual);   //dodavanje jedinke kod koje je ispunjen uslov mutacije
            } else {
                mutatedInvididuals.add(selectedIndividuals.get(i)); //dodavanje jedinke kod koje nije ispunjen uslov mutacije
            }
            i++;
        }
        return mutatedInvididuals; //vracamo mutirane jedinke
    }

    private static String doMutation(String binaryCodedStr, Integer mutationPoint) {
        StringBuilder str = new StringBuilder(binaryCodedStr);
        mutationPoint = binaryCodedStr.length() - mutationPoint - 1; //dobijamo mjesto gdje trebamo vrsiti mutaciju
        if (mutationPoint < 0) //ako se generise broj koji je veci od duzine broja bita dobijenih brojeva, npr. generise se broj 8, a duzina bita brojeva je 8
            mutationPoint = 0;
        char bit = str.charAt(mutationPoint) == '0' ? '1' : '0';
        str.setCharAt(mutationPoint, bit); //promjena bita na mjestu, mutiranje hromozoma na prethodno generisanoj poziciji (mutation point)
        return str.toString();
    }

    private static void printingGeneration(List<Double> mutatedIndividualsX, List<Double> mutatedIndividualsY, boolean isMin, boolean isNextGeneration) {
        String str = isMin ? "minimum" : "maximum";
        String generation = isNextGeneration ? "naredne" : "među";
        System.out.println("Ispis " + generation + " generacije za " + str);
        System.out.println("===============================================");
        System.out.println("i x");
        System.out.println("===============================================");
        int[] i = {0};
        mutatedIndividualsX.forEach(coordinate -> {
            System.out.println(i[0]++ + " " + coordinate);
        });

        System.out.println("===============================================");
        System.out.println("i y");
        System.out.println("===============================================");
        i[0] = 0;
        mutatedIndividualsY.forEach(coordinate -> {
            System.out.println(i[0]++ + " " + coordinate);
        });
        System.out.println("===============================================");
    }

    public static void main(String[] args) throws IOException {

        //generisanje pocetne populacije (xi, yi)
        List<Double> cooridantesX = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesX); //zaokuruzivanje jedinki na 5 decimale
        List<Double> cooridantesY = generateInitialPopulation(SIZE_OF_POPULATION);
        roundCoordinates(cooridantesY); //zaokuruzivanje jedinki na 5 decimale

        //broj bita potrebnih za kodovanje (n)
        LENGTH_OF_BITS_FOR_CODING = computeLENGTH_OF_BITS_FOR_CODING();

        System.out.println("===============================================");
        System.out.println("INTERVAL: [" + LOWER_BOUND + "," + HIGHER_BOUND + "]");
        System.out.println("VELIČINA POPULACIJE: " + SIZE_OF_POPULATION);
        System.out.println("VJEROVATNOĆA REKOMBINACIJE: " + RECOMBINATION_PROBAIBILITY);
        System.out.println("VJEROVATNOĆA MUTACIJE: " + MUTATION_PROBAIBILITY);
        System.out.println("BROJ BITA POTREBNIH ZA KODOVANJE: " + LENGTH_OF_BITS_FOR_CODING);
        System.out.println("===============================================");
        //decimalno kodovanje (bd)
        List<Integer> decimalCodedCoordinatesX = codeCoordinatesToDecimal(cooridantesX);
        List<Integer> decimalCodedCoordinatesY = codeCoordinatesToDecimal(cooridantesY);

        //pretvaranje u binarni kod i ispis (Tabela 2. u PDF-u) - pocetna populacija
        List<String> binaryCodedCoordinatesX = codeCoordinatesToBinary(decimalCodedCoordinatesX);
        List<String> binaryCodedCoordinatesY = codeCoordinatesToBinary(decimalCodedCoordinatesY);
        printingInitialPopulation(cooridantesX, cooridantesY, decimalCodedCoordinatesX, decimalCodedCoordinatesY, binaryCodedCoordinatesX, binaryCodedCoordinatesY);


        for(int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            System.out.println("ITERACIJA BROJ " + (i + 1));
            //racunanje f(x), ff(x) i ispis (Tabela 3. u PDF-u) - ocjena pocetne populacije
            List<Double> computedFunctionZ = computeFunctionOfCoordinates(cooridantesX, cooridantesY);

            //racunanje ff(x) za minimum funkcije
            List<Double> fitnessMinComputedFunction = computeFitnessFunction(computedFunctionZ, true);
            printingInitialPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMinComputedFunction, true);

            //racunanje ff(x) za maximum funkcije
            List<Double> fitnessMaxComputedFunction = computeFitnessFunction(computedFunctionZ, false);
            printingInitialPopulationRating(cooridantesX, cooridantesY, computedFunctionZ, fitnessMaxComputedFunction, false);

            //racunanje ocjene cijele pocetne populacije (F)
            Double rateOfPopulation = computeRateOfPopulation(fitnessMinComputedFunction);
            System.out.println("Ocjena populacije za minimum iznosi: " + rateOfPopulation);
            rateOfPopulation = computeRateOfPopulation(fitnessMaxComputedFunction);
            System.out.println("Ocjena populacije za maximum iznosi: " + rateOfPopulation);
            System.out.println("===============================================");

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

            List<Double> selectedIndividualsXMin = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin, cooridantesX, true); //ispis koji hromozomi su izabrani za min
            List<Double> selectedIndividualsYMin = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMin, cooridantesY, true); //ispis koji hromozomi su izabrani za min

            List<Double> selectedIndividualsXMax = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax, cooridantesX, false); //ispis koji hromozomi su izabrani za max
            List<Double> selectedIndividualsYMax = turnRoulette(randomNumbers, individualsCumulativeProbaibilityMax, cooridantesY, false); //ispis koji hromozomi su izabrani za max

            //PieChart minChart = new org.knowm.xchart.PieChartBuilder().width(900).height(700).title("Simuliran tocak ruleta (za minimum)").theme(Styler.ChartTheme.GGPlot2).build();
            //      setChartProperties(minChart, individualsSelectionProbaibilityMax);

            //    PieChart maxChart = new org.knowm.xchart.PieChartBuilder().width(900).height(7500).title("Simulirani tocak ruleta (za maximum)").theme(Styler.ChartTheme.GGPlot2).build();
            //  setChartProperties(maxChart, individualsSelectionProbaibilityMax);

            //ispis medjugeneracije (ispis kao Tabela 6.)
            printingGeneration(selectedIndividualsXMin, selectedIndividualsYMin, true, false); //međugeneracija za minimum
            printingGeneration(selectedIndividualsXMax, selectedIndividualsYMax, false, false); //međugeneracija za minimum

            //parovi se formiraju uzimanjem po redu 2 hromozoma iz tabele (0,1 hromozom cine par, zatim 2,3 i tako dalje)

            //odluka o ukrstanju, dobijanje liste parova za ukrstanje
            List<Double> recombinationPairsXMin = getRecombinationPairs(selectedIndividualsXMin, true, true); //izbor parova za rekombinaciju i rekombinacija unutar metoda(za minimum)
            List<Double> recombinationPairsYMin = getRecombinationPairs(selectedIndividualsYMin, true, false); //izbor parova za rekombinaciju i rekombinacija unutar metoda(za minimum)
            List<Double> recombinationPairsXMax = getRecombinationPairs(selectedIndividualsXMax, false, true); //izbor parova za rekombinaciju i rekombinacija unutar metoda (za maximum)
            List<Double> recombinationPairsYMax = getRecombinationPairs(selectedIndividualsYMax, false, false); //izbor parova za rekombinaciju i rekombinacija unutar metoda (za maximum)

            //odluka o mutaciji i izbor tacke za mutaciju
            List<Double> mutatedIndividualsXMin = mutateIndividuals(recombinationPairsXMin, true); //mutiranje individua kod kojih je ispunjen uslov mutacije (za minimum)
            List<Double> mutatedIndividualsYMin = mutateIndividuals(recombinationPairsYMin, true); //mutiranje individua kod kojih je ispunjen uslov mutacije (za minimum)
            List<Double> mutatedIndividualsXMax = mutateIndividuals(recombinationPairsXMax, false); //mutiranje individua kod kojih je ispunjen uslov mutacije (za maximum)
            List<Double> mutatedIndividualsYMax = mutateIndividuals(recombinationPairsYMax, false); //mutiranje individua kod kojih je ispunjen uslov mutacije (za maximum)

            //ispis naredne generacije
            printingGeneration(mutatedIndividualsXMin, mutatedIndividualsYMin, true, true);  //ispis naredne generacije (za minimum)
            printingGeneration(mutatedIndividualsXMax, mutatedIndividualsYMax, false, true); //ispis naredne generacije (za maximum)
            cooridantesX = mutatedIndividualsXMin;
            cooridantesY = mutatedIndividualsYMin;
        }
    }
}
