package pl.radical.tools.healthcheck;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import lombok.val;
import org.json.JSONException;

/**
 * @author <a href="mailto:lukasz.rzanek@radical.com.pl">Łukasz Rżanek</a>
 * @since 11.05.2018
 */
public class HealthCheck {
    public static void main(String[] args) {
        if (checkStatus(args[0])) {
            System.exit(0); // IT WORKED
        } else {
            System.exit(1); // IT FAILED
        }
    }

    static boolean checkStatus(String url) {
        try {
            Webb webb = Webb.create();
            val response = webb.get(url).ensureSuccess().asJsonObject().getBody();
            if (response.has("status") && response.getString("status").equalsIgnoreCase("UP")) {
                System.out.println(response.toString());
                return true;
            }
        } catch (JSONException e) {
            System.out.println("Response not readable");
        } catch (WebbException e) {
            System.out.println(e.getCause().getMessage());
        }
        return false;
    }
}
