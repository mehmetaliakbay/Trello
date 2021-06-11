import com.thoughtworks.gauge.Step;

import static io.restassured.RestAssured.*;

import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class StepImplementation {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static String URL = "https://api.trello.com/1/";
    private static String KEY = "6c0b9f6f35d69d184471c955f8995b28";
    private static String TOKEN = "5ed26e82419d7dd83d927e38a6ceb477d72cc1a93e6b33d2dfe2b947334ed601";


    @Step("<boardName> adinda yeni Board olusturulur")
    public void createBoard(String boardName) {
        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);
        request.put("name", boardName);

        Response response = given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                post(URL + "boards").
                then().
                statusCode(200).
                extract().
                response();

        String id = response.jsonPath().getString("id");
        ScenarioDataStore.put("board_id", id);
        logger.info(boardName + " adinda yeni board olusturuldu");

    }


    @Step("<cardName> adinda Card olusturulur")
    public void createCard(String cardName) {

        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);
        request.put("idList", getIdList());
        request.put("name", cardName);


        given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                post(URL + "cards").
                then().
                statusCode(200);

        logger.info(cardName + " adinda  Card olusturuldu");

    }

    private static String getRandomCardId() {
        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);

        String board_id = (String) ScenarioDataStore.get("board_id");

        Response response = given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                get(URL + "boards/" + board_id + "/cards").
                then().statusCode(200).
                extract().
                response();

        List<String> jsonResponse = response.jsonPath().getList("$");
        int randomCardNum = (int) (Math.random() * jsonResponse.size());

        return response.jsonPath().getString("id[" + randomCardNum + "]");

    }

    private static String getIdList() {
        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);

        String board_id = (String) ScenarioDataStore.get("board_id");

        Response response = given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                get(URL + "boards/" + board_id + "/lists").
                then().
                statusCode(200).
                extract().
                response();


        return response.jsonPath().getString("id[0]");
    }


    @Step("Random bir Cardin adini <updatedCardName> olarak guncelle")
    public void updateACardRandomly(String updatedCardName) {

        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);
        request.put("name", updatedCardName);

        given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                put(URL + "cards/" + getRandomCardId()).
                then().
                statusCode(200);

        logger.info("Random bir Cardin ismi = " + updatedCardName + " olarak degistirildi");

    }

    @Step("Cardlari tek tek sil")
    public void deleteAllCard() {
        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);

        String board_id = (String) ScenarioDataStore.get("board_id");

        Response response = given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                get(URL + "boards/" + board_id + "/cards").
                then().statusCode(200).
                extract().
                response();

        List<String> jsonResponse = response.jsonPath().getList("$");
        for (int i = 0; i < jsonResponse.size(); i++) {

            logger.info("Cardlar tek tek siliniyor");
            String cardId = response.jsonPath().getString("id[" + i + "]");

            given().
                    header("Content-Type", "application/json").
                    contentType(ContentType.JSON).
                    accept(ContentType.JSON).
                    body(request.toJSONString()).
                    when().
                    delete(URL + "cards/" + cardId).
                    then().
                    statusCode(200);

            logger.info((i+1) + ". Card silindi");
        }
        logger.info("Tum Cardlar silindi");
    }

    @Step("<10> saniye bekle")
    public void waitSeconds(int seconds) {
        try {
            logger.info(seconds + " saniye bekleniyor...");
            Thread.sleep(seconds * 1000);
            logger.info(seconds + " saniye beklendi.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Step("Boardi sil")
    public void deleteBoard() {
        JSONObject request = new JSONObject();
        request.put("key", KEY);
        request.put("token", TOKEN);

        String board_id = (String) ScenarioDataStore.get("board_id");

        given().
                header("Content-Type", "application/json").
                contentType(ContentType.JSON).
                accept(ContentType.JSON).
                body(request.toJSONString()).
                when().
                delete(URL + "boards/" + board_id).
                then().
                statusCode(200);

        logger.info("Board Silindi");

    }
}
