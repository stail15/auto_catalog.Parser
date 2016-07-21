package jsoupParser;

import jsoupParser.pojo.Car;
import jsoupParser.service.BrandUrlParser;
import jsoupParser.service.ModelUrlParser;
import jsoupParser.service.UrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Main {

    public static List<String> modelsUrl;
    public static UrlService urlServise;
    public static Set<Car> carList;


    public static void main(String[] args) throws IOException{

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        urlServise = context.getBean(UrlService.class);

        modelsUrl = new ArrayList<String>();
        modelsUrl = Collections.synchronizedList(modelsUrl);

        carList = new TreeSet<Car>(new Comparator<Car>() {
            @Override
            public int compare(Car o1, Car o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        carList = Collections.synchronizedSet(carList);

        ArrayList<Thread> brandThreadsList = new ArrayList<Thread>();
        ArrayList<Thread> modelThreadsList = new ArrayList<Thread>();


        String mainUrl = urlServise.getMainUrl();
        String brandSeparator = urlServise.getBrandClassSeparator();
        Elements allBrandsUrl = getAllBrandsUrl(mainUrl, brandSeparator); //getting list of <a> tag with URL of all car brands

        for (Element link : allBrandsUrl) { //searching URLs for each model in brand;
            BrandUrlParser urlParser = new BrandUrlParser();
            urlParser.setElement(link);
            Thread brandThread = new Thread(urlParser);
            brandThread.start();
            brandThreadsList.add(brandThread);
        }

        for(Thread th :brandThreadsList){ // the main thread is waiting for other threads
            try {
                th.join();
            }
            catch (InterruptedException ex){ex.getMessage();}
        }

        System.out.println("Final size is " + modelsUrl.size());


        for(String link : modelsUrl){ // looking for car model information & creating a new Car
            ModelUrlParser modelUrlParser = new ModelUrlParser();
            modelUrlParser.setModelUrl(link);
            Thread modelThread = new Thread(modelUrlParser);
            modelThread.start();
            modelThreadsList.add(modelThread);

            try {
                Thread.currentThread().sleep(50);
            }
            catch (InterruptedException ex){ex.getMessage();}
        }

        for(Thread th :modelThreadsList){ // the main thread is waiting for other threads
            try {
                th.join();
            }
            catch (InterruptedException ex){ex.getMessage();}
        }
        System.out.println("Final size of carList is " + carList.size());
        writeToFile(carList);
    }

    private static Elements getAllBrandsUrl (String mainUrl, String brandSeparator){
        Document document = null;
        try {
            document=Jsoup.connect(mainUrl).get();
        }
        catch (IOException ex){System.out.println("Error occurred while getting all car brands." );}
        Element modelList = document.select(brandSeparator).first(); // get div.* which contains URLs for all car brands;
        Elements elements = modelList.getElementsByTag("a");
        return elements;
    }

    private static void writeToFile(Set<Car> carList){

        String fileDirectiry = "D:\\Auto.ru_16.02";
        File path = new File(fileDirectiry);
        File file = new File(fileDirectiry+"\\CarModelsDate.txt");
        BufferedWriter fileWriter=null;

        path.mkdirs();
        try {
            fileWriter = new BufferedWriter(new FileWriter(file));
        }
        catch (IOException ex){System.out.println("Unnable to write file");}

        for(Car car :carList){
           try {
               fileWriter.write(car.toString());
           }
           catch (IOException ex){ex.getMessage();}

        }
        try {
            fileWriter.close();
        }
        catch (IOException ex){ex.getMessage();}

    }

}