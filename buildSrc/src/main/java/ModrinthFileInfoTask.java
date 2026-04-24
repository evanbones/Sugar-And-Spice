/*
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class ModrinthFileInfoTask extends DefaultTask {
    @InputFiles
    // Contains only 1 file
    public abstract ConfigurableFileCollection getInput();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    public ModrinthFileInfoTask() {
        setGroup("modrinth");
    }

    @TaskAction
    public void run() throws URISyntaxException, IOException, InterruptedException {
        String sha512 = sha512(getInput().getSingleFile().toPath());

        HttpClient http = HttpClient.newHttpClient();
        String url = "https://api.modrinth.com/v2/version_file/" + sha512;

        HttpResponse<Path> response = http.send(
                HttpRequest.newBuilder(new URI(url)).build(),
                HttpResponse.BodyHandlers.ofFile(getOutput().get().getAsFile().toPath())
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch file info: " + response.statusCode() + " " + url);
        }
    }

    private static String sha512(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(Files.readAllBytes(path));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}