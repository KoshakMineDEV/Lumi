package cn.nukkit.metrics.chart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The following code can be attributed to the PaperMC project.
 */
public class JavaVersionRetriever implements Callable<Map<String, Map<String, Integer>>> {

    @Override
    public Map<String, Map<String, Integer>> call() {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        String javaVersion = System.getProperty("java.version");
        Map<String, Integer> entry = new HashMap<>();
        entry.put(javaVersion, 1);

        // http://openjdk.java.net/jeps/223
        // Java decided to change their versioning scheme and in doing so modified the java.version system
        // property to return $major[.$minor][.$security][-ea], as opposed to 1.$major.0_$identifier
        // we can handle pre-9 by checking if the "major" is equal to "1", otherwise, 9+
        String majorVersion = javaVersion.split("\\.")[0];
        String release;

        int indexOf = javaVersion.lastIndexOf('.');

        if (majorVersion.equals("1")) {
            release = "Java " + javaVersion.substring(0, indexOf);
        } else {
            // of course, it really wouldn't be all that simple if they didn't add a quirk, now would it
            // valid strings for the major may potentially include values such as -ea to deannotate a pre-release
            Matcher versionMatcher = Pattern.compile("\\d+").matcher(majorVersion);
            if (versionMatcher.find()) {
                majorVersion = versionMatcher.group(0);
            }
            release = "Java " + majorVersion;
        }
        map.put(release, entry);
        return map;
    }
}
