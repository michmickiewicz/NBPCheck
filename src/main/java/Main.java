import com.google.gson.Gson;
import org.joda.time.LocalDate;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Gson GSON = new Gson();
    private static DecimalFormat df = new DecimalFormat("#.###");

    public static void main(String[] args) throws IOException {

        check100zlInCurrency(df, "eur");
        check100zlInCurrency(df, "usd");
        check100zlInCurrency(df, "gbp");
        check100zlInCurrency(df, "chf");

        compareCurrencyValueFor30DaysDifference("eur");
        compareCurrencyValueFor30DaysDifference("chf");
        compareCurrencyValueFor30DaysDifference("usd");
        compareCurrencyValueFor30DaysDifference("gbp");

    }

    private static void compareCurrencyValueFor30DaysDifference(String currency) throws IOException {
        double oldValue = 0;
        double newValue = 0;
        final String text = "Przy sprzedaży 100" + currency + " dzisiaj, mogłeś";
        if(getRate(createDateForApi(""),currency).isPresent()){
            oldValue =  getRate(createDateForApi(""),currency).get();
        }

        if(getRate(createDateForApi("now"),currency).isPresent()){
            newValue = getRate(createDateForApi("now"),currency).get();
        }

        double roznica = newValue-oldValue;
        final String s = df.format(roznica * 100) + " pln";

        System.out.println("\n\nW dniu : "+ createDateForApi("")+"\n"+"  kurs "+currency+" był :" + df.format(oldValue)+" pln");
        System.out.println("Dzisiaj : "+createDateForApi("now")+"\n"+"  kurs "+currency+" jest :" +df.format(newValue)+" pln");
        if(roznica>0){
            System.out.println(text + " zarobić : " + s);
                }
            else if(roznica<0){
                System.out.println(text+" stracić : " + s);
            }
            else System.out.println("Kurs jest taki sam i wynosi  :" + df.format(newValue));
                System.out.println("\n\n\n");
    }

    private static Optional<Double> getRate(String date, String currency) throws IOException {
       Currency actualFromInternet = downloadCurrencyFromMonthAgo(
               "http://api.nbp.pl/api/exchangerates/rates/a/"+currency+"/"+date+"/?format=json");
        return Arrays.stream(actualFromInternet.rates).map(Rates::getMid).findAny();

    }

    private static void check100zlInCurrency(DecimalFormat df, String api) throws IOException {
        Currency actualFromInternet = downloadActualCurrencyFromInternet(
                "http://api.nbp.pl/api/exchangerates/rates/c/"+api+"/?format=json");
        Arrays.stream(actualFromInternet.rates)
                .forEach(e -> System.out.println("Aktualny kurs " + actualFromInternet.code + "\n" +
                        "     , przy przeliczeniu ile można kupić za 100 zł - " +
                        df.format(100 / e.ask) + " " + actualFromInternet.code +
                        " przy kursie :" + df.format(e.ask)));
    }

    private static Currency downloadCurrencyFromMonthAgo(String pattern) throws IOException {
        URL url = new URL(pattern);
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("user-agent", "Chrome");
        InputStream is = connection.getInputStream();
        Scanner scanner = new Scanner(is);
        String line = scanner.nextLine();
        return GSON.fromJson(line, Currency.class);
    }


    private static Currency downloadActualCurrencyFromInternet(String pattern) throws IOException {
        URL url = new URL(pattern);
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("user-agent", "Chrome");
        InputStream is = connection.getInputStream();
        Scanner scanner = new Scanner(is);
        String line = scanner.nextLine();
        return GSON.fromJson(line, Currency.class);
    }

    private static String createDateForApi(String date){
        if(date.equals("now")){
            return refactorDate(LocalDate.now());
        }
        else {
            LocalDate today = LocalDate.now();
            return refactorDate(today.minusDays(31));
        }
    }

    private static String refactorDate(LocalDate now) {
        final int month = now.monthOfYear().get();
        final int day = now.dayOfMonth().get();
        final int year = now.year().get();
        return getDate(year, month, day);
    }

    private static String getDate(int year, int month, int day) {
        if(month <10){
            if(day<10){
                return  year +"-0"+ month +"-0"+ day;
            }
            return year +"-0"+ month +"-"+ day;
        }
        else return  year +"-"+ month +"-0"+ day;
    }

}
