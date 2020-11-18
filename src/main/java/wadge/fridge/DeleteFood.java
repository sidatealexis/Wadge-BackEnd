package wadge.fridge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static wadge.fridge.Fridge.writeFridge;

public class DeleteFood {
    public static void delete(List<Map<String, Object>> deleteList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> fridgeList = mapper.readValue(Files.readAllBytes(Paths.get("fridge.json")), new TypeReference<List<Map<String, Object>>>(){});
        fridgeList.forEach(foodElement -> {
            deleteList.forEach(foodDelete -> {
                if(foodElement.get("nom").equals(foodDelete.get("nom"))) {
                    Object productList = foodElement.get("products");
                    Object productDelete = foodDelete.get("produics");
                    ((List<Map<String, Object>>) productList).forEach(productL -> {
                        ((List<Map<String, Object>>) productDelete).forEach(productD -> {
                            if(productL.get("dateAjoutee").equals(productD.get("dateAjoutee"))){
                                Integer qDelete = (Integer) productD.get("quantity");
                                Integer qStrock = (Integer) productL.get("quantity");
                                int quantiteFinal = qStrock - qDelete;
                                /*if(quantiteFinal == 0) {

                                }
                                else*/ productL.replace("quantite" ,quantiteFinal);
                            }
                        });
                    });
                }
            });
        });
       // writeFridge(fridgeList);
    }
}
