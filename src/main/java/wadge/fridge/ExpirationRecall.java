package wadge.fridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExpirationRecall {
    private final SimpleDateFormat dateFormater;
    private final Date currentDate;

    public enum RecallType {
        TWO_DAYS, FIVE_DAYS, SEVEN_DAYS, FORTEEN_DAYS, EXPIRED
    }

    public ExpirationRecall() {
        dateFormater = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        currentDate = new Date();
    }

    private static long dateDifference(Date d1, Date d2) {
        long diff = d1.getTime() - d2.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public List<Map<String, String>> getExpirationList(RecallType type) {
        List<Map<String, String>> rtr = null;
        switch(type) {
            case TWO_DAYS:
                rtr = this.getExpirationDateFromPredicate(x -> x < 3 && x >= 0);
                break;
            case FIVE_DAYS:
                rtr = this.getExpirationDateFromPredicate(x -> x < 5 && x >= 2);
                break;
            case SEVEN_DAYS:
                rtr = this.getExpirationDateFromPredicate(x -> x < 7 && x >= 5);
                break;
            case FORTEEN_DAYS:
                rtr = this.getExpirationDateFromPredicate(x -> x < 14 && x >= 7);
                break;
            case EXPIRED:
                rtr = this.getExpirationDateFromPredicate(x -> x < 0);
                break;
        }
        return rtr;
    }

    private List<Map<String, String>> getExpirationDateFromPredicate(Predicate<Long> predicate) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> fridgeList = null;
        try {
            fridgeList = mapper.readValue(Files.readAllBytes(Paths.get("fridge.json")),
                    new TypeReference<List<Map<String, Object>>>() {});

        } catch (JsonParseException | JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Map<String, String>> result = new ArrayList<>();
        
        if(fridgeList != null) {
            fridgeList.forEach(foodElement -> {
                Object products = foodElement.get("produits");

                ((List<Map<String, Object>>) products).forEach(product -> {
                    try {
                        Date expirationDate = dateFormater.parse((String) product.get("dateLimite"));
                        long dayDifference = dateDifference(expirationDate, currentDate);
                        if(predicate.test(dayDifference)) {
                            result.add(Map.of(
                                "dateAjout", (String) product.get("dateAjout"),
                                "nom", (String) foodElement.get("nom"),
                                "quantite", product.get("quantite").toString()
                            ));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        return result;

    }    
}
