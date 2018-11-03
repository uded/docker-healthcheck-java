package pl.radical.tools.healthcheck;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import lombok.val;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author <a href="mailto:lukasz.rzanek@radical.com.pl">Łukasz Rżanek</a>
 * @since 11.05.2018
 */
public class HealthCheck {
    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0) {
            Class clazz = HealthCheck.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (!classPath.startsWith("jar")) {
                // Class not from JAR
                System.exit(11);
            }
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue("Implementation-Version");
            System.out.println("Version: " + value);
            System.exit(0);
        }
        if (checkStatus(args[0])) {
            System.exit(0); // IT WORKED
        } else {
            System.exit(1); // IT FAILED
        }
    }

    static boolean checkStatus(String url) {
        try {
            Webb webb = Webb.create();
            val response = webb.get(url).ensureSuccess().asJsonObject();
            if (response.getBody().has("status") && response.getBody().getString("status").equalsIgnoreCase("UP")) {
                System.out.println(response.toString());
                return true;
            } else if (response.getBody().has("status") && response.getBody().getString("status").equalsIgnoreCase("DOWN")) { // JUST IN CASE...
                System.out.println(response.toString());
                return false;
            }
        } catch (JSONException e) {
            System.out.println("Response not readable");
        } catch (WebbException e) {
            if (e.getResponse() != null && e.getResponse().getErrorBody() != null) { // Usual case
                System.out.print(e.getResponse().getErrorBody());
            } else {
                System.out.println("Error: [" + e.getMessage() + "]");
            }
        }
        return false;
    }
}
