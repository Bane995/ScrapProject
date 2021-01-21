
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Postavi naslov prozora
        stage.setTitle("Aplikacija za web scraping");

        // View lista izdvojenih poveznica
        ListView<String> linksView = new ListView<String>();
        // Lista poveznica će se dinamicki siriti kako mijenjamo sirinu prozora
        VBox.setVgrow(linksView, Priority.ALWAYS);

        // Container u kojega se dodani elementi dodaju horizontalno
        HBox hbox = new HBox();
        // Razmaci između pojedinih elemenata
        hbox.setSpacing(5);

        // Polje za unos linka stranice iz koje izdvajamo poveznice
        TextField url = new TextField();
        // Element url će se dinamicki siriti kako mijenjamo sirinu prozora
        HBox.setHgrow(url, Priority.ALWAYS);

        // Polje za unos filter texta sa kojim scrapani link mora pocinjati
        ComboBox<String> filters = new ComboBox<String>();
        filters.getItems().addAll("HTTP, HTTPS", "SVE");

        // Gumb akcije kojim zapocinje izdvajanje poveznica
        Button button = new Button("Izdvoji poveznice");
        // Event koji se okida kada se klikne na gumb
        EventHandler<ActionEvent> event = ScrapEvent(url, filters, linksView);
        button.setOnAction(event);

        // Polje za unos, gumb i filtere dodajemo u horizontalni container
        hbox.getChildren().addAll(url, filters, button);

        // Glavni box(vertikalni) unutar kojega prikazujemo hbox i linksView
        VBox mainVBox = new VBox();
        mainVBox.setPadding(new Insets(10));
        mainVBox.setSpacing(5);
        mainVBox.getChildren().addAll(hbox, linksView);

        // Kreiraj Scene objekt i unutar njega prikazi glavni vertikalni container
        Scene scene = new Scene(mainVBox, 500, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private EventHandler<ActionEvent> ScrapEvent(TextField url, ComboBox filterSelect, ListView<String> lv_links) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                // Dohvati html source
                String html = GetHtml(url.getText());
                // Izbaci grešku ako html nije moguće dohvatiti
                if (html == null) {
                    ShowDialog(AlertType.ERROR, "Greska", "Greska prilikom izdvajanja poveznica");
                }
                // Izdvoji poveznice iz preuzetog html-a
                String urlStartFilter = (filterSelect.getValue() == "HTTP, HTTPS") ? "http" : "";
                List<String> links = ExtractLinksFromHtml(html, urlStartFilter);
                // Predaj izdvojenu listu poveznica view elementu(obriši prethodnu listu)
                lv_links.getItems().clear();
                lv_links.getItems().addAll(links);
                ShowDialog(AlertType.CONFIRMATION, "Uspjeh", "Izdvojeno je " + links.size() + " poveznica");
            }
        };
    }

    private void ShowDialog(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String GetHtml(String url) {
        String content = null;
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
            return content;
        } catch (Exception ex) {
            return null;
        }
    }

    private List<String> ExtractLinksFromHtml(String html, String urlStartFilter) {
        List<String> links = new ArrayList<String>();
        // Regex izraz za string koji mora:
        // - zapocinjati sa " ili '
        // - u sredini imati string koji zapocinje sa urlStartFilter
        // - završavati sa " ili '
        // Regularni izraz moze biti href=[\"'](.*?)[\"'] ili href=[\"'](http.*?)[\"']
        String regex = "href=[\"']" + "(" + urlStartFilter + ".*?)" + "[\"']";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        // Svakim pozivom funkcije find() se dohvaca sljedeci link
        // Novi nadjeni link se dohvaca preko funkcije matcher.group(1) i sprema u listu
        while (matcher.find()) {
            String link = matcher.group(1);
            links.add(link);
        }

        return links;
    }

}
