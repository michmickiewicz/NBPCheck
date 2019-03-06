import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Gson GSON = new Gson();
    private static DecimalFormat THREE_DIGITS_AFTER_COMMA = new DecimalFormat("#.###");


    public static void main(String[] args) throws IOException {

        for(Currencies code : Currencies.values()) {
            check100zlInCurrency(THREE_DIGITS_AFTER_COMMA, code.toString());
        }
        for(Currencies code : Currencies.values())
        compareCurrencyValueFor30DaysDifference(code.toString());
    }

    private static void compareCurrencyValueFor30DaysDifference(String currency) throws IOException {
        double oldValue = 0;
        double newValue = 0;
        final String text = "Przy sprzedaży 100" + currency + " dzisiaj, mogłeś";
        Optional<Double> optionalOldRate = getRate(getDateMonthAgo(),currency);
        Optional<Double> optionalNewRate = getRate(getActualDate(),currency);
            if(optionalOldRate.isPresent()){
                optionalOldRate.get();
            }
            if(optionalNewRate.isPresent()){
                optionalNewRate.get();
            }
        double roznica = newValue-oldValue;
        final String s = THREE_DIGITS_AFTER_COMMA.format(roznica * 100) + " pln";
        System.out.println("\nW dniu : "+ getDateMonthAgo()+"\n"+"  kurs "+currency+" był :" + THREE_DIGITS_AFTER_COMMA.format(oldValue)+" pln");
        System.out.println("Dzisiaj : "+getActualDate()+"\n"+"  kurs "+currency+" jest :" + THREE_DIGITS_AFTER_COMMA.format(newValue)+" pln");
            if(roznica>0){
                System.out.println(text + " zarobić : " + s);
            }
            else if(roznica<0){
                System.out.println(text+" stracić : " + s);
            }
            else
                System.out.println("Kurs jest taki sam i wynosi  :" + THREE_DIGITS_AFTER_COMMA.format(newValue));
                System.out.println("\n\n");
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

    private  static String getActualDate(){
        LocalDateTime actualDate = LocalDateTime.now();
        DateTimeFormatter date_Format = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        return date_Format.format(actualDate);
    }

    private static String getDateMonthAgo(){
        LocalDateTime dateMonthAgo = LocalDateTime.now().minusDays(30);
        DateTimeFormatter date_Format = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        return date_Format.format(dateMonthAgo);
    }
}
